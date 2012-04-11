package at.tugraz.ist.akm.webservice.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

import android.util.Log;
import at.tugraz.ist.akm.webservice.handler.MyRequestHandler;

public class SimpleWebServer extends Thread {
	ServerSocket serverSocket = null;

	@Override
	public void run() {

		try {
			serverSocket = new ServerSocket(8888);
			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(100);
			
			
			Log.v("SimpleWebServer", "waiting for connection at "
					+ serverSocket);
			
			while(true){
				final Socket socket = serverSocket.accept();

				new Thread(
					new Runnable() {
						public void run(){
							Log.v("SimpleWebServer", "get connection " + socket);
							HttpProcessor httpProcessor = new BasicHttpProcessor();
							BasicHttpContext httpContext = new BasicHttpContext();
							HttpService httpService = new HttpService(httpProcessor,
									new DefaultConnectionReuseStrategy(),
									new DefaultHttpResponseFactory());
	
							HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
							registry.register("/", new MyRequestHandler());
							Log.v("SimpleWebServer", "after registry");
							
							httpService.setHandlerResolver(registry);
	
							DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
							try {
								serverConn.bind(socket, new BasicHttpParams());
								Log.v("SimpleWebServer", "after bind");
								httpService.handleRequest(serverConn, httpContext);
								Log.v("SimpleWebServer", "after handleRequest");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (HttpException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				).start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
