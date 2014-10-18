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
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import android.util.Base64;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.server.IHttpAccessCallback;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class AuthorizationInterceptor extends AbstractRequestInterceptor
{
    protected final LogClient mLog = new LogClient(this);
    private final static String mDefaultEncoding = "UTF8";


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


    private void tryCallback(boolean authSucceeded)
    {
        if (mAuthCallback != null)
        {
            if (authSucceeded)
            {
                mAuthCallback.onLoginSuccess();
            } else
            {
                mAuthCallback.onLogFailed();
            }
        }
    }


    @Override
    public boolean process(HttpRequest httpRequest, String requestData,
            HttpResponse httpResponse)
    {
        Header header = httpRequest
                .getFirstHeader(WebServerConstants.HTTP.HEADER_AUTHENTICATION);

        if (mServerConfig.isUserAuthEnabled == false)
        {
            httpResponse.setStatusCode(WebServerConstants.HTTP.HTTP_CODE_OK);

            tryCallback(true);
            return true;
        }

        boolean authSuccess = false;

        if (header != null)
        {
            mLog.debug("header contains username/password -> checking restricted access");

            StringBuffer requestUserName = new StringBuffer(), requestPassword = new StringBuffer();
            extractCredentialsFromRequestHeader(header, requestUserName,
                    requestPassword);

            if (requestUserName.toString().compareTo(mServerConfig.username) == 0
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
            mLog.info("require authentication");
            httpResponse
                    .setStatusCode(WebServerConstants.HTTP.HTTP_CODE_UNAUTHORIZED);
            httpResponse.setHeader(
                    WebServerConstants.HTTP.HEADER_WWW_AUTHENTICATE,
                    String.format("Basic realm=\"%s\"",
                            WebServerConstants.HTTP.AUTHENTICATION_REALM));

            // display error page
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

        tryCallback(authSuccess);
        return authSuccess;
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
}
