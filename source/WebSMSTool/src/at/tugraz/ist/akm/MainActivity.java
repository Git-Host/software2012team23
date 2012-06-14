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

package at.tugraz.ist.akm;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends ActionBarActivity implements
		View.OnClickListener {
	private Intent mSmsServiceIntent = null;
	private Logable mLog = null;
	final String mServiceName = WebSMSToolService.class.getName();
	private ToggleButton mButton = null;
	private TextView mInfoFieldView = null;
	private Config mApplicationConfig = null;
	private ServiceStateListener mServiceListener = null;

	private class ServiceStateListener extends BroadcastReceiver {

		private MainActivity mCallback = null;

		public ServiceStateListener(MainActivity callback) {
			mCallback = callback;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (0 == action.compareTo(WebSMSToolService.SERVICE_STARTED)) {
				mCallback.webServiceStarted();
			} else if (0 == action
					.compareTo(WebSMSToolService.SERVICE_STARTED_BOGUS)) {
				mCallback.webServiceStartFailed();
			} else if (0 == action
					.compareTo(WebSMSToolService.SERVICE_STARTING)) {
				mCallback.webServiceStarting();
			} else if (0 == action.compareTo(WebSMSToolService.SERVICE_STOPPED)) {
				mCallback.webServiceStopped();
			} else if (0 == action
					.compareTo(WebSMSToolService.SERVICE_STOPPED_BOGUS)) {
				mCallback.webServiceStopFailed();
			} else if (0 == action.compareTo(WebSMSToolService.SERVICE_STOPPING)) {
				mCallback.webServiceStopping();
			} else if (0 == action
					.compareTo(WebSMSToolService.SERVICE_STOPPING)) {
			}
		}
	}

	public MainActivity() {
		mLog = new Logable(getClass().getSimpleName());
		mServiceListener = new ServiceStateListener(this);
	}

	/**
	 * starts the main service (web server etc.)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mSmsServiceIntent = new Intent(this.getApplicationContext(),
				WebSMSToolService.class);
		mButton = (ToggleButton) findViewById(R.id.start_stop_server);
		mInfoFieldView = (TextView) findViewById(R.id.adress_data_field);
		mApplicationConfig = new Config(getApplicationContext());

		mButton.setChecked(false);
		if (isServiceRunning(mServiceName)) {
			mButton.setChecked(true);
		}
		
		registerServiceStateChangeReceiver();
		mButton.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		unregisterServiceStateChangeReceiver();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			Intent myIntent = new Intent(MainActivity.this,
					SettingsActivity.class);
			MainActivity.this.startActivity(myIntent);
			break;

		case R.id.info:
			Intent aboutIntent = new Intent(MainActivity.this,
					AboutActivity.class);
			MainActivity.this.startActivity(aboutIntent);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public String getLocalIpAddress() {
		/*
		 * //Lookup in all network interfaces try { for
		 * (Enumeration<NetworkInterface> en =
		 * NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		 * NetworkInterface intf = en.nextElement(); for
		 * (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
		 * enumIpAddr.hasMoreElements();) { InetAddress inetAddress =
		 * enumIpAddr.nextElement(); if (!inetAddress.isLoopbackAddress()) {
		 * return inetAddress.getHostAddress().toString(); } } } } catch
		 * (SocketException ex) { Log.e("GetLocalIpAddress", ex.toString()); }
		 */

		// the direct wifi way
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = Formatter.formatIpAddress(ipAddress);
		return ip;
	}

	private boolean isServiceRunning(String serviceName) {
		boolean serviceRunning = false;
		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
		Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
		while (i.hasNext()) {
			ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i
					.next();

			if (runningServiceInfo.service.getClassName().equals(serviceName)) {
				serviceRunning = true;
			}
		}
		return serviceRunning;
	}

	@Override
	public void onClick(View view) {
		if (!isServiceRunning(mServiceName)) {
			mLog.logVerbose("Going to start web service");
			displayStartigService();
			view.getContext().startService(mSmsServiceIntent);
		} else {
			displayStoppingService();
			view.getContext().stopService(mSmsServiceIntent);
		}
	}

	private void displayConnectionUrl() {
		String wifiIp = getLocalIpAddress();
		mLog.logDebug("Actual IP: " + wifiIp);
		mInfoFieldView.setText(mApplicationConfig.getProtocol() + "://"
				+ wifiIp + ":" + mApplicationConfig.getPort());
	}

	private void displayStoppingService()
	{
		mInfoFieldView.setText("stopping service...");
	}
	
	private void displayStartigService()
	{
		mInfoFieldView.setText("starting service...");
	}
	
	private void registerServiceStateChangeReceiver() {
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STARTING));
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STARTED));
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STARTED_BOGUS));
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STOPPING));
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STOPPED));
		registerReceiver(mServiceListener, new IntentFilter(
				WebSMSToolService.SERVICE_STOPPED_BOGUS));
	}
	
	private void unregisterServiceStateChangeReceiver() {
		unregisterReceiver(mServiceListener);
	}
	
	public void webServiceStarting() {
		displayStartigService();
	}

	public void webServiceStarted() {
		displayConnectionUrl();
		mButton.setChecked(true);
	}

	public void webServiceStartFailed() {
		mInfoFieldView.setText("service started bogusly ;( please destroy it manually");
	}

	public void webServiceStopping() {
		displayStoppingService();
	}

	public void webServiceStopped() {
		mInfoFieldView.setText("");
		mButton.setChecked(false);
	}

	public void webServiceStopFailed() {
		mInfoFieldView.setText("service stopped bogusly ;( please destroy it manually");
	}
}
