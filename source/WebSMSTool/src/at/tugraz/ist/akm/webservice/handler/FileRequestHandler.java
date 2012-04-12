package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class FileRequestHandler extends AbstractHttpRequestHandler {
    private final static String CFG_DATA_FILE = "dataFile";
    private final static String CFG_CONTENT_TYPE = "contentType";

    private final String dataFile;
    private final String contentType;

    public FileRequestHandler(final Context context, final XmlNode config) {
        super(context, config);

        dataFile = config.getAttributeValue(CFG_DATA_FILE);
        if (dataFile == null) {
            // TODO: throw exception
        }
        contentType = config.getAttributeValue(CFG_CONTENT_TYPE);
        if (contentType == null) {
            // TODO throw exception
        }

        Log.d("FileRequestHandler", "Config: dataFile <" + dataFile + "> contentType <"
                + contentType + ">");
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        Log.v("FileRequestHandler", "handle request: " + httpRequest);

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                String resp = new FileReader(context, "web/" + dataFile).read();

                writer.write(resp);
                writer.flush();
            }
        });
        httpResponse.setHeader("Content-Type", contentType);
        httpResponse.setEntity(entity);
    }
}