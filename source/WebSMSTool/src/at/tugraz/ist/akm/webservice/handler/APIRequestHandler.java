package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class APIRequestHandler extends AbstractHttpRequestHandler {
    private final static String JSON_METHOD = "method";
    private final static String JSON_PARAMS = "params";

    public APIRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);
        String uri = config.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_URI_PATTERN);
        if (uri == null || uri.trim().length() == 0) {
            Log.e(getLogTag(), "no uri configured");
        } else {
            register(uri);
        }
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {

        if (httpRequest.getRequestLine().getMethod().equals("POST")) {
            BasicHttpEntityEnclosingRequest post = (BasicHttpEntityEnclosingRequest) httpRequest;
            JSONObject json;
            try {
                json = new JSONObject(EntityUtils.toString(post.getEntity()));
                String method = json.getString(JSON_METHOD);
                if (method != null && method.length() > 0) {
                    JSONObject jsonParams = json.getJSONObject(JSON_PARAMS);
                    JSONObject jsonResponse = processMethod(method, jsonParams);
                    sendResponse(httpResponse, jsonResponse);
                } else {
                    Log.e(getLogTag(), "no method defined in JSON post request");
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(HttpResponse httpResponse, JSONObject jsonResponse) {
    }

    private JSONObject processMethod(String method, JSONObject jsonParams) {
        throw new RuntimeException("Method not yet implemented");
    }
}