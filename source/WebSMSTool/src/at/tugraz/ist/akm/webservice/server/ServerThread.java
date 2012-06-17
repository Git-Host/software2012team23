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

import javax.net.ssl.SSLException;

import my.org.apache.http.ConnectionClosedException;
import my.org.apache.http.HttpVersion;
import my.org.apache.http.impl.DefaultHttpServerConnection;
import my.org.apache.http.params.BasicHttpParams;
import my.org.apache.http.params.HttpParams;
import my.org.apache.http.params.HttpProtocolParams;
import my.org.apache.http.protocol.HTTP;
import my.org.apache.http.protocol.HttpService;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebserviceThreadPool;

public class ServerThread extends Thread {
    private final static Logable mLog = new Logable(ServerThread.class.getSimpleName());	
    private final SimpleWebServer mWebServer;
    private final ServerSocket mServerSocket;
    private boolean mRunning = false;
    private boolean mStopServerThread = false;

    private final WebserviceThreadPool mThreadPool;
    
    public ServerThread(final SimpleWebServer webServer, final ServerSocket serverSocket) {
        this.mWebServer = webServer;
        this.mServerSocket = serverSocket;
        this.mThreadPool = new WebserviceThreadPool();
    }

    @Override
    public void run() {
        mRunning = true;
        while (mRunning) {
            Socket socket = null;
            try {
                socket = mServerSocket != null ? mServerSocket.accept() : null;
            } catch (IOException ioException) {
                // OK: no need to write trace
            }

            if (mStopServerThread) {
                break;
            }
            if (socket != null) {
                mLog.logDebug("connection request from ip <" + socket.getInetAddress()
                        + "> on port <" + socket.getPort() + ">");
            
                final Socket tmpSocket = socket;
                mThreadPool.executeTask(new Runnable() {
                    @Override
                    public void run() {
                        DefaultHttpServerConnection serverConn = new DefaultHttpServerConnection();
                        try {
                            HttpParams params = new BasicHttpParams();
                            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                            serverConn.bind(tmpSocket, params);
                            HttpService httpService = mWebServer.initializeHTTPService();
                            httpService.handleRequest(serverConn, mWebServer.getHttpContext());
                        } catch (SSLException iDon_tCare) {
                        	; // some browser send connection closed, some not ...
                        	mLog.logVerbose("ignoring SSL-connection closed by peer");
                        } catch (ConnectionClosedException iDon_tCare) {
                        	; // some browser send connection closed, some not ...
                        	mLog.logVerbose("ignoring SSL-connection closed by peer");
                        } catch (Exception ex) {
                            mLog.logError("Exception caught while processing HTTP client connection", ex);
                        }
                    }
                });
            }
        }

        mRunning = false;
        mLog.logInfo("Webserver stopped");
    }

    public void stopThread() {
        mThreadPool.shutdown();
        mStopServerThread = true;
        try {
			mServerSocket.close();
		} catch (IOException exp) {
			mLog.logWarning("Could not close server socket on stopping server thread", exp);
		}
    }

    public boolean isRunning() {
        return mRunning;
    }

    public int getPort() {
        return mServerSocket.getLocalPort();
    }
}
