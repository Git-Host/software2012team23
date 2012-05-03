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
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.SmsSentBroadcastReceiver;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonAPIRequestHandler extends AbstractHttpRequestHandler implements SmsIOCallback, ContactModifiedCallback {
    private final static String JSON_METHOD = "method";
    private final static String JSON_PARAMS = "params";
    private final static String JSON_STATE_SUCCESS = "success";
    private final static String JSON_STATE_ERROR = "error";

    private JsonFactory mJsonFactory = new JsonFactory();

    private TextingAdapter mTextingAdapter;

    
    /** members to represent a state */
    private volatile boolean mSMSSentSuccessfully = false;
    private volatile boolean mContactsChanged = false;
    private volatile boolean mSMSReceived = false;
    private volatile boolean mSMSSentError = false;
    
    private volatile HashMap<String,Integer> mWaitingSMSSentCallback = new HashMap<String, Integer>();
    private volatile List<String> mSentSMS = new ArrayList<String>();
    private volatile List<TextMessage> mReceivedSMSMessages = new ArrayList<TextMessage>();
    private volatile List<TextMessage> mSMSSentErrorMessages = new ArrayList<TextMessage>();    
    
    
    
    
    public JsonAPIRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
    	
        super(context, config, registry);        
        mTextingAdapter = new TextingAdapter(context, null, null);
        mTextingAdapter.start();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
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
    	JSONObject resultObject = new JSONObject();
		mLog.logI("Handle get_contacts request.");
		ContactFilter allFilter = new ContactFilter();
		List<Contact> contacts = mTextingAdapter.fetchContacts(allFilter);
		
        JSONArray contactList = new JSONArray();
		for(int i = 0; i < contacts.size(); i++){
			contactList.put(mJsonFactory.createJsonObject(contacts.get(i)));
		}
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
    		if(this.mWaitingSMSSentCallback.containsKey(address)){
    			int tmpCount = this.mWaitingSMSSentCallback.get(address);
    			this.mWaitingSMSSentCallback.put(address, (tmpCount+parts));
    		} else {
    			this.mWaitingSMSSentCallback.put(address, parts);
    		}
    		
    		this.setSuccessState(resultObject);
    	} else {
    		this.setErrorState(resultObject, "One or all of the given parameters are empty or corrupt.");
    	}
		return resultObject;
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
			if(mWaitingSMSSentCallback.containsKey(address)){
				int tmpCount = mWaitingSMSSentCallback.get(address);
				tmpCount = tmpCount - 1;
				
				//if we received all callbacks for an specific address we can assume, that the sms was sent successfully and the count is 0
				if(tmpCount == 0){ 
					mSMSSentSuccessfully = true;  //poll will watch for this var to check the notificiation
					mSentSMS.add(address);		//in this list all address the user will be notified are stored
					mWaitingSMSSentCallback.remove(address);  //delete the address from the waiting map
					mLog.logV("Received all sms callbacks for address "+address+" going to notify webapp.");					
				} else {
					mWaitingSMSSentCallback.put(address, tmpCount); //put back the count -1 in the map
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
		this.mSMSSentErrorMessages.add(message);
	}

	@Override
	public synchronized void smsDeliveredCallback(Context context, Intent intent) {
		//not working so we do not bother about it
		
	}

	@Override
	public synchronized void smsReceivedCallback(Context context, Intent intent) {
		this.mSMSReceived = true;
		TextMessage message = this.parseToTextMessgae(intent);
		this.mReceivedSMSMessages.add(message);
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