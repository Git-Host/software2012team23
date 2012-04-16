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
    private static SimpleWebServer webserver;

    private void startServer() {
        Log.d("SimpleWebServerTest", "start server!!!!");
        httpClient = new DefaultHttpClient();
        webserver = new SimpleWebServer(getInstrumentation().getContext());
        Assert.assertTrue(webserver.startServer(8888));
        Assert.assertFalse(webserver.startServer(9999));
        while (!webserver.isRunning()) {
            synchronized (SimpleWebServerTest.class) {
                try {
                    SimpleWebServerTest.class.wait(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopServer() {
        Log.d("SimpleWebServerTest", "stop server!!!!");
        webserver.stopServer();
        while (webserver.isRunning()) {
            synchronized (SimpleWebServerTest.class) {
                try {
                    SimpleWebServerTest.class.wait(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public void testSimpleJsonRequest() {
        startServer();

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
        stopServer();
    }

    public void testSimpleFileRequest() {
        startServer();
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
        stopServer();
    }

}
