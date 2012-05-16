package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import my.org.apache.http.HttpEntity;
import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.entity.ContentProducer;
import my.org.apache.http.entity.EntityTemplate;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class FileRequestHandler extends AbstractHttpRequestHandler {
  
    public FileRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);

    }

    
    // TODO: error handling
    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        mLog.logV("handle request <" + httpRequest.getRequestLine().getUri() + ">");

        String uri = httpRequest.getRequestLine().getUri();
        final FileInfo fileInfo = mUri2FileInfo.get(uri);
        if (fileInfo == null) {
            mLog.logV("no mapping found for uri <" + uri + ">");
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
