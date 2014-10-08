package at.tugraz.ist.akm.activities;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ProgressBar mProgressBar = null;
    
    private TextView mSmsSentView = null;
    private TextView mSmsRecievedView = null;

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
        setUpMainFragmentUI(view);
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
        mLog.debug("on destroy");
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
        setUpMainFragmentUI(getView());
        mLog.debug("on start");
    }


    public void onStop()
    {
        tearDownMainFragmentUI();
        mLog.debug("on stop");
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("on resume");
        setServiceDisabledIcon();
        getActivity().getApplicationContext().bindService(
                mStartSmsServiceIntent, this, Context.BIND_AUTO_CREATE);
        askWebServiceForClientRegistrationAsync();
    }


    @Override
    public void onPause()
    {
        mLog.debug("on pause");
        askWebServiceForClientUnregistrationAsync();
        super.onStop();
    }


    private void setUpMainFragmentUI(View view)
    {
        mStartSmsServiceIntent = new Intent(getActivity(), mServiceClass);
        mButton = (ToggleButton) view.findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) view.findViewById(R.id.adress_data_field);
        mButton.setOnClickListener(this);
        mWifiState = new WifiIpAddress(view.getContext());
        mProgressBar = (ProgressBar) view
                .findViewById(R.id.start_stop_server_progress_bar);
        mSmsRecievedView = (TextView) view.findViewById(R.id.main_fragment_sms_recieved);
        mSmsSentView = (TextView) view.findViewById(R.id.main_fragment_sms_sent);
    }


    private void tearDownMainFragmentUI()
    {
        mButton.setOnClickListener(null);
    }


    @Override
    public void onClick(View view)
    {
        mLog.debug("on click");
        if (!isServiceRunning())
        {
            if (mWifiState.isWifiEnabled() || mWifiState.isWifiAPEnabled()
                    || AppEnvironment.isRunningOnEmulator())
            {
                mLog.info("starting web service");
                displayStartingService();

                mProgressBar.setIndeterminate(true);
                getActivity().getApplicationContext().startService(
                        mStartSmsServiceIntent);
                getActivity().getApplicationContext().bindService(
                        mStartSmsServiceIntent, StartServiceFragment.this,
                        Context.BIND_AUTO_CREATE);
            } else
            {
                displayNoWifiConnected();
                String message = "failed starting service without wifi connection or if not in ap mode";
                mLog.warning(message);
                mLog.error(message);
            }
        } else
        {
            mLog.debug("service seems running, asking for stop");
            askWebServiceForServiceStopAsync();
            try
            {
                getActivity().getApplicationContext().unbindService(this);
            }
            catch (IllegalArgumentException iae)
            {
                mLog.debug("failed to unbind service");
                mLog.error("failed to unbind service", iae);
            }
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
        mInfoFieldView.setText("service started");

    }


    protected void onWebServiceRunningStateBeforeSingularity()
    {
        setRunningState(ServiceRunningStates.BEFORE_SINGULARITY);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_before_first_start));
    }


    protected void onWebServiceRunning()
    {
        mButton.setChecked(true);
        mProgressBar.setIndeterminate(false);
        setServiceEnabledIcon();
        if (mServiceRunningState == ServiceRunningStates.RUNNING)
        {
            return;
        }
        setRunningState(ServiceRunningStates.RUNNING);
        askWebServiceForRepublishStatesAsync();
    }


    protected void onWebServiceURLChanged(String newUrl)
    {
        mLog.debug("url changed [" + newUrl + "]");
        mInfoFieldView.setText(newUrl);
    }


    protected void onWebServiceStartErroneous()
    {
        setRunningState(ServiceRunningStates.STARTED_ERRONEOUS);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_started_erroneous));
    }


    protected void onWebServiceStarting()
    {
        mProgressBar.setVisibility(View.VISIBLE);
        setRunningState(ServiceRunningStates.STARTING);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_starting));
    }


    protected void onWebServiceStoppedErroneous()
    {
        setRunningState(ServiceRunningStates.STOPPED_ERRONEOUS);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_stopped_erroneous));
    }


    protected void onWebServiceStopped()
    {
        setRunningState(ServiceRunningStates.STOPPED);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_stopped));
        mButton.setChecked(false);
        setServiceDisabledIcon();
    }


    protected void onWebServiceStopping()
    {
        setRunningState(ServiceRunningStates.STOPPING);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_stopping));
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


    private void askWebServiceForClientUnregistrationAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.UNREGISTER_TO_SERVICE);
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


    public void setServiceDisabledIcon()
    {
        ImageView icActionbarLauncher = (ImageView) getView().findViewById(
                R.id.main_fragment_ic_service);
        icActionbarLauncher
                .setImageBitmap(convertToGrayScaleImage(BitmapFactory
                        .decodeResource(getResources(), R.drawable.ic_launcher)));

        LinearLayout statsLayout = (LinearLayout) getView().findViewById(
                R.id.main_fragment_statistics_linear_layout);
        statsLayout.setAlpha((float) 0.5);
    }


    public void setServiceEnabledIcon()
    {
        ImageView icActionbarLauncher = (ImageView) getView().findViewById(
                R.id.main_fragment_ic_service);
        icActionbarLauncher.setImageBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_launcher));

        LinearLayout statsLayout = (LinearLayout) getView().findViewById(
                R.id.main_fragment_statistics_linear_layout);
        statsLayout.setAlpha(1);
    }


    public static Bitmap convertToGrayScaleImage(Bitmap src)
    {
        double redGrayscale = 0.3, greenGrayscale = 0.6, blueGrayscale = 0.3;

        Bitmap grayScaleBitmap = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), src.getConfig());

        for (int x = 0; x < src.getWidth(); ++x)
        {
            int pxAlpha, pxRed, pxGreen, pxBlue;
            for (int y = 0; y < src.getHeight(); ++y)
            {
                int px = src.getPixel(x, y);

                pxAlpha = Color.alpha(px);
                pxRed = Color.red(px);
                pxGreen = Color.green(px);
                pxBlue = Color.blue(px);

                pxRed = (int) (redGrayscale * pxRed + greenGrayscale * pxGreen + blueGrayscale
                        * pxBlue);
                pxGreen = pxRed;
                pxBlue = pxRed;

                grayScaleBitmap.setPixel(x, y,
                        Color.argb(pxAlpha, pxRed, pxGreen, pxBlue));
            }
        }

        return grayScaleBitmap;
    }


    protected void onWebServiceSmsSent(int smsSentCount)
    {
        mLog.debug("sms sent [" + smsSentCount + "]");
        mSmsSentView.setText(Integer.toString(smsSentCount));
    }
    
    protected void onWebServiceSmsDelivered(int smsDeliveredCount)
    {
        mLog.debug("sms delivered [" + smsDeliveredCount + "]");
    }
    
    protected void onWebServiceSmsSentErroneous(int smsSentCount)
    {
        mLog.debug("sms sent erroneous [" + smsSentCount + "]");
    }
    
    protected void onWebServiceSmsReceived(int smsReceivedCount)
    {
        mLog.debug("sms received [" + smsReceivedCount + "]");
        mSmsRecievedView.setText(Integer.toString(smsReceivedCount));
    }
}
