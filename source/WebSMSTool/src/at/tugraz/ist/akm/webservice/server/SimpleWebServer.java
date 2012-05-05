package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

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

public class SimpleWebServer {
    private final static Logable LOG = new Logable(SimpleWebServer.class.getSimpleName());

    HttpRequestHandlerRegistry mRegistry = new HttpRequestHandlerRegistry();
    private BasicHttpContext mHttpContext = new BasicHttpContext();

    // android context
    private final Context mContext;

    private ServerThread mServerThread = null;
    
    private SSLContext sc;  
    private KeyManagerFactory keyFactory;  
    private KeyManager[] keyManager;  
    private KeyStore kStore; 

    private static class WorkerThread extends Thread {
        private final SimpleWebServer mWebServer;
        private final SSLSocket mSocket;

        public WorkerThread(final SimpleWebServer webServer, final SSLSocket socket) {
            this.mWebServer = webServer;
            this.mSocket = socket;
        }

        @Override
        public void run() {
            DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
                
                serverConn.bind(mSocket, params);
                HttpService httpService = mWebServer.initializeHTTPService();
                httpService.handleRequest(serverConn, mWebServer.getHttpContext());
            } catch (Exception e) {
                e.printStackTrace();
                LOG.e("Exception caught when processing HTTP client connection", e);
            }
        }
    }

    private static class ServerThread extends Thread {
        private final SimpleWebServer mWebServer;
        private final SSLServerSocket mServerSocket;
        private boolean mRunning = false;
        private boolean mStopServerThread = false;

        public ServerThread(final SimpleWebServer webServer, final SSLServerSocket serverSocket) {
            this.mWebServer = webServer;
            this.mServerSocket = serverSocket;
        }

        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {

                SSLSocket socket = null;
                LOG.d("waiting for connection at " + mServerSocket);
                try {
                    socket = mServerSocket != null ? (SSLSocket) mServerSocket.accept() : null;
                } catch (IOException e) {
                    // LOG.i("Exception caught while waiting for client connection",
                    // e);
                }

                if (mStopServerThread) {
                    break;
                }
                if (socket != null) {
//                    try {
//                        socket.startHandshake();
//                    } catch (IOException e) {
//                        Log.e("Error while handshake!", e.getMessage());
//                    }
                    LOG.d("connection request from ip <" + socket.getInetAddress() + "> on port <"
                            + socket.getPort() + ">");

                    WorkerThread workerThread = new WorkerThread(mWebServer, socket);
                    workerThread.setDaemon(true);
                    workerThread.start();
                }
            }
            mRunning = false;
            Log.i("SimpleWebServer", "Webserver stopped");
        }

        public void stopThread() {
            mStopServerThread = true;
        }

        public boolean isRunning() {
            return mRunning;
        }

        public int getPort() {
            return mServerSocket.getLocalPort();
        }
    }

    public SimpleWebServer(Context context) {
        this.mContext = context;
        readRequestHandlers();
    }

    protected synchronized HttpService initializeHTTPService() {
//        HttpProcessor httpProcessor = new BasicHttpProcessor();
        
        HttpProcessor httpProcessor = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[] {
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
            });
        
        HttpParams params = new SyncBasicHttpParams()
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8*1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        
        HttpService httpService = new HttpService(
                httpProcessor,
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(),
                mRegistry,
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
                LOG.e("request handler <" + node.getName()
                        + ">: no corresponding class to load found");
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);

                Constructor<?> constr = clazz.getConstructor(Context.class, XmlNode.class,
                        HttpRequestHandlerRegistry.class);
                constr.newInstance(mContext, node, mRegistry);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.e("Loading of class <" + className + "> failed", e);
                stopServer();
            }
        }
        LOG.v("request handlers read from configuration");
    }

    public boolean isRunning() {
        return mServerThread != null;
    }

    public synchronized boolean startServer(int port) {
        try {
            if (this.isRunning()) {
              LOG.i("Web service is already running at port <" + mServerThread.getPort() + ">");
              return true;
            }
            
            sc = SSLContext.getInstance("TLS");  
            keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());  
            kStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            
            InputStream is = mContext.getResources().openRawResource(R.raw.websms);
            
            kStore.load(is, "foobar64".toCharArray());
            
            keyFactory.init(kStore, "foobar64".toCharArray());  
            keyManager = keyFactory.getKeyManagers();  
            sc.init(keyManager, null, new SecureRandom());
                
            SSLServerSocketFactory sslserversocketfactory = sc.getServerSocketFactory();
            SSLServerSocket sslserversocket =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
//            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

            printServerSocketInfo(sslserversocket);
//            
            mServerThread = new ServerThread(this, sslserversocket);
            mServerThread.setDaemon(true);
            mServerThread.start();
            
//            InputStream inputstream = sslsocket.getInputStream();
//            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
//
//            String string = null;
//            while ((string = bufferedreader.readLine()) != null) {
//                System.out.println("odpowiedz z servera: "+string);
//                System.out.flush();
//            }
            
            return true;
        } catch (IOException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        } catch (KeyStoreException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        } catch (NoSuchAlgorithmException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        } catch (CertificateException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        } catch (UnrecoverableKeyException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        } catch (KeyManagementException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        }
        
//        try {
//            if (this.isRunning()) {
//                LOG.i("Web service is already running at port <" + mServerThread.getPort() + ">");
//                return true;
//            }
//            ServerSocket serverSocket = new ServerSocket(port);
//            serverSocket.setReuseAddress(true);
//            serverSocket.setSoTimeout(2000);
//
//            mServerThread = new ServerThread(this, serverSocket);
//            mServerThread.setDaemon(true);
//            mServerThread.start();
//            return true;
//        } catch (IOException e) {
//            LOG.v("Cannot create server socket on port <" + port + ">", e);
//            return false;
//        }
    }
    
    private static void printServerSocketInfo(SSLServerSocket s) {
        LOG.v("Server socket class: "+s.getClass());
        LOG.v("   Socket address = "
           +s.getInetAddress().toString());
        LOG.v("   Socket port = "
           +s.getLocalPort());
        LOG.v("   Need client authentication = "
           +s.getNeedClientAuth());
        LOG.v("   Want client authentication = "
           +s.getWantClientAuth());
        LOG.v("   Use client mode = "
           +s.getUseClientMode());
     }

    public synchronized void stopServer() {
        LOG.v("stop web server");
        if (mServerThread != null) {
            mServerThread.stopThread();
            while (mServerThread.isRunning()) {
                try {
                    this.wait(200);
                } catch (InterruptedException e) {
                    ;
                }
            }

            mServerThread = null;
        }
    }

}
