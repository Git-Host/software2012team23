package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Context context;

    public AbstractHttpRequestHandler(final Context context) {
        this.context = context;
    }

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

}
