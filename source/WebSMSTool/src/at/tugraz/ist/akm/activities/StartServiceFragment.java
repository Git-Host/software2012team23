package at.tugraz.ist.akm.activities;

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
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.ServiceConnectionMessageTypes;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;

public class StartServiceFragment extends Fragment implements
        View.OnClickListener, ServiceConnection
{
    LogClient mLog = new LogClient(
            StartServiceFragment.class.getCanonicalName());

    private Intent mStartSmsServiceIntent = null;
    final String mServiceName = WebSMSToolService.class.getName();
    private ToggleButton mButton = null;
    private TextView mInfoFieldView = null;

    private Class<WebSMSToolService> mServiceClass = WebSMSToolService.class;
    private final String mServiceComponentNameSuffix = mServiceClass
            .getSimpleName();

    private WifiIpAddress mWifiState = null;

    private Messenger mServiceMessenger = null;
    final Messenger mClientMessenger = new Messenger(
            new IncomingServiceMessageHandler(this));

    private ServiceRunningStates mServiceRunningState = ServiceRunningStates.BEFORE_SINGULARITY;


    public StartServiceFragment()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        mLog.debug("on create view");
        // setUpApplicationConfig();
        setUpUiReferences(view);
        if (savedInstanceState != null)
        {
            // TODO
        }
        // updateLocalIp();
        return view;
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("fragment goes to hades");
        mStartSmsServiceIntent = null;
        mLog = null;
        mButton = null;
        mInfoFieldView = null;
        super.onDestroy();
    }


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
    }


    private void updateUi()
    {
        if (isServiceRunning())
        {
            mButton.setChecked(true);
            // TODO
            // updateLocalIp();
            // displayConnectionUrl();
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


    private void setUpUiReferences(View view)
    {
        mStartSmsServiceIntent = new Intent(getActivity(), mServiceClass);
        mButton = (ToggleButton) view.findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) view.findViewById(R.id.adress_data_field);
        mButton.setOnClickListener(this);
        mWifiState = new WifiIpAddress(view.getContext());
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
            if (mWifiState.isWifiEnabled() || mWifiState.isWifiAPEnabled()
                    || AppEnvironment.isRunningOnEmulator())
            {
                mLog.info("start web service");
                displayStartingService();
                view.getContext().startService(mStartSmsServiceIntent);
                getActivity().getApplicationContext().bindService(
                        mStartSmsServiceIntent, this, Context.BIND_AUTO_CREATE);
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
            askWebServiceForServiceStopAsync();
            getActivity().getApplicationContext().unbindService(this);
        }
    }


    private boolean isServiceRunning()
    {
        mLog.debug("service is in runningstate ["
                + mServiceRunningState.equals(ServiceRunningStates.RUNNING)
                + "] state [" + mServiceRunningState + "]");
        return (mServiceRunningState.equals(ServiceRunningStates.RUNNING));
    }


    private void displayNoWifiConnected()
    {
        mInfoFieldView.setText("no WIFI connection available");
    }


    private void displayStartingService()
    {
        mInfoFieldView.setText("starting service...");
    }


    private void setRunningState(ServiceRunningStates newState)
    {
        mLog.debug("client mirrored running state changed ["
                + mServiceRunningState + "] -> [" + newState + "]");
        mServiceRunningState = newState;
        mInfoFieldView.setText("client state set to [" + mServiceRunningState
                + "]");

    }


    protected void onWebServiceRunningBeforeSingularity()
    {
        setRunningState(ServiceRunningStates.BEFORE_SINGULARITY);
        mInfoFieldView.setText("state: " + mServiceRunningState);
        // askWebServiceForConnectionUrlAsync();
    }


    protected void onWebServiceRunning()
    {
        // displayConnectionUrl();
        setRunningState(ServiceRunningStates.RUNNING);
        mInfoFieldView.setText("state: " + mServiceRunningState);
        // askWebServiceForConnectionUrlAsync();
    }


    protected void onWebServiceURLChanged(String newUrl)
    {
        mLog.debug("url changed [" + newUrl + "]");
        mInfoFieldView.setText(newUrl);
    }


    protected void onWebServiceStartErroneous()
    {
        setRunningState(ServiceRunningStates.STARTED_ERRONEOUS);
        mInfoFieldView.setText("state: " + mServiceRunningState);
    }


    protected void onWebServiceStarting()
    {
        setRunningState(ServiceRunningStates.STARTING);
        mInfoFieldView.setText("state: " + mServiceRunningState);
    }


    protected void onWebServiceStoppedErroneous()
    {
        setRunningState(ServiceRunningStates.STOPPED_ERRONEOUS);
        mInfoFieldView.setText("state: " + mServiceRunningState);
    }


    protected void onWebServiceStopped()
    {

        setRunningState(ServiceRunningStates.STOPPED);
        mInfoFieldView.setText("state: " + mServiceRunningState);
        mButton.setChecked(false);
        // getActivity().getApplicationContext().unbindService(this);
    }


    protected void onWebServiceStopping()
    {
        // displayStoppingService();
        setRunningState(ServiceRunningStates.STOPPING);
        mInfoFieldView.setText("state: " + mServiceRunningState);
    }


    protected void onWebServiceClientRegistered()
    {
        mLog.debug("client registered to service");
        askWebServiceForRepublishStatesAsync();
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        String inServiceName = name.flattenToShortString();
        if (inServiceName.endsWith(mServiceComponentNameSuffix))
        {
            mLog.debug("bound to service [" + mServiceComponentNameSuffix + "]");
            mServiceMessenger = new Messenger(service);
            askWebServiceForClientRegistrationAsync();
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
            mLog.debug("unbound from service [" + mServiceComponentNameSuffix
                    + "]");
            mServiceMessenger = null;
        } else
        {
            mLog.error("failed unbinding fragment to service[" + inServiceName
                    + "] expected [*" + mServiceComponentNameSuffix + "]");
        }

    }


    private void askWebServiceForConnectionUrlAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.CONNECTION_URL);
    }


    private void askWebServiceForClientRegistrationAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE);
    }


    private void askWebServiceForRepublishStatesAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.REPUBLISH_STATES);
    }


    private void askWebServiceForServiceStopAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE);
    }


    private void sendMessageToService(int messageId)
    {
        String messageName = ServiceConnectionMessageTypes
                .getMessageName(messageId);

        mLog.debug("client sending [" + messageName + "] to service");
        if (mServiceMessenger != null)
        {
            try
            {
                Message messengerMessage = Message.obtain(null, messageId);
                if (messageId == ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE)
                {
                    messengerMessage.replyTo = mClientMessenger;
                }

                mServiceMessenger.send(messengerMessage);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending [" + messageName + "] to service", e);
            }
        } else
        {
            mLog.error("failed sending [" + messageName
                    + "], service not available");
        }
    }
}
