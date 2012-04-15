package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import android.util.Log;
import at.tugraz.ist.akm.io.xml.XmlNode;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Context context;
    protected final XmlNode config;
    protected final HttpRequestHandlerRegistry registry;

    public AbstractHttpRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        this.context = context;
        this.config = config;
        this.registry = registry;
    }

    protected void register(String uri) {
        if (registry != null) {
            Log.i(this.getClass().getSimpleName(), "register for uri '" + uri + "'");
            registry.register(uri, this);
        } else {
            Log.w(this.getClass().getSimpleName(), "cannot register uri '" + uri
                    + "' => no registry provided!");
        }
    }

    protected String getLogTag() {
        return this.getClass().getSimpleName();
    }

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

}
