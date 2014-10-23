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

package at.tugraz.ist.akm.webservice.requestprocessor.interceptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import my.org.apache.http.Header;
import my.org.apache.http.HeaderElement;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.protocol.HttpContext;
import android.content.Context;
import android.util.Base64;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.server.IHttpAccessCallback;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class AuthorizationInterceptor extends AbstractRequestInterceptor
{
    private final LogClient mLog = new LogClient(this);
    private final static String mDefaultEncoding = "UTF8";
    private HttpClientBackLog mBackLog = new HttpClientBackLog();


    public AuthorizationInterceptor(WebserverProtocolConfig config,
            Context context, IHttpAccessCallback authCallback)
    {
        super(config, context, authCallback);
    }


    @Override
    public void close() throws IOException
    {
        super.close();
    }


    private void tryCallback(boolean authSucceeded, String username)
    {
        if (mAuthCallback != null)
        {
            if (authSucceeded)
            {
                mAuthCallback.onLoginSuccess(username);
            } else
            {
                mAuthCallback.onLogFailed(username);
            }
        }
    }


    @Override
    synchronized public boolean process(HttpRequest httpRequest,
            String requestData, HttpResponse httpResponse,
            HttpContext httpContext)
    {
        boolean authSuccess = false;
        String userName = "";

        if (mServerConfig.isUserAuthEnabled == false)
        {
            httpResponse.setStatusCode(WebServerConstants.HTTP.HTTP_CODE_OK);
            authSuccess = true;
        } else
        {
            Header header = httpRequest
                    .getFirstHeader(WebServerConstants.HTTP.HEADER_AUTHENTICATION);

            if (header != null)
            {
                mLog.debug("credential header: " + headerToString(header));

                StringBuffer requestUserName = new StringBuffer();
                StringBuffer requestPassword = new StringBuffer();
                extractCredentialsFromRequestHeader(header, requestUserName,
                        requestPassword);
                userName = requestUserName.toString();

                if (userName.compareTo(mServerConfig.username) == 0
                        && requestPassword.toString().compareTo(
                                mServerConfig.password) == 0)
                {
                    authSuccess = true;
                    httpResponse
                            .setStatusCode(WebServerConstants.HTTP.HTTP_CODE_OK);
                }
            }

            if (authSuccess == false)
            {
                appendAuthFailedResponseData(httpResponse);
            }
        }

        boolean isPseudoAuthExpired = mBackLog.isAuthExpired(httpRequest,
                httpContext);

        if (authSuccess)
        {
            mBackLog.memorizeClient(httpRequest, httpContext);
            if (isPseudoAuthExpired)
            {
                tryCallback(true, userName);
            }
        } else
        {
            mBackLog.forgetClient(httpRequest, httpContext);
            tryCallback(false, userName);
        }
        return authSuccess;
    }


    private void appendAuthFailedResponseData(HttpResponse httpResponse)
    {
        mLog.info("require authentication");
        httpResponse
                .setStatusCode(WebServerConstants.HTTP.HTTP_CODE_UNAUTHORIZED);
        httpResponse.setHeader(WebServerConstants.HTTP.HEADER_WWW_AUTHENTICATE,
                String.format("Basic realm=\"%s\"",
                        WebServerConstants.HTTP.AUTHENTICATION_REALM));

        // error page
        FileReader filereader = new FileReader(mContext,
                WebServerConstants.RES.UNAUTHORIZED);
        responseDataAppender.appendHttpResponseData(httpResponse,
                WebServerConstants.HTTP.CONTENTY_TYPE_TEXT_HTML,
                filereader.read());
        try
        {
            filereader.close();
        }
        catch (Throwable e)
        {
        }
        filereader = null;
    }


    private String getUsernameSubstring(String authenticationCode)
    {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0)
        {
            return authenticationCode.substring(0, idx);
        }
        return "";
    }


    private String getPasswordSubstring(String authenticationCode)
    {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0)
        {
            return authenticationCode.substring(idx + 1);
        }
        return "";
    }


    private void extractCredentialsFromRequestHeader(Header header,
            StringBuffer requestUserName, StringBuffer requestPassword)
    {
        String headerValue = header.getValue();
        int idx = headerValue.indexOf(" ");
        if (idx >= 0)
        {
            String userCredentials;
            try
            {
                userCredentials = new String(Base64.decode(
                        headerValue.substring(idx + 1), 0), mDefaultEncoding);
                requestUserName.append(getUsernameSubstring(userCredentials));
                requestPassword.append(getPasswordSubstring(userCredentials));
            }
            catch (UnsupportedEncodingException e)
            {
                mLog.error("failed to extract string from header: "
                        + e.getMessage());
            }
        }
    }


    private String headerToString(Header header)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("h[" + header.getName() + "]->[" + header.getValue() + "]");

        int heidx = 0;
        for (HeaderElement he : header.getElements())
        {

            for (int i = 0; i < he.getParameterCount(); i++)
            {
                sb.append("he" + (heidx++) + "(" + he.getParameter(i).getName()
                        + ")->(" + he.getParameter(i).getValue() + ")");
            }
        }
        return sb.toString();
    }


    @SuppressWarnings("unused")
    private String headerToString(Header[] headers)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        for (Header h : headers)
        {
            sb.append(headerToString(h));
        }
        sb.append("}");
        return sb.toString();
    }
}
