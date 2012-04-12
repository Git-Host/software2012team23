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

public class DisplayWebSiteRequestHandler extends AbstractHttpRequestHandler {

    private final String htmlFile;

    public DisplayWebSiteRequestHandler(final Context context, final String htmlFile) {
        super(context);
        this.htmlFile = htmlFile;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        Log.v("MyRequestHandler", "handle request: " + httpRequest);

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                String resp = new FileReader(context, "web/" + htmlFile).read();

                writer.write(resp);
                writer.flush();
            }
        });
        httpResponse.setHeader("Content-Type", "text/html");
        httpResponse.setEntity(entity);
    }
}