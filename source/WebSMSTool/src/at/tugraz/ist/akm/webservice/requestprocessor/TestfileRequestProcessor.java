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

import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class TestfileRequestProcessor extends FileRequestProcessor
{

    public TestfileRequestProcessor(final Context context,
            final XmlNode config, final HttpRequestHandlerRegistry registry)
    {
        super(context, config, registry);
    }


    @Override
    synchronized public void handleRequest(RequestLine requestLine,
            String requestData, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException
    {
        super.handleRequest(requestLine, requestData, httpResponse, httpContext);
    }


    @Override
    public void close() throws IOException
    {
        super.close();
    }
}
