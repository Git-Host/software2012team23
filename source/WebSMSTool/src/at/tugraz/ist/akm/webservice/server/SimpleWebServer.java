package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
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
import my.org.apache.http.HttpVersion;
import my.org.apache.http.impl.DefaultConnectionReuseStrategy;
import my.org.apache.http.impl.DefaultHttpResponseFactory;
import my.org.apache.http.impl.DefaultHttpServerConnection;
import my.org.apache.http.params.BasicHttpParams;
import my.org.apache.http.params.CoreConnectionPNames;
import my.org.apache.http.params.CoreProtocolPNames;
import my.org.apache.http.params.HttpParams;
import my.org.apache.http.params.HttpProtocolParams;
import my.org.apache.http.params.SyncBasicHttpParams;
import my.org.apache.http.protocol.BasicHttpContext;
import my.org.apache.http.protocol.HTTP;
import my.org.apache.http.protocol.HttpProcessor;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import my.org.apache.http.protocol.HttpService;
import my.org.apache.http.protocol.ImmutableHttpProcessor;
import my.org.apache.http.protocol.ResponseConnControl;
import my.org.apache.http.protocol.ResponseContent;
import my.org.apache.http.protocol.ResponseDate;
import my.org.apache.http.protocol.ResponseServer;
import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.WebserviceThreadPool;
import at.tugraz.ist.akm.webservice.handler.AbstractHttpRequestHandler;
import at.tugraz.ist.akm.webservice.handler.interceptor.IRequestInterceptor;

public class SimpleWebServer {
    private final static Logable mLog = new Logable(SimpleWebServer.class.getSimpleName());

    HttpRequestHandlerRegistry mRegistry = new HttpRequestHandlerRegistry();
    private BasicHttpContext mHttpContext = new BasicHttpContext();

    private final Context mContext;
    private ServerThread mServerThread = null;
    private Vector<AbstractHttpRequestHandler> mHandlerReferenceListing = new Vector<AbstractHttpRequestHandler>();

    private boolean mHttps;

    // ssl and keystore
    private SSLContext mSSLContext;
    private KeyManagerFactory mKeyFactory;
    private KeyManager[] mKeyManager;
    private KeyStore mKeyStore;

    private static class ServerThread extends Thread {
        private final SimpleWebServer mWebServer;
        private final ServerSocket mServerSocket;
        private boolean mRunning = false;
        private boolean mStopServerThread = false;

        private final WebserviceThreadPool mThreadPool;
        
        public ServerThread(final SimpleWebServer webServer, final ServerSocket serverSocket) {
            this.mWebServer = webServer;
            this.mServerSocket = serverSocket;
            this.mThreadPool = new WebserviceThreadPool();
        }

        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {
                Socket socket = null;
                try {
                    socket = mServerSocket != null ? mServerSocket.accept() : null;
                } catch (IOException ioException) {
                    //no need to write trace
                }

                if (mStopServerThread) {
                    break;
                }
                if (socket != null) {
                    mLog.logDebug("connection request from ip <" + socket.getInetAddress()
                            + "> on port <" + socket.getPort() + ">");
                
                    final Socket tmpSocket = socket;
                    mThreadPool.executeTask(new Runnable() {
                        @Override
                        public void run() {
                            DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
                            try {
                                HttpParams params = new BasicHttpParams();
                                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                                serverConn.bind(tmpSocket, params);
                                HttpService httpService = mWebServer.initializeHTTPService();
                                httpService.handleRequest(serverConn, mWebServer.getHttpContext());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                mLog.logError("Exception caught when processing HTTP client connection", ex);
                            }
                        }
                    });
                }
            }

            mRunning = false;
            Log.i("SimpleWebServer", "Webserver stopped");
        }

        public void stopThread() {
            mThreadPool.shutdown();
            mStopServerThread = true;
        }

        public boolean isRunning() {
            return mRunning;
        }

        public int getPort() {
            return mServerSocket.getLocalPort();
        }
    }

    public SimpleWebServer(Context context, final boolean https) {
        this.mContext = context;
        this.mHttps = https;

        readRequestHandlers();
        readRequestInterceptors();

        if (mHttps) {
            initSSLContext();
        }
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
        return mServerThread != null;
    }

    public synchronized boolean startServer(int port) {
        if (this.isRunning()) {
            mLog.logInfo("Web service is already running at port <" + mServerThread.getPort() + ">");
            return true;
        }

        try {
            ServerSocket serverSocket = null;

            if (mHttps) {
                final SSLServerSocketFactory sslServerSocketFactory = mSSLContext
                        .getServerSocketFactory();
                serverSocket = sslServerSocketFactory.createServerSocket(port);
            } else {
                serverSocket = new ServerSocket(port);
            }

            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(2000);

            mServerThread = new ServerThread(this, serverSocket);
            mServerThread.setDaemon(true);
            mServerThread.start();

            return true;
        } catch (IOException ioException) {
            mLog.logError("Cannot create server socket on port <" + port + ">", ioException);
            return false;
        }
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

            mKeyStore.load(is, "foobar64".toCharArray());

            mKeyFactory.init(mKeyStore, "foobar64".toCharArray());
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
}
