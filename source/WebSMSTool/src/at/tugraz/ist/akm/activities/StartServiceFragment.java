package at.tugraz.ist.akm.activities;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class StartServiceFragment extends Fragment implements
        View.OnClickListener
{
    LogClient mLog = new LogClient(
            StartServiceFragment.class.getCanonicalName());

    public static final String SERVER_IP_ADDRESS_INTENT_KEY = "at.tugraz.ist.akm.SERVER_IP_ADDRESS_INTENT_KEY";
    private Intent mSmsServiceIntent = null;
    final String mServiceName = WebSMSToolService.class.getName();
    private ToggleButton mButton = null;
    private TextView mInfoFieldView = null;
    private SharedPreferencesProvider mApplicationConfig = null;
    private ServiceStateListener mServiceListener = null;

    private WifiManager mWifiManager = null;
    private ConnectivityManager mConnectivityManager = null;

    private String mLocalIp = null;

    private static class ServiceStateListener extends BroadcastReceiver
    {

        private StartServiceFragment mCallback = null;


        public ServiceStateListener(StartServiceFragment callback)
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


    public StartServiceFragment()
    {
        mLog.debug("constructing " + getClass().getSimpleName() + " "
                + (Object) this);
        mServiceListener = new ServiceStateListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        // View view = super.onCreateView(inflater, container,
        // savedInstanceState);
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        mLog.debug("on create view " + (Object) this);
        setUpApplicationConfig();
        setUpUiReferences(view);
        return view;
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("fragment goes to hades " + (Object) this);
        mSmsServiceIntent = null;
        mLog = null;
        mButton = null;
        mInfoFieldView = null;
        mApplicationConfig.close();
        mApplicationConfig = null;
        mServiceListener = null;
        mWifiManager = null;
        mConnectivityManager = null;
        mLocalIp = null;
        super.onDestroy();
    }


    private void unregisterServiceStateChangeReceiver()
    {
        getActivity().unregisterReceiver(mServiceListener);
    }


    private void registerServiceStateChangeReceiver()
    {
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STARTING));
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STARTED));
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STARTED_BOGUS));
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STOPPING));
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STOPPED));
        getActivity().registerReceiver(mServiceListener,
                new IntentFilter(WebSMSToolService.SERVICE_STOPPED_BOGUS));
    }


    @Override
    public void onStart()
    {
        super.onStart();
        mLog.debug("brought fragment to front " + (Object) this);
    }


    public void onStop()
    {
        tearDownMainFragmentUI();
        mLog.debug("fragment no longer visible " + (Object) this);
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("user returned to fragment  - updating local ip address for "
                + (Object) this);
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
        registerServiceStateChangeReceiver();
    }


    @Override
    public void onPause()
    {
        mLog.debug("fragment goes to background " + (Object) this);
        unregisterServiceStateChangeReceiver();
        super.onStop();
    }


    private void setUpApplicationConfig()
    {
        mApplicationConfig = new SharedPreferencesProvider(getActivity()
                .getApplicationContext());
        mSmsServiceIntent = new Intent(getActivity(), WebSMSToolService.class);

        mWifiManager = (WifiManager) getActivity().getSystemService(
                Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    private void setUpUiReferences(View view)
    {
        mButton = (ToggleButton) view.findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) view.findViewById(R.id.adress_data_field);

        mButton.setChecked(false);
        if (isServiceRunning(mServiceName))
        {
            mButton.setChecked(true);
        }

        mButton.setOnClickListener(this);

        updateLocalIp();
    }


    private void tearDownMainFragmentUI()
    {
        mButton.setOnClickListener(null);
    }


    @Override
    public void onClick(View view)
    {
        if (!isServiceRunning(mServiceName))
        {
            if (updateLocalIp() || isRunningOnEmulator())
            {
                mLog.info("starting web service with address " + mLocalIp);
                displayStartingService();
                view.getContext().startService(mSmsServiceIntent);
            } else
            {
                displayNoWifiConnected();
                String message = "will not start service without wifi connection";
                mLog.warning(message);
                mLog.verbose(message);
                mButton.setChecked(false);
            }
        } else
        {
            displayStoppingService();
            view.getContext().stopService(mSmsServiceIntent);
        }
    }


    private boolean isServiceRunning(String serviceName)
    {
        int serviceMaxCount = 75;
        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager
                .getRunningServices(serviceMaxCount);
        Iterator<ActivityManager.RunningServiceInfo> service = runningServices
                .iterator();
        mLog.debug("found [" + runningServices.size() + "] running services ");
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


    private void displayNoWifiConnected()
    {
        mInfoFieldView.setText("no WIFI connection available");
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


    private void displayStartingService()
    {
        mInfoFieldView.setText("starting service...");
    }


    private boolean updateLocalIp()
    {
        mLocalIp = readLocalIpAddress();
        mSmsServiceIntent.putExtra(SERVER_IP_ADDRESS_INTENT_KEY, mLocalIp);
        return mWifiManager.isWifiEnabled();
    }


    private String readLocalIpAddress()
    {
        String ip4Address = "0.0.0.0";

        NetworkInfo mobileNetInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifiManager.isWifiEnabled())
        {
            ip4Address = readWifiIP4Address();
        } else if (isRunningOnEmulator() && mobileNetInfo != null
                && mobileNetInfo.isConnected())
        {
            try
            {
                ip4Address = readIP4AddressOfEmulator();
            }
            catch (SocketException e)
            {
            } // don't care
        }
        return ip4Address;
    }


    @SuppressWarnings("deprecation")
    private String readWifiIP4Address()
    {
        String ip4Address = "0.0.0.0";
        byte[] ipAddress = BigInteger.valueOf(
                mWifiManager.getConnectionInfo().getIpAddress()).toByteArray();
        try
        {
            InetAddress address = InetAddress.getByAddress(ipAddress);
            String concreteAddressString = address.getHostAddress()
                    .toUpperCase(Locale.getDefault());
            if (InetAddressUtils.isIPv4Address(concreteAddressString))
            {
                // do not replace formatter by InetAddress here since this
                // returns "1.0.0.127" instead of "127.0.0.1"
                ip4Address = Formatter.formatIpAddress(mWifiManager
                        .getConnectionInfo().getIpAddress());
            }
        }
        catch (UnknownHostException e)
        {
            return ip4Address;
        }
        return ip4Address;
    }


    private String readIP4AddressOfEmulator() throws SocketException
    {
        String inet4Address = "0.0.0.0";

        for (Enumeration<NetworkInterface> iter = NetworkInterface
                .getNetworkInterfaces(); iter.hasMoreElements();)
        {
            NetworkInterface nic = iter.nextElement();

            if (nic.getName().startsWith("eth"))
            {
                Enumeration<InetAddress> addresses = nic.getInetAddresses();
                addresses.nextElement(); // skip first
                if (addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();

                    String concreteAddressString = address.getHostAddress()
                            .toUpperCase(Locale.getDefault());
                    if (InetAddressUtils.isIPv4Address(concreteAddressString))
                    {
                        inet4Address = concreteAddressString;
                    }
                }
            }
        }
        return inet4Address;
    }


    public final boolean isRunningOnEmulator()
    {
        return ("google_sdk".equals(Build.PRODUCT) || "sdk_x86"
                .equals(Build.PRODUCT));
    }


    public void webServiceStarted()
    {
        displayConnectionUrl();
        mButton.setChecked(true);
        mLog.verbose("service started");
    }


    public void webServiceStartFailed()
    {
        mLog.verbose("service started erroneous - please stop it manually before starting again");
    }


    public void webServiceStarting()
    {
        displayStartingService();
    }


    public void webServiceStopFailed()
    {
        mLog.verbose("service stopped erroneous - please stop it manually");
    }


    public void webServiceStopped()
    {
        mInfoFieldView.setText("");
        mButton.setChecked(false);
        mLog.verbose("service stopped");
    }


    public void webServiceStopping()
    {
        displayStoppingService();
        mLog.verbose("shut down service");
    }
}
