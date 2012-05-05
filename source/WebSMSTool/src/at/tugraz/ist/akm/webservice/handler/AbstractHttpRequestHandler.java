package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandler;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;

import android.content.Context;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public abstract class AbstractHttpRequestHandler implements HttpRequestHandler {
    protected final Logable mLog = new Logable(getClass().getSimpleName());
    protected final Context mContext;
    protected final XmlNode mConfig;
    protected final HttpRequestHandlerRegistry mRegistry;
    
    protected HashMap<String, FileInfo> mUri2FileInfo = new HashMap<String, AbstractHttpRequestHandler.FileInfo>();

    public AbstractHttpRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        this.mContext = context;
        this.mConfig = config;
        this.mRegistry = registry;
        assignUriMappingToRegistry();
    }

    protected void register(String uri) {
        if (mRegistry != null) {
            mLog.logI("register for uri '" + uri + "'");
            mRegistry.register(uri, this);
        } else {
            mLog.logW("cannot register uri '" + uri + "' => no registry provided!");
        }
    }
    
    
    
    private void assignUriMappingToRegistry() {
        if (mConfig == null) {
            return;
        }
        List<XmlNode> childNodes = mConfig.getChildNodes(WebServerConfig.XML.TAG_REQUEST);
        for (XmlNode node : childNodes) {
            String uri = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_URI_PATTERN);
            if (uri == null || uri.trim().length() == 0) {
                mLog.logE("no uri configured, ignore this request configuration");
                continue;
            }
            String contentType = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_CONTENT_TYPE);
            if (contentType == null || contentType.trim().length() == 0) {
                mLog.logE("no content type configured for uri <" + uri + ">");
                continue;
            }
            String file = node.getAttributeValue(WebServerConfig.XML.ATTRIBUTE_DATA_FILE);
            if (file == null || file.trim().length() == 0) {
                mLog.logE("no data file configured for uri <" + uri + ">");
                continue;
            }
            uri = uri.trim();

            FileInfo fileInfo = new FileInfo(file.trim(), contentType.trim());
            mUri2FileInfo.put(uri, fileInfo);
            mLog.logI("read mapping uri <" + uri + "> ==> <" + fileInfo+">");

            register(uri);
        }
    }    
    
    

    @Override
    public abstract void handle(HttpRequest httpRequest, HttpResponse httpResponse,
            HttpContext httpContext) throws HttpException, IOException;

    
    
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
}
