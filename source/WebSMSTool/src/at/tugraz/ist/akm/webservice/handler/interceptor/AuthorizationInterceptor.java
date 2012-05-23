package at.tugraz.ist.akm.webservice.handler.interceptor;

import my.org.apache.http.Header;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import android.util.Base64;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.cookie.Cookie;
import at.tugraz.ist.akm.webservice.cookie.CookieManager;

public class AuthorizationInterceptor extends AbstractRequestInterceptor {
    protected final Logable mLog = new Logable(getClass().getSimpleName());

    public AuthorizationInterceptor(Context context) {
        super(context);
    }

    @Override
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse) {
        Header header = httpRequest.getFirstHeader("Authorization");
        printHeaders(httpRequest.getAllHeaders());
        if (header != null) {
            mLog.logD("login!");
            String headerValue = header.getValue();
            int idx = headerValue.indexOf(" ");
            if (idx >= 0) {
                String authenticationCode = headerValue.substring(idx + 1);

                byte[] byteAuthenticationCode = Base64.decode(authenticationCode, 0);
                mLog.logD("user credentials: " + new String(byteAuthenticationCode));
            }

            Cookie cookie = CookieManager.createCookie();
            mLog.logD("logged in: create session cookie " + cookie);

            // validate user credentials

            httpResponse.setStatusCode(200);
            httpResponse.setHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE, cookie.toString());
        } else {
            mLog.logV("require authentication");
            httpResponse.setStatusCode(401);
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"thomas\"");
            return false;
        }

        // } else {
        // Header cookieHeader =
        // httpRequest.getFirstHeader(WebServerConfig.HTTP.HEADER_COOKIE);
        // boolean cookieExpired = true;
        // if (cookieHeader != null) {
        // mLog.logV("cookie to be checked: " + cookieHeader.getValue());
        // Cookie cookie = CookieManager.lookupCookie(cookieHeader.getValue());
        //
        // mLog.logV("received cookie: " + cookie);
        // if (cookie != null) {
        // if (CookieManager.validateCookie(cookie)) {
        // httpResponse.setHeader(WebServerConfig.HTTP.HEADER_SET_COOKIE,
        // cookie.toString());
        // cookie.accessed();
        // cookieExpired = false;
        // } else {
        // mLog.logI("cookie " + cookie.getValue() + " is expired");
        // }
        // }
        // }
        // if (cookieExpired) {
        // httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"thomas\"");
        // httpResponse.setStatusCode(401);
        // mLog.logI("cookie expired or not set");
        // return false;
        // }
        //
        // }
        return true;
    }

    private void printHeaders(Header[] headers) {
        for (Header header : headers) {
            mLog.logV("header: name=" + header.getName() + " value=" + header.getValue());
        }
    }

}
