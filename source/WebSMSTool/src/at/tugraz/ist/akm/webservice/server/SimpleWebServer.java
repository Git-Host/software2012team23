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

package at.tugraz.ist.akm.webservice.server;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import my.org.apache.http.protocol.BasicHttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.networkInterface.WifiIpAddress;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.sms.ISmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.requestprocessor.AbstractHttpRequestProcessor;
import at.tugraz.ist.akm.webservice.requestprocessor.JsonAPIRequestProcessor;
import at.tugraz.ist.akm.webservice.requestprocessor.TestfileRequestProcessor;
import at.tugraz.ist.akm.webservice.requestprocessor.interceptor.IRequestInterceptor;

public class SimpleWebServer implements ISmsIOCallback, Closeable
{
    private LogClient mLog = new LogClient(SimpleWebServer.class.getName());
    private HttpRequestHandlerRegistry mRegistry = new HttpRequestHandlerRegistry();
    private BasicHttpContext mHttpContext = new BasicHttpContext();
    private Context mContext = null;
    private ServerThread mServerThread = null;
    private Vector<AbstractHttpRequestProcessor> mHandlerReferenceListing = new Vector<AbstractHttpRequestProcessor>();
    private InetAddress mSocketAddress = null;
    private ServerSocket mServerSocket = null;
    private String mKeyStorePass = null;
    private WebserverProtocolConfig mServerConfig = null;
    private SSLContext mSSLContext = null;
    private boolean mIsServerRunning = false;
    private WakeLock mWakeLock = null;
    private ISmsIOCallback mExternalSmsIoCallback = null;
    private IHttpAccessCallback mHttpAuthCallback = null;


    public synchronized void registerSmsIoCallback(ISmsIOCallback callback)
    {
        mExternalSmsIoCallback = callback;
    }


    public synchronized void unregisterSMSIoCallback()
    {
        mExternalSmsIoCallback = null;
    }


    public SimpleWebServer(Context context,
            WebserverProtocolConfig serverConfig, IHttpAccessCallback callback)
            throws Exception
    {
        mHttpAuthCallback = callback;
        mContext = context;
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);

        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        WifiIpAddress wifiAddressReader = new WifiIpAddress(context);

        mSocketAddress = InetAddress.getByName(wifiAddressReader
                .readLocalIpAddress());
        wifiAddressReader.close();
        wifiAddressReader = null;

        setNewServerConfiguration(serverConfig);

