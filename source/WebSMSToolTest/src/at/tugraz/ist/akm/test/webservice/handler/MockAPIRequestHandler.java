package at.tugraz.ist.akm.test.webservice.handler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.webservice.handler.AbstractHttpRequestHandler;

public class MockAPIRequestHandler extends AbstractHttpRequestHandler {

    public MockAPIRequestHandler(final Context context, final XmlNode config) {
        super(context, config, null);
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {
        Log.d("MockAPIRequestHandler", "called");
        if (httpRequest.getRequestLine().getMethod().equals("POST")) {
            BasicHttpEntityEnclosingRequest post = (BasicHttpEntityEnclosingRequest) httpRequest;
            JSONObject json;
            try {
                json = new JSONObject(EntityUtils.toString(post.getEntity()));
                httpResponse.setEntity(new StringEntity(json.toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
