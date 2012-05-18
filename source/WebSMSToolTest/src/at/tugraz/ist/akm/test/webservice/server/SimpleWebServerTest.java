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
import my.org.apache.http.Header;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.client.CookieStore;
import my.org.apache.http.client.HttpClient;
import my.org.apache.http.client.methods.HttpPost;
import my.org.apache.http.client.protocol.ClientContext;
import my.org.apache.http.entity.StringEntity;
import my.org.apache.http.impl.client.BasicCookieStore;
import my.org.apache.http.impl.client.DefaultHttpClient;
import my.org.apache.http.protocol.BasicHttpContext;
import my.org.apache.http.protocol.HttpContext;

import org.json.JSONObject;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.cookie.CookieManager;
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

            Assert.assertEquals(requestJson.toString(), new String(baos.toByteArray()));
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

            Assert.assertEquals(
                    new FileReader(getInstrumentation().getContext(), "web/index.html").read(),
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

    public void testSessionCookie() {
        startServer(false);

        Log.d("test", "testSimpleJsonRequest");

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpPost httppost = new HttpPost("http://localhost:8888/api.html");

        try {
            // --------------------------------------------------------------------------------------
            // send a simple request withouth a cookie
            // --------------------------------------------------------------------------------------
            JSONObject requestJson = new JSONObject();
            requestJson.put("method", "getSMS");

            JSONObject paramJson = new JSONObject();
            paramJson.put("user_id", 5);
            paramJson.put("detail_string", "foobar");

            requestJson.put("params", paramJson);

            httppost.setHeader("Accept", WebServerConfig.HTTP.CONTENT_TYPE_JSON);
            httppost.setHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE,
                    WebServerConfig.HTTP.CONTENT_TYPE_JSON);

            httppost.setEntity(new StringEntity(requestJson.toString()));

            HttpResponse response = httpClient.execute(httppost, context);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            // result of this should be that the response object contains an
            // error indicating
            // that the session cookie is expired
            JSONObject responseObject = new JSONObject(new String(baos.toByteArray()));

            Log.v("test", responseObject.toString());

            String state = responseObject.getString(WebServerConfig.JSON.STATE);
            Assert.assertEquals(WebServerConfig.JSON.STATE_SESSION_COOKIE_EXPIRED, state);

            // --------------------------------------------------------------------------------------
            // send a second request for logging in an user
            // --------------------------------------------------------------------------------------
            httppost.setHeader("Accept", WebServerConfig.HTTP.CONTENT_TYPE_JSON);
            httppost.setHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE,
                    WebServerConfig.HTTP.CONTENT_TYPE_JSON);

            requestJson = new JSONObject();
            requestJson.put("method", "login");

            httppost.setEntity(new StringEntity(requestJson.toString()));

            response = httpClient.execute(httppost, context);
            baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            // result of this should be that the response object contains an
            // error indicating
            // that the session cookie is expired
            responseObject = new JSONObject(new String(baos.toByteArray()));

            state = responseObject.getString(WebServerConfig.JSON.STATE);
            Assert.assertEquals(WebServerConfig.JSON.STATE_SUCCESS, state);

            Header cookieHeader = response.getFirstHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE);
            Assert.assertNotNull(cookieHeader);

            Log.d("test", "cookie header = " + cookieHeader);

            // --------------------------------------------------------------------------------------
            // next request should work without any error
            // --------------------------------------------------------------------------------------
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            requestJson = new JSONObject();
            requestJson.put("method", "getSMS");

            httppost.setEntity(new StringEntity(requestJson.toString()));

            response = httpClient.execute(httppost, context);
            baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            Header newCookieHeader = response
                    .getFirstHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE);
            Assert.assertNotNull(newCookieHeader);
            Assert.assertEquals(cookieHeader.getValue(), newCookieHeader.getValue());

            Assert.assertEquals(requestJson.toString(), new String(baos.toByteArray()));

            // --------------------------------------------------------------------------------------
            // last request to check wehter a cookie is expired or not
            // --------------------------------------------------------------------------------------
            CookieManager.COOKIE_VALID_TIME = 1;

            try {
                synchronized (this) {
                    this.wait(2000);
                }
            } catch (Exception e) {

            }

            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            requestJson = new JSONObject();
            requestJson.put("method", "getSMS");

            httppost.setEntity(new StringEntity(requestJson.toString()));

            response = httpClient.execute(httppost, context);
            baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            // result of this should be that the response object contains an
            // error indicating
            // that the session cookie is expired
            responseObject = new JSONObject(new String(baos.toByteArray()));

            state = responseObject.getString(WebServerConfig.JSON.STATE);
            Assert.assertEquals(WebServerConfig.JSON.STATE_SESSION_COOKIE_EXPIRED, state);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        stopServer();
    }
}
