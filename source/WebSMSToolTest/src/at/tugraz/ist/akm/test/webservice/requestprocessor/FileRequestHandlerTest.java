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

package at.tugraz.ist.akm.test.webservice.requestprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ProtocolVersion;
import my.org.apache.http.message.BasicHttpEntityEnclosingRequest;
import my.org.apache.http.message.BasicHttpResponse;
import my.org.apache.http.message.BasicStatusLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.webservice.WebServerConstants;
import at.tugraz.ist.akm.webservice.requestprocessor.FileRequestProcessor;

public class FileRequestHandlerTest extends InstrumentationTestCase {
    private final static String URI = "/xyz";
    private final static String DATA_FILE = "web/index.html";
    private final static String CONTENT_TYPE = "text/html";
    private final static String DEFAULT_ENCODING="UTF8";

    private HttpRequestHandlerRegistry registry = null;
    private FileRequestProcessor testInstance = null;

    private String buildConfig() {
        StringBuffer sb = new StringBuffer();
        sb.append("<config>");
        sb.append("<requestHandler>");
        sb.append("<request uriPattern=\"").append(URI).append("\" contentType=\"");
        sb.append(CONTENT_TYPE).append("\" dataFile=\"").append(DATA_FILE).append("\"/>");
        sb.append("</requestHandler>");
        sb.append("</config>");
        return sb.toString();
    }

    protected void setUp() throws Exception {
        registry = new HttpRequestHandlerRegistry();

        String xmlConfig = buildConfig();
        XmlReader reader = new XmlReader(xmlConfig);
        List<XmlNode> nodesList = reader.getNodes(WebServerConstants.XML.TAG_REQUEST_HANDLER);

        Assert.assertNotNull(nodesList);
        Assert.assertEquals(1, nodesList.size());

        testInstance = new FileRequestProcessor(getInstrumentation().getContext(), nodesList.get(0),
                registry);
    }

    public void testRegisterUri() {
        Assert.assertEquals(testInstance, registry.lookup(URI));
    }

    public void testHandle() {
        HttpRequest httpRequest = new BasicHttpEntityEnclosingRequest("none", URI);
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
                "HTTP", 1, 1), 200, ""));

        try {
            testInstance.handle(httpRequest, httpResponse, null);
            

            Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
            Assert.assertNotNull(httpResponse.getEntity());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(baos);


            FileReader reader = new FileReader(getInstrumentation().getContext(), DATA_FILE);
            Assert.assertEquals(
                    reader.read(),
                    new String(baos.toByteArray(), DEFAULT_ENCODING));
            reader.onClose();
            reader = null;

        } catch (HttpException httpException) {
            Assert.fail("Exception => " + httpException.getMessage());
            httpException.printStackTrace();
        } catch (IOException ioException) {
            Assert.fail("Exception => " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }
}
