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

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.networkInterface.WifiIpAddress;
import at.tugraz.ist.akm.secureRandom.PRNGFixes;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class WebSMSToolService extends Service implements SmsIOCallback
{
    private LogClient mLog = new LogClient(this);
    private SimpleWebServer mServer = null;
    private WebserverProtocolConfig mServerConfig = null;
    private static BroadcastReceiver mIntentReceiver = null;
    private Messenger mClientMessenger = null;
    private Messenger mServiceMessenger = new Messenger(
            new IncomingClientMessageHandler(this));
    private ServiceRunningStates mServiceRunningState = ServiceRunningStates.BEFORE_SINGULARITY;
    private WifiIpAddress mWifiState = null;
    private int mSmsSentCount = 0;
    private int mSmsReceivedCount = 0;
    private int mSmsSentErroneousCount = 0;
    private int mSmsDeliveredCount = 0;
    private NetworkTrafficPropagationTimer mNetworkStatsPropagationTimer = null;

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


    protected void onClientRequestRegister(Messenger clientMessenger)
    {
        if (clientMessenger != null)
        {
            mLog.debug("on clientmessenger UNregister");
        } else
        {
            mLog.debug("on clientmessenger register");
        }

        mClientMessenger = clientMessenger;

        if (mClientMessenger != null)
        {
            sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE);
            mNetworkStatsPropagationTimer = new NetworkTrafficPropagationTimer(
                    60 * 60, 10, this);
            mNetworkStatsPropagationTimer.start();
        } else
        {
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
            mWifiState = new WifiIpAddress(getApplicationContext());
            if ((AppEnvironment.isRunningOnEmulator() == false)
                    && (mWifiState.isWifiEnabled() == false || mWifiState
                            .isWifiAPEnabled() == false))
            {
                onWirelessNetworkNotAvailable();
            } else if ((getRunningState() == ServiceRunningStates.BEFORE_SINGULARITY || getRunningState() == ServiceRunningStates.STOPPED))
            {
                setRunningState(ServiceRunningStates.STARTING);
                registerIntentReceiver();

                try
                {
                    mServer = new SimpleWebServer(this, mServerConfig);
                    mServer.registerSmsIoCallback(this);
                    // getApplicationContext().removeStickyBroadcast(
                    // mServiceStartedStickyIntend);

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
            mWifiState.onClose();
            mWifiState = null;
        }
        if (mNetworkStatsPropagationTimer != null)
        {
            mNetworkStatsPropagationTimer.cancel();
            mNetworkStatsPropagationTimer = null;
        }
        mServer = null;
        mServerConfig = null;
        mIntentReceiver = null;
        mClientMessenger = null;
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
        onClientRequestCurrentRunningState();
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
                mServer.onClose();
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


    protected void onClientRequestCurrentRunningState()
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE,
                translateRunningStateToInt(getRunningState()));
    }


    private void sendMessageToClient(int what)
    {
        sendMessageToClient(what, 0);
    }


    private void sendMessageToClient(int what, int arg1)
    {

        String messageName = ServiceConnectionMessageTypes.getMessageName(what);
        String messageValue = ServiceConnectionMessageTypes
                .getMessageName(arg1);
        if (messageValue == null)
        {
            messageValue = Integer.toString(arg1);
        }

        if (mClientMessenger != null)
        {
            try
            {
                Message message = Message.obtain(null, what, arg1, 0);
                appendDataToMessage(message, what);
                mClientMessenger.send(message);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending to client [" + messageName + "]", e);
            }
        } else
        {
            mLog.debug("failed sending [" + messageName + "=" + messageValue
                    + " to not registered client");
        }
    }


    private void appendDataToMessage(Message message, int messageId)
    {
        Bundle bundle = new Bundle();
        switch (messageId)
        {
        case ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_CONNECTION_URL,
                    formatConnectionUrl());
            message.setData(bundle);
            break;

        case ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD,
                    mServer.getMaskedHttpPassword());
            message.setData(bundle);
            break;
        case ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME,
                    mServer.getHttpUsername());
            message.setData(bundle);
            break;
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


    protected void onClientRequestConnectionUrl()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL);
    }


    protected void onClientRequestRepublishStates()
    {
        try
        {
            sendMessageToClient(
                    ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE,
                    translateRunningStateToInt(getRunningState()));

            onClientRequestConnectionUrl();
            onClientRequestHttpPassword();
            onClientRequestHttpUsername();
            onClientRequestIsHttpAccessRestrictionEnabled();
        }
        catch (NullPointerException npe)
        {
            mLog.debug("failed sending connection url, server not ready yet");
        }
    }


    protected void onWirelessNetworkNotAvailable()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.NETWORK_NOT_AVAILABLE);
    }


    protected void onClientRequestStopWEBService()
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


    protected void onClientRequestSentBytesCount(int txBytes)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_TX_BYTES,
                txBytes);
    }


    protected void onClientRequestReceivedBytesCount(int rxBytes)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_RX_BYTES,
                rxBytes);
    }


    protected void onClientRequestStartWEBService()
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


    protected void onClientRequestHttpPassword()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD);
    }


    protected void onClientRequestHttpUsername()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME);
    }


    protected void onClientRequestIsHttpAccessRestrictionEnabled()
    {
        int isRestrictionEnabled = 0;

        if (mServer.isHttpAccessRestrictionEnabled())
        {
            isRestrictionEnabled = 1;
        }

        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.HTTP_ACCESS_RESCRICTION_ENABLED,
                isRestrictionEnabled);
    }


    protected void onClientResponseServerSettingsChanged(
            WebserverProtocolConfig newConfig)
    {
        mServerConfig = newConfig;
    }


    private int translateRunningStateToInt(ServiceRunningStates state)
    {
        switch (state)
        {
        case BEFORE_SINGULARITY:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_BEFORE_SINGULARITY;
        case STARTED_ERRONEOUS:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTED_ERRONEOUS;
        case RUNNING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_RUNNING;
        case STARTING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTING;
        case STOPPED:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED;
        case STOPPED_ERRONEOUS:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED_ERRONEOUS;
        case STOPPING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPING;
        default:
            return -1;
        }
    }


    @Override
    public synchronized void smsSentCallback(Context context,
            List<TextMessage> messages)
    {
        mSmsSentCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_SENT,
                mSmsSentCount);
    }


    @Override
    public synchronized void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        mSmsSentErroneousCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_SENT_ERRONEOUS,
                mSmsSentErroneousCount);
    }


    @Override
    public synchronized void smsDeliveredCallback(Context context,
            List<TextMessage> messagea)
    {
        mSmsDeliveredCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_DELIVERED,
                mSmsDeliveredCount);
    }


    @Override
    public synchronized void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        mSmsReceivedCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_RECEIVED,
                mSmsReceivedCount);
    }
}
