package at.tugraz.ist.akm.webservice.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConfig;

public class FaviconLoader extends AbstractHttpRequestHandler {

	private final LogClient mLog = new LogClient(this);

	public FaviconLoader(Context context, XmlNode config,
			HttpRequestHandlerRegistry registry) {
		super(context, config, registry);
	}

	@Override
	public void handleRequest(RequestLine requestLine, String requestData,
			HttpResponse httpResponse) throws HttpException, IOException {
		try {
			
			Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.raw.favicon);
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, os); 
			byte[] imageBytes = os.toByteArray();
			
			responseDataAppender.appendHttpResponseMediaType(httpResponse,
					WebServerConfig.HTTP.CONTENTY_TYPE_IMAGE_PNG, imageBytes);
		} catch (Exception ex) {
			mLog.error("what a terrible failure", ex);
		}
	}
}
