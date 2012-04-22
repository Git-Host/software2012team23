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
import at.tugraz.ist.akm.trace.Logable;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Logable LOG = new Logable(getClass().getSimpleName());
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
            LOG.i("register for uri '" + uri + "'");
            registry.register(uri, this);
        } else {
            LOG.w("cannot register uri '" + uri + "' => no registry provided!");
        }
    }

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

}
