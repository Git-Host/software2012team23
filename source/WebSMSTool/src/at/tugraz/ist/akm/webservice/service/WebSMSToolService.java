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

package at.tugraz.ist.akm.webservice.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Messenger;
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.networkInterface.WifiIpAddress;
import at.tugraz.ist.akm.secureRandom.PRNGFixes;
import at.tugraz.ist.akm.sms.ISmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.ui.LoginEvent;
import at.tugraz.ist.akm.trace.ui.MessageEvent;
import at.tugraz.ist.akm.trace.ui.ResourceStringLoader;
import at.tugraz.ist.akm.trace.ui.ServiceEvent;
import at.tugraz.ist.akm.trace.ui.SettingsChangedEvent;
import at.tugraz.ist.akm.trace.ui.UiEvent;
import at.tugraz.ist.akm.webservice.server.IHttpAccessCallback;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ClientMessageBuilder;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.VerboseMessageSubmitter;

public class WebSMSToolService extends Service implements ISmsIOCallback,
        IHttpAccessCallback
{
    private LogClient mLog = new LogClient(this);
    private SimpleWebServer mServer = null;
    private WebserverProtocolConfig mServerConfig = null;
    private BroadcastReceiver mIntentReceiver = null;
    private Messenger mManagementClientMessenger = null;
    private Messenger mEventClientMessenger = null;
    private String mEventClientName = "EventClient";
    private String mManagementClientName = "MgmtClient";
    private String mServiceName = "service";
    private Messenger mServiceMessenger = new Messenger(
            new IncomingClientMessageHandler(this));
    private VerboseMessageSubmitter mManagementClientTransmitter = new VerboseMessageSubmitter(
            null, mServiceName, mManagementClientName);
    private VerboseMessageSubmitter mEventClientTransmitter = new VerboseMessageSubmitter(
            null, mServiceName, mEventClientName);
    private ServiceRunningStates mServiceRunningState = ServiceRunningStates.BEFORE_SINGULARITY;
    private WifiIpAddress mWifiState = null;
    private int mSmsSentCount = 0;
    private int mSmsReceivedCount = 0;
    private int mSmsSentErroneousCount = 0;
    private int mSmsDeliveredCount = 0;
    private NetworkTrafficPropagationTimer mNetworkStatsPropagationTimer = null;
    private LinkedList<UiEvent> mBufferedLogEvents = new LinkedList<UiEvent>();
    private int mMaxBufferedLogs = 100;
    private ResourceStringLoader mStringLoader = null;

    public enum ServiceRunningStates {
        STOPPED, STARTING, STARTED_ERRONEOUS, RUNNING, STOPPING, STOPPED_ERRONEOUS, BEFORE_SINGULARITY;
        private ServiceRunningStates()
        {
        }
    }


    protected long getReceivedBytesCount()
    {
        if (mServer == null)
        {
            return 0;
        }
        return mServer.getReceivedBytesCount();
    }


    protected long getSentBytesCount()
    {
        if (mServer == null)
        {
            return 0;
        }
        return mServer.getSentBytesCount();
    }

    private static class WebSMSToolBroadcastReceiver extends BroadcastReceiver
    {

        WebSMSToolService mCallback = null;


        public WebSMSToolBroadcastReceiver(WebSMSToolService callback)
        {
            mCallback = callback;
        }


        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (0 == action.compareTo(WifiManager.WIFI_STATE_CHANGED_ACTION))
            {
                mCallback.onWifiStateChanged(context, intent);
            } else if (0 == action
                    .compareTo(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            {
                mCallback.onNetworkStateChanged(context, intent);
            } else
            {
                mCallback.onUnknownIntent(context, intent);
            }
        }


        public IntentFilter newFilter()
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            return filter;
        }
    }


    protected void onEventClientRequestRegisterForEvents(
            Messenger clientMessenger)
    {
        if (clientMessenger != null)
        {
            mLog.debug("on clientmessenger register for events");
            mEventClientMessenger = clientMessenger;
            mEventClientTransmitter = new VerboseMessageSubmitter(
                    mEventClientMessenger, mServiceName, mEventClientName);
            mEventClientTransmitter.submit(ClientMessageBuilder
                    .newRegisteredToServiceEventsMessage());
            sendEventMessageBufferToEventClient();
        } else
        {
            mLog.debug("on clientmessenger UNregister from events");
        }
    }


    protected void onManagementClientRequestRegisterForManagement(
            Messenger clientMessenger)
    {
        if (clientMessenger != null)
        {
            mLog.debug("on clientmessenger register for management");
            mManagementClientMessenger = clientMessenger;
            mManagementClientTransmitter = new VerboseMessageSubmitter(
                    mManagementClientMessenger, mServiceName,
                    mManagementClientName);
            mManagementClientTransmitter.submit(ClientMessageBuilder
                    .newRegisteredToServiceManagementMessage());
            mNetworkStatsPropagationTimer = new NetworkTrafficPropagationTimer(
                    60 * 60, 10, this);
            mNetworkStatsPropagationTimer.start();
        } else
        {
            mLog.debug("on clientmessenger UNregister from management");
            mNetworkStatsPropagationTimer.cancel();
            mNetworkStatsPropagationTimer = null;
        }
    }


    public WebSMSToolService()
    {
        PRNGFixes.apply();
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        mLog.debug("on create");
        mStringLoader = new ResourceStringLoader(getApplicationContext());
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        mLog.debug("on bind");
        return mServiceMessenger.getBinder();
    }


    @Override
    public boolean onUnbind(Intent intent)
    {
        mLog.debug("on unbind");
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLog.debug("on start command");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    private void startWebSMSToolServiceThread()
    {
        synchronized (this)
        {
            mLog.debug("running on emulator ["
                    + AppEnvironment.isRunningOnEmulator()
                    + "] running on device ["
                    + (!AppEnvironment.isRunningOnEmulator()) + "]");
            mWifiState = new WifiIpAddress(getApplicationContext());
            if (AppEnvironment.isRunningOnEmulator() == false
                    && mWifiState.isWifiEnabled() == false)
            {
                onWirelessNetworkNotAvailable();
            } else if ((getRunningState() == ServiceRunningStates.BEFORE_SINGULARITY || getRunningState() == ServiceRunningStates.STOPPED))
            {
                setRunningState(ServiceRunningStates.STARTING);
                registerIntentReceiver();

                try
                {
                    mServer = new SimpleWebServer(this, mServerConfig, this);
                    mServer.registerSmsIoCallback(this);
                    mServer.startServer();

                    if (mServer.isRunning())
                    {
                        setRunningState(ServiceRunningStates.RUNNING);
                    } else
                    {
                        throw new Exception("server failed to start");
                    }

                    mLog.info("service has been started");
                }
                catch (Exception ex)
                {
                    mLog.error("failed starting web service", ex);
                    setRunningState(ServiceRunningStates.STARTED_ERRONEOUS);
                    stopSelf();
                }
            } else
            {
                mLog.error("failed to start web service, state ["
                        + getRunningState() + "]");
            }
        }
    }


    @Override
    public boolean stopService(Intent name)
    {
        mLog.debug("stop service");
        stopWebSMSToolServiceThread();
        super.stopService(name);
        return true;
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("on destroy");
        if (mWifiState != null)
        {
            try
            {
                mWifiState.close();
                mWifiState = null;
            }
            catch (Throwable e)
            {
                mLog.error("failed closing wifi resource");
            }
        }
        if (mNetworkStatsPropagationTimer != null)
        {
            mNetworkStatsPropagationTimer.cancel();
            mNetworkStatsPropagationTimer = null;
        }
        mServer = null;
        mServerConfig = null;
        mIntentReceiver = null;
        mManagementClientMessenger = null;
        mManagementClientTransmitter = null;
        mEventClientMessenger = null;
        mEventClientTransmitter = null;
        mStringLoader = null;
        super.onDestroy();
    }


    @Override
    public void onRebind(Intent intent)
    {
        mLog.debug("on rebind");
        super.onRebind(intent);
    }


    @Override
    public void onLowMemory()
    {
        mLog.error("on low memory");
        super.onLowMemory();
    }


    @Override
    public void onTrimMemory(int level)
    {
        mLog.warning("on trim memory");
        super.onTrimMemory(level);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        mLog.debug("on configuration changed");
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        mLog.debug("on task removed");
        super.onTaskRemoved(rootIntent);
    }


    protected ServiceRunningStates getRunningState()
    {
        return mServiceRunningState;
    }


    private void setRunningState(ServiceRunningStates newState)
    {
        mServiceRunningState = newState;
        onManagementClientRequestCurrentRunningState();

        if (mServiceRunningState == ServiceRunningStates.RUNNING
                || mServiceRunningState == ServiceRunningStates.STOPPED)
        {
            onWebServiceLogEventReceived(newUiEvent(mServiceRunningState));
        }
    }


    protected String getServerProtocol()
    {
        return mServer.getServerProtocol();
    }


    protected String getServerAddress()
    {
        return mServer.getServerAddress();
    }


    protected int getServerPort()
    {
        return mServer.getServerPort();
    }


    private boolean stopWebSMSToolServiceThread()
    {
        setRunningState(ServiceRunningStates.STOPPING);
        synchronized (this)
        {
            unregisterIntentReceiver();
            try
            {
                mServer.stopServer();
                waitForServiceBeingStopped();
                if (mServer.isRunning())
                {
                    throw new Exception("server failed to stop for any reason");
                }
                setRunningState(ServiceRunningStates.STOPPED);
                mServer.unregisterSMSIoCallback();
                mServer.close();
            }
            catch (Exception ex)
            {
                mLog.error("error while stopping server!", ex);
                setRunningState(ServiceRunningStates.STOPPED_ERRONEOUS);
            }
        }
        return (getRunningState() == ServiceRunningStates.STOPPED);
    }


    public void onWifiStateChanged(Context context, Intent intent)
    {
        String extraKey = WifiManager.EXTRA_WIFI_STATE;
        boolean disabled = WifiManager.WIFI_STATE_DISABLED == intent
                .getIntExtra(extraKey, -1);
        boolean disabling = WifiManager.WIFI_STATE_DISABLING == intent
                .getIntExtra(extraKey, -1);
        boolean enabled = WifiManager.WIFI_STATE_ENABLED == intent.getIntExtra(
                extraKey, -1);
        boolean enabling = WifiManager.WIFI_STATE_ENABLING == intent
                .getIntExtra(extraKey, -1);
        boolean unknown = WifiManager.WIFI_STATE_UNKNOWN == intent.getIntExtra(
                extraKey, -1);

        mLog.debug("wifi state changed disabled [" + disabled
                + "], disabling [" + disabling + "], enabled [" + enabled
                + "], enabling [" + enabling + "], unknown [" + unknown + "]");

        if (enabled)
        {
            return;
        }

        mLog.debug("wifi state changed, turning off service");
        stopWebSMSToolServiceThread();
    }


    public void onNetworkStateChanged(Context context, Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo) intent.getExtras().get(
                WifiManager.EXTRA_NETWORK_INFO);
        mLog.debug("network state changed to [" + networkInfo + "]");
        if (0 == NetworkInfo.State.CONNECTED.compareTo(networkInfo.getState()))
        {
            return;
        }
        mLog.debug("turning off service");
        stopWebSMSToolServiceThread();
    }


    public void onUnknownIntent(Context context, Intent intent)
    {
        mLog.debug("recived unknown intent [" + intent.getAction() + "]");
    }


    private void registerIntentReceiver()
    {
        WebSMSToolBroadcastReceiver intentReceiver = new WebSMSToolBroadcastReceiver(
                this);
        mIntentReceiver = intentReceiver;
        mLog.debug("register wifi change intent receiver");
        registerReceiver(mIntentReceiver, intentReceiver.newFilter());
    }


    private void unregisterIntentReceiver()
    {
        mLog.debug("unregister wifi change intent receiver");
        unregisterReceiver(mIntentReceiver);
        mIntentReceiver = null;
    }


    private void waitForServiceBeingStopped()
    {
        int maxTries = 20, delayMs = 200;

        while (mServer.isRunning() && (maxTries-- > 0))
        {
            try
            {
                this.wait(delayMs);
            }
            catch (InterruptedException ex)
            {
                mLog.error("interrupted during wait", ex);
            }
        }
    }


    protected void onManagementClientRequestCurrentRunningState()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newCurrentRunningStateMessage(getRunningState()));
    }


    private void sendEventMessageToEventClient(UiEvent event)
    {
        mEventClientTransmitter.submit(ClientMessageBuilder
                .newServiceEventMessage(event));
    }


    private synchronized void sendEventMessageBufferToEventClient()
    {
        if (mBufferedLogEvents.size() > 0)
        {
            mEventClientTransmitter.submit(ClientMessageBuilder
                    .newServiceEventMessage(new ArrayList<UiEvent>(
                            mBufferedLogEvents)));
        }
    }


    private String formatConnectionUrl()
    {
        StringBuffer connectionUrl = new StringBuffer();
        connectionUrl.append(mServer.getServerProtocol()).append("://")
                .append(mServer.getServerAddress()).append(":")
                .append(mServer.getServerPort());
        return connectionUrl.toString();
    }


    protected void onManagementClientRequestConnectionUrl()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newConnectionUrlMessage(formatConnectionUrl()));
    }


    protected void onManagementClientRequestRepublishStates()
    {
        try
        {
            onManagementClientRequestCurrentRunningState();
            onManagementClientRequestConnectionUrl();
            onManagementClientRequestHttpPassword();
            onManagementClientRequestHttpUsername();
            onManagementClientRequestIsHttpAccessRestrictionEnabled();
        }
        catch (NullPointerException npe)
        {
            mLog.debug("failed sending connection url, server not ready yet");
        }
    }


    protected void onWirelessNetworkNotAvailable()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newNetworkNotAvailableMessage());
    }


    protected void onManagementClientRequestStopWEBService()
    {
        if (getRunningState() == ServiceRunningStates.RUNNING)
        {
            stopWebSMSToolServiceThread();
        } else
        {
            mLog.debug("failed client request for stopping service in state ["
                    + getRunningState() + "]");
        }
    }


    protected void onManagementClientRequestSentBytesCount(int txBytes)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newSentBytesCountMessage(txBytes));
    }


    protected void onManagementClientRequestReceivedBytesCount(int rxBytes)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newReceivedBytesCountMessage(rxBytes));
    }


    protected void onManagementClientRequestStartWEBService()
    {
        if (getRunningState() == ServiceRunningStates.BEFORE_SINGULARITY
                || getRunningState() == ServiceRunningStates.STOPPED)
        {
            startWebSMSToolServiceThread();
        } else
        {
            mLog.debug("failed client request for starting service in state ["
                    + getRunningState() + "]");
        }
    }


    protected void onManagementClientRequestHttpPassword()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newHttpPasswordMessage(mServer.getMaskedHttpPassword()));
    }


    protected void onManagementClientRequestHttpUsername()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newHttpUsernameMessage(mServer.getHttpUsername()));
    }


    protected void onManagementClientRequestIsHttpAccessRestrictionEnabled()
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newHttpAccessRestrictionMessage(mServer
                        .isHttpAccessRestrictionEnabled()));
    }


    protected void onManagementClientResponseServerSettingsChanged(
            WebserverProtocolConfig newConfig)
    {
        mServerConfig = newConfig;
        if (getRunningState() == ServiceRunningStates.RUNNING)
        {
            mLog.warning("not implemented: restart needed");
            onWebServiceLogEventReceived(newUiEvent(newConfig));
        }
    }


    @Override
    public synchronized void smsSentCallback(Context context,
            List<TextMessage> messages)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newSmsSentMessage(mSmsSentCount++));

        for (TextMessage message : messages)
        {
            onWebServiceLogEventReceived(newUiEvent(message, false));
        }
    }


    @Override
    public synchronized void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newSmsSentErroneousMessage(mSmsSentErroneousCount++));
    }


    @Override
    public synchronized void smsDeliveredCallback(Context context,
            List<TextMessage> messagea)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newSmsDeliveredMessage(mSmsDeliveredCount++));
    }


    @Override
    synchronized public void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        mManagementClientTransmitter.submit(ClientMessageBuilder
                .newSmsReceivedMessage(mSmsReceivedCount++));
        for (TextMessage message : messages)
        {
            onWebServiceLogEventReceived(newUiEvent(message, true));
        }
    }


    private synchronized void onWebServiceLogEventReceived(UiEvent event)
    {
        mBufferedLogEvents.addFirst(event);
        if (mBufferedLogEvents.size() > mMaxBufferedLogs)
        {
            mBufferedLogEvents.removeLast();
        }
        sendEventMessageToEventClient(event);
    }


    private UiEvent newUiEvent(TextMessage textMessage,
            boolean isIncomingMessage)
    {
        MessageEvent event = new MessageEvent(isIncomingMessage);
        return event.load(mStringLoader, textMessage);
    }


    private UiEvent newUiEvent(WebserverProtocolConfig config)
    {
        SettingsChangedEvent event = new SettingsChangedEvent();
        return event.load(mStringLoader);
    }


    private UiEvent newUiEvent(ServiceRunningStates runningState)
    {
        ServiceEvent event = null;
        if (runningState == ServiceRunningStates.STOPPED)
        {
            event = new ServiceEvent(false);
            return event.load(mStringLoader);
        } else if (runningState == ServiceRunningStates.RUNNING)
        {
            event = new ServiceEvent(true);
            return event.load(mStringLoader);
        }
        mLog.error("failed bulding service event for that state ["
                + runningState + "]");
        return null;
    }


    @Override
    public void onLogFailed()
    {
        LoginEvent event = new LoginEvent(false);
        onWebServiceLogEventReceived(event.load(mStringLoader));
    }


    @Override
    public void onLoginSuccess()
    {
        LoginEvent event = new LoginEvent(true);
        onWebServiceLogEventReceived(event.load(mStringLoader));
    }
}
