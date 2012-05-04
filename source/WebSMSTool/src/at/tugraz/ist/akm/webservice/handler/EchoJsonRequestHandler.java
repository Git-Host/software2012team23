package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class EchoJsonRequestHandler extends AbstractHttpRequestHandler {

    public EchoJsonRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        mLog.logD("called");
        if (httpRequest.getRequestLine().getMethod().equals("POST")) {
            BasicHttpEntityEnclosingRequest post = (BasicHttpEntityEnclosingRequest) httpRequest;
            final JSONObject json;
            try {
                json = new JSONObject(EntityUtils.toString(post.getEntity()));

                httpResponse.setEntity(new EntityTemplate(new ContentProducer() {

                    @Override
                    public void writeTo(OutputStream arg0) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(arg0);
                        writer.write(json.toString());
                        writer.flush();
                        writer.close();
                    }
                }));
                httpResponse.setHeader("Content-type", "application/json");
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
