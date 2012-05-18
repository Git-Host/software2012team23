package at.tugraz.ist.akm.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import my.org.apache.http.HttpResponse;
import my.org.apache.http.entity.ContentProducer;
import my.org.apache.http.entity.EntityTemplate;

import org.json.JSONObject;

public class HttpResponseDataAppender {

    public void appendHttpResponseData(HttpResponse httpResponse, final JSONObject data) {
        appendHttpResponseData(httpResponse, WebServerConfig.HTTP.CONTENT_TYPE_JSON,
                data.toString());
    }

    public void appendHttpResponseData(HttpResponse httpResponse, final String contentType,
            final String data) {
        httpResponse.setEntity(new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream);
                writer.write(data.toString());
                writer.flush();
                writer.close();
            }
        }));
        httpResponse.setHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE, contentType);
    }
}
