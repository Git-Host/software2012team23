package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Context context;
    protected final XmlNode config;

    public AbstractHttpRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        this.context = context;
        this.config = config;
    }

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

}
