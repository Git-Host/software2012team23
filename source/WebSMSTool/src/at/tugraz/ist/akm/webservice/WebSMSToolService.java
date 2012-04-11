package at.tugraz.ist.akm.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class WebSMSToolService extends Service {

	int mPort;
	String mUserName;
	String mPassword;
	
	SimpleWebServer mServer;

	public WebSMSToolService(int port, String usernamen, String password) {
		mPort = port;
		mUserName = usernamen;
		mPassword = password;
	}

	@Override
	public void onCreate() {
		Log.v("WebService", "Try to start webserver.");
		mServer = new SimpleWebServer();
		mServer.run();
		Log.v("WebService", "Webserver running.");		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		mServer.stopServer();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
