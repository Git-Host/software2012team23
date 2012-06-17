package at.tugraz.ist.akm.webservice.handler;

import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.resource.DrawableResource;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class FaviconLoader extends AbstractHttpRequestHandler {

	private final Logable mLog = new Logable(getClass().getSimpleName());

	public FaviconLoader(Context context, XmlNode config,
			HttpRequestHandlerRegistry registry) {
		super(context, config, registry);
	}

	@Override
	public void handleRequest(RequestLine requestLine, String requestData,
			HttpResponse httpResponse) throws HttpException, IOException {
		try {
			byte[] imageBytes = new DrawableResource(mContext)
					.getBytes(R.drawable.ic_launcher);
			responseDataAppender.appendHttpResponseMediaType(httpResponse,
					WebServerConfig.HTTP.CONTENTY_TYPE_IMAGE_PNG, imageBytes);
		} catch (Exception ex) {
			mLog.logError("what a terrible failure", ex);
		}

	}

}
