package at.tugraz.ist.akm.test.webservice.handler.interceptor;

import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.cookie.Cookie;
import at.tugraz.ist.akm.webservice.cookie.CookieManager;
import at.tugraz.ist.akm.webservice.handler.interceptor.AbstractRequestInterceptor;

public class MockAuthorizationInterceptor extends AbstractRequestInterceptor {
    protected final Logable mLog = new Logable(getClass().getSimpleName());

    public MockAuthorizationInterceptor(Context context) {
        super(context);
    }

    @Override
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse) {
        mLog.logD("request data => "+ requestData);
        try {
            // TODO: handle login
            JSONObject json = new JSONObject(requestData);
            String method = json.getString(WebServerConfig.JSON.METHOD);
            if (method != null && method.equals("login")) {

                Cookie cookie = CookieManager.createCookie();
                mLog.logD("logged in: create session cookie " + cookie);

                httpResponse.setHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE, cookie.toString());
                JSONObject resultObject = new JSONObject();
                resultObject.put(WebServerConfig.JSON.STATE, WebServerConfig.JSON.STATE_SUCCESS);

                responseDataAppender.appendHttpResponseData(httpResponse, resultObject);
                return false;
            }

        } catch (JSONException e) {

        }
        return true;
    }

}
