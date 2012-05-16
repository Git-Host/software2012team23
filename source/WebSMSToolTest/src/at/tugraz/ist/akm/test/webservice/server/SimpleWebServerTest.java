package at.tugraz.ist.akm.test.webservice.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import junit.framework.Assert;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.client.HttpClient;
import my.org.apache.http.client.methods.HttpPost;
import my.org.apache.http.entity.StringEntity;
import my.org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONObject;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class SimpleWebServerTest extends InstrumentationTestCase {

    private HttpClient httpClient;
    private static SimpleWebServer webserver;

    private void startServer(final boolean https) {
        if (webserver != null && webserver.isRunning()) {
            stopServer();
        }
        
        Log.d("SimpleWebServerTest", "start server");
        
        httpClient = new DefaultHttpClient();
        webserver = new SimpleWebServer(getInstrumentation().getContext(), https);
        Assert.assertTrue(webserver.startServer(8888));
        Assert.assertTrue(webserver.startServer(9999));
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
        Log.d("SimpleWebServerTest", "stop server");
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
        startServer(false);

        Log.d("test", "testSimpleJsonRequest");

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

            Assert.assertEquals(requestJson.toString(),
                    new String(baos.toByteArray()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        stopServer();
    }

    public void testSimpleFileRequest() {
        startServer(false);
        Log.d("test", "testSimpleFileRequest");       
        
        HttpPost httppost = new HttpPost("http://localhost:8888/");
        try {
            httppost.setHeader("Accept", "application/text");
            httppost.setHeader("Content-type", "application/text");

            HttpResponse response = httpClient.execute(httppost);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            Assert.assertEquals(new FileReader(getInstrumentation()
                    .getContext(), "web/index.html").read(),
                    new String(baos.toByteArray()));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        stopServer();
    }
    
    public void testStartSecureServer() {
        Log.d("test", "testStartSecureServer");
        
        startServer(true);
        Assert.assertTrue(webserver.isRunning());
        
        stopServer();
        Assert.assertFalse(webserver.isRunning());
    }

    public void testKeyStore() {
        try {
            KeyStore keystore = KeyStore.getInstance("BKS");
            
            Context context = this.getInstrumentation().getTargetContext().getApplicationContext();
            InputStream is = context.getResources().openRawResource(R.raw.websms);
            
            keystore.load(is, "foobar64".toCharArray());
            String alias = "at.tugraz.ist.akm.websms";
    
            Key key = keystore.getKey(alias, "foobar64".toCharArray());
            if (!(key instanceof PrivateKey)) {
                Assert.fail("Private key not found!");
            }
        } catch (KeyStoreException e) {
            Assert.fail(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage());
        } catch (CertificateException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (UnrecoverableKeyException e) {
            Assert.fail(e.getMessage());
        }
    }
}
