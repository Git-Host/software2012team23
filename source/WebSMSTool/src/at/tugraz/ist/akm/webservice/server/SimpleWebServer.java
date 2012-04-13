package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.http.HttpException;
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

public class SimpleWebServer implements Runnable {
    private ServerSocket serverSocket = null;
    private final int port;
    private boolean running = true;

    BasicHttpContext httpContext = new BasicHttpContext();
    HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();

    // android context
    private final Context context;

    public SimpleWebServer(Context context, int port) {
        this.context = context;
        this.port = port;
        readRequestHandlers();
    }

    private HttpService initializeHTTPService() {
        HttpProcessor httpProcessor = new BasicHttpProcessor();
        HttpService httpService = new HttpService(httpProcessor,
                new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

        httpService.setHandlerResolver(registry);
        return httpService;
    }

    private void readRequestHandlers() {
        XmlReader reader = new XmlReader(context, "web/web.xml");
        List<XmlNode> nodes = reader.getNodes("requestHandler");
        for (XmlNode node : nodes) {
            String className = node.getAttributeValue("class");

            if (className == null) {
                Log.e("SimpleWebServer", "request handler <" + node.getName()
                        + "> no corresponding class found");
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);

                Constructor<?> constr = clazz.getConstructor(Context.class, XmlNode.class,
                        HttpRequestHandlerRegistry.class);
                constr.newInstance(context, node, registry);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Log.v("SimpleWebServer", "request handlers read from configuration");
    }

    @Override
    public synchronized void run() {

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            Log.v("SimpleWebServer", "waiting for connection at " + serverSocket);

            while (running) {
                final Socket socket = serverSocket.accept();
                if (!running) {
                    break;
                }

                new Thread(new Runnable() {
                    public synchronized void run() {
                        Log.v("SimpleWebServer", "get connection " + socket);

                        DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
                        try {
                            serverConn.bind(socket, new BasicHttpParams());
                            HttpService httpService = initializeHTTPService();
                            Log.v("SimpleWebServer", "after bind");
                            httpService.handleRequest(serverConn, httpContext);
                            Log.v("SimpleWebServer", "after handleRequest");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (HttpException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("SimpleWebServer", "Webserver stopped");
    }

    public void stopServer() {
        // TODO synchronize?
        try {
            running = false;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
