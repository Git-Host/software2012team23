package at.tugraz.ist.akm.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class WebSMSToolService extends Service {

	SimpleWebServer mServer; 
	
	public class LocalBinder extends Binder {
		WebSMSToolService getService() {
            return WebSMSToolService.this;
        }
    }	
	
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();	
	
	
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("WebService", "Try to start webserver.");
		Thread serverThread = new Thread(new SimpleWebServer());
		serverThread.start();
		Log.v("WebService", "Webserver running.");		
    	return super.onStartCommand(intent, flags, startId);
    }
	

	@Override
	public void onDestroy() {
		mServer.stopServer();
		super.onDestroy();
	}

}
