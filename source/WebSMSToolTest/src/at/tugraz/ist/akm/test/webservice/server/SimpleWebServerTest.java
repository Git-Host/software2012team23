package at.tugraz.ist.akm.test.webservice.server;

import java.io.ByteArrayOutputStream;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class SimpleWebServerTest extends InstrumentationTestCase {

    private HttpClient httpClient;
    private SimpleWebServer webserver;
    
    
    protected void setUp() throws Exception {
        httpClient = new DefaultHttpClient();
        webserver = new SimpleWebServer(getInstrumentation().getContext(), 8888);
        new Thread(webserver).start();
    };

    public void testSimpleJsonRequest() {

        Log.d("test", "testSimpleJsonRequest!!!!");

        HttpPost httppost = new HttpPost("http://localhost:8888/api.html");
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            Assert.assertEquals(requestJson.toString(), new String(baos.toByteArray()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testSimpleFileRequest() {

        Log.d("test", "testSimpleFileRequest");

        HttpPost httppost = new HttpPost("http://localhost:8888/");
        try {
            httppost.setHeader("Accept", "application/text");
            httppost.setHeader("Content-type", "application/text");

            HttpResponse response = httpClient.execute(httppost);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            Assert.assertEquals(
                    new FileReader(getInstrumentation().getContext(), "web/index.html").read(),
                    new String(baos.toByteArray()));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        webserver.stopServer();
    }
}
