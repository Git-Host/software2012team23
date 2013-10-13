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

package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import my.org.apache.http.HttpResponseInterceptor;
import my.org.apache.http.impl.DefaultConnectionReuseStrategy;
import my.org.apache.http.impl.DefaultHttpResponseFactory;
import my.org.apache.http.params.CoreConnectionPNames;
import my.org.apache.http.params.CoreProtocolPNames;
import my.org.apache.http.params.HttpParams;
import my.org.apache.http.params.SyncBasicHttpParams;
import my.org.apache.http.protocol.BasicHttpContext;
import my.org.apache.http.protocol.HttpProcessor;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import my.org.apache.http.protocol.HttpService;
import my.org.apache.http.protocol.ImmutableHttpProcessor;
import my.org.apache.http.protocol.ResponseConnControl;
import my.org.apache.http.protocol.ResponseContent;
import my.org.apache.http.protocol.ResponseDate;
import my.org.apache.http.protocol.ResponseServer;
import android.content.Context;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.PreferencesProvider;
import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.handler.AbstractHttpRequestHandler;
import at.tugraz.ist.akm.webservice.handler.interceptor.IRequestInterceptor;

public class SimpleWebServer {
    private final static LogClient mLog = new LogClient(SimpleWebServer.class.getName());

    HttpRequestHandlerRegistry mRegistry = new HttpRequestHandlerRegistry();
    private BasicHttpContext mHttpContext = new BasicHttpContext();

    private final Context mContext;
    private ServerThread mServerThread = null;
    private Vector<AbstractHttpRequestHandler> mHandlerReferenceListing = new Vector<AbstractHttpRequestHandler>();

    private InetAddress mSocketAddress = null;
    private ServerSocket mServerSocket = null;
    private boolean mHttps;
    private int mServerPort;
	private String mKeyStorePass;
	
    private SSLContext mSSLContext;

    private boolean mIsServerRunning = false;


    public SimpleWebServer(Context context, String socketAddress) throws Exception {
        this.mContext = context;
        this.mSocketAddress = InetAddress.getByName(socketAddress);
        readRequestHandlers();
        readRequestInterceptors();
    }

