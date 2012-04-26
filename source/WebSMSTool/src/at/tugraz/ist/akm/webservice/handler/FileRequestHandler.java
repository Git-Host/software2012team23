package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class FileRequestHandler extends AbstractHttpRequestHandler {

	private HashMap<String, FileInfo> mUri2FileInfo = new HashMap<String, FileRequestHandler.FileInfo>();
   
    public FileRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);

    }

    
    // TODO: error handling
    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        LOG.v("handle request <" + httpRequest.getRequestLine().getUri() + ">");

        String uri = httpRequest.getRequestLine().getUri();
        final FileInfo fileInfo = mUri2FileInfo.get(uri);
        if (fileInfo == null) {
            LOG.e("no mapping found for uri <" + uri + ">");
            return;
        }

        final String data = new FileReader(mContext, fileInfo.mFile).read();

        HttpEntity entity = new EntityTemplate(new ContentProducer() {

            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream);
                writer.write(data);
                writer.flush();
                writer.close();
            }
        });

        httpResponse.setHeader("Content-Type", fileInfo.mContentType);
        httpResponse.setEntity(entity);
    }
}
