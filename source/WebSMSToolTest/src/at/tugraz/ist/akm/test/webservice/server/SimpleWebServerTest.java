package at.tugraz.ist.akm.test.webservice.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class SimpleWebServerTest extends InstrumentationTestCase {

    HttpClient httpClient;

    protected void setUp() throws Exception {
        httpClient = new DefaultHttpClient();
        new Thread(new SimpleWebServer(getInstrumentation().getContext(), 8888)).start();
    };

    public void testSimpleJsonRequest() {

        HttpPost httppost = new HttpPost("http://localhost:8888/api.html");

        Log.d("test", "testSimpleJsonRequest!!!!");
        try {

            JSONObject requestJson = new JSONObject();
            requestJson.put("method", "getSMS");

            JSONObject paramJson = new JSONObject();
            paramJson.put("user_id", 5);
            paramJson.put("detail_string", "foobar");

            requestJson.put("params", paramJson);

            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            httppost.setEntity(new StringEntity(requestJson.toString()));

            HttpResponse response = httpClient.execute(httppost);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
