package at.tugraz.ist.akm.activities;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.networkInterface.WifiIpAddress;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.ServiceConnectionMessageTypes;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;

public class StartServiceFragment extends Fragment implements
        View.OnClickListener, ServiceConnection
{
    LogClient mLog = new LogClient(
            StartServiceFragment.class.getCanonicalName());

    // public static final String SERVER_IP_ADDRESS_INTENT_KEY =
    // "at.tugraz.ist.akm.SERVER_IP_ADDRESS_INTENT_KEY";
    private Intent mSmsServiceIntent = null;
    final String mServiceName = WebSMSToolService.class.getName();
    private ToggleButton mButton = null;
    private TextView mInfoFieldView = null;
    private SharedPreferencesProvider mApplicationConfig = null;
    // private ServiceStateListener mServiceListener = null;

    private Class<WebSMSToolService> mServiceClass = WebSMSToolService.class;
    private String mServiceComponentNameSuffix = mServiceClass.getSimpleName();

    private WifiIpAddress mWifiIp = null;
    // private String mLocalIp = null;
    private String mServiceUrl = "";
    private WebSMSToolService.ServiceRunningStates mServiceRunningState = WebSMSToolService.ServiceRunningStates.UNKNOWN;

    private Messenger mServiceMessenger = null;
    final Messenger mClientMessenger = new Messenger(
            new IncomingServiceMessageHandler(this));


    // private static class ServiceStateListener extends BroadcastReceiver
    // {
    //
    // private StartServiceFragment mCallback = null;
    //
    //
    // public ServiceStateListener(StartServiceFragment callback)
    // {
    // mCallback = callback;
    // }
    //
    //
    // @Override
    // public void onReceive(Context context, Intent intent)
    // {
    // String action = intent.getAction();
    // if (0 == action.compareTo(WebSMSToolService.SERVICE_STARTED))
    // {
    // mCallback.onWebServiceStarted();
    // } else if (0 == action
    // .compareTo(WebSMSToolService.SERVICE_STARTED_BOGUS))
    // {
    // mCallback.onWebServiceStartFailed();
    // } else if (0 == action
    // .compareTo(WebSMSToolService.SERVICE_STARTING))
    // {
    // mCallback.onWebServiceStarting();
    // } else if (0 == action.compareTo(WebSMSToolService.SERVICE_STOPPED))
    // {
    // mCallback.onWebServiceStopped();
    // } else if (0 == action
    // .compareTo(WebSMSToolService.SERVICE_STOPPED_BOGUS))
    // {
    // mCallback.onWebServiceStopFailed();
    // } else if (0 == action
    // .compareTo(WebSMSToolService.SERVICE_STOPPING))
    // {
    // mCallback.onWebServiceStopping();
    // } else if (0 == action
    // .compareTo(WebSMSToolService.SERVICE_STOPPING))
    // {
    // }
    // }
    // }

    public StartServiceFragment()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
        // mServiceListener = new ServiceStateListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        mLog.debug("on create view");
        setUpApplicationConfig();
        setUpUiReferences(view);
        // updateLocalIp();
        return view;
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("fragment goes to hades");
        mSmsServiceIntent = null;
        mLog = null;
        mButton = null;
        mInfoFieldView = null;
        mApplicationConfig.close();
        mApplicationConfig = null;
        // mServiceListener = null;
        // mLocalIp = null;
        super.onDestroy();
    }


    // private void unregisterServiceStateChangeReceiver()
    // {
    // getActivity().unregisterReceiver(mServiceListener);
    // }

    protected void setHttpUrl(String serviceUrl)
    {
        mServiceUrl = serviceUrl;
    }


    protected void setServiceRunningState(
            WebSMSToolService.ServiceRunningStates runningState)
    {
        mServiceRunningState = runningState;
    };


    // private void registerServiceStateChangeReceiver()
    // {
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STARTING));
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STARTED));
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STARTED_BOGUS));
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STOPPING));
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STOPPED));
    // getActivity().registerReceiver(mServiceListener,
    // new IntentFilter(WebSMSToolService.SERVICE_STOPPED_BOGUS));
    // }

    @Override
    public void onStart()
    {
        super.onStart();
        mLog.debug("brought fragment to front");
    }


    public void onStop()
    {
        tearDownMainFragmentUI();
        mLog.debug("fragment no longer visible");
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("user returned to fragment, update ui");
        updateUi();
        // registerServiceStateChangeReceiver();
    }


    private void updateUi()
    {
        if (isServiceRunning())
        {
            mButton.setChecked(true);
            // TODO
            // updateLocalIp();
            displayConnectionUrl();
        } else
        {
            mInfoFieldView.setText("");
            mButton.setChecked(false);
        }
    }


    @Override
    public void onPause()
    {
        mLog.debug("fragment goes to background");
        // unregisterServiceStateChangeReceiver();
        super.onStop();
    }


    private void setUpApplicationConfig()
    {
        mApplicationConfig = new SharedPreferencesProvider(getActivity()
                .getApplicationContext());
        mSmsServiceIntent = new Intent(getActivity(), mServiceClass);
    }


    private void setUpUiReferences(View view)
    {
        mButton = (ToggleButton) view.findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) view.findViewById(R.id.adress_data_field);
        mButton.setOnClickListener(this);
        mWifiIp = new WifiIpAddress(view.getContext());
    }


    private void tearDownMainFragmentUI()
    {
        mButton.setOnClickListener(null);
    }


    @Override
    public void onClick(View view)
    {
        if (!isServiceRunning())
        {
            if (mWifiIp.isWifiEnabled() || AppEnvironment.isRunningOnEmulator())
            {
                mLog.info("start web service");
                displayStartingService();
                 view.getContext().startService(mSmsServiceIntent);
                getActivity().getApplicationContext().bindService(
                        mSmsServiceIntent, this, Context.BIND_AUTO_CREATE);
            } else
            {
                displayNoWifiConnected();
                String message = "will not start service without wifi connection";
                mLog.warning(message);
                mLog.error(message);
                mButton.setChecked(false);
            }
        } else
        {
            displayStoppingService();
            // getActivity().getApplicationContext().unbindService(this);
            askForServiceStopAsync();
            // view.getContext().stopService(mSmsServiceIntent);
        }
    }


    private boolean isServiceRunning()
    {
        ActivityManager activityManager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        Iterator<ActivityManager.RunningServiceInfo> service = runningServices
                .iterator();

        while (service.hasNext())
        {
            ActivityManager.RunningServiceInfo serviceInfo = (ActivityManager.RunningServiceInfo) service
                    .next();
            if (serviceInfo.service.getClassName().equals(mServiceName))
            {
                mLog.debug("our service is running [" + mServiceName + "]");
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
        mLog.debug("service url [" + mServiceUrl + "]");
        // TODO

        // mInfoFieldView.setText(mApplicationConfig.getProtocol() + "://"
        // + mLocalIp + ":" + mApplicationConfig.getPort());
        mInfoFieldView.setText(mServiceUrl);
    }


    private void displayStoppingService()
    {
        mInfoFieldView.setText("stopping service...");
    }


    private void displayStartingService()
    {
        mInfoFieldView.setText("starting service...");
    }


    // private boolean updateLocalIp()
    // {
    // mLocalIp = mWifiIp.readLocalIpAddress();
    // return mWifiIp.isWifiEnabled();
    // }

    public void onWebServiceStarted()
    {
        displayConnectionUrl();
        mButton.setChecked(true);
        mLog.verbose("service started");
    }


    public void onWebServiceStartFailed()
    {
        mLog.verbose("service started erroneous - please stop it manually before starting again");
    }


    public void onWebServiceStarting()
    {
        displayStartingService();
    }


    public void onWebServiceStopFailed()
    {
        mLog.verbose("service stopped erroneous - please stop it manually");
    }


    public void onWebServiceStopped()
    {
        mInfoFieldView.setText("");
        mButton.setChecked(false);
        mLog.verbose("service stopped");
    }


    public void onWebServiceStopping()
    {
        displayStoppingService();
        mLog.verbose("shut down service");
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        String inServiceName = name.flattenToShortString();
        if (inServiceName.endsWith(mServiceComponentNameSuffix))
        {
            mLog.debug("bound to service [" + mServiceComponentNameSuffix + "]");
            mServiceMessenger = new Messenger(service);
            askForClientRegistrationAsync();
            askForConnectionUrlAsync();
        } else
        {
            mLog.error("failed binding fragment to service[" + inServiceName
                    + "] expected [*" + mServiceComponentNameSuffix + "]");
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        String inServiceName = name.flattenToShortString();
        if (inServiceName.endsWith(mServiceComponentNameSuffix))
        {
            mLog.debug("bound to service [" + mServiceComponentNameSuffix + "]");
            mServiceMessenger = null;
        } else
        {
            mLog.error("failed unbinding fragment to service[" + inServiceName
                    + "] expected [*" + mServiceComponentNameSuffix + "]");
        }

    }


    private void askForConnectionUrlAsync()
    {
        mLog.debug("asking for connection url");
        if (mServiceMessenger != null)
        {
            try
            {
                mServiceMessenger.send(Message.obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.HTTP_URL));
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending message to service", e);
            }
        } else
        {
            mLog.error("failed asking for url, service not available");
        }
    }


    private void askForClientRegistrationAsync()
    {
        mLog.debug("asking for client registration");
        if (mServiceMessenger != null)
        {
            try
            {
                Message registration = Message
                                .obtain(null,
                                        ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE);
                registration.replyTo = mClientMessenger;

                mServiceMessenger
                        .send(registration);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending message to service", e);
            }
        } else
        {
            mLog.error("failed asking for client registration, service not available");
        }
    }


    private void askForServiceStopAsync()
    {
        mLog.debug("asking for service being stopped");
        if (mServiceMessenger != null)
        {
            try
            {
                mServiceMessenger
                        .send(Message
                                .obtain(null,
                                        ServiceConnectionMessageTypes.Client.Request.UNREGISTER_TO_SERVICE));
                mServiceMessenger
                        .send(Message
                                .obtain(null,
                                        ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE));
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending message to service", e);
            }
        } else
        {
            mLog.error("failed asking for service being stopped, service not available");
        }
    }
}
