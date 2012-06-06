package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import my.org.apache.http.HttpEntity;
import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.message.BasicHttpEntityEnclosingRequest;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandler;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import my.org.apache.http.util.EntityUtils;
import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.HttpResponseDataAppender;
import at.tugraz.ist.akm.webservice.WebServerConfig;
import at.tugraz.ist.akm.webservice.handler.interceptor.IRequestInterceptor;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Logable mLog = new Logable(getClass().getSimpleName());
    protected final Context mContext;
    protected final XmlNode mConfig;
    protected final HttpRequestHandlerRegistry mRegistry;
    private final List<IRequestInterceptor> requestInterceptorList = new ArrayList<IRequestInterceptor>();

    protected HttpResponseDataAppender responseDataAppender = new HttpResponseDataAppender();

    protected HashMap<String, FileInfo> mUri2FileInfo = new HashMap<String, AbstractHttpRequestHandler.FileInfo>();

    private String mUriPattern = null;

    public static class FileInfo {
        final String mContentType;
        final String mFile;

        public FileInfo(final String file, final String contentType) {
            this.mFile = file;
            this.mContentType = contentType;
        }

        @Override
        public String toString() {
            return new StringBuffer().append("dataFile '").append(mFile).append("' contentType '")
                    .append(mContentType).append("'").toString();
        }
    }

    public AbstractHttpRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        this.mContext = context;
        this.mConfig = config;
        this.mRegistry = registry;
        assignUriMappingToRegistry();
    }

    public void addRequestInterceptor(IRequestInterceptor interceptor) {
        requestInterceptorList.add(interceptor);
    }

    private void assignUriMappingToRegistry() {
        if (mConfig == null) {
            return;
        }
        List<XmlNode> childNodes = mConfig.getChildNodes(WebServerConfig.XML.TAG_REQUEST);
        for (XmlNode node : childNodes) {
            String uri = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_URI_PATTERN);
            if (uri == null || uri.trim().length() == 0) {
                mLog.logError("no uri configured, ignore this request configuration");
                continue;
            }
            String contentType = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CONTENT_TYPE);
            if (contentType == null || contentType.trim().length() == 0) {
                mLog.logError("no content type configured for uri <" + uri + ">");
                continue;
            }
            String file = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_DATA_FILE);
            if (file == null || file.trim().length() == 0) {
                mLog.logError("no data file configured for uri <" + uri + ">");
                continue;
            }
            uri = uri.trim();

            FileInfo fileInfo = new FileInfo(file.trim(), contentType.trim());
            mUri2FileInfo.put(uri, fileInfo);
            mLog.logInfo("read mapping uri <" + uri + "> ==> <" + fileInfo + ">");

            register(uri);
        }
    }

    protected void register(String uri) {
        if (mRegistry != null) {
            mLog.logInfo("register for uri '" + uri + "'");
            mUriPattern = uri;
            mRegistry.register(mUriPattern, this);
        } else {
            mLog.logWarning("cannot register uri '" + uri + "' => no registry provided!");
        }
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {

        String requestData = null;

        if (httpRequest instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity entitiy = ((BasicHttpEntityEnclosingRequest)httpRequest).getEntity();
            if (entitiy != null) {
                requestData = EntityUtils.toString(entitiy);
            }
        }
        for (IRequestInterceptor interceptor : requestInterceptorList) {
            if (!interceptor.process(httpRequest, requestData, httpResponse)) {
                return;
            }
        }
        handleRequest(httpRequest.getRequestLine(), requestData, httpResponse);
    }

    public abstract void handleRequest(RequestLine requestLine, String requestData,
            HttpResponse httpResponse) throws HttpException, IOException;

    /**
     * adapter: call it to ensure proper cleanup
     */
    public void onClose() {
        if (mRegistry != null) {
            mLog.logInfo("close request handler for URI [" + mUriPattern + "]");
            mRegistry.unregister(mUriPattern);
        }
        requestInterceptorList.clear();
    }
}
