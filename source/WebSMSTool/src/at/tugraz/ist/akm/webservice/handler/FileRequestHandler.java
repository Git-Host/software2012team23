package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;

public class FileRequestHandler extends AbstractHttpRequestHandler {

    public FileRequestHandler(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry) {
        super(context, config, registry);

    }

    @Override
    public void handleRequest(RequestLine requestLine, String requestData, HttpResponse httpResponse)
            throws HttpException, IOException {
        mLog.logVerbose("handle request <" + requestLine.getUri() + ">");

        String uri = requestLine.getUri();
        final FileInfo fileInfo = mUri2FileInfo.get(uri);
        if (fileInfo == null) {
            mLog.logVerbose("no mapping found for uri <" + uri + ">");
            return;
        }

        final String data = new FileReader(mContext, fileInfo.mFile).read();
        responseDataAppender.appendHttpResponseData(httpResponse, fileInfo.mContentType, data);
    }
}
