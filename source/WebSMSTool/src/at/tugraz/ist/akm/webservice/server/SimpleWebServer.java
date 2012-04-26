package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;

import android.content.Context;
import android.util.Log;
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

    private static class WorkerThread extends Thread {
        private final SimpleWebServer mWebServer;
        private final Socket mSocket;

        public WorkerThread(final SimpleWebServer webServer, final Socket socket) {
            this.mWebServer = webServer;
            this.mSocket = socket;
        }

        @Override
        public void run() {
            DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
            try {
                serverConn.bind(mSocket, new BasicHttpParams());
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
        private final ServerSocket mServerSocket;
        private boolean mRunning = false;
        private boolean mStopServerThread = false;

        public ServerThread(final SimpleWebServer webServer, final ServerSocket serverSocket) {
            this.mWebServer = webServer;
            this.mServerSocket = serverSocket;
        }

        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {

                Socket socket = null;
                //LOG.d("waiting for connection at " + serverSocket);
                try {
                    socket = mServerSocket != null ? mServerSocket.accept() : null;
                } catch (IOException e) {
                    //LOG.i("Exception caught while waiting for client connection", e);
                }

                if (mStopServerThread) {
                    break;
                }
                if (socket != null) {
                    LOG.d("connection request from ip <" + socket.getInetAddress()
                            + "> on port <" + socket.getPort() + ">");

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
        HttpProcessor httpProcessor = new BasicHttpProcessor();
        HttpService httpService = new HttpService(httpProcessor,
                new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

        httpService.setHandlerResolver(mRegistry);
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
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(2000);

            mServerThread = new ServerThread(this, serverSocket);
            mServerThread.setDaemon(true);
            mServerThread.start();
            return true;
        } catch (IOException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        }
    }

    public synchronized void stopServer() {
        LOG.v("stop web server");
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
