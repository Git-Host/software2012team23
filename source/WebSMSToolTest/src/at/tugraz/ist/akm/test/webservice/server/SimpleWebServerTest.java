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
import at.tugraz.ist.akm.test.texting.TextingAdapterTest;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class SimpleWebServerTest extends InstrumentationTestCase {

    private HttpClient httpClient;
    private static SimpleWebServer webserver;
    private Logable mLog = null;

    public SimpleWebServerTest()
    {
        Logger.setSink(new ThrowingLogSink());
        mLog = new Logable(SimpleWebServerTest.class.getSimpleName());
    }
    
    private void startServer(final boolean https, final boolean useMockServer) {
        if (webserver != null && webserver.isRunning()) {
            stopServer();
        }

        mLog.logDebug("start server");

        httpClient = new DefaultHttpClient();
        webserver = useMockServer ? new MockSimpleWebServer(getInstrumentation().getContext(), https) : 
                                    new SimpleWebServer(getInstrumentation().getContext(), https);
        Assert.assertTrue(webserver.startServer(8888));
        Assert.assertTrue(webserver.startServer(9999));
        while (!webserver.isRunning()) {
            synchronized (SimpleWebServerTest.class) {
                try {
                    SimpleWebServerTest.class.wait(200);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }

    private void stopServer() {
        mLog.logDebug("stop server");
        webserver.stopServer();
        while (webserver.isRunning()) {
            synchronized (SimpleWebServerTest.class) {
                try {
                    SimpleWebServerTest.class.wait(200);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }

    public void testSimpleJsonRequest() {
        startServer(false, true);

        mLog.logDebug("testSimpleJsonRequest");

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
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        stopServer();
    }

    public void testSimpleFileRequest() {
        startServer(false, true);
        mLog.logDebug("testSimpleFileRequest");

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

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        stopServer();
    }

    public void testStartSecureServer() {
        mLog.logDebug("testStartSecureServer");

        startServer(true, true);
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
        } catch (KeyStoreException keyException) {
            Assert.fail(keyException.getMessage());
        } catch (NoSuchAlgorithmException algoException) {
            Assert.fail(algoException.getMessage());
        } catch (CertificateException certificateException) {
            Assert.fail(certificateException.getMessage());
        } catch (IOException ioException) {
            Assert.fail(ioException.getMessage());
        } catch (UnrecoverableKeyException keyException) {
            Assert.fail(keyException.getMessage());
        }
    }
}
