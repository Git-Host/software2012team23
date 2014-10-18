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

import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import at.tugraz.ist.akm.webservice.requestprocessor.HttpResponseDataAppender;
import at.tugraz.ist.akm.webservice.server.IHttpAccessCallback;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public abstract class AbstractRequestInterceptor implements IRequestInterceptor
{
    protected HttpResponseDataAppender responseDataAppender = new HttpResponseDataAppender();
    protected WebserverProtocolConfig mServerConfig;
    protected Context mContext;
    protected IHttpAccessCallback mAuthCallback = null;


    public AbstractRequestInterceptor(WebserverProtocolConfig config,
            Context context, IHttpAccessCallback authCallback)
    {
        mServerConfig = config;
        mContext = context;
        mAuthCallback = authCallback;
    }


    @Override
    public abstract boolean process(HttpRequest httpRequest,
            String requestData, HttpResponse httpResponse);


    @Override
    public void close() throws IOException
    {
        mServerConfig = null;
        mContext = null;
        responseDataAppender = null;
    }
}
