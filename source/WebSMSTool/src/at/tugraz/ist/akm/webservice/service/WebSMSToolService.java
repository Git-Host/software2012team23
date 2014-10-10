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
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import at.tugraz.ist.akm.secureRandom.PRNGFixes;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class WebSMSToolService extends Service implements SmsIOCallback
{

    public static final String SERVICE_STARTED = "at.tugraz.ist.akm.sms.SERVICE_STARTED";

    private Intent mServiceStartedStickyIntend = null;
    private final LogClient mLog = new LogClient(this);
    private SimpleWebServer mServer = null;
    private WebserverProtocolConfig mServerConfig = null;
    private static BroadcastReceiver mIntentReceiver = null;
    private Messenger mClientMessenger = null;
    private final Messenger mServiceMessenger = new Messenger(
            new IncomingClientMessageHandler(this));

    private ServiceRunningStates mServiceRunningState = ServiceRunningStates.BEFORE_SINGULARITY;

    private int mSmsSentCount = 0;
    private int mSmsReceivedCount = 0;
    private int mSmsSentErroneousCount = 0;
    private int mSmsDeliveredCount = 0;

    public enum ServiceRunningStates {
        STOPPED, STARTING, STARTED_ERRONEOUS, RUNNING, STOPPING, STOPPED_ERRONEOUS, BEFORE_SINGULARITY;
        private ServiceRunningStates()
        {
        }
    }

    private CountDownTimer mNetworkStatsPropagationTimer = new CountDownTimer(
            (60 * 1000) * 60, 10 * 1000) {
        private int mLastTxBytesPropagated = 0;
        private int mLastRxBytesPropagated = 0;
        private int mPropagationBytesDelta = 2 * (2 ^ 10);


        @Override
        public void onTick(long millisUntilFinished)
        {
            if (mServer == null)
            {
                return;
            }

            int currentTxByteStatus = (int) (mServer.getSentBytesCount());

            if ((mLastTxBytesPropagated <= 0)
                    || (mLastTxBytesPropagated + mPropagationBytesDelta) < currentTxByteStatus)
            {
                mLastTxBytesPropagated = currentTxByteStatus;
                WebSMSToolService.this
                        .sendMessageToClient(
                                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_TX_BYTES,
                                mLastTxBytesPropagated, null);
            }

            int currentRxByteStatus = (int) (mServer.getReceivedBytesCount());
            if ((mLastRxBytesPropagated <= 0)
                    || (mLastRxBytesPropagated + mPropagationBytesDelta) < currentRxByteStatus)
            {
                mLastRxBytesPropagated = currentRxByteStatus;
                WebSMSToolService.this
                        .sendMessageToClient(
                                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_RX_BYTES,
                                mLastRxBytesPropagated, null);
            }
        }


        @Override
        public void onFinish()
        {
            this.start();
        }
    };

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
        mClientMessenger = clientMessenger;

        if (mClientMessenger != null)
        {
            sendMessageToClient(
                    ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE,
                    0, null);
            mNetworkStatsPropagationTimer.start();
        } else
        {
            mNetworkStatsPropagationTimer.cancel();
        }
    }


    protected Messenger getClientMessenger()
    {
        return mClientMessenger;
    }


    public WebSMSToolService()
    {
        mServiceStartedStickyIntend = new Intent(SERVICE_STARTED);
        PRNGFixes.apply();
    }

    public class WebSMSToolServiceBinder extends Binder
    {
        WebSMSToolService getService()
        {
            return WebSMSToolService.this;
        }
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
        mLog.debug("server binder created");
        mClientMessenger = null;
        return mServiceMessenger.getBinder();
    }


    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLog.debug("on start command received");
        super.onStartCommand(intent, flags, startId);
        synchronized (this)
        {
            if (getRunningState() == ServiceRunningStates.BEFORE_SINGULARITY
                    || getRunningState() == ServiceRunningStates.STOPPED)
            {
                setRunningState(ServiceRunningStates.STARTING);
                registerIntentReceiver();

                try
                {
                    mServer = new SimpleWebServer(this, mServerConfig);
                    mServer.registerSmsIoCallback(this);
                    getApplicationContext().removeStickyBroadcast(
                            mServiceStartedStickyIntend);

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

            return START_NOT_STICKY;
        }
    }


    @Override
    public boolean stopService(Intent name)
    {
        boolean stopped = stopWebSMSToolService();
        super.stopService(name);
        return stopped;
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
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


    private boolean stopWebSMSToolService()
    {
        setRunningState(ServiceRunningStates.STOPPING);
        synchronized (this)
        {
            unregisterIntentReceiver();
            getApplicationContext().removeStickyBroadcast(
                    mServiceStartedStickyIntend);
            try
            {
                mServer.stopServer();
                waitForServiceBeingStopped();
                if (mServer.isRunning())
                {
                    throw new Exception("server failed to stop");
                }
                setRunningState(ServiceRunningStates.STOPPED);
            }
            catch (Exception ex)
            {
                mLog.error("Error while stopping server!", ex);
                setRunningState(ServiceRunningStates.STOPPED_ERRONEOUS);
            }
        }
        onClientRequestRegister(null);
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
        // TODO stopwerbsmstoolservice() before stopself();
        // check on enabled only?
        mLog.debug("wifi state changed, turning off service");
        stopSelf();
    }


    public void onNetworkStateChanged(Context context, Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo) intent.getExtras().get(
                WifiManager.EXTRA_NETWORK_INFO);
        mLog.debug("network state: " + networkInfo);
        if (0 == NetworkInfo.State.CONNECTED.compareTo(networkInfo.getState()))
        {
            return;
        }
        mLog.debug("network state changed, turning off service");
        stopSelf();
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
                translateRunningStateToInt(getRunningState()), null);
    }


    private void sendMessageToClient(int what, int arg1, String optConnectionUrl)
    {

        String messageName = ServiceConnectionMessageTypes.getMessageName(what);

        mLog.debug("service sending [" + messageName + "=" + arg1 + "] obj ["
                + optConnectionUrl + "] to client");

        if (mClientMessenger != null)
        {
            try
            {
                Message message = Message.obtain(null, what, arg1, 0);
                if (null != optConnectionUrl)
                {
                    Bundle objBundleParameter = new Bundle();
                    objBundleParameter
                            .putString(
                                    ServiceConnectionMessageTypes.Bundle.Key.STRING_CONNECTION_URL,
                                    optConnectionUrl);
                    message.setData(objBundleParameter);
                }
                mClientMessenger.send(message);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending to client [" + messageName + "]", e);
            }
        } else
        {
            mLog.debug("failed sending [" + messageName + "=" + arg1
                    + "] obj [" + optConnectionUrl + "] client not registered");
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
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL,
                0, formatConnectionUrl());
    }


    protected void onClientRequestRepublishStates()
    {
        try
        {
            sendMessageToClient(
                    ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE,
                    translateRunningStateToInt(getRunningState()), null);

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


    protected void onClientRequstStopService()
    {
        if (getRunningState() == ServiceRunningStates.RUNNING)
        {
            stopWebSMSToolService();
            stopSelf();
        } else
        {
            mLog.debug("failed client request for stopping service in state ["
                    + getRunningState() + "]");
        }
    }


    // TODO: wrong!
    protected void onClientRequestHttpPassword()
    {
        // password has to be stored with password_string_type not
        // as kind of url_string_type
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD,
                0, mServer.getMaskedHttpPassword());
    }


    // TODO: wrong!
    protected void onClientRequestHttpUsername()
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME,
                0, mServer.getHttpUsername());
    }


    // TODO: wrong!
    protected void onClientRequestIsHttpAccessRestrictionEnabled()
    {
        int isRestrictionEnabled = 0;

        if (mServer.isHttpAccessRestrictionEnabled())
        {
            isRestrictionEnabled = 1;
        }

        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.HTTP_ACCESS_RESCRICTION_ENABLED,
                isRestrictionEnabled, null);
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
                mSmsSentCount, null);
    }


    @Override
    public synchronized void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        mSmsSentErroneousCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_SENT_ERRONEOUS,
                mSmsSentErroneousCount, null);
    }


    @Override
    public synchronized void smsDeliveredCallback(Context context,
            List<TextMessage> messagea)
    {
        mSmsDeliveredCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_DELIVERED,
                mSmsDeliveredCount, null);
    }


    @Override
    public synchronized void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        mSmsReceivedCount++;
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_RECEIVED,
                mSmsReceivedCount, null);
    }
}
