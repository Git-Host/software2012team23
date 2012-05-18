package at.tugraz.ist.akm.webservice.handler.interceptor;

import my.org.apache.http.Header;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.cookie.Cookie;
import at.tugraz.ist.akm.webservice.cookie.CookieManager;

public class SessionCookieInterceptor extends AbstractRequestInterceptor {
    protected final Logable mLog = new Logable(getClass().getSimpleName());

    public SessionCookieInterceptor(Context context) {
        super(context);
    }

    @Override
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse) {

        Header cookieHeader = httpRequest.getFirstHeader(WebServerConfig.HTTP.HEADER_COOKIE);
        boolean cookieExpired = true;
        if (cookieHeader != null) {
            mLog.logV("cookie to be checked: " + cookieHeader.getValue());
            Cookie cookie = CookieManager.lookupCookie(cookieHeader.getValue());

            mLog.logV("received cookie: " + cookie);

            if (CookieManager.validateCookie(cookie)) {
                httpResponse.setHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE, cookie.toString());
                cookie.accessed();
                cookieExpired = false;
            } else {
                mLog.logI("cookie " + cookie.getValue() + " is expired");
            }
        }
        if (cookieExpired) {
            mLog.logI("cookie expired or not set");
            try {
                JSONObject resultObject = new JSONObject();
                resultObject.put(WebServerConfig.JSON.STATE,
                        WebServerConfig.JSON.STATE_SESSION_COOKIE_EXPIRED);
                responseDataAppender.appendHttpResponseData(httpResponse, resultObject);
            } catch (JSONException e) {
                mLog.logE("Could not create jsonobject for whilst handling session cookie.", e);
            }
            return false;
        }
        return true;
    }
}
