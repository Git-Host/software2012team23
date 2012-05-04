package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.SmsSentBroadcastReceiver;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.texting.TextingInterface;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonAPIRequestHandler extends AbstractHttpRequestHandler implements SmsIOCallback, ContactModifiedCallback {
    private final static String JSON_METHOD = "method";
    private final static String JSON_PARAMS = "params";
    private final static String JSON_STATE_SUCCESS = "success";
    private final static String JSON_STATE_ERROR = "error";

    private JsonFactory mJsonFactory = new JsonFactory();

    private volatile TextingInterface mTextingAdapter;
    private volatile SystemMonitor mSystemMonitor;

    
    /** members to represent a state */
    private volatile boolean mSMSSentSuccess = false;
    private volatile boolean mContactsChanged = false;
    private volatile boolean mSMSReceived = false;
    private volatile boolean mSMSSentError = false;
    
    private volatile HashMap<String,Integer> mSMSWaitingForSentCallback = new HashMap<String, Integer>();
    private volatile List<TextMessage> mSMSSentList = new ArrayList<TextMessage>();
    private volatile List<TextMessage> mSMSReceivedList = new ArrayList<TextMessage>();
    private volatile List<TextMessage> mSMSSentErrorList = new ArrayList<TextMessage>();    
    
    
    
    
    public JsonAPIRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
    	
        super(context, config, registry);        
        mTextingAdapter = new TextingAdapter(context, this, this);
        mTextingAdapter.start();
        
        mSystemMonitor = new SystemMonitor(context);
        mSystemMonitor.start();
    }

    @Override
    public synchronized void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {

        if (httpRequest.getRequestLine().getMethod().equals("POST")) {
            BasicHttpEntityEnclosingRequest post = (BasicHttpEntityEnclosingRequest) httpRequest;
            JSONObject json;
            try {
                json = new JSONObject(EntityUtils.toString(post.getEntity()));
                String method = json.getString(JSON_METHOD);
                if (method != null && method.length() > 0) {
                	JSONArray jsonParams = null;
                	if(json.isNull(JSON_PARAMS) == false){
                		jsonParams = json.getJSONArray(JSON_PARAMS);
                	}
                    JSONObject jsonResponse = processMethod(method, jsonParams);
                    sendResponse(httpResponse, jsonResponse);
                } else {
                    mLog.logE("no method defined in JSON post request ==> <" + json.toString() + ">");
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(HttpResponse httpResponse, final JSONObject jsonResponse) {
        httpResponse.setEntity(new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream);
                writer.write(jsonResponse.toString());
                writer.flush();
                writer.close();
            }
        }));
        httpResponse.setHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE,
                WebServerConfig.HTTP.VALUE_CONTENT_TYPE_JSON);
    }

    private JSONObject processMethod(String method, JSONArray jsonParams) {
        // TODO: call any API class to either retrieved the desired data or
        // executed an action
    	mLog.logI("Handle api request with given method: "+method);

        JSONObject resultObject = new JSONObject();
        
        if(jsonParams == null){
        	mLog.logI("No parameter for request "+method);
        } else {
        	mLog.logI("Given parameters: "+jsonParams.toString());
        }
		
        try 
        {        
	    	if(method.compareTo("get_contacts") == 0)
	    	{
	    		resultObject = this.getContacts();
	    	} 
	    	else if(method.compareTo("send_sms_message") == 0)
	    	{
	    		resultObject = this.sendSMS(jsonParams);
	    	}
	    	else if(method.compareTo("info") == 0)
	    	{
	    		resultObject = this.createPollingInfo();
	    	}
	    	else 
	    	{
	    		String logMsg = "No method found for given request method: "+method;
	            mLog.logW(logMsg);
				resultObject.put("state", JSON_STATE_ERROR);
				resultObject.put("error_msg", logMsg);
	    	}
		} catch (JSONException e) {
            mLog.logE("Could not create jsonobject for handling api request.",e);
		}
    	
//        Object obj = new Object(); // this a place holder for any data retrieved
//                                   // from the API.
//
//
//        if (obj instanceof List<?>) {
//            resultObject = jsonFactory.createJsonObjectFromList((List<?>) obj);
//        } else {
//            resultObject = jsonFactory.createJsonObject(obj);
//        }
        return resultObject;
    }
    
    
    
    private void setSuccessState(JSONObject obj){
    	try {
			obj.put("state", JSON_STATE_SUCCESS);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    
    private void setErrorState(JSONObject obj, String msg){
    	try {
			obj.put("state", JSON_STATE_ERROR);
			obj.put("error_msg", msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}    	
    }
    
    
    
    
    private synchronized JSONObject getContacts(){
		mLog.logI("Handle get_contacts request.");
		JSONObject resultObject = new JSONObject();	
        JSONArray contactList = this.fetchContactsJsonArray();
        try {
			resultObject.put("contacts", contactList);
		} catch (JSONException e) {
            mLog.logE("Could not append contact list to json object.",e);
			return new JSONObject();
		}
        
		if(resultObject.length() > 0){
			this.setSuccessState(resultObject);
		} else {
			this.setErrorState(resultObject, "No contacts could be found on the device.");
		}
        
        return resultObject;
    }
    
    
    
    private JSONArray fetchContactsJsonArray(){
    	ContactFilter allFilter = new ContactFilter();
		List<Contact> contacts = mTextingAdapter.fetchContacts(allFilter);
        JSONArray contactList = new JSONArray();
		for(int i = 0; i < contacts.size(); i++){
			contactList.put(mJsonFactory.createJsonObject(contacts.get(i)));
		}
        return contactList;
    }
    
    
    
    private synchronized JSONObject sendSMS(JSONArray params){
    	JSONObject resultObject = new JSONObject();
    	
    	String address = "";
    	String message = "";
    	    	
    	int paramsLength = params.length();
    	try
    	{
	    	if(paramsLength == 1){
	    		JSONObject jsonParams = params.getJSONObject(0);
	    		address = jsonParams.getString("address");
	    		message = jsonParams.getString("message");
	    		mLog.logV("Fetch parameter adress: "+address+" and message: "+message+" from send sms request.");
	    	} else {
	    		this.setErrorState(resultObject, "Corrupt amount of parameters given to send sms.");
	    		return resultObject;
	    	}
    	} catch(JSONException e){
    		mLog.logE("Parameters for sending sms could not be fetched from the given api request.",e);
    	}
    	
    	if(address.length() > 0 && message.length() > 0){
    		TextMessage sentMessage = new TextMessage();
    		sentMessage.setAddress(address);
    		sentMessage.setBody(message);
    		int parts = mTextingAdapter.sendTextMessage(sentMessage);
    		
    		//store the part count or arrange an already set count for this specific address
    		if(this.mSMSWaitingForSentCallback.containsKey(address)){
    			int tmpCount = this.mSMSWaitingForSentCallback.get(address);
    			this.mSMSWaitingForSentCallback.put(address, (tmpCount+parts));
    		} else {
    			this.mSMSWaitingForSentCallback.put(address, parts);
    		}
    		mLog.logV("Message queued to waiting for sent list with address "+address+" and parts "+parts);
    		
    		this.setSuccessState(resultObject);
    	} else {
    		this.setErrorState(resultObject, "One or all of the given parameters are empty or corrupt.");
    	}
		return resultObject;
    }
    
    
    
    /**
     * Provide the following informations:
     * --> Update of the statusbar
     * --> Inform about contact changes and if true send whole contacts back.
     * --> Inform about successfully sent sms and if true array of address which has been sent
     * --> Inform about received sms and if true the return the textmessages as json
     * @return
     */
    private synchronized JSONObject createPollingInfo() {
    	JSONObject result = new JSONObject();
    	mLog.logI("Create polling json object");
		try {
			
			mLog.logV("Evaluate contact changed state.");
			result.put("contact_changed", this.mContactsChanged);
			if(this.mContactsChanged){
	    		result.put("contacts",this.fetchContactsJsonArray());
	    		this.mContactsChanged = false;
	    	}
			
	    	

			mLog.logV("Evaluate sms sent error.");
	    	result.put("sms_sent_error", this.mSMSSentError);
	    	if(this.mSMSSentError){
	    		JSONArray errorList = new JSONArray();
	    		for(int i = 0; i < mSMSSentErrorList.size(); i++){
	    			errorList.put(mJsonFactory.createJsonObject(mSMSSentErrorList.get(i)));
	    		}
	    		result.put("sms_sent_error_messages", errorList);
	    		this.mSMSSentError = false;
	    	}
	    	

			mLog.logV("Evaluate sms sent success.");
	    	result.put("sms_sent_success", this.mSMSSentSuccess);
	    	if(this.mSMSSentSuccess){
	    		JSONArray sentList = new JSONArray();
	    		for(int i = 0; i < mSMSSentList.size(); i++){
	    			sentList.put(mJsonFactory.createJsonObject(mSMSSentList.get(i)));
	    		}
	    		result.put("sms_sent_success_messages", sentList);
	    		this.mSMSSentSuccess = false;
	    	}
	    	

			mLog.logV("Evaluate sms received.");
	    	result.put("sms_received", this.mSMSReceived);
	    	if(this.mSMSSentSuccess){
	    		JSONArray recvList = new JSONArray();
	    		for(int i = 0; i < mSMSSentList.size(); i++){
	    			recvList.put(mJsonFactory.createJsonObject(mSMSReceivedList.get(i)));
	    		}
	    		result.put("sms_received_messages", recvList);
	    		this.mSMSReceived = false;
	    	}
	    	
			mLog.logV("Clear temporary member lists.");
	    	this.mSMSSentErrorList.clear();
	    	this.mSMSSentList.clear();
	    	this.mSMSReceivedList.clear();
	    	
	    	
			mLog.logV("Evaluate actual telephone state.");
	    	BatteryStatus status = this.mSystemMonitor.getBatteryStatus();
	    	TelephonySignalStrength signal = this.mSystemMonitor.getSignalStrength();
	    	if(status != null){
	    		result.put("battery", mJsonFactory.createJsonObject(status));
	    	}
	    	if(signal != null){
	    		result.put("signal", mJsonFactory.createJsonObject(signal));
	    	}
	    	
	    	this.setSuccessState(result);
		
		} catch (JSONException e) {
			mLog.logE("Could not create the polling json object",e);
	    	this.setErrorState(result, "Could not create polling object");
		}    	

    	return result;
    }
    
    

    
	@Override
	public synchronized void contactModifiedCallback() {
		this.mContactsChanged = true;
	}

	@Override
	public synchronized void smsSentCallback(Context context, Intent intent) {
		TextMessage message = this.parseToTextMessgae(intent);
		if(message != null){
			String address = message.getAddress();
			mLog.logV("Looking for address in waiting queue with :"+address);
			if(mSMSWaitingForSentCallback.containsKey(address)){
				int tmpCount = mSMSWaitingForSentCallback.get(address);
				tmpCount = tmpCount - 1;
				
				//if we received all callbacks for an specific address we can assume, that the sms was sent successfully and the count is 0
				if(tmpCount == 0){ 
					mSMSSentSuccess = true;  //poll will watch for this var to check the notificiation
					mSMSSentList.add(message);		//in this list all address the user will be notified are stored
					mSMSWaitingForSentCallback.remove(address);  //delete the address from the waiting map
					mLog.logV("Received all sms callbacks for address "+address+" going to notify webapp.");
				} else {
					mSMSWaitingForSentCallback.put(address, tmpCount); //put back the count -1 in the map
					mLog.logV("Received sms callback for address "+address+" - count is: "+tmpCount);
				}
			} else {
				mLog.logE("Got a callback for address "+address+" but could not be found in waiting list!");
			}
		} else {
			mLog.logW("A received callback could not be converted to an TextMessage - Possible lost of sms notification to the webapp");
		}
	}

	@Override
	public synchronized void smsSentErrorCallback(Context context, Intent intent) {
		this.mSMSSentError = true;
		TextMessage message = this.parseToTextMessgae(intent);
		this.mSMSSentErrorList.add(message);
	}

	@Override
	public synchronized void smsDeliveredCallback(Context context, Intent intent) {
		//not working so we do not bother about it
		
	}

	@Override
	public synchronized void smsReceivedCallback(Context context, Intent intent) {
		this.mSMSReceived = true;
		TextMessage message = this.parseToTextMessgae(intent);
		this.mSMSReceivedList.add(message);
	}
	
	
	//TODO: This is copy and paste from SmsBridge.java - change it to public static in the bridge or think generally about it
	private TextMessage parseToTextMessgae(Intent intent) {
		try {
			Bundle extrasBundle = intent.getExtras();
			if (extrasBundle != null) {
				Serializable serializedTextMessage = extrasBundle
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);

				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					return sentMessage;
				}

			} else {
				mLog.logV("couldn't find any text message infos at all :(");
			}
		} catch (Exception e) {
			mLog.logV("FAILED to gather text message extras from intent");
		}
		return null;
	}	
    
}