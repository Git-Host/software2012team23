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

package at.tugraz.ist.akm.webservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class WebSMSToolService extends Service {
	
	public static final String SERVICE_STARTING = "at.tugraz.ist.akm.sms.SERVICE_STARTING";
	public static final String SERVICE_STARTED_BOGUS = "at.tugraz.ist.akm.sms.SERVICE_STARTED_BOGUS";
	public static final String SERVICE_STARTED = "at.tugraz.ist.akm.sms.SERVICE_STARTED";
	public static final String SERVICE_STOPPING = "at.tugraz.ist.akm.sms.SERVICE_STOPPING";
	public static final String SERVICE_STOPPED_BOGUS = "at.tugraz.ist.akm.sms.SERVICE_STOPPED_BOGUS";
	public static final String SERVICE_STOPPED = "at.tugraz.ist.akm.sms.SERVICE_STOPPED";
	
	private Intent mServiceStartedStickyIntend = null;
	private String mSocketIp = null;
    private final static Logable LOG = new Logable(
            WebSMSToolService.class.getSimpleName());
    private static boolean mServiceRunning = false;
    private SimpleWebServer mServer = null;
    private final IBinder mBinder = new LocalBinder();
    private static BroadcastReceiver mIntentReceiver = null;
    
    private class WebSMSToolBroadcastReceiver extends BroadcastReceiver {
    	
    	WebSMSToolService mCallback = null;
    	
    	public WebSMSToolBroadcastReceiver(WebSMSToolService callback) {
			mCallback = callback;
		}
    	
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ( 0 == action.compareTo(WifiManager.WIFI_STATE_CHANGED_ACTION) ) {
				mCallback.wifiStateChanged(context, intent);
			}
			else {
				mCallback.unknownIntent(context, intent);
			}
		}
    }
    
    public WebSMSToolService() {
    	mServiceStartedStickyIntend = new Intent(SERVICE_STARTED);
    }

    public class LocalBinder extends Binder {
        WebSMSToolService getService() {
            return WebSMSToolService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
    	synchronized (this) {
	    	if ( !mServiceRunning )
	    	{
	    		registerIntentReceiver();
	    		
	    		mSocketIp = intent.getStringExtra(MainActivity.SERVER_IP_ADDRESS_INTENT_KEY);
		        LOG.logVerbose("Try to start webserver.");
		        try {
		        	mServer = new SimpleWebServer(this, mSocketIp);
		        	getApplicationContext().removeStickyBroadcast(mServiceStartedStickyIntend);
		        
		        	getApplicationContext().sendBroadcast(new Intent(SERVICE_STARTING));
		            mServer.startServer();
		            mServiceRunning = mServer.isRunning();
		            if ( !mServiceRunning )
		            	throw new Exception("server failed to start");
		            getApplicationContext().sendStickyBroadcast(mServiceStartedStickyIntend);
		            LOG.logInfo("Web service has been started");
		        } 
		        catch (Exception ex) {
		            LOG.logError("Couldn't start web service", ex);
		            getApplicationContext().sendBroadcast(new Intent(SERVICE_STARTED_BOGUS));
		        }
	    	}
	    	else 
	    	{
	    		LOG.logError("Couldn't start web service (already running)");
	    	}
	
	        super.onStartCommand(intent, flags, startId);
	        return START_STICKY;
    	}
    }
    
    @Override
    public boolean stopService(Intent name) {
    	boolean stopped = stopWebSMSToolService(name);
    	super.stopService(name);
    	return stopped;
    }
    
    @Override
    public void onDestroy() {
    	stopWebSMSToolService(null);
        super.onDestroy();
    }
    
    private boolean stopWebSMSToolService(Intent name)
    {
    	boolean stopped;
    	synchronized (this) {
    		stopped = false;
    		unregisterIntentReceiver();
    		getApplicationContext().removeStickyBroadcast(mServiceStartedStickyIntend);
	        try {
	        	getApplicationContext().sendBroadcast(new Intent(SERVICE_STOPPING));
	            mServer.stopServer();
	            mServiceRunning = mServer.isRunning();
	            if ( mServiceRunning )
	            	throw new Exception("server failed to stop");
	            getApplicationContext().sendBroadcast(new Intent(SERVICE_STOPPED));
	            stopped = true;
	        } catch (Exception ex) {
	            LOG.logError("Error while stopping server!", ex);
	            getApplicationContext().sendBroadcast(new Intent(SERVICE_STOPPED_BOGUS));
	        }
    	}
    	return stopped;
    }
    
    public void wifiStateChanged(Context context, Intent intent) {
    	String extraKey ="wifi_state";
    	boolean disabled = WifiManager.WIFI_STATE_DISABLED == intent.getIntExtra(extraKey, -1);
    	boolean disabling = WifiManager.WIFI_STATE_DISABLING == intent.getIntExtra(extraKey, -1);
    	boolean enabled = WifiManager.WIFI_STATE_ENABLED == intent.getIntExtra(extraKey, -1);
    	boolean enabling = WifiManager.WIFI_STATE_ENABLING == intent.getIntExtra(extraKey, -1);
    	boolean unknown = WifiManager.WIFI_STATE_UNKNOWN == intent.getIntExtra(extraKey, -1);
    	
    	LOG.logDebug("wifi statechanged: disabled [" + disabled + "], disabling [" + disabling + "], enabled [" + enabled + "], enabling [" + enabling + "], unknown [" + unknown + "]");
    	
    	if ( enabled ) {
    		return;
    	}
    	LOG.logDebug("wifi state schanged: address may be invalid from now on - turning of service");
    	stopSelf();
    }
    
    public void unknownIntent(Context context, Intent intent) {
    	LOG.logDebug("recived unhandled intent [" + intent.getAction() + "]");
    }
    
    private void registerIntentReceiver()
    {
    	mIntentReceiver = new WebSMSToolBroadcastReceiver(this);
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    	registerReceiver(mIntentReceiver, filter);
    }
    
    private void unregisterIntentReceiver()
    {
    	unregisterReceiver(mIntentReceiver);
    }
}
