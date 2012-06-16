/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.json.JSONArray;
import org.json.JSONObject;

import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class SimpleWebServerTest extends WebSMSToolActivityTestcase {

    private HttpClient mHttpClient = null;
    private static SimpleWebServer mWebserver = null;

    public SimpleWebServerTest()
    {
    	super(SimpleWebServer.class.getSimpleName());
    }
    
    
    private void startServer(final boolean useMockServer) throws Exception {
    	logDebug("start server");
        if (mWebserver != null) {
        	if (mWebserver.isRunning()) {
        		stopServer();
        	}
        	mWebserver = null;
        } 
        
        mWebserver = useMockServer ? new MockSimpleWebServer(mContext) : 
        	new SimpleWebServer(mContext, "0.0.0.0");
        
        mHttpClient = new DefaultHttpClient();

        Assert.assertTrue(mWebserver.startServer());
        waitForServiceBeingStarted(50, 200);
        assertTrue(mWebserver.isRunning());
    }

    private void stopServer() {
        logDebug("stop server");
        if ( mWebserver.isRunning() ) {
	        mWebserver.stopServer();
	        waitForServiceBeingStopped(50, 200);
        }
    }
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	Config serverConfig = new Config(mContext);
    	serverConfig.setProtocol("http");
    	serverConfig.setPort("8888");
    }
    
    public void testStartStopServer() {
    	
    	try {
	    	Config c = new Config(mContext);
	    	mWebserver = new SimpleWebServer(mContext, c.getPort());
	    	mWebserver.startServer();
	    	
	    	waitForServiceBeingStarted(20, 200);
	        assertTrue(mWebserver.isRunning());
    	}
    	catch (Exception ex ) {
    		ex.printStackTrace();
    		Assert.fail("failed to stat server");
    	}
    	
    	try {
	    	mWebserver.stopServer();
	    	waitForServiceBeingStopped(20, 200);
	    	assertFalse(mWebserver.isRunning());
    	}
    	catch (Exception ex ) {
    		ex.printStackTrace();
    		Assert.fail("failed to stop server");
    	}
    }
    
    private void waitForServiceBeingStarted(int maxAttempts, int delayMs)
    {
        while ( (!mWebserver.isRunning()) && (maxAttempts > 0) ) {
            synchronized (this) {
                try {
                    SimpleWebServerTest.class.wait(delayMs);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            --maxAttempts;
        }
    }
    
    private void waitForServiceBeingStopped(int maxAttempts, int delayMs)
    {
        while ( (mWebserver.isRunning()) && (maxAttempts > 0) ) {
            synchronized (this) {
                try {
                    SimpleWebServerTest.class.wait(delayMs);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            --maxAttempts;
        }
    }

    public void testSimpleJsonRequest() {
        try {
        	startServer(true);
            logDebug("testSimpleJsonRequest");
            HttpPost httppost = new HttpPost("http://localhost:8888/api.html");
            
            JSONObject request = new JSONObject();
            request.put("method", "info");
            httppost.setEntity(new StringEntity(request.toString()));

            HttpResponse httpresponse = mHttpClient.execute(httppost);
            stopServer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpresponse.getEntity().writeTo(baos);

            JSONObject response = new JSONObject(baos.toString());
            JSONObject batteryStatus = response.getJSONObject("battery");
            String batteryIsCharging = batteryStatus.getString("is_charging");
            String batteryLevel = batteryStatus.getString("battery_level");
            String batteryLevelIcon = batteryStatus.getString("battery_level_icon");
            String isAcCharge = batteryStatus.getString("is_ac_charge");
            String isFull = batteryStatus.getString("is_full");
            String isUsbCharge = batteryStatus.getString("is_usb_charge");
            
            String hasContactChanged = response.getString("contact_changed"); 
            
            /*
            // wel'll get no signal strength changed intent in the very first
            // seconds the app is running on device.
            // if app is running on emulator we won't get the intent at all.
            JSONObject signalStatus = response.getJSONObject("signal");
            String cdmaLevel = batteryStatus.getString("cdma_level");
            String evdoLevel = batteryStatus.getString("evdo_level");
            String gsmLevel = batteryStatus.getString("gsm_level");
            String signalIcon = batteryStatus.getString("signal_icon");
            String signalStrength = batteryStatus.getString("signal_strength");
            */
            String smsReceived = response.getString("sms_received");
            String smsSentError = response.getString("sms_sent_error");
            String smsSentSuccess = response.getString("sms_sent_success");
            
            String requestStatus = response.getString("state");

            assertFalse( null == batteryIsCharging);
            assertFalse( null == batteryLevel);
            assertFalse( null == batteryLevelIcon);
            assertFalse( null == isAcCharge);
            assertFalse( null == isFull);
            assertFalse( null == isUsbCharge);
            assertFalse( null == hasContactChanged);
            /*
            assertFalse( null == signalStatus);
            assertFalse( null == cdmaLevel);
            assertFalse( null == evdoLevel);
            assertFalse( null == gsmLevel);
            assertFalse( null == signalIcon);
            assertFalse( null == signalStrength);
            */
            assertFalse( null == smsReceived);
            assertFalse( null == smsSentError);
            assertFalse( null == smsSentSuccess);
            assertFalse( null == requestStatus);
        } catch (Exception ex) {
        	ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

    public void testSimpleFileRequest() {
        try {
            startServer(true);
            logDebug("testSimpleFileRequest");
            HttpPost httppost = new HttpPost("http://localhost:8888/");
            httppost.setHeader("Accept", "application/text");
            httppost.setHeader("Content-type", "application/text");

            HttpResponse response = mHttpClient.execute(httppost);
            stopServer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);

            Assert.assertEquals(
                    new FileReader(mContext, "web/index.html").read(),
                    new String(baos.toByteArray()));

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testStartSecureServer() {
        logDebug("testStartSecureServer");
        
    	Config serverConfig = new Config(mContext);
    	serverConfig.setProtocol("https");
    	serverConfig.setPort("8888");
    	
    	try {
	        startServer(true);
	        Assert.assertTrue(mWebserver.isRunning());
    	} catch (Exception ex)
    	{
    		Assert.assertTrue(false);
    	}

    	stopServer();
        Assert.assertFalse(mWebserver.isRunning());
    }

    public void testKeyStore() {
        try {
            KeyStore keystore = KeyStore.getInstance("BKS");

            InputStream is = mContext.getResources().openRawResource(R.raw.websms);

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
