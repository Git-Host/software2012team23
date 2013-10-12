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

package at.tugraz.ist.akm.activities;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.preferences.PreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends DefaultActionBar implements
        View.OnClickListener
{

    public static final String SERVER_IP_ADDRESS_INTENT_KEY = "at.tugraz.ist.akm.SERVER_IP_ADDRESS_INTENT_KEY";
    private Intent mSmsServiceIntent = null;
    private LogClient mLog = new LogClient(this);
    final String mServiceName = WebSMSToolService.class.getName();
    private ToggleButton mButton = null;
    private TextView mInfoFieldView = null;
    private PreferencesProvider mApplicationConfig = null;
    private ServiceStateListener mServiceListener = null;

    private WifiManager mWifiManager = null;
    private String mLocalIp = null;

    private static class ServiceStateListener extends BroadcastReceiver
    {

        private MainActivity mCallback = null;


        public ServiceStateListener(MainActivity callback)
        {
            mCallback = callback;
        }


        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (0 == action.compareTo(WebSMSToolService.SERVICE_STARTED))
            {
                mCallback.webServiceStarted();
            } else if (0 == action
                    .compareTo(WebSMSToolService.SERVICE_STARTED_BOGUS))
            {
                mCallback.webServiceStartFailed();
            } else if (0 == action
                    .compareTo(WebSMSToolService.SERVICE_STARTING))
            {
                mCallback.webServiceStarting();
            } else if (0 == action.compareTo(WebSMSToolService.SERVICE_STOPPED))
            {
                mCallback.webServiceStopped();
            } else if (0 == action
                    .compareTo(WebSMSToolService.SERVICE_STOPPED_BOGUS))
            {
                mCallback.webServiceStopFailed();
            } else if (0 == action
                    .compareTo(WebSMSToolService.SERVICE_STOPPING))
            {
                mCallback.webServiceStopping();
            } else if (0 == action
                    .compareTo(WebSMSToolService.SERVICE_STOPPING))
            {
            }
        }
    }


    public MainActivity()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
        mServiceListener = new ServiceStateListener(this);
    }


    /**
     * starts the main service (web server etc.)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSmsServiceIntent = new Intent(this.getApplicationContext(),
                WebSMSToolService.class);
        mButton = (ToggleButton) findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) findViewById(R.id.adress_data_field);
        mApplicationConfig = new PreferencesProvider(getApplicationContext());

        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        mButton.setChecked(false);
        if (isServiceRunning(mServiceName))
        {
            mButton.setChecked(true);
        }

        mLog.debug("launched activity on device [" + Build.PRODUCT + "]");

        registerServiceStateChangeReceiver();
        mButton.setOnClickListener(this);

        updateLocalIp();
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mLog.debug("brought activity to front");
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mLog.debug("user returned to activity  - updating local ip address");
        if (isServiceRunning(mServiceName))
        {
            mButton.setChecked(true);
            updateLocalIp();
            displayConnectionUrl();
        } else
        {
            mInfoFieldView.setText("");
            mButton.setChecked(false);
        }

    }


    @Override
    protected void onPause()
    {
        mLog.debug("activity goes to background");
        super.onStop();
    }


    @Override
    protected void onStop()
    {
        mLog.debug("activity no longer visible");
        super.onStop();
    }


    @Override
    protected void onDestroy()
    {
        mLog.debug("activity goes to Hades");
        unregisterServiceStateChangeReceiver();
        super.onDestroy();
    }


    private boolean updateLocalIp()
    {
        mLocalIp = getLocalIpAddress();
        mSmsServiceIntent.putExtra(SERVER_IP_ADDRESS_INTENT_KEY, mLocalIp);
        return mWifiManager.isWifiEnabled();
    }


    private String getLocalIpAddress()
    {
        WifiInfo connectionInfo = mWifiManager.getConnectionInfo();

        if (!mWifiManager.isWifiEnabled())
            return "0.0.0.0";

        int ipAddress = connectionInfo.getIpAddress();
        return Formatter.formatIpAddress(ipAddress);
    }


    private boolean isServiceRunning(String serviceName)
    {
        int serviceMaxCount = 75;
        ActivityManager activityManager = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager
                .getRunningServices(serviceMaxCount);
        Iterator<ActivityManager.RunningServiceInfo> service = runningServices
                .iterator();
        mLog.debug("found running [" + runningServices.size() + "] services ");
        while (service.hasNext())
        {
            ActivityManager.RunningServiceInfo serviceInfo = (ActivityManager.RunningServiceInfo) service
                    .next();
            if (serviceInfo.service.getClassName().equals(serviceName))
            {
                mLog.debug("back service is actually running [" + serviceName
                        + "]");
                return true;
            }
        }
        return false;
    }


    private boolean isRunningOnEmulator()
    {
        return "google_sdk".equals(Build.PRODUCT);

    }


    @Override
    public void onClick(View view)
    {
        if (!isServiceRunning(mServiceName))
        {
            if (updateLocalIp() || isRunningOnEmulator())
            {
                mLog.info("Going to start web service");
                displayStartigService();
                view.getContext().startService(mSmsServiceIntent);
            } else
            {
                displayNoWifiConnected();
                mLog.debug("will not start service without wifi connection");
                mButton.setChecked(false);
            }
        } else
        {
            displayStoppingService();
            view.getContext().stopService(mSmsServiceIntent);
        }
    }


    private void displayConnectionUrl()
    {
        String wifiIp = mLocalIp;
        mLog.debug("Actual IP: " + wifiIp);
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


    private void displayNoWifiConnected()
    {
        mInfoFieldView.setText("no WIFI connection available");
    }


    private void registerServiceStateChangeReceiver()
    {
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


    private void unregisterServiceStateChangeReceiver()
    {
        unregisterReceiver(mServiceListener);
    }


    public void webServiceStarting()
    {
        displayStartigService();
    }


    public void webServiceStarted()
    {
        displayConnectionUrl();
        mButton.setChecked(true);
    }


    public void webServiceStartFailed()
    {
        mInfoFieldView
                .setText("service started erroneous - please stop it manually before starting again");
    }


    public void webServiceStopping()
    {
        displayStoppingService();
    }


    public void webServiceStopped()
    {
        mInfoFieldView.setText("");
        mButton.setChecked(false);
    }


    public void webServiceStopFailed()
    {
        mInfoFieldView
                .setText("service stopped erroneous - please stop it manually");
    }
}
