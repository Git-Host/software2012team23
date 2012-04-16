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

    HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
    private BasicHttpContext httpContext = new BasicHttpContext();

    // android context
    private final Context context;

    private ServerThread serverThread = null;

    private static class WorkerThread extends Thread {
        private final SimpleWebServer webServer;
        private final Socket socket;

        public WorkerThread(final SimpleWebServer webServer, final Socket socket) {
            this.webServer = webServer;
            this.socket = socket;
        }

        @Override
        public void run() {
            DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
            try {
                serverConn.bind(socket, new BasicHttpParams());
                HttpService httpService = webServer.initializeHTTPService();
                httpService.handleRequest(serverConn, webServer.getHttpContext());
            } catch (Exception e) {
                LOG.e("Exception caught when processing HTTP client connection", e);
            }
        }
    }

    private static class ServerThread extends Thread {
        private final SimpleWebServer webServer;
        private final ServerSocket serverSocket;
        private boolean running = false;
        private boolean stopServerThread = false;

        public ServerThread(final SimpleWebServer webServer, final ServerSocket serverSocket) {
            this.webServer = webServer;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            running = true;
            while (running) {

                Socket socket = null;
                //LOG.d("waiting for connection at " + serverSocket);
                try {
                    socket = serverSocket != null ? serverSocket.accept() : null;
                } catch (IOException e) {
                    //LOG.i("Exception caught while waiting for client connection", e);
                }

                if (stopServerThread) {
                    break;
                }
                if (socket != null) {
                    LOG.d("connection request from ip <" + socket.getInetAddress()
                            + "> on port <" + socket.getPort() + ">");

                    WorkerThread workerThread = new WorkerThread(webServer, socket);
                    workerThread.setDaemon(true);
                    workerThread.start();
                }
            }
            running = false;
            Log.i("SimpleWebServer", "Webserver stopped");
        }

        public void stopThread() {
            stopServerThread = true;
        }

        public boolean isRunning() {
            return running;
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }
    }

    public SimpleWebServer(Context context) {
        this.context = context;
        readRequestHandlers();
    }

    protected synchronized HttpService initializeHTTPService() {
        HttpProcessor httpProcessor = new BasicHttpProcessor();
        HttpService httpService = new HttpService(httpProcessor,
                new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

        httpService.setHandlerResolver(registry);
        return httpService;
    }

    protected BasicHttpContext getHttpContext() {
        return httpContext;
    }

    private void readRequestHandlers() {
        XmlReader reader = new XmlReader(context, WebServerConfig.RES.WEB_XML);
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
                constr.newInstance(context, node, registry);
            } catch (Exception e) {
                LOG.e("Loading of class <" + className + "> failed", e);
            }
        }
        LOG.v("request handlers read from configuration");
    }

    public boolean isRunning() {
        return serverThread != null;
    }

    public boolean startServer(int port) {
        try {
            synchronized (this) {
                if (serverThread != null) {
                    LOG.i("Web service is already running at port <" + serverThread.getPort() + ">");
                    return false;
                } else if (serverThread != null) {
                    // kill old server thread
                    serverThread.stopThread();
                }
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(2000);

                serverThread = new ServerThread(this, serverSocket);
                serverThread.setDaemon(true);
                serverThread.start();
            }
            return true;
        } catch (IOException e) {
            LOG.v("Cannot create server socket on port <" + port + ">", e);
            return false;
        }
    }

    public void stopServer() {
        synchronized (this) {
            LOG.v("stop web server");
            serverThread.stopThread();
            while (serverThread.isRunning()) {
                try {
                    this.wait(200);
                } catch (InterruptedException e) {
                    ;
                }
            }
            serverThread = null;
        }
    }

}
