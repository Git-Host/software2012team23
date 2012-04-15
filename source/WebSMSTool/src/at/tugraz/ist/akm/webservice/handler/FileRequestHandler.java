package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class FileRequestHandler extends AbstractHttpRequestHandler {
    private HashMap<String, FileInfo> uri2FileInfo = new HashMap<String, FileRequestHandler.FileInfo>();

    private static class FileInfo {
        final String contentType;
        final String file;

        public FileInfo(final String file, final String contentType) {
            this.file = file;
            this.contentType = contentType;
        }

        @Override
        public String toString() {
            return new StringBuffer().append("dataFile '").append(file).append("' contentType '")
                    .append(contentType).append("'").toString();
        }
    }

    public FileRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);

        readConfig(config, registry);
    }

    private void readConfig(XmlNode config, HttpRequestHandlerRegistry registry) {
        if (config == null) {
            return;
        }
        List<XmlNode> childNodes = config.getChildNodes(WebServerConfig.XML.TAG_REQUEST);
        for (XmlNode node : childNodes) {
            String uri = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_URI_PATTERN);
            if (uri == null || uri.trim().length() == 0) {
                Log.e(getLogTag(), "no uri configured, ignore this request configuration");
                continue;
            }
            String contentType = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CONTENT_TYPE);
            if (contentType == null || contentType.trim().length() == 0) {
                Log.e(getLogTag(), "no content type configured for uri '" + uri + "'");
                continue;
            }
            String file = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_DATA_FILE);
            if (file == null || file.trim().length() == 0) {
                Log.e(getLogTag(), "no data file configured for uri '" + uri + "'");
                continue;
            }
            uri = uri.trim();

            FileInfo fileInfo = new FileInfo(file.trim(), contentType.trim());
            uri2FileInfo.put(uri, fileInfo);
            Log.i(getLogTag(), "read mapping uri '" + uri + "' ==> " + fileInfo);

            register(uri);
        }
    }

    // TODO: error handling
    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        Log.v(getLogTag(), "handle request '" + httpRequest.getRequestLine().getUri() + "'");

        String uri = httpRequest.getRequestLine().getUri();
        final FileInfo fileInfo = uri2FileInfo.get(uri);
        if (fileInfo == null) {
            Log.e(getLogTag(), "no mapping found for uri '" + uri + "'");
            return;
        }

        final String data = new FileReader(context, fileInfo.file).read();

        HttpEntity entity = new EntityTemplate(new ContentProducer() {

            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream);
                writer.write(data);
                writer.flush();
                writer.close();
            }
        });

        httpResponse.setHeader("Content-Type", fileInfo.contentType);
        httpResponse.setEntity(entity);
    }
}