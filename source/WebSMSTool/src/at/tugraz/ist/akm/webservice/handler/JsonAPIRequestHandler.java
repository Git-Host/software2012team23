package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
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
    
    private volatile HashMap<String,Integer> mWaitingSMSSentCallback = new HashMap<String, Integer>();
    private volatile List<String> mSentSMS = new ArrayList<String>();
    private volatile List<TextMessage> mReceivedSMSMessages = new ArrayList<TextMessage>();
    
    
    
    
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
            mLog.logE("Could not create jsonobject for handling api request.");				
			e.printStackTrace();
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
            mLog.logE("Could not append contact list to json object.");
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
    		mLog.logE("Parameters for sending sms could not be fetched from the given api request.");
    	}
    	
    	if(address.length() > 0 && message.length() > 0){
    		TextMessage sentMessage = new TextMessage();
    		sentMessage.setAddress(address);
    		sentMessage.setBody(message);
    		int parts = mTextingAdapter.sendTextMessage(sentMessage);
    		//TODO create a map in the class to store send messages with parts as count
    		//TODO determine the sms io callback to count down the part message count in the map
    		//TODO if part message count is zero set a variable or a map with stupid data which can be returned at the next poll request
    		this.setSuccessState(resultObject);
    	} else {
    		this.setErrorState(resultObject, "One or all of the given parameters are empty or corrupt.");
    	}
		return resultObject;
    }

    
	@Override
	public void contactModifiedCallback() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void smsSentCallback(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void smsSentErrorCallback(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		//not working so we do not bother about it
		
	}

	@Override
	public void smsReceivedCallback(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
    
}