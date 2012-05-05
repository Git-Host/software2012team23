package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ParseException;
import my.org.apache.http.entity.ContentProducer;
import my.org.apache.http.entity.EntityTemplate;
import my.org.apache.http.message.BasicHttpEntityEnclosingRequest;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import my.org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class EchoJsonRequestHandler extends JsonAPIRequestHandler {

    public EchoJsonRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        LOG.d("called");
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
