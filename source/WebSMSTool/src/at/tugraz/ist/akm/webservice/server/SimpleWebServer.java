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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
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
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.handler.AbstractHttpRequestHandler;
import at.tugraz.ist.akm.webservice.handler.interceptor.IRequestInterceptor;

public class SimpleWebServer {
    private final static Logable mLog = new Logable(SimpleWebServer.class.getSimpleName());

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
	
    // ssl and keystore
    private SSLContext mSSLContext;
    private KeyManagerFactory mKeyFactory;
    private KeyManager[] mKeyManager;
    private KeyStore mKeyStore;

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
                mLog.logError("request handler <" + node.getName()
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
                mLog.logError("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.logVerbose("request handlers read from configuration");
    }

    private void readRequestInterceptors() {
        XmlReader reader = new XmlReader(mContext, WebServerConfig.RES.WEB_XML);
        List<XmlNode> interceptorNodes = reader
                .getNodes(WebServerConfig.XML.TAG_REQUEST_INTERCEPTORS);
        if (interceptorNodes.size() == 0) {
            mLog.logWarning("no request interceptors configured");
            return;
        }
        List<XmlNode> nodes = interceptorNodes.get(0).getChildNodes(
                WebServerConfig.XML.TAG_INTERCEPTOR);
        for (XmlNode node : nodes) {
            String className = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CLASS);

            if (className == null) {
                mLog.logError("request interceptor <" + node.getName()
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
                mLog.logError("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.logVerbose("request interceptors read from configuration");
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
            mLog.logInfo("Web service is already running at port <" + mServerThread.getPort() + ">");
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
            mLog.logInfo("WebServer started on port: " + mServerPort);
            
            return true;
        } catch (IOException ioException) {
            mLog.logError("Cannot create server socket on port <" + mServerPort + ">", ioException);
            return false;
        }
        
        
    }

    private void updateWebServerConfiguration() {
        Config config = new Config(mContext);
        
        if(config.getProtocol().compareTo("https") == 0){
        	mHttps = true;
        } else {
        	mHttps = false;
        }
        
        mServerPort = Integer.parseInt(config.getPort());
        mKeyStorePass = config.getKeyStorePassword();
	}

	public synchronized void stopServer() {
        mLog.logVerbose("stop web server");
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
            mKeyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            mKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            InputStream is = mContext.getResources().openRawResource(R.raw.websms);

            mKeyStore.load(is, mKeyStorePass.toCharArray());

            mKeyFactory.init(mKeyStore, mKeyStorePass.toCharArray());
            mKeyManager = mKeyFactory.getKeyManagers();
            mSSLContext.init(mKeyManager, null, new SecureRandom());
        } catch (IOException ioException) {
            mLog.logError("Cannot read keystore!", ioException);
        } catch (KeyStoreException keyStoreException) {
            mLog.logError("Error while loading keystore!", keyStoreException);
        } catch (NoSuchAlgorithmException algoException) {
            mLog.logError("Wrong keystore algorithm!", algoException);
        } catch (CertificateException certificateException) {
            mLog.logError("Error while loading certificate!", certificateException);
        } catch (UnrecoverableKeyException keyException) {
            mLog.logError("Error while loading keystore", keyException);
        } catch (KeyManagementException keyException) {
            mLog.logError("Error while getting keymanagers!", keyException);
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