    protected synchronized HttpService initializeHTTPService() {
        HttpProcessor httpProcessor = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                new ResponseDate(), new ResponseServer(), new ResponseContent(),
                new ResponseConnControl() });

        HttpParams params = new SyncBasicHttpParams()
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

        HttpService httpService = new HttpService(httpProcessor,
                new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory(), mRegistry,
                params);

        return httpService;
    }

    protected BasicHttpContext getHttpContext() {
        return mHttpContext;
    }

    private void readRequestHandlers() {
        XmlReader reader = new XmlReader(mContext, WebServerConfig.RES.WEB_XML);
        List<XmlNode> nodes = reader.getNodes(WebServerConfig.XML.TAG_REQUEST_HANDLER);
        for (XmlNode node : nodes) {
            String className = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CLASS);

            if (className == null) {
                mLog.error("request handler <" + node.getName()
                        + ">: no corresponding class to load found");
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(Context.class, XmlNode.class,
                        HttpRequestHandlerRegistry.class);
                AbstractHttpRequestHandler newHandler = (AbstractHttpRequestHandler) constr
                        .newInstance(mContext, node, mRegistry);
                mHandlerReferenceListing.add(newHandler);
            } catch (Exception ex) {
                mLog.error("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.info("request handlers read from configuration");
    }

    private void readRequestInterceptors() {
        XmlReader reader = new XmlReader(mContext, WebServerConfig.RES.WEB_XML);
        List<XmlNode> interceptorNodes = reader
                .getNodes(WebServerConfig.XML.TAG_REQUEST_INTERCEPTORS);
        if (interceptorNodes.size() == 0) {
            mLog.warning("no request interceptors configured");
            return;
        }
        List<XmlNode> nodes = interceptorNodes.get(0).getChildNodes(
                WebServerConfig.XML.TAG_INTERCEPTOR);
        for (XmlNode node : nodes) {
            String className = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CLASS);

            if (className == null) {
                mLog.error("request interceptor <" + node.getName()
                        + ">: no corresponding class to load found");
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(Context.class);
                IRequestInterceptor interceptor = (IRequestInterceptor) constr
                        .newInstance(mContext);
                setInterceptor(interceptor);
            } catch (Exception ex) {
                mLog.error("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.info("request interceptors read from configuration");
    }

    protected void setInterceptor(IRequestInterceptor reqInterceptor) {
        for (AbstractHttpRequestHandler reqHandler : mHandlerReferenceListing) {
            reqHandler.addRequestInterceptor(reqInterceptor);
        }
    }

    public boolean isRunning() {
    	return mIsServerRunning;
        //return mServerThread != null;
    }

    public synchronized boolean startServer() {
        if (this.isRunning()) {
            mLog.info("Web service is already running at port <" + mServerThread.getPort() + ">");
            return true;
        }
        
        updateWebServerConfiguration();
        
        try {
            if (mHttps) {
				initSSLContext();
                final SSLServerSocketFactory sslServerSocketFactory = mSSLContext
                        .getServerSocketFactory();
                mServerSocket = sslServerSocketFactory.createServerSocket(mServerPort, 0, mSocketAddress);
            } else {
                mServerSocket = new ServerSocket(mServerPort, 0, mSocketAddress);
            }

            statusbarPrintConnectionUrl();
            mServerSocket.setReuseAddress(true);
            mServerSocket.setSoTimeout(2000);
            
            mIsServerRunning = true;
            mServerThread = new ServerThread(this, mServerSocket);
            mServerThread.setDaemon(true);
            mServerThread.start();
            mLog.info("WebServer started on port: " + mServerPort);
            
            return true;
        } catch (IOException ioException) {
            mLog.error("Cannot create server socket on port <" + mServerPort + ">", ioException);
            return false;
        }
        
        
    }

    private void updateWebServerConfiguration() {
        PreferencesProvider config = new PreferencesProvider(mContext);
        
        if(config.getProtocol().compareTo("https") == 0){
        	mHttps = true;
        } else {
        	mHttps = false;
        }
        
        mServerPort = Integer.parseInt(config.getPort());
        mKeyStorePass = config.getKeyStorePassword();
	}

	public synchronized void stopServer() {
        mLog.info("stop web server");
        if (mServerThread != null) {
            mServerThread.stopThread();
            while (mServerThread.isRunning()) {
                try {
                    this.wait(200);
                } catch (InterruptedException interruptedException) {
                    ;
                }
            }
            try {
				mServerSocket.close();
			} catch (IOException e) {
				// i ton't care
			}
            mIsServerRunning = false;
            statusbarClearConnectionUrl();
        }

        closeRegistry();
        mServerThread = null;
    }

    private void closeRegistry() {
        for (AbstractHttpRequestHandler toBeCleanedUp : mHandlerReferenceListing) {
            toBeCleanedUp.onClose();
        }
    }

    private void initSSLContext() {
        try {
            mSSLContext = SSLContext.getInstance("TLS");
            
            ApplicationKeyStore appKeystore = new ApplicationKeyStore();
            String keystoreFilePath = mContext.getFilesDir().getPath().toString() + "/" + mContext.getResources().getString(
                    R.string.preferences_keystore_store_filename);
            appKeystore.loadKeystore(mKeyStorePass, keystoreFilePath);
            
            mSSLContext.init(appKeystore.getKeystoreManagers(), null, new SecureRandom());
        } catch (NoSuchAlgorithmException algoException) {
            mLog.error("Wrong keystore algorithm!", algoException);
        } catch (KeyManagementException keyException) {
            mLog.error("Error while getting keymanagers!", keyException);
        }
    }
    
    
    
    public synchronized int getServerPort(){
    	if(mServerThread != null && mServerThread.isRunning()){
    		return mServerPort;
    	} else {
    		return -1;
    	}
    }
    
    private void statusbarPrintConnectionUrl()
    {
    	FireNotification notificator = new FireNotification(mContext);
    	FireNotification.NotificationInfo info = new FireNotification.NotificationInfo();
    	
    	StringBuffer connectionUrl = new StringBuffer();
    	if (  mHttps ) {
    		connectionUrl.append("https://");
    	} else {
    		connectionUrl.append("http://");
    	}
    	
    	info.text = connectionUrl.append("/"+ mSocketAddress.getHostAddress() + "/" + mServerPort).toString();
    	info.title="WebSMSTool";
    	info.tickerText="service running";
		notificator.fireStickyInfos(info);
    }
    
    private void statusbarClearConnectionUrl()
    {
    	new FireNotification(mContext).cancelAll();
    }
    
}
