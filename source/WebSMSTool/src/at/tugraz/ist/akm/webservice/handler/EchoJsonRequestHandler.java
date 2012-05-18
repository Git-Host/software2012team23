package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.ParseException;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class EchoJsonRequestHandler extends AbstractHttpRequestHandler {

    public EchoJsonRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);
    }

    @Override
    public void handleRequest(RequestLine requestLine, String requestData, HttpResponse httpResponse)
            throws HttpException, IOException {
        mLog.logD("called");
        if (requestLine.getMethod().equals(WebServerConfig.HTTP.REQUEST_TYPE_POST)) {
            final JSONObject json;
            try {
                json = new JSONObject(requestData);
                responseDataAppender.appendHttpResponseData(httpResponse, json);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
