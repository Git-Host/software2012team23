package at.tugraz.ist.akm.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class WebServerRequestTest extends TestCase {

	HttpClient httpClient;
	
	protected void setUp() throws Exception {
	    httpClient = new DefaultHttpClient();
	};
	
	
	@Test
	public void testSimpleJsonRequest(){
	    HttpPost httppost = new HttpPost("localhost:8888/api.html");

	    try {
	  
	        JSONObject requestJson = new JSONObject();
	        try {
				requestJson.put("method", "getSMS");
		        
		        JSONObject paramJson = new JSONObject();
		        paramJson.put("user_id", 5);
		        paramJson.put("detail_string", "foobar");
		        
		        requestJson.put("params", paramJson);
		        
	            httppost.setHeader("Accept", "application/json");
	            httppost.setHeader("Content-type", "application/json");
	               
		        httppost.setEntity(new StringEntity(requestJson.toString()));

		        httppost.toString();
		        
		        HttpResponse response = this.httpClient.execute(httppost);				
				response.getEntity().getContent().toString();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	}

	
	
}
