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
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;
import at.tugraz.ist.akm.webservice.service.ServiceConnectionMessageTypes;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;

public class StartServiceFragment extends Fragment implements
        View.OnClickListener, ServiceConnection
{
    private LogClient mLog = new LogClient(
            StartServiceFragment.class.getCanonicalName());
    final String mServiceName = WebSMSToolService.class.getName();
    private LinearLayout mAccessRestrictionLayout = null;
    private ToggleButton mButton = null;
    private ProgressBar mProgressBar = null;
    private TextView mInfoFieldView = null;
    private TextView mSmsSentView = null;
    private TextView mSmsRecievedView = null;
    private TextView mNetworkTxBytes = null;
    private TextView mNetworkRxBytes = null;
    private TextView mAccessRestrictionUsername = null;
    private TextView mAccessRestrictionPassword = null;
    private boolean mIsUnbindingFromService = false;
    private boolean mIsRegisteredToService = false;
    private Messenger mServiceMessenger = null;
    private IncomingServiceMessageHandler mIncomingServiceMessageHandler = new IncomingServiceMessageHandler(
            this);
    private Messenger mClientMessenger = new Messenger(
            mIncomingServiceMessageHandler);
    private ServiceRunningStates mServiceRunningState = ServiceRunningStates.BEFORE_SINGULARITY;


    public StartServiceFragment()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLog.debug("on create");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        mLog.debug("on create view");
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        setUpMainFragmentUI(view);
        return view;
    }


    @Override
    public void onDestroyView()
    {
        mLog.debug("on destry view");
        invalidateMainFragmentUI();
        super.onDestroyView();
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("on destroy");
        mButton = null;
        mInfoFieldView = null;
        mIncomingServiceMessageHandler.onClose();
        mIncomingServiceMessageHandler = null;
        super.onDestroy();
    }


    @Override
    public void onDetach()
    {
        mLog.debug("on detach");
        mLog = null;
        super.onDetach();
    }


    private void unbindFromService()
    {
        boolean hasServiceMessenger = mServiceMessenger != null;
        if (mIsUnbindingFromService == false && hasServiceMessenger)
        {
            askWebServiceForClientUnregistrationAsync();
            getActivity().getApplicationContext().unbindService(this);
            mServiceMessenger = null;
        } else
        {
            mLog.error("failed to UNbind because isUNbinding ["
                    + mIsUnbindingFromService + "] and hasServiceMessenger ["
                    + hasServiceMessenger + "]");
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        mLog.debug("on start");
    }


    public void onStop()
    {
        mLog.debug("on stop");
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("on resume");
        setServiceDisabledUI();
        bindToService();
    }


    private void bindToService()
    {
        boolean hasServiceMessenger = mServiceMessenger != null;
        if (mIsUnbindingFromService == false && hasServiceMessenger == false)
        {
            getActivity().getApplicationContext().bindService(
                   newServiceIntent(), this,
                    Context.BIND_AUTO_CREATE);
        } else
        {
            mLog.error("failed to bind because isUNbinding ["
                    + mIsUnbindingFromService + "] and hasServiceMessenger ["
                    + hasServiceMessenger + "]");
        }
    }


    @Override
    public void onPause()
    {
        mLog.debug("on pause");
        unbindFromService();
        super.onPause();
    }


    private void setUpMainFragmentUI(View view)
    {
        mButton = (ToggleButton) view.findViewById(R.id.start_stop_server);
        mInfoFieldView = (TextView) view.findViewById(R.id.adress_data_field);
        mButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) view
                .findViewById(R.id.start_stop_server_progress_bar);

        mSmsRecievedView = (TextView) view
                .findViewById(R.id.main_fragment_sms_recieved);
        mSmsSentView = (TextView) view
                .findViewById(R.id.main_fragment_sms_sent);

        mNetworkRxBytes = (TextView) view
                .findViewById(R.id.main_fragment_total_bytes_recieved);
        mNetworkTxBytes = (TextView) view
                .findViewById(R.id.main_fragment_total_bytes_sent);

        mAccessRestrictionUsername = (TextView) view
                .findViewById(R.id.main_fragment_http_access_username);
        mAccessRestrictionPassword = (TextView) view
                .findViewById(R.id.main_fragment_http_access_password);
        mAccessRestrictionLayout = (LinearLayout) view
                .findViewById(R.id.main_fragment_access_restriction_info);

    }


    private void invalidateMainFragmentUI()
    {
        mButton.setOnClickListener(null);
        mButton = null;
        mInfoFieldView = null;
        mProgressBar = null;
        mSmsRecievedView = null;
        mSmsSentView = null;
        mNetworkRxBytes = null;
        mNetworkTxBytes = null;
        mAccessRestrictionUsername = null;
        mAccessRestrictionPassword = null;
        mAccessRestrictionLayout = null;
    }


    @Override
    public void onClick(View view)
    {
        mLog.debug("on click isRegisteredToService [" + mIsRegisteredToService
                + "]");
        if (!isServiceRunning())
        {
            mLog.info("webservice not running -> starting");
            displayStartingService();
            mProgressBar.setIndeterminate(true);
            startService();
            sendWebServiceServerConfigChangedAsync();
            askWebServiceForServerStartAsync();
        } else
        {
            mLog.debug("webservice service seems running -> stopping");
            askWebServiceForServerStopAsync();
            stopService();
        }
    }


    private Intent newServiceIntent()
    {
        return new Intent(getActivity(), WebSMSToolService.class);
    }


    private void startService()
    {
        getActivity().getApplicationContext().startService(newServiceIntent());
    }


    private void stopService()
    {
        getActivity().getApplicationContext().stopService(newServiceIntent());
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
        setServiceEnabledUI();
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
        setServiceDisabledUI();
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
        mIsRegisteredToService = true;
        askWebServiceForRepublishStatesAsync();
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        String serviceComponentNameSuffix = WebSMSToolService.class
                .getSimpleName();
        String inServiceName = name.flattenToShortString();
        if (inServiceName.endsWith(serviceComponentNameSuffix))
        {
            mLog.debug("bound to service [" + serviceComponentNameSuffix + "]");
            mServiceMessenger = new Messenger(service);
            askWebServiceForClientRegistrationAsync();
        } else
        {
            mLog.error("failed binding fragment to service[" + inServiceName
                    + "] expected [*" + serviceComponentNameSuffix + "]");
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        String inServiceName = name.flattenToShortString();
        String serviceComponentNameSuffix = WebSMSToolService.class
                .getSimpleName();
        if (inServiceName.endsWith(serviceComponentNameSuffix))
        {
            mLog.debug("unbound from service [" + serviceComponentNameSuffix
                    + "]");
            mServiceMessenger = null;
            mIsUnbindingFromService = false;
        } else
        {
            mLog.error("failed unbinding fragment to service[" + inServiceName
                    + "] expected [*" + serviceComponentNameSuffix + "]");
        }

    }


    private void askWebServiceForClientUnregistrationAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.UNREGISTER_TO_SERVICE);
        mIsRegisteredToService = false;
    }


    private void askWebServiceForClientRegistrationAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE);
    }


    private void askWebServiceForRepublishStatesAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.REPUBLISH_STATES);
    }


    private void sendWebServiceServerConfigChangedAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Response.SERVER_SETTINGS_GHANGED);
    }


    private void askWebServiceForServerStartAsync()
    {
        sendMessageToService(ServiceConnectionMessageTypes.Client.Request.START_WEB_SERVICE);
    }


    private void askWebServiceForServerStopAsync()
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
                appendDataToMessage(messengerMessage, messageId);
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


    private void appendDataToMessage(Message message, int messageId)
    {
        switch (messageId)
        {
        case ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE:
            message.replyTo = mClientMessenger;
            break;
        case ServiceConnectionMessageTypes.Client.Request.START_WEB_SERVICE:
        case ServiceConnectionMessageTypes.Client.Response.SERVER_SETTINGS_GHANGED:
            WebserverProtocolConfig config = newWebserverConfig();
            Bundle objBundleParameter = new Bundle();
            putServerconfigToBundle(objBundleParameter, config);
            message.setData(objBundleParameter);
            break;
        }
    }


    private void putServerconfigToBundle(Bundle aBundle,
            WebserverProtocolConfig configToStore)
    {
        aBundle.putBoolean(
                ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_HTTPS,
                configToStore.isHttpsEnabled);
        aBundle.putBoolean(
                ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_USER_AUTH,
                configToStore.isUserAuthEnabled);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD,
                configToStore.password);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PROTOCOL,
                configToStore.protocolName);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME,
                configToStore.username);
        aBundle.putInt(
                ServiceConnectionMessageTypes.Bundle.Key.INT_ARG_SERVER_PORT,
                configToStore.port);
    }


    private WebserverProtocolConfig newWebserverConfig()
    {
        WebserverProtocolConfig config = new WebserverProtocolConfig();
        SharedPreferencesProvider preferences = new SharedPreferencesProvider(
                getActivity().getApplicationContext());

        config.isHttpsEnabled = preferences.isHttpsEnabled();
        config.isUserAuthEnabled = preferences.isAccessRestrictionEnabled();
        config.password = preferences.getPassword();
        config.port = preferences.getPort();
        config.protocolName = preferences.getProtocol();
        config.username = preferences.getUsername();
        return config;
    }


    public void setServiceDisabledUI()
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


    public void setServiceEnabledUI()
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
        mSmsSentView.setText(Integer.toString(smsSentCount));
    }


    protected void onWebServiceSmsDelivered(int smsDeliveredCount)
    {
    }


    protected void onWebServiceSmsSentErroneous(int smsSentCount)
    {
    }


    protected void onWebServiceSmsReceived(int smsReceivedCount)
    {
        mSmsRecievedView.setText(Integer.toString(smsReceivedCount));
    }


    protected void onWebServiceNetworkNotConnected()
    {
        displayNoWifiConnected();
        String message = "failed starting service without wifi connection or if not in ap mode";
        mLog.warning(message);
        mLog.error(message);
    }


    protected void onWebServiceTxBytesUpdate(int newTxBytesStatus)
    {
        mNetworkTxBytes.setText(Formatter.formatFileSize(getActivity(),
                newTxBytesStatus));
    }


    protected void onWebServiceRxBytesUpdate(int newRxBytesStatus)
    {
        mNetworkRxBytes.setText(Formatter.formatFileSize(getActivity(),
                newRxBytesStatus));
    }


    protected void onWebServiceHttpPassword(String maskedPassword)
    {
        mAccessRestrictionPassword.setText(maskedPassword);
    }


    protected void onWebServiceHttpUsername(String username)
    {
        mAccessRestrictionUsername.setText(username);
    }


    protected void onWebServiceHttpAccessRestriction(int isEnabled)
    {
        if (0 == isEnabled)
        {
            mAccessRestrictionLayout.setAlpha(1 / 2);
        } else
        {
            mAccessRestrictionLayout.setAlpha(1);
        }
    }
}
