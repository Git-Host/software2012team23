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
    protected final Context mContext;
    protected final XmlNode mConfig;
    protected final HttpRequestHandlerRegistry mRegistry;

    public AbstractHttpRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        this.mContext = context;
        this.mConfig = config;
        this.mRegistry = registry;
    }

    protected void register(String uri) {
        if (mRegistry != null) {
            LOG.i("register for uri '" + uri + "'");
            mRegistry.register(uri, this);
        } else {
            LOG.w("cannot register uri '" + uri + "' => no registry provided!");
        }
    }

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

}
