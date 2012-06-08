package at.tugraz.ist.akm.test.webservice.server;

import android.content.Context;
import at.tugraz.ist.akm.webservice.handler.interceptor.IRequestInterceptor;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class MockSimpleWebServer extends SimpleWebServer {

    public MockSimpleWebServer(Context context) {
        super(context);
    }
    
    @Override
    protected void setInterceptor(IRequestInterceptor reqInterceptor) {
    }
}
