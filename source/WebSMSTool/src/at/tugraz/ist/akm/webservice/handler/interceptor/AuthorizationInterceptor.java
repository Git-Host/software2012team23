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

package at.tugraz.ist.akm.webservice.handler.interceptor;

import java.io.UnsupportedEncodingException;

import my.org.apache.http.Header;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import android.util.Base64;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class AuthorizationInterceptor extends AbstractRequestInterceptor {
    protected final LogClient mLog = new LogClient(this);
    private Config mConfig;
    private final static String mDefaultEncoding = "UTF8";
    
    public AuthorizationInterceptor(Context context) {
    	super(context);
    	mConfig = new Config(context);
    }

    
	@Override
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse) {
        Header header = httpRequest.getFirstHeader(WebServerConfig.HTTP.HEADER_AUTHENTICATION);
        
        String userToCheck = mConfig.getUserName();
        String passToCkeck = mConfig.getPassWord();
        
        //config values are not correctly set -> no authentication will be done
        if(userToCheck.length() == 0 || passToCkeck.length() == 0){
        	httpResponse.setStatusCode(WebServerConfig.HTTP.HTTP_CODE_OK);
        	return true;
        }

        boolean authSuccess = false;
        
        if (header != null) {
            mLog.debug("login!");
            String headerValue = header.getValue();
            int idx = headerValue.indexOf(" ");
            String user = "";
            String pass = "";
            if (idx >= 0) {
                String userCredentials;
                try
                {
                    userCredentials = new String(
                            Base64.decode(headerValue.substring(idx + 1), 0), mDefaultEncoding);
                    user = getUsername(userCredentials);
                    pass = getPassword(userCredentials);
                } catch (UnsupportedEncodingException e)
                {
                    mLog.error("failed to extract string from header: " + e.getMessage());
                }
            }
            
            
            if(user.compareTo(userToCheck) == 0 && pass.compareTo(passToCkeck) == 0){
            	authSuccess = true;
            	httpResponse.setStatusCode(WebServerConfig.HTTP.HTTP_CODE_OK);
            }
        }
        
        
        if(authSuccess == false) {
            mLog.info("require authentication");
            httpResponse.setStatusCode(WebServerConfig.HTTP.HTTP_CODE_UNAUTHORIZED);
            httpResponse.setHeader(WebServerConfig.HTTP.HEADER_WWW_AUTHENTICATE,
                    String.format("Basic realm=\"%s\"", WebServerConfig.HTTP.AUTHENTICATION_REALM));

            // display error page
            FileReader filereader = new FileReader(mContext, WebServerConfig.RES.UNAUTHORIZED);
            responseDataAppender.appendHttpResponseData(httpResponse,
                    WebServerConfig.HTTP.CONTENTY_TYPE_TEXT_HTML, filereader.read());
        }
        
        return authSuccess;
    }

    private String getUsername(String authenticationCode) {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0) {
            return authenticationCode.substring(0, idx);
        }
        return "";
    }

    private String getPassword(String authenticationCode) {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0) {
            return authenticationCode.substring(idx + 1);
        }
        return "";
    }

}
