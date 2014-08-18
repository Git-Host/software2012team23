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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.requestprocessor.AbstractHttpRequestProcessor;
import at.tugraz.ist.akm.webservice.requestprocessor.interceptor.IRequestInterceptor;

public class SimpleWebServer
{
    private final static LogClient mLog = new LogClient(
            SimpleWebServer.class.getName());

    HttpRequestHandlerRegistry mRegistry = new HttpRequestHandlerRegistry();
    private BasicHttpContext mHttpContext = new BasicHttpContext();

    private final Context mContext;
    private ServerThread mServerThread = null;
    private Vector<AbstractHttpRequestProcessor> mHandlerReferenceListing = new Vector<AbstractHttpRequestProcessor>();

    private SharedPreferencesProvider mConfig = null;
    private InetAddress mSocketAddress = null;
    private ServerSocket mServerSocket = null;
    private boolean mHttps;
    private int mServerPort;
    private String mKeyStorePass;

    private SSLContext mSSLContext;

    private boolean mIsServerRunning = false;

    private WakeLock mWakeLock = null;


    public SimpleWebServer(Context context, String socketAddress)
            throws Exception
    {
        this.mContext = context;
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);

        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        this.mSocketAddress = InetAddress.getByName(socketAddress);
        openSettings();
        readRequestHandlers();
        readRequestInterceptors();
    }


    protected BasicHttpContext getHttpContext()
    {
        return mHttpContext;
    }


    private void readRequestHandlers()
    {
        XmlReader reader = new XmlReader(mContext, WebServerConfig.RES.WEB_XML);
        List<XmlNode> nodes = reader
                .getNodes(WebServerConfig.XML.TAG_REQUEST_HANDLER);
        for (XmlNode node : nodes)
        {
            String className = node
                    .getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CLASS);

            if (className == null)
            {
                mLog.error("request handler <" + node.getName()
                        + ">: no corresponding class to load found");
                continue;
            }
            try
            {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(Context.class,
                        XmlNode.class, HttpRequestHandlerRegistry.class);
                AbstractHttpRequestProcessor newHandler = (AbstractHttpRequestProcessor) constr
                        .newInstance(mContext, node, mRegistry);
                mHandlerReferenceListing.add(newHandler);
            }
            catch (Exception ex)
            {
                mLog.error("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.info("request handlers read from configuration");
    }


    private void readRequestInterceptors()
    {
        XmlReader reader = new XmlReader(mContext, WebServerConfig.RES.WEB_XML);
        List<XmlNode> interceptorNodes = reader
                .getNodes(WebServerConfig.XML.TAG_REQUEST_INTERCEPTORS);
        if (interceptorNodes.size() == 0)
        {
            mLog.warning("no request interceptors configured");
            return;
        }
        List<XmlNode> nodes = interceptorNodes.get(0).getChildNodes(
                WebServerConfig.XML.TAG_INTERCEPTOR);
        for (XmlNode node : nodes)
        {
            String className = node
                    .getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CLASS);

            if (className == null)
            {
                mLog.error("request interceptor <" + node.getName()
                        + ">: no corresponding class to load found");
                continue;
            }
            try
            {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constr = clazz.getConstructor(Context.class);
                IRequestInterceptor interceptor = (IRequestInterceptor) constr
                        .newInstance(mContext);
                setInterceptor(interceptor);
            }
            catch (Exception ex)
            {
                mLog.error("Loading of class <" + className + "> failed", ex);
                stopServer();
            }
        }
        mLog.info("request interceptors read from configuration");
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
        // return mServerThread != null;
    }


    public synchronized boolean startServer()
    {
        openSettings();
        if (false == mWakeLock.isHeld())
        {
            mWakeLock.acquire();
        }

        if (this.isRunning())
        {
            mLog.info("Web service is already running at port <"
                    + mServerThread.getPort() + ">");
            return true;
        }

        readWebServerConfiguration();
        String socketType = "https";

        try
        {
            if (mHttps)
            {
                initSSLContext();
                final SSLServerSocketFactory sslServerSocketFactory = mSSLContext
                        .getServerSocketFactory();
                mServerSocket = sslServerSocketFactory.createServerSocket(
                        mServerPort, 0, mSocketAddress);
            } else
            {
                mServerSocket = new ServerSocket(mServerPort, 0, mSocketAddress);
                socketType = "http";
            }
            statusbarIndicateConnectionUrl();
            mServerSocket.setReuseAddress(true);
            mServerSocket.setSoTimeout(2000);
            mIsServerRunning = true;
            mServerThread = new ServerThread(mServerSocket, mHttpContext,
                    mRegistry);
            mServerThread.setDaemon(true);
            mServerThread.start();
            mLog.info("WebServer started on port: " + mServerPort);

            return true;
        }
        catch (IOException ioException)
        {
            mLog.error("cannot bind <" + socketType + "> socket to <"
                    + mSocketAddress + ":" + mServerPort + ">", ioException);
            return false;
        }

    }


    private void readWebServerConfiguration()
    {

        if (mConfig.getProtocol().compareTo("https") == 0)
        {
            mHttps = true;
        } else
        {
            mHttps = false;
        }

        mServerPort = Integer.parseInt(mConfig.getPort());
        mKeyStorePass = mConfig.getKeyStorePassword();
    }


    public synchronized void stopServer()
    {
        mLog.info("stop web server");
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
                // don't care
            }
            finally
            {
                mIsServerRunning = false;
                statusbarClearConnectionUrl();
                mWakeLock.release();
            }
        }

        closeRegistry();
        closeSettings();
        mServerThread = null;

    }


    private void closeSettings()
    {
        if (mConfig != null)
            ;
        mConfig.close();
        mConfig = null;
    }


    private void openSettings()
    {
        if (mConfig == null)
            ;
        mConfig = new SharedPreferencesProvider(mContext);
    }


    private void closeRegistry()
    {
        for (AbstractHttpRequestProcessor toBeCleanedUp : mHandlerReferenceListing)
        {
            toBeCleanedUp.onClose();
        }
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

            mSSLContext.init(appKeystore.getKeystoreManagers(), null,
                    new SecureRandom());
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
            appKeystore.close();
        }
    }


    public synchronized int getServerPort()
    {
        if (mServerThread != null && mServerThread.isRunning())
        {
            return mServerPort;
        } else
        {
            return -1;
        }
    }


    private void statusbarIndicateConnectionUrl()
    {
        FireNotification notificator = new FireNotification(mContext);
        FireNotification.NotificationInfo info = new FireNotification.NotificationInfo();
        StringBuffer connectionUrl = new StringBuffer();

        connectionUrl.append(mConfig.getProtocol() + "://");

        info.text = connectionUrl.append(
                mSocketAddress.getHostAddress() + ":" + mConfig.getPort())
                .toString();
        info.title = "WebSMSTool";
        info.tickerText = "service running";
        notificator.fireStickyInfos(info);
    }


    private void statusbarClearConnectionUrl()
    {
        new FireNotification(mContext).cancelAll();
    }

}
