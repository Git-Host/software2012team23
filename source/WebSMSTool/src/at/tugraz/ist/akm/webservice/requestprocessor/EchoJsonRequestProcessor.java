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

package at.tugraz.ist.akm.webservice.requestprocessor;

import java.io.Closeable;
import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ParseException;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;

public class EchoJsonRequestProcessor extends AbstractHttpRequestProcessor
        implements Closeable
{
    private LogClient mLog = new LogClient(this);


    public EchoJsonRequestProcessor(final Context context,
            final XmlNode config, final HttpRequestHandlerRegistry registry)
    {
        super(context, config, registry);
    }


    @Override
    public void handleRequest(RequestLine requestLine, String requestData,
            HttpResponse httpResponse) throws HttpException, IOException
    {
        if (requestLine.getMethod().equals(
                WebServerConstants.HTTP.REQUEST_TYPE_POST))
        {
            final JSONObject json;
            try
            {
                json = new JSONObject(requestData);
                mResponseDataAppender
                        .appendHttpResponseData(httpResponse, json);
            }
            catch (ParseException parseException)
            {
                parseException.printStackTrace();
            }
            catch (JSONException jsonException)
            {
                jsonException.printStackTrace();
            }
            finally
            {
                mLog.error("failed to parse json request");
            }
        } else
        {
            mLog.debug("ignoring request type: " + requestLine.getMethod());
        }
    }


    @Override
    public void close()
    {
        mLog = null;
        super.close();
    }
}
