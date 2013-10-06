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

package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
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
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.texting.TextingInterface;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonAPIRequestHandler extends AbstractHttpRequestHandler implements
        SmsIOCallback, ContactModifiedCallback {
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

    public JsonAPIRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) throws Throwable {
        super(context, config, registry);
        mTextingAdapter = new TextingAdapter(context, this, this);

        mLog.verbose("precaching contacts [start]");
        mJsonContactList = fetchContactsJsonArray();
        mLog.verbose("precaching contacts [done]");
        
        mSystemMonitor = new SystemMonitor(context);
        mSMSThreadMessageCount = 20;
        
        mSystemMonitor.start();
        mTextingAdapter.start();
    }

    @Override
    public synchronized void handleRequest(RequestLine requestLine, String requestData, HttpResponse httpResponse) throws HttpException, IOException {

        if (requestLine.getMethod().equals(WebServerConfig.HTTP.REQUEST_TYPE_POST)) {
            JSONObject json;
            try {
                json = new JSONObject(requestData);
                String method = json.getString(WebServerConfig.JSON.METHOD);
                if (method != null && method.length() > 0) {
                    JSONArray jsonParams = null;
                    if (json.isNull(WebServerConfig.JSON.PARAMS) == false) {
                        jsonParams = json.getJSONArray(WebServerConfig.JSON.PARAMS);
                    }
                    JSONObject jsonResponse = processMethod(method, jsonParams);
                    responseDataAppender.appendHttpResponseData(httpResponse, jsonResponse);
                } else {
                    mLog.error("no method defined in JSON post request ==> <" + json.toString()
                            + ">");
                    return;
                }
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void onClose() {
    	mTextingAdapter.stop();
    	mSystemMonitor.stop();
        
        mJsonFactory = null;
        mTextingAdapter = null;
        mSystemMonitor = null;
        mSMSWaitingForSentCallback = null;
        mSMSSentList = null;
        mSMSReceivedList = null;
        mSMSSentErrorList = null;
    	
        super.onClose();
    }

    @Override
    public void contactModifiedCallback() {
        mLog.verbose("reloading all contacts from provider");
        synchronized (mJsonContactListLock) {
        	mJsonContactList = fetchContactsJsonArray();
        	this.mContactsChanged = true;
        }
    }

	@Override
	public synchronized void smsSentCallback(Context context, List<TextMessage> messages) {
		if(messages.isEmpty() == false){
			for(TextMessage message : messages) {
				String address = message.getAddress();
				mLog.verbose("Looking for address in waiting queue with :" + address);
				if (mSMSWaitingForSentCallback.containsKey(address)) {
					int tmpCount = mSMSWaitingForSentCallback.get(address);
					tmpCount = tmpCount - 1;
	
					// if we received all callbacks for an specific address we can
					// assume, that the sms was sent successfully and the count is 0
					if (tmpCount == 0) {
						mSMSSentSuccess = true;
						mSMSSentList.add(message);
						mSMSWaitingForSentCallback.remove(address);
						mLog.verbose("Received all sms callbacks for address "
								+ address + " going to notify webapp.");
					} else {
						mSMSWaitingForSentCallback.put(address, tmpCount);
						mLog.verbose("Received sms callback for address " + address
								+ " - count is: " + tmpCount);
					}
				} else {
					mLog.error("Got a callback for address " + address
							+ " but could not be found in waiting list!");
				}
			}
		} else {
			mLog.warning("A sms sent callback was delivered but textmessages list was empty");
		}
	}

	@Override
	public synchronized void smsSentErrorCallback(Context context, List<TextMessage> messages) {
		this.mSMSSentError = true;
		for(TextMessage message : messages){
			this.mSMSSentErrorList.add(message);
		}
	}

	@Override
	public synchronized void smsDeliveredCallback(Context context, List<TextMessage> message) {
		// not working so we do not bother about it
	}

	@Override
	public synchronized void smsReceivedCallback(Context context, List<TextMessage> messages) {
		this.mSMSReceived = true;
		for ( TextMessage message : messages ) {
			mLog.verbose("Textmessage from "+message.getAddress()+" received in api request handler.");
			this.mSMSReceivedList.add(message);
		}
	}

    private JSONObject processMethod(String method, JSONArray jsonParams) {
        mLog.info("Handle api request with given method: " + method);

        JSONObject resultObject = new JSONObject();

        if (jsonParams == null) {
            mLog.info("No parameter for request " + method);
        } else {
            mLog.info("Given parameters: " + jsonParams.toString());
        }

        try {
            if (method.compareTo("get_contacts") == 0) {
                resultObject = this.getContacts();
            } else if (method.compareTo("send_sms_message") == 0) {
                resultObject = this.sendSMS(jsonParams);
            } else if (method.compareTo("info") == 0) {
                resultObject = this.createPollingInfo();
            } else if (method.compareTo("fetch_sms_thread") == 0) {
                resultObject = this.fetchSMSThread(jsonParams);
            } else {
                String logMsg = "No method found for given request method: "
                        + method;
                mLog.warning(logMsg);
                resultObject.put("state", JSON_STATE_ERROR);
                resultObject.put("error_msg", logMsg);
            }
        } catch (JSONException jsonException) {
            mLog.error("Could not create jsonobject for handling api request.",
                    jsonException);
        }

        return resultObject;
    }

    
    private static class TextMessageThreadSort implements Comparator<TextMessage> {
        @Override
        public int compare(TextMessage message1, TextMessage message2) {
            Date date1 = new Date(Long.parseLong(message1.getDate()));
            Date date2 = new Date(Long.parseLong(message2.getDate()));
            return (-1) * (date1.compareTo(date2));
        }
    }

    
    private synchronized JSONObject fetchSMSThread(JSONArray params) {
        JSONObject resultObject = new JSONObject();
        String contact_id = "";
        int paramsLength = params.length();
        int messageCount = this.mSMSThreadMessageCount;
        try {
            if (paramsLength == 1) {
                List<TextMessage> threadList = new ArrayList<TextMessage>();
                
                JSONObject jsonParams = params.getJSONObject(0);
                contact_id = jsonParams.getString("contact_id");
                mLog.verbose("Fetch SMS Thread with given contact_id: "
                        + contact_id);

                ContactFilter conFilter = new ContactFilter();
                conFilter.setId(Integer.parseInt(contact_id));
                List<Contact> contact = mTextingAdapter
                        .fetchContacts(conFilter);
                mLog.verbose("Found contact - list size:  "+ contact.size());              
                if (contact.size() == 1) {
                    Contact con = contact.get(0);
                    List<Contact.Number> phoneNumbers = con.getPhoneNumbers();
                    for(Contact.Number entry : phoneNumbers){
                        if(messageCount <= 0){
                        	break;
                        }
                        String number = entry.getNumber();
                        mLog.verbose("Fetch SMS Thread with number: "+ entry.getNumber()+" replaced to: "+number);
                        List<Integer> threadIds = mTextingAdapter.fetchThreadIds(number);
                        for(Integer threadId : threadIds){
                            if(messageCount <= 0){
                            	break;
                            }
                            TextMessageFilter msgFilter = new TextMessageFilter();
                            msgFilter.setThreadId(threadId.longValue());
                            msgFilter.setBox(SmsContent.ContentUri.BASE_URI);
                            mLog.verbose("Fetch SMS Thread with threadID: "+ threadId);
                            List<TextMessage> threadMessages = mTextingAdapter.fetchTextMessages(msgFilter);

                            for(TextMessage msg : threadMessages){
                                if(messageCount <= 0){
                                	break;
                                }
                                threadList.add(msg);
                                mLog.verbose("Adding sms to thread list with message-id: "+ msg.getId()+ " and Person-id: "+msg.getPerson());
                                messageCount--;
                            }
                        }
                    }
                    
                    //sort the list by date
                    Collections.sort(threadList, new TextMessageThreadSort());
                    JSONArray thread_messages = new JSONArray();
                    for(TextMessage msg : threadList){
                    	JSONObject entry = new JSONObject();
                    	if(msg.getPerson() != null && msg.getPerson().compareTo("null") != 0 && msg.getPerson().length() > 0 ){
                    		entry.put("real_contact_id", contact_id);
                    	} else {
                    		entry.put("real_contact_id", "");
                    	}
                    	entry.put("message", mJsonFactory.createJsonObject(msg));
                        thread_messages.put(entry);
                    }
                    setSuccessState(resultObject);
                    resultObject.put("thread_messages", thread_messages);
                    resultObject.put("contact_id", contact_id);
                } else {
                    mLog.warning("Contact with given id " + contact_id
                            + " could not be found or is ambigious.");
                    this.setErrorState(resultObject,
                            "Contact could not be determined.");
                    return resultObject;
                }

            } else {
                this.setErrorState(resultObject,
                        "Corrupt amount of parameters given to fetch sms tread.");
                return resultObject;
            }
        } catch (JSONException jsonException) {
            mLog.error(
                    "Parameters for fetching sms thread could not be extracted from the given api request.",
                    jsonException);
        }

        return resultObject;
    }

    private void setSuccessState(JSONObject obj) {
        try {
            obj.put("state", JSON_STATE_SUCCESS);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private void setErrorState(JSONObject obj, String msg) {
        try {
            obj.put("state", JSON_STATE_ERROR);
            obj.put("error_msg", msg);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private JSONObject getContacts() {
    	
    	synchronized (mJsonContactListLock) {
	        mLog.info("Handle get_contacts request.");
	        JSONObject resultObject = new JSONObject();
	        try {
	            resultObject.put("contacts", mJsonContactList);
	        } catch (JSONException jsonException) {
	            mLog.error("Could not append contact list to json object.", jsonException);
	            return new JSONObject();
	        }
	
	        if (resultObject.length() > 0) {
	            this.setSuccessState(resultObject);
	        } else {
	            this.setErrorState(resultObject,
	                    "No contacts could be found on the device.");
	        }
	
	        return resultObject;
    	}
    }

    private synchronized JSONArray fetchContactsJsonArray() {

    	mLog.verbose("fetch contacts from provider [start]");
        ContactFilter allFilter = new ContactFilter();
        allFilter.setWithPhone(true);
        allFilter.setOrderByDisplayName(true, ContactFilter.SORT_ORDER_ASCENDING);
        List<Contact> contacts = mTextingAdapter.fetchContacts(allFilter);
        
        JSONArray contactList = new JSONArray();
        while ( contacts.size() > 0 ) {
        	mLog.verbose("fetched contact from provider [" + contacts.get(0).getDisplayName() + "] id [" + contacts.get(0).getId() + "]"); 
            contactList.put(mJsonFactory.createJsonObject(contacts.get(0)));
            contacts.remove(0);
        }
        mLog.verbose("fetch contacts from provider [done]");
        return contactList;
    }

    private synchronized JSONObject sendSMS(JSONArray params) {
        JSONObject resultObject = new JSONObject();

        String address = "";
        String message = "";
        
        int paramsLength = params.length();
        try {
            if (paramsLength == 1) {
                JSONObject jsonParams = params.getJSONObject(0);
                address = jsonParams.getString("address");
                message = URLDecoder.decode(jsonParams.getString("message"));
                mLog.verbose("Fetch parameter adress: " + address
                        + " and message: " + message
                        + " from send sms request.");
            } else {
                this.setErrorState(resultObject,
                        "Corrupt amount of parameters given to send sms.");
                return resultObject;
            }
        } catch (JSONException jsonException) {
            mLog.error(
                    "Parameters for sending sms could not be fetched from the given api request.",
                    jsonException);
        }

        if (address.length() > 0 && message.length() > 0) {
            TextMessage sentMessage = new TextMessage();
            sentMessage.setAddress(address);
            sentMessage.setBody(message);
            int parts = mTextingAdapter.sendTextMessage(sentMessage);

            // store the part count or arrange an already set count for this
            // specific address
            if (this.mSMSWaitingForSentCallback.containsKey(address)) {
                int tmpCount = this.mSMSWaitingForSentCallback.get(address);
                this.mSMSWaitingForSentCallback
                        .put(address, (tmpCount + parts));
            } else {
                this.mSMSWaitingForSentCallback.put(address, parts);
            }
            mLog.verbose("Message queued to waiting for sent list with address "
                    + address + " and parts " + parts);

            this.setSuccessState(resultObject);
        } else {
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
    private synchronized JSONObject createPollingInfo() {
        JSONObject result = new JSONObject();
        mLog.info("Create polling json object");
        try {

            mLog.verbose("Evaluate contact changed state.");
            result.put("contact_changed", this.mContactsChanged);
            if (this.mContactsChanged) {
                result.put("contacts", this.mJsonContactList);
                this.mContactsChanged = false;
            }

            mLog.verbose("Evaluate sms sent error.");
            result.put("sms_sent_error", this.mSMSSentError);
            if (this.mSMSSentError) {
                JSONArray errorList = new JSONArray();
                for (int idx = 0; idx < mSMSSentErrorList.size(); idx++) {
                    errorList.put(mJsonFactory
                            .createJsonObject(mSMSSentErrorList.get(idx)));
                }
                result.put("sms_sent_error_messages", errorList);
                this.mSMSSentError = false;
            }

            mLog.verbose("Evaluate sms sent success.");
            result.put("sms_sent_success", this.mSMSSentSuccess);
            if (this.mSMSSentSuccess) {
                JSONArray sentList = new JSONArray();
                for (int idx = 0; idx < mSMSSentList.size(); idx++) {
                    sentList.put(mJsonFactory.createJsonObject(mSMSSentList
                            .get(idx)));
                }
                result.put("sms_sent_success_messages", sentList);
                this.mSMSSentSuccess = false;
            }

            mLog.verbose("Evaluate sms received.");
            result.put("sms_received", this.mSMSReceived);
            if (this.mSMSReceived) {                
                JSONArray recvList = new JSONArray();
                for (int idx = 0; idx < mSMSReceivedList.size(); idx++) {
                    recvList.put(mJsonFactory.createJsonObject(mSMSReceivedList
                            .get(idx)));
                }
                result.put("sms_received_messages", recvList);
                this.mSMSReceived = false;
            }

            mLog.verbose("Clear temporary member lists.");
            this.mSMSSentErrorList.clear();
            this.mSMSSentList.clear();
            this.mSMSReceivedList.clear();

            mLog.verbose("Evaluate actual telephone state.");
            BatteryStatus status = this.mSystemMonitor.getBatteryStatus();
            TelephonySignalStrength signal = this.mSystemMonitor
                    .getSignalStrength();
            if (status != null) {
                result.put("battery", mJsonFactory.createJsonObject(status));
            }
            if (signal != null) {
                result.put("signal", mJsonFactory.createJsonObject(signal));
            }

            this.setSuccessState(result);

        } catch (JSONException jsonException) {
            mLog.error("Could not create the polling json object", jsonException);
            this.setErrorState(result, "Could not create polling object");
        }

        return result;
    }

}

