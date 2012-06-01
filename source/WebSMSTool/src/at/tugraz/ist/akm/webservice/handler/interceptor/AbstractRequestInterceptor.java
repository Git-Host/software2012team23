package at.tugraz.ist.akm.webservice.handler.interceptor;

import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import android.content.Context;
import at.tugraz.ist.akm.webservice.HttpResponseDataAppender;

public abstract class AbstractRequestInterceptor implements IRequestInterceptor {
    protected final Context mContext;
    protected final HttpResponseDataAppender responseDataAppender = new HttpResponseDataAppender();

    public AbstractRequestInterceptor(final Context context) {
        mContext = context;
    }

    @Override
    public abstract boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse);
}