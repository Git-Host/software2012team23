package at.tugraz.ist.akm.test.webservice.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ProtocolVersion;
import my.org.apache.http.message.BasicHttpEntityEnclosingRequest;
import my.org.apache.http.message.BasicHttpRequest;
import my.org.apache.http.message.BasicHttpResponse;
import my.org.apache.http.message.BasicStatusLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.test.InstrumentationTestCase;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.handler.FileRequestHandler;

public class FileRequestHandlerTest extends InstrumentationTestCase {
    private final static String URI = "/xyz";
    private final static String DATA_FILE = "web/index.html";
    private final static String CONTENT_TYPE = "text/html";

    private HttpRequestHandlerRegistry registry = null;
    private FileRequestHandler testInstance = null;

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
        List<XmlNode> nodesList = reader.getNodes(WebServerConfig.XML.TAG_REQUEST_HANDLER);

        Assert.assertNotNull(nodesList);
        Assert.assertEquals(1, nodesList.size());

        testInstance = new FileRequestHandler(getInstrumentation().getContext(), nodesList.get(0),
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

            Assert.assertEquals(
                    new FileReader(getInstrumentation().getContext(), DATA_FILE).read(),
                    new String(baos.toByteArray()));

        } catch (HttpException e) {
            Assert.fail("Exception => " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Assert.fail("Exception => " + e.getMessage());
            e.printStackTrace();
        }
    }
}
