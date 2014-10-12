/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.webservice.requestprocessor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ParseException;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.content.SmsContentConstants;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.texting.TextingInterface;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonAPIRequestProcessor extends AbstractHttpRequestProcessor
        implements SmsIOCallback, IContactModifiedCallback
{
    private final static String JSON_STATE_SUCCESS = "success";
    private final static String JSON_STATE_ERROR = "error";

    private final LogClient mLog = new LogClient(this);
    private JsonFactory mJsonFactory = new JsonFactory();

    private volatile TextingInterface mTextingAdapter;
    private volatile SystemMonitor mSystemMonitor;
    private int mSMSThreadMessageCount;

    /** members to represent a state */
    private volatile boolean mSMSSentSuccess = false;
    private volatile boolean mContactsChanged = false;
    private volatile boolean mSMSReceived = false;
    private volatile boolean mSMSSentError = false;

    private volatile HashMap<String, Integer> mSMSWaitingForSentCallback = new HashMap<String, Integer>();
    private volatile List<TextMessage> mSMSSentList = new ArrayList<TextMessage>();
    private volatile List<TextMessage> mSMSReceivedList = new ArrayList<TextMessage>();
    private volatile List<TextMessage> mSMSSentErrorList = new ArrayList<TextMessage>();
    private volatile JSONArray mJsonContactList = null;
    private Object mJsonContactListLock = new Object();

    private SmsIOCallback mExternalSMSIoCallback = null;


    public synchronized void registerSMSIoListener(SmsIOCallback smsListener)
    {
        mExternalSMSIoCallback = smsListener;
    }


    public synchronized void unregisterSMSIoListener()
    {
        mExternalSMSIoCallback = null;
    }


    public JsonAPIRequestProcessor(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) throws Throwable
    {
        super(context, config, registry);
        mTextingAdapter = new TextingAdapter(context, this, this);

        mLog.debug("preloading contacts [start]");
        mJsonContactList = fetchContactsJsonArray();
        mLog.debug("preloading contacts [done]");

        mSystemMonitor = new SystemMonitor(context);
        mSMSThreadMessageCount = 20;

        mSystemMonitor.start();
        mTextingAdapter.start();
    }


    private JsonAPIRequestProcessor()
    {
        super(null, null, null);
    }


    @Override
    public synchronized void handleRequest(RequestLine requestLine,
            String requestData, HttpResponse httpResponse)
            throws HttpException, IOException
    {

        if (requestLine.getMethod().equals(
                WebServerConstants.HTTP.REQUEST_TYPE_POST))
        {
            JSONObject json;
            try
            {
                json = new JSONObject(requestData);
                String method = json.getString(WebServerConstants.JSON.METHOD);
                if (method != null && method.length() > 0)
                {
                    JSONArray jsonParams = null;
                    if (json.isNull(WebServerConstants.JSON.PARAMS) == false)
                    {
                        jsonParams = json
                                .getJSONArray(WebServerConstants.JSON.PARAMS);
                    }
                    JSONObject jsonResponse = processMethod(method, jsonParams);
                    mResponseDataAppender.appendHttpResponseData(httpResponse,
                            jsonResponse);
                } else
                {
                    mLog.error("no method defined in JSON post request ==> <"
                            + json.toString() + ">");
                    return;
                }
            }
            catch (ParseException parseException)
            {
                parseException.printStackTrace();
            }
            catch (JSONException jsonException)
            {
                jsonException.printStackTrace();
            }
        }
    }


    @Override
    public synchronized void close()
    {
        mTextingAdapter.stop();
        try
        {
            mTextingAdapter.close();
        }
        catch (IOException e)
        {
            mLog.debug("failed closing texting adapter", e);
        }
        mSystemMonitor.stop();
        mSystemMonitor.onClose();

        mJsonFactory = null;
        mTextingAdapter = null;
        mSystemMonitor = null;
        mSMSWaitingForSentCallback = null;
        mSMSSentList = null;
        mSMSReceivedList = null;
        mSMSSentErrorList = null;

        mJsonContactList = null;
        mJsonContactListLock = null;

        mExternalSMSIoCallback = null;

        super.close();
    }


    @Override
    public void contactModifiedCallback()
    {
        mLog.debug("reloading all contacts from provider");
        synchronized (mJsonContactListLock)
        {
            mJsonContactList = fetchContactsJsonArray();
            this.mContactsChanged = true;
        }
    }


    @Override
    public synchronized void smsSentCallback(Context context,
            List<TextMessage> messages)
    {
        if (messages.isEmpty() == false)
        {
            for (TextMessage message : messages)
            {
                String address = message.getAddress();
                mLog.debug("looking for address in waiting queue with :"
                        + address);
                if (mSMSWaitingForSentCallback.containsKey(address))
                {
                    int tmpCount = mSMSWaitingForSentCallback.get(address);
                    tmpCount = tmpCount - 1;

                    // if we received all callbacks for an specific address we
                    // can
                    // assume, that the sms was sent successfully and the count
                    // is 0
                    if (tmpCount == 0)
                    {
                        mSMSSentSuccess = true;
                        mSMSSentList.add(message);
                        mSMSWaitingForSentCallback.remove(address);
                        mLog.debug("received all sms callbacks for address "
                                + address + " going to notify webapp");
                    } else
                    {
                        mSMSWaitingForSentCallback.put(address, tmpCount);
                        mLog.debug("received sms callback for address "
                                + address + " - count is: " + tmpCount);
                    }
                } else
                {
                    mLog.error("Got a callback for address " + address
                            + " but could not be found in waiting list!");
                }
            }
        } else
        {
            mLog.warning("A sms sent callback was delivered but textmessages list was empty");
        }

        if (null != mExternalSMSIoCallback)
        {
            mExternalSMSIoCallback.smsSentCallback(context, messages);
        }
    }


    @Override
    public synchronized void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        this.mSMSSentError = true;
        for (TextMessage message : messages)
        {
            this.mSMSSentErrorList.add(message);
        }

        if (null != mExternalSMSIoCallback)
        {
            mExternalSMSIoCallback.smsSentErrorCallback(context, messages);
        }
    }


    @Override
    public synchronized void smsDeliveredCallback(Context context,
            List<TextMessage> message)
    {
        if (null != mExternalSMSIoCallback)
        {
            mExternalSMSIoCallback.smsDeliveredCallback(context, message);
        }
    }


    @Override
    public synchronized void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        this.mSMSReceived = true;
        for (TextMessage message : messages)
        {
            mLog.debug("textmessage from " + message.getAddress()
                    + " received in api request handler.");
            this.mSMSReceivedList.add(message);
        }

        if (null != mExternalSMSIoCallback)
        {
            mExternalSMSIoCallback.smsReceivedCallback(context, messages);
        }
    }


    private JSONObject processMethod(String method, JSONArray jsonParams)
    {
        mLog.debug("handle api request <" + method + ">");

        JSONObject resultObject = new JSONObject();

        if (jsonParams == null)
        {
            mLog.debug("0 parameter found for request <" + method + ">");
        } else
        {
            mLog.debug("parameters " + jsonParams.toString());
        }

        try
        {
            if (method.compareTo("get_contacts") == 0)
            {
                resultObject = this.getContacts();
            } else if (method.compareTo("send_sms_message") == 0)
            {
                resultObject = this.sendSMS(jsonParams);
            } else if (method.compareTo("info") == 0)
            {
                resultObject = this.createPollingInfo();
            } else if (method.compareTo("fetch_sms_thread") == 0)
            {
                resultObject = this.fetchSMSThread(jsonParams);
            } else
            {
                String logMsg = "No method found for given request method: "
                        + method;
                mLog.warning(logMsg);
                resultObject.put("state", JSON_STATE_ERROR);
                resultObject.put("error_msg", logMsg);
            }
        }
        catch (JSONException jsonException)
        {
            mLog.error("Could not create jsonobject for handling api request.",
                    jsonException);
        }

        return resultObject;
    }

    private static class TextMessageThreadSort implements
            Comparator<TextMessage>
    {
        @Override
        public int compare(TextMessage message1, TextMessage message2)
        {
            Date date1 = new Date(Long.parseLong(message1.getDate()));
            Date date2 = new Date(Long.parseLong(message2.getDate()));
            return (-1) * (date1.compareTo(date2));
        }
    }


    private synchronized JSONObject fetchSMSThread(JSONArray params)
    {
        JSONObject resultObject = new JSONObject();
        String contact_id = "";
        int paramsLength = params.length();
        int messageCount = this.mSMSThreadMessageCount;
        try
        {
            if (paramsLength == 1)
            {
                List<TextMessage> threadList = new ArrayList<TextMessage>();

                JSONObject jsonParams = params.getJSONObject(0);
                contact_id = jsonParams.getString("contact_id");
                mLog.debug("fetch SMS thread with given contact_id ["
                        + contact_id + "]");

                ContactFilter conFilter = new ContactFilter();
                conFilter.setId(Integer.parseInt(contact_id));
                List<Contact> contacts = mTextingAdapter
                        .fetchContacts(conFilter);
                mLog.debug("found contacts - list size [" + contacts.size()
                        + "]");
                if (contacts.size() == 1)
                {
                    Contact contact = contacts.get(0);
                    List<Contact.Number> phoneNumbers = contact
                            .getPhoneNumbers();
                    for (Contact.Number entry : phoneNumbers)
                    {
                        if (messageCount <= 0)
                        {
                            break;
                        }
                        mLog.debug("fetch SMS thread of ["
                                + contact.getDisplayName() + "] with number ["
                                + entry.getNumber() + "]");
                        List<Integer> threadIds = mTextingAdapter
                                .fetchThreadIds(entry.getNumber());
                        for (Integer threadId : threadIds)
                        {
                            if (messageCount <= 0)
                            {
                                break;
                            }
                            TextMessageFilter msgFilter = new TextMessageFilter();
                            msgFilter.setThreadId(threadId.longValue());
                            msgFilter.setBox(SmsContentConstants.Uri.BASE_URI);
                            mLog.debug("fetch SMS thread with ID [" + threadId
                                    + "]");
                            List<TextMessage> threadMessages = mTextingAdapter
                                    .fetchTextMessages(msgFilter);

                            for (TextMessage msg : threadMessages)
                            {
                                if (messageCount <= 0)
                                {
                                    break;
                                }
                                threadList.add(msg);
                                messageCount--;
                            }
                        }
                    }

                    // sort the list by date
                    Collections.sort(threadList, new TextMessageThreadSort());
                    JSONArray thread_messages = new JSONArray();
                    for (TextMessage msg : threadList)
                    {
                        JSONObject entry = new JSONObject();
                        if (msg.getPerson() != null
                                && msg.getPerson().compareTo("null") != 0
                                && msg.getPerson().length() > 0)
                        {
                            entry.put("real_contact_id", contact_id);
                        } else
                        {
                            entry.put("real_contact_id", "");
                        }
                        entry.put("message", mJsonFactory.createJsonObject(msg));
                        thread_messages.put(entry);
                    }
                    setSuccessState(resultObject);
                    resultObject.put("thread_messages", thread_messages);
                    resultObject.put("contact_id", contact_id);
                } else
                {
                    mLog.warning("Contact with given id " + contact_id
                            + " could not be found or is ambiguous.");
                    this.setErrorState(resultObject,
                            "Contact could not be determined.");
                    return resultObject;
                }

            } else
            {
                this.setErrorState(resultObject,
                        "Corrupt amount of parameters given to fetch sms tread.");
                return resultObject;
            }
        }
        catch (JSONException jsonException)
        {
            mLog.error(
                    "Parameters for fetching sms thread could not be extracted from the given api request.",
                    jsonException);
        }

        return resultObject;
    }


    private void setSuccessState(JSONObject obj)
    {
        try
        {
            obj.put("state", JSON_STATE_SUCCESS);
        }
        catch (JSONException jsonException)
        {
            jsonException.printStackTrace();
        }
    }


    private void setErrorState(JSONObject obj, String msg)
    {
        try
        {
            obj.put("state", JSON_STATE_ERROR);
            obj.put("error_msg", msg);
        }
        catch (JSONException jsonException)
        {
            jsonException.printStackTrace();
        }
    }


    private JSONObject getContacts()
    {

        synchronized (mJsonContactListLock)
        {
            mLog.debug("handle get_contacts request");
            JSONObject resultObject = new JSONObject();
            try
            {
                resultObject.put("contacts", mJsonContactList);
            }
            catch (JSONException jsonException)
            {
                mLog.error("Could not append contact list to json object.",
                        jsonException);
                return new JSONObject();
            }

            if (resultObject.length() > 0)
            {
                this.setSuccessState(resultObject);
            } else
            {
                this.setErrorState(resultObject,
                        "No contacts could be found on the device.");
            }

            return resultObject;
        }
    }


    private synchronized JSONArray fetchContactsJsonArray()
    {

        mLog.debug("fetch contacts from provider [start]");
        ContactFilter allFilter = new ContactFilter();
        allFilter.setWithPhone(true);
        allFilter.setOrderByDisplayName(true,
                ContactFilter.SORT_ORDER_ASCENDING);
        List<Contact> contacts = mTextingAdapter.fetchContacts(allFilter);

        JSONArray contactList = new JSONArray();
        while (contacts.size() > 0)
        {
            contactList.put(mJsonFactory.createJsonObject(contacts.get(0)));
            contacts.remove(0);
        }
        mLog.debug("fetch " + contacts.size()
                + " contacts from provider [done]");
        return contactList;
    }


    private synchronized JSONObject sendSMS(JSONArray params)
    {
        JSONObject resultObject = new JSONObject();

        String address = "";
        String message = "";
        String exceptionalMessage = "Parameters for sending sms could not be fetched from the given api request.";

        int paramsLength = params.length();
        try
        {
            if (paramsLength == 1)
            {
                JSONObject jsonParams = params.getJSONObject(0);
                address = jsonParams.getString("address");
                message = URLDecoder.decode(jsonParams.getString("message"),
                        "UTF-8");
                mLog.debug("fetch parameter adress [" + address
                        + "] and message [" + message
                        + "] from send sms request");
            } else
            {
                this.setErrorState(resultObject,
                        "Corrupt amount of parameters given to send sms.");
                return resultObject;
            }
        }
        catch (JSONException e)
        {
            mLog.error(exceptionalMessage, e);
        }
        catch (UnsupportedEncodingException e)
        {
            mLog.error(exceptionalMessage, e);
        }

        if (address.length() > 0 && message.length() > 0)
        {
            TextMessage sentMessage = new TextMessage();
            sentMessage.setAddress(address);
            sentMessage.setBody(message);
            int parts = mTextingAdapter.sendTextMessage(sentMessage);

            // store the part count or arrange an already set count for this
            // specific address
            if (this.mSMSWaitingForSentCallback.containsKey(address))
            {
                int tmpCount = this.mSMSWaitingForSentCallback.get(address);
                this.mSMSWaitingForSentCallback
                        .put(address, (tmpCount + parts));
            } else
            {
                this.mSMSWaitingForSentCallback.put(address, parts);
            }
            mLog.debug("message queued for sending to address " + address
                    + "] and parts [" + parts + "]");

            this.setSuccessState(resultObject);
        } else
        {
            this.setErrorState(resultObject,
                    "One or all of the given parameters are empty or corrupt.");
        }
        return resultObject;
    }


    /**
     * Provide the following informations: --> Update of the statusbar -->
     * Inform about contact changes and if true send whole contacts back. -->
     * Inform about successfully sent sms and if true array of address which has
     * been sent --> Inform about received sms and if true the return the
     * textmessages as json
     * 
     * @return
     */
    private synchronized JSONObject createPollingInfo()
    {
        JSONObject result = new JSONObject();
        try
        {

            mLog.debug("evaluate contact changed state");
            result.put("contact_changed", this.mContactsChanged);
            if (this.mContactsChanged)
            {
                result.put("contacts", this.mJsonContactList);
                this.mContactsChanged = false;
            }

            mLog.debug("evaluate sms sent error");
            result.put("sms_sent_error", this.mSMSSentError);
            if (this.mSMSSentError)
            {
                JSONArray errorList = new JSONArray();
                for (int idx = 0; idx < mSMSSentErrorList.size(); idx++)
                {
                    errorList.put(mJsonFactory
                            .createJsonObject(mSMSSentErrorList.get(idx)));
                }
                result.put("sms_sent_error_messages", errorList);
                this.mSMSSentError = false;
            }

            mLog.debug("evaluate sms sent success");
            result.put("sms_sent_success", this.mSMSSentSuccess);
            if (this.mSMSSentSuccess)
            {
                JSONArray sentList = new JSONArray();
                for (int idx = 0; idx < mSMSSentList.size(); idx++)
                {
                    sentList.put(mJsonFactory.createJsonObject(mSMSSentList
                            .get(idx)));
                }
                result.put("sms_sent_success_messages", sentList);
                this.mSMSSentSuccess = false;
            }

            mLog.debug("evaluate sms received");
            result.put("sms_received", this.mSMSReceived);
            if (this.mSMSReceived)
            {
                JSONArray recvList = new JSONArray();
                for (int idx = 0; idx < mSMSReceivedList.size(); idx++)
                {
                    recvList.put(mJsonFactory.createJsonObject(mSMSReceivedList
                            .get(idx)));
                }
                result.put("sms_received_messages", recvList);
                this.mSMSReceived = false;
            }

            mLog.debug("clear temporary member lists");
            this.mSMSSentErrorList.clear();
            this.mSMSSentList.clear();
            this.mSMSReceivedList.clear();

            mLog.debug("evaluate current telephone state");
            BatteryStatus batteryStatus = this.mSystemMonitor
                    .getBatteryStatus();
            TelephonySignalStrength telSignalStrength = this.mSystemMonitor
                    .getTelephonySignalStrength();

            if (batteryStatus != null)
            {
                result.put("battery",
                        mJsonFactory.createJsonObject(batteryStatus));
                batteryStatus.onClose();
            }
            if (telSignalStrength != null)
            {
                result.put("signal",
                        mJsonFactory.createJsonObject(telSignalStrength));
                telSignalStrength.onClose();
            }

            this.setSuccessState(result);

        }
        catch (JSONException jsonException)
        {
            mLog.error("Could not create the polling json object",
                    jsonException);
            this.setErrorState(result, "Could not create polling object");
        }

        return result;
    }

}
