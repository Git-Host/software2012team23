package at.tugraz.ist.akm.webservice.requestprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.WebServerConstants;

public class FaviconLoadRequestProcessor extends AbstractHttpRequestProcessor
{

    private LogClient mLog = new LogClient(this);


    public FaviconLoadRequestProcessor(Context context, XmlNode config,
            HttpRequestHandlerRegistry registry)
    {
        super(context, config, registry);
    }


    @Override
    public void close() throws IOException
    {
        mLog = null;
        super.close();
    }


    @Override
    public void handleRequest(RequestLine requestLine, String requestData,
            HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException
    {
        try
        {

            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                    R.raw.favicon);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            bmp = null;
            byte[] imageBytes = os.toByteArray();

            mResponseDataAppender
                    .appendHttpResponseMediaType(httpResponse,
                            WebServerConstants.HTTP.CONTENTY_TYPE_IMAGE_PNG,
                            imageBytes);
        }
        catch (Exception ex)
        {
            mLog.error("what a terrible failure", ex);
        }
    }
}
