package at.tugraz.ist.akm.webservice.handler.interceptor;

import my.org.apache.http.Header;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import android.util.Base64;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class AuthorizationInterceptor extends AbstractRequestInterceptor {
    protected final Logable mLog = new Logable(getClass().getSimpleName());

    public AuthorizationInterceptor(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
	@Override
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse) {
        Header header = httpRequest.getFirstHeader(WebServerConfig.HTTP.HEADER_AUTHENTICATION);
        if (header != null) {
            mLog.logDebug("login!");
            String headerValue = header.getValue();
            int idx = headerValue.indexOf(" ");
            String user = "";
            String pass = "";
            if (idx >= 0) {
                String userCredentials = new String(
                        Base64.decode(headerValue.substring(idx + 1), 0));
                user = getUsername(userCredentials);
                pass = getPassword(userCredentials);
            }
            // TODO
            // validate user credentials
            httpResponse.setStatusCode(WebServerConfig.HTTP.HTTP_CODE_OK);
        } else {
            mLog.logVerbose("require authentication");
            httpResponse.setStatusCode(WebServerConfig.HTTP.HTTP_CODE_UNAUTHORIZED);
            httpResponse.setHeader(WebServerConfig.HTTP.HEADER_WWW_AUTHENTICATE,
                    String.format("Basic realm=\"%s\"", WebServerConfig.HTTP.AUTHENTICATION_REALM));

            // display error page
            FileReader filereader = new FileReader(mContext, WebServerConfig.RES.UNAUTHORIZED);
            responseDataAppender.appendHttpResponseData(httpResponse,
                    WebServerConfig.HTTP.CONTENTY_TYPE_TEXT_HTML, filereader.read());

            return false;
        }
        return true;
    }

    private String getUsername(String authenticationCode) {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0) {
            return authenticationCode.substring(0, idx);
        }
        return "";
    }

    private String getPassword(String authenticationCode) {
        int idx = authenticationCode.indexOf(":");
        if (idx >= 0) {
            return authenticationCode.substring(idx + 1);
        }
        return "";
    }

}
