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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLException;

import my.org.apache.http.ConnectionClosedException;
import my.org.apache.http.HttpConnectionMetrics;
import my.org.apache.http.HttpResponseInterceptor;
import my.org.apache.http.HttpVersion;
import my.org.apache.http.impl.DefaultConnectionReuseStrategy;
import my.org.apache.http.impl.DefaultHttpResponseFactory;
import my.org.apache.http.impl.DefaultHttpServerConnection;
import my.org.apache.http.params.BasicHttpParams;
import my.org.apache.http.params.CoreConnectionPNames;
import my.org.apache.http.params.CoreProtocolPNames;
import my.org.apache.http.params.HttpParams;
import my.org.apache.http.params.HttpProtocolParams;
import my.org.apache.http.params.SyncBasicHttpParams;
import my.org.apache.http.protocol.BasicHttpContext;
import my.org.apache.http.protocol.HTTP;
import my.org.apache.http.protocol.HttpProcessor;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import my.org.apache.http.protocol.HttpService;
import my.org.apache.http.protocol.ImmutableHttpProcessor;
import my.org.apache.http.protocol.ResponseConnControl;
import my.org.apache.http.protocol.ResponseContent;
import my.org.apache.http.protocol.ResponseDate;
import my.org.apache.http.protocol.ResponseServer;
import at.tugraz.ist.akm.trace.LogClient;

public class ServerThread extends Thread
{
    private final static LogClient mLog = new LogClient(
            ServerThread.class.getName());

    private final ServerSocket mServerSocket;
    private final BasicHttpContext mHttpContext;
    private final HttpRequestHandlerRegistry mRequestHandlerRegistry;

    private boolean mRunning = false;
    private boolean mStopServerThread = false;

    private final RequestThreadPool mThreadPool;

    private long mSentBytesCount = 0;
    private long mReceivedBytesCount = 0;


    @SuppressWarnings("unused")
    private ServerThread()
    {
        mThreadPool = null;
        mServerSocket = null;
        mRequestHandlerRegistry = null;
        mHttpContext = null;
    }


    public ServerThread(final ServerSocket serverSocket,
            final BasicHttpContext httpContext,
            final HttpRequestHandlerRegistry requestHandlerRegistry)
    {
        this.setName(ServerThread.class.getCanonicalName());
        this.mServerSocket = serverSocket;
        mHttpContext = httpContext;
        mRequestHandlerRegistry = requestHandlerRegistry;

        this.mThreadPool = new RequestThreadPool();
    }


    @Override
    public void run()
    {
        mRunning = true;
        while (mRunning)
        {
            Socket socket = null;
            try
            {
                socket = mServerSocket != null ? mServerSocket.accept() : null;
            }
            catch (IOException ioException)
            {
                // OK: no need to write trace
            }

            if (mStopServerThread)
            {
                break;
            }
            if (socket != null)
            {
                mLog.debug("connection request from ip <"
                        + socket.getInetAddress() + "> on port <"
                        + socket.getPort() + ">");

                final Socket finalSocketReference = socket;
                try
                {
                    mThreadPool.executeTask(new Runnable() {
                        @Override
                        public void run()
                        {
                            DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
                            try
                            {
                                HttpParams params = new BasicHttpParams();
                                HttpProtocolParams.setVersion(params,
                                        HttpVersion.HTTP_1_1);
                                HttpProtocolParams.setContentCharset(params,
                                        HTTP.UTF_8);

                                serverConn.bind(finalSocketReference, params);
                                HttpService httpService = initializeHTTPService();
                                httpService.handleRequest(serverConn,
                                        mHttpContext);

                                synchronized (ServerThread.this)
                                {
                                    HttpConnectionMetrics connMetrics = serverConn
                                            .getMetrics();
                                    mSentBytesCount += connMetrics
                                            .getSentBytesCount();
                                    mReceivedBytesCount += connMetrics
                                            .getReceivedBytesCount();
                                }
                            }
                            catch (SSLException iDon_tCare)
                            {
                                ; // some browser send connection closed, some
                                  // not
                                  // ...
                                mLog.info("ignore SSL-connection closed by peer");
                            }
                            catch (ConnectionClosedException iDon_tCare)
                            {
                                mLog.info("ignore connection closed by peer");
                            }
                            catch (Exception ex)
                            {
                                mLog.error(
                                        "Exception caught while processing HTTP client connection",
                                        ex);
                            }
                        }
                    });
                }
                catch (RejectedExecutionException reason)
                {
                    mLog.error(
                            "request execution rejected because pool works at its limit",
                            reason);
                }
            }
        }

        mRunning = false;
        mLog.info("Webserver stopped");
    }


    public void stopThread()
    {
        mThreadPool.shutdown();
        mStopServerThread = true;
        try
        {
            mServerSocket.close();
        }
        catch (IOException exp)
        {
            mLog.warning(
                    "Could not close server socket on stopping server thread",
                    exp);
        }
    }


    public boolean isRunning()
    {
        return mRunning;
    }


    public int getPort()
    {
        return mServerSocket.getLocalPort();
    }


    protected synchronized HttpService initializeHTTPService()
    {
        HttpProcessor httpProcessor = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[] { new ResponseDate(),
                        new ResponseServer(), new ResponseContent(),
                        new ResponseConnControl() });

        HttpParams params = new SyncBasicHttpParams()
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
                        8 * 1024)
                .setBooleanParameter(
                        CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER,
                        "HttpComponents/1.1");

        HttpService httpService = new HttpService(httpProcessor,
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(), mRequestHandlerRegistry,
                params);

        return httpService;
    }


    public synchronized long getSentBytesCount()
    {
        return mSentBytesCount;
    }


    public synchronized long getReceivedBytesCount()
    {
        return mReceivedBytesCount;
    }
}
