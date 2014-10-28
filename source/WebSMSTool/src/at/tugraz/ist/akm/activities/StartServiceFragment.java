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
import android.os.Messenger;
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
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceMessageBuilder;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.VerboseMessageSubmitter;

public class StartServiceFragment extends Fragment implements
        View.OnClickListener, ServiceConnection
{
    private LogClient mLog = new LogClient(
            StartServiceFragment.class.getCanonicalName());
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
    private String mClientName = "MgmtClient";
    private String mServiceName = "service";
    private VerboseMessageSubmitter mMessageSender = new VerboseMessageSubmitter(
            null, mClientName, mServiceName);
    private ServiceDirectorIncomingServiceMessageHandler mIncomingServiceMessageHandler = new ServiceDirectorIncomingServiceMessageHandler(
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

        if (isFirstLaunch())
        {
            mLog.debug("first launch, creating certificate");
            createNewCertificate();
        }
    }


    private void createNewCertificate()
    {

        SharedPreferencesProvider preferences = new SharedPreferencesProvider(
                getActivity().getApplicationContext());
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();

        String keystoreFilePath = getActivity().getApplicationContext()
                .getFilesDir().getPath().toString()
                + "/"
                + getActivity()
                        .getApplicationContext()
                        .getResources()
                        .getString(R.string.preferences_keystore_store_filename);

        appKeystore.loadKeystore(preferences.getKeyStorePassword(),
                keystoreFilePath);

        try
        {
            preferences.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }

        try
        {
            appKeystore.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing keystore");
        }
    }


    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        mLog.warning("on trim memory");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        mLog.debug("on create view");
        View view = inflater.inflate(R.layout.start_service_fragment,
                container, false);
        setUpMainFragmentUI(view);
        return view;
    }


    @Override
    public void onDestroyView()
    {
        mLog.debug("on destroy view");
        invalidateMainFragmentUI();
        super.onDestroyView();
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("on destroy");
        mButton = null;
        mInfoFieldView = null;
        try
        {
            mIncomingServiceMessageHandler.close();
            mIncomingServiceMessageHandler = null;
        }
        catch (Throwable e)
        {
            mLog.error("failed closing service message handler");
        }
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
            mLog.debug("skipped to UNbinding because isUNbinding ["
                    + mIsUnbindingFromService + "] and hasServiceMessenger ["
                    + hasServiceMessenger + "]");
        }
    }


    private void bindToService()
    {
        boolean hasServiceMessenger = mServiceMessenger != null;
        if (mIsUnbindingFromService == false && hasServiceMessenger == false)
        {
            getActivity().getApplicationContext().bindService(
                    newServiceIntent(), this, Context.BIND_AUTO_CREATE);
        } else
        {
            mLog.error("failed to bind because isUNbinding ["
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


    protected Messenger getMessenger()
    {
        return mClientMessenger;
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
        mMessageSender = null;
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
        mProgressBar.setIndeterminate(true);
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
        mProgressBar.setIndeterminate(false);
        mInfoFieldView
                .setText(getString(R.string.StartServiceFragment_service_stopped));
        mButton.setChecked(false);
        setServiceDisabledUI();
    }


    protected void onWebServiceStopping()
    {
        setRunningState(ServiceRunningStates.STOPPING);
        mProgressBar.setIndeterminate(true);
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
            mMessageSender = new VerboseMessageSubmitter(mServiceMessenger,
                    mClientName, mServiceName);
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
        mMessageSender.submit(ServiceMessageBuilder
                .newManagementClientUnregistrationMessage());
    }


    private void askWebServiceForClientRegistrationAsync()
    {
        mMessageSender.submit(ServiceMessageBuilder
                .newManagementClientRegistrationMessage(mClientMessenger));
    }


    private void askWebServiceForRepublishStatesAsync()
    {
        mMessageSender
                .submit(ServiceMessageBuilder.newRepublishStatesMessage());
    }


    private void sendWebServiceServerConfigChangedAsync()
    {
        WebserverProtocolConfig config = newWebserverConfig();
        mMessageSender.submit(ServiceMessageBuilder
                .newServiceConfigurationChanged(config));
    }


    private void askWebServiceForServerStartAsync()
    {
        WebserverProtocolConfig config = newWebserverConfig();
        mMessageSender.submit(ServiceMessageBuilder
                .newStartServiceMessage(config));
    }


    private void askWebServiceForServerStopAsync()
    {
        mMessageSender.submit(ServiceMessageBuilder.newStopServiceMessage());
    }


    private boolean isFirstLaunch()
    {
        SharedPreferencesProvider preferences = new SharedPreferencesProvider(
                getActivity().getApplicationContext());

        boolean isFistLaunced = preferences.isFirstLaunch();

        try
        {
            preferences.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
        return isFistLaunced;
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

        try
        {
            preferences.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
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
        statsLayout.setAlpha((float) (0.5));
        setUserAuthLayoutAlpha(true);
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
        setUserAuthLayoutAlpha();
    }


    private void setUserAuthLayoutAlpha()
    {
        WebserverProtocolConfig config = newWebserverConfig();
        setUserAuthLayoutAlpha(config.isUserAuthEnabled);
    }


    private void setUserAuthLayoutAlpha(boolean activatedState)
    {
        if (activatedState)
        {
            mAccessRestrictionLayout.setAlpha(1);
        } else
        {
            mAccessRestrictionLayout.setAlpha((float) 0.5);
        }
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
            onWebServiceHttpAccessRestriction(false);
        } else
        {
            onWebServiceHttpAccessRestriction(true);
        }

    }


    private void onWebServiceHttpAccessRestriction(boolean isEnabled)
    {
        setUserAuthLayoutAlpha(isEnabled);
    }
}
