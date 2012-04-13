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

public class FileRequestHandler extends AbstractHttpRequestHandler {
    private final static String CFG_DATA_FILE = "dataFile";
    private final static String CFG_CONTENT_TYPE = "contentType";
    private final static String CFG_PATTERN = "pattern";

    private final static String TAG_REQUEST = "request";

    private HashMap<String, FileInfo> pattern2FileInfo = new HashMap<String, FileRequestHandler.FileInfo>();

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
        List<XmlNode> childNodes = config.getChildNodes(TAG_REQUEST);
        for (XmlNode node : childNodes) {
            String pattern = node.getAttributeValue(CFG_PATTERN);
            if (pattern == null || pattern.trim().length() == 0) {
                Log.e("FileRequestHandler", "no pattern found, ignore this request configuration");
                continue;
            }
            String contentType = node.getAttributeValue(CFG_CONTENT_TYPE);
            if (contentType == null || contentType.trim().length() == 0) {
                Log.e("FileRequestHandler", "no content type configured for pattern '" + pattern
                        + "'");
                continue;
            }
            String file = node.getAttributeValue(CFG_DATA_FILE);
            if (file == null || file.trim().length() == 0) {
                Log.e("FileRequestHandler", "no data file configured for pattern '" + pattern + "'");
                continue;
            }
            pattern = pattern.trim();

            FileInfo fileInfo = new FileInfo(file.trim(), contentType.trim());
            pattern2FileInfo.put(pattern, fileInfo);
            Log.i("FileRequestHandler", "read mapping pattern '" + pattern + "' ==> " + fileInfo);

            registry.register(pattern, this);
        }
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        Log.v("FileRequestHandler", "handle request: " + httpRequest.getRequestLine().getUri());

        String uri = httpRequest.getRequestLine().getUri();
        final FileInfo fileInfo = pattern2FileInfo.get(uri);
        if (fileInfo == null) {
            Log.e("FileRequestHandler", "no mapping found for URI '" + uri + "'");
            return;
        }

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                String resp = new FileReader(context, fileInfo.file).read();

                writer.write(resp);
                writer.flush();
            }
        });
        httpResponse.setHeader("Content-Type", fileInfo.contentType);
        httpResponse.setEntity(entity);
    }
}