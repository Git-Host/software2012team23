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

package at.tugraz.ist.akm.test.webservice.handler;

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
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.requestprocessor.JsonAPIRequestProcessor;

public class EchoJsonRequestHandler extends JsonAPIRequestProcessor {

    public EchoJsonRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) throws Throwable {
        super(context, config, registry);
    }

    @Override
    public void handleRequest(RequestLine requestLine, String requestData, HttpResponse httpResponse)
            throws HttpException, IOException {
        if (requestLine.getMethod().equals(WebServerConstants.HTTP.REQUEST_TYPE_POST)) {
            final JSONObject json;
            try {
                json = new JSONObject(requestData);
                mResponseDataAppender.appendHttpResponseData(httpResponse, json);
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