        mLog.debug("building server for [" + mServerConfig.protocolName + "://"
                + mSocketAddress + ":" + mServerConfig.port + "]");
        readRequestHandlers();
        readRequestInterceptors();
    }


    protected BasicHttpContext getHttpContext()
    {
        return mHttpContext;
    }


    private void readRequestHandlers()
    {
        XmlReader reader = new XmlReader(mContext,
                WebServerConstants.RES.WEB_XML);
        List<XmlNode> nodes = reader
                .getNodes(WebServerConstants.XML.TAG_REQUEST_HANDLER);
        for (XmlNode node : nodes)
        {
            String className = node
                    .getAttributeValue(WebServerConstants.XML.ATTRIBUTE_CLASS);

            if (className == null)
            {
                mLog.error("request handler [" + node.getName()
                        + "] no corresponding class to load found");
                continue;
            }

            try
            {

                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(Context.class,
                        XmlNode.class, HttpRequestHandlerRegistry.class);

                AbstractHttpRequestProcessor newHandler = null;

                if (clazz == TestfileRequestProcessor.class
                        && AppEnvironment.isDebuggable() == false)
                {
                    mLog.debug("ignoring web ui test file handler");
                    continue;
                }

                if (className.equals(JsonAPIRequestProcessor.class
                        .getCanonicalName()))
                {
                    mLog.debug("registered to sms callback");
                    JsonAPIRequestProcessor jsonRequesProcessor = (JsonAPIRequestProcessor) constr
                            .newInstance(mContext, node, mRegistry);
                    newHandler = jsonRequesProcessor;
                    jsonRequesProcessor.registerSMSIoListener(this);
                } else
                {
                    newHandler = (AbstractHttpRequestProcessor) constr
                            .newInstance(mContext, node, mRegistry);
                }

                mHandlerReferenceListing.add(newHandler);
            }
            catch (Exception ex)
            {
                mLog.error("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.debug("request handlers read from configuration");
    }


    private void readRequestInterceptors()
    {
        XmlReader reader = new XmlReader(mContext,
                WebServerConstants.RES.WEB_XML);
        List<XmlNode> interceptorNodes = reader
                .getNodes(WebServerConstants.XML.TAG_REQUEST_INTERCEPTORS);
        if (interceptorNodes.size() == 0)
        {
            mLog.warning("no request interceptors configured");
            return;
        }
        List<XmlNode> nodes = interceptorNodes.get(0).getChildNodes(
                WebServerConstants.XML.TAG_INTERCEPTOR);
        for (XmlNode node : nodes)
        {
            String className = node
                    .getAttributeValue(WebServerConstants.XML.ATTRIBUTE_CLASS);

            if (className == null)
            {
                mLog.error("request interceptor [" + node.getName()
                        + "], no corresponding class to load");
                continue;
            }
            try
            {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(
                        WebserverProtocolConfig.class, Context.class,
                        IHttpAccessCallback.class);
                IRequestInterceptor interceptor = (IRequestInterceptor) constr
                        .newInstance(mServerConfig, mContext, mHttpAuthCallback);
                setInterceptor(interceptor);
            }
            catch (Exception ex)
            {
                mLog.error("loading class [" + className + "] failed", ex);
                stopServer();
            }
        }
        mLog.debug("request interceptors read from configuration");
    }


    protected void setInterceptor(IRequestInterceptor reqInterceptor)
    {
        for (AbstractHttpRequestProcessor reqHandler : mHandlerReferenceListing)
        {
            reqHandler.addRequestInterceptor(reqInterceptor);
        }
    }


    public boolean isRunning()
    {
        return mIsServerRunning;
    }


    public synchronized boolean startServer()
    {
        if (false == mWakeLock.isHeld())
        {
            mWakeLock.acquire();
        }
        if (this.isRunning())
        {
            mLog.info("web service is already running at port ["
                    + mServerThread.getPort() + "]");
            return true;
        }

        try
        {
            if (mServerConfig.isHttpsEnabled)
            {
                initSSLContext();
                final SSLServerSocketFactory sslServerSocketFactory = mSSLContext
                        .getServerSocketFactory();
                mServerSocket = sslServerSocketFactory.createServerSocket(
                        mServerConfig.port, 0, mSocketAddress);
            } else
            {
                mServerSocket = new ServerSocket(mServerConfig.port, 0,
                        mSocketAddress);
            }
            statusbarIndicateConnectionUrl();
            mServerSocket.setReuseAddress(true);
            mServerSocket.setSoTimeout(2000);
            mIsServerRunning = true;
            mServerThread = new ServerThread(mServerSocket, mHttpContext,
                    mRegistry);
            mServerThread.setDaemon(true);
            mServerThread.start();
            mLog.info("server started at port [" + mServerConfig.port + "]");

            return true;
        }
        catch (IOException ioException)
        {
            mLog.error("cannot bind [" + mServerConfig.protocolName
                    + "] socket to [" + mSocketAddress + ":"
                    + mServerConfig.port + "]", ioException);
            return false;
        }

    }


    public void setNewServerConfiguration(WebserverProtocolConfig serverConfig)
    {
        mServerConfig = new WebserverProtocolConfig(serverConfig);
        SharedPreferencesProvider configProvider = new SharedPreferencesProvider(
                mContext);
        mKeyStorePass = configProvider.getKeyStorePassword();
        try
        {
            configProvider.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing config provider");
        }
    }


    public synchronized void stopServer()
    {
        if (mServerThread != null)
        {
            mServerThread.stopThread();
            while (mServerThread.isRunning())
            {
                try
                {
                    this.wait(200);
                }
                catch (InterruptedException interruptedException)
                {
                    ;
                }
            }
            try
            {
                mServerSocket.close();
            }
            catch (IOException e)
            {
                mLog.error("error closing socket", e);
            }
            finally
            {
                mIsServerRunning = false;
                statusbarClearConnectionUrl();
                mWakeLock.release();
            }
        }

        closeRegistry();
        mServerThread = null;
    }


    private void closeRegistry()
    {
        for (AbstractHttpRequestProcessor toBeCleanedUp : mHandlerReferenceListing)
        {
            try
            {
                toBeCleanedUp.close();
            }
            catch (Throwable e)
            {
                mLog.error("failed closing request processor");
            }
        }
    }


    @Override
    public void close() throws IOException
    {
        mRegistry = null;
        mHttpContext = null;
        mContext = null;
        mHandlerReferenceListing = null;
        mSocketAddress = null;
        mServerSocket = null;
        mKeyStorePass = null;
        mServerConfig = null;
        mSSLContext = null;
        mWakeLock = null;
        mExternalSmsIoCallback = null;
        mHttpAuthCallback = null;
        mLog = null;
    }


    private void initSSLContext()
    {
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        try
        {
            mSSLContext = SSLContext.getInstance("TLS");

            String keystoreFilePath = mContext.getFilesDir().getPath()
                    .toString()
                    + "/"
                    + mContext.getResources().getString(
                            R.string.preferences_keystore_store_filename);
            appKeystore.loadKeystore(mKeyStorePass, keystoreFilePath);

            mSSLContext.init(appKeystore.getKeystoreManagers(), null, null);
        }
        catch (NoSuchAlgorithmException algoException)
        {
            mLog.error("Wrong keystore algorithm!", algoException);
        }
        catch (KeyManagementException keyException)
        {
            mLog.error("Error while getting keymanagers!", keyException);
        }
        finally
        {
            try
            {
                appKeystore.close();
            }
            catch (Throwable e)
            {
                mLog.error("failed closing application keystore");
            }
        }
    }


    public synchronized String getServerAddress()
    {
        return mSocketAddress.getHostAddress();
    }


    public synchronized int getServerPort()
    {
        return mServerConfig.port;
    }


    private void statusbarIndicateConnectionUrl()
    {
        FireNotification notificator = new FireNotification(mContext);
        FireNotification.NotificationInfo info = new FireNotification.NotificationInfo();
        StringBuffer connectionUrl = new StringBuffer();

        connectionUrl.append(mServerConfig.protocolName + "://");

        info.text = connectionUrl.append(
                mSocketAddress.getHostAddress() + ":" + mServerConfig.port)
                .toString();
        info.title = "WebSMSTool";
        info.tickerText = "service running";
        notificator.fireStickyInfos(info);
        try
        {
            notificator.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing status bar notificator");
        }
    }


    private void statusbarClearConnectionUrl()
    {
        FireNotification notificator = new FireNotification(mContext);
        notificator.cancelAll();
        try
        {
            notificator.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing status bar notificator");
        }
    }


    @Override
    public synchronized void smsSentCallback(Context context,
            List<TextMessage> messages)
    {
        if (null != mExternalSmsIoCallback)
        {
            mExternalSmsIoCallback.smsSentCallback(context, messages);
        }
    }


    @Override
    public synchronized void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        if (null != mExternalSmsIoCallback)
        {
            mExternalSmsIoCallback.smsSentErrorCallback(context, messages);
        }
    }


    @Override
    public synchronized void smsDeliveredCallback(Context context,
            List<TextMessage> messagea)
    {
        if (null != mExternalSmsIoCallback)
        {
            mExternalSmsIoCallback.smsDeliveredCallback(context, messagea);
        }
    }


    @Override
    public synchronized void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        if (null != mExternalSmsIoCallback)
        {
            mExternalSmsIoCallback.smsReceivedCallback(context, messages);
        }
    }


    public long getReceivedBytesCount()
    {
        if (mServerThread == null)
        {
            return 0;
        }
        return mServerThread.getReceivedBytesCount();
    }


    public long getSentBytesCount()
    {
        if (mServerThread == null)
        {
            return 0;
        }
        return mServerThread.getSentBytesCount();
    }


    public String getMaskedHttpPassword()
    {
        StringBuffer maskedSecret = new StringBuffer();
        for (int i = 0; i < mServerConfig.password.length(); i++)
        {
            maskedSecret.append("*");
        }
        return maskedSecret.toString();
    }


    public String getHttpUsername()
    {
        return mServerConfig.username;
    }


    public boolean isHttpAccessRestrictionEnabled()
    {
        return (mServerConfig.isUserAuthEnabled);
    }


    public String getServerProtocol()
    {
        return mServerConfig.protocolName;
    }
}
