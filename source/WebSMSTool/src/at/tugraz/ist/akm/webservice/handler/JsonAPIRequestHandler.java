package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

import android.app.Activity;
import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonAPIRequestHandler extends AbstractHttpRequestHandler {
    private final static String JSON_METHOD = "method";
    private final static String JSON_PARAMS = "params";

    private JsonFactory jsonFactory = new JsonFactory();

    private TextingAdapter mTextingAdapter;
    
    public JsonAPIRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);
        String uri = config.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_URI_PATTERN);
        if (uri == null || uri.trim().length() == 0) {
            LOG.e("no uri configured");
        } else {
            register(uri);
        }
        
        mTextingAdapter = new TextingAdapter((Activity)context, null, null);
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
                    LOG.e("no method defined in JSON post request ==> <" + json.toString() + ">");
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
    	   	
        JSONObject resultObject = new JSONObject();
    	
        if(jsonParams != null){
        	LOG.i("No parameter for request "+method);
        } else {
        	LOG.i("Given parameters: "+jsonParams.toString());
        }
        
        
    	if(method == "get_contacts"){
    		LOG.i("Handle get_contacts request.");
    		ContactFilter allFilter = new ContactFilter();
    		List<Contact> contacts = mTextingAdapter.fetchContacts(allFilter);
    		
            JSONArray contactList = new JSONArray();
    		for(int i = 0; i < contacts.size(); i++){
    			contactList.put(jsonFactory.createJsonObject(contacts.get(i)));
    		}
            try {
        		resultObject.put("state", "success");
				resultObject.put("contacts", contactList);
			} catch (JSONException e) {
                LOG.e("Could not append contact list to json object.");
				e.printStackTrace();
			}
    	} else {
            LOG.w("No method found for given request method: "+method);
    		try {
				resultObject.put("state", "error");
			} catch (JSONException e) {
                LOG.e("Could not create default error object.");				
				e.printStackTrace();
			}
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
}