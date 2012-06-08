package at.tugraz.ist.akm.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;

public class WebSMSToolService extends Service {
    private final static Logable LOG = new Logable(
            WebSMSToolService.class.getSimpleName());

    SimpleWebServer mServer = null;

    public WebSMSToolService() {}

    public class LocalBinder extends Binder {
        WebSMSToolService getService() {
            return WebSMSToolService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.logVerbose("Try to start webserver.");
        mServer = new SimpleWebServer(this);

        try {
            mServer.startServer();
            LOG.logInfo("Web service has been started");
        } catch (Exception ex) {
            LOG.logError("Couldn't start web service", ex);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            mServer.stopServer();
        } catch (Exception ex) {
            LOG.logError("Error while stopping server!", ex);
        }
        super.onDestroy();
    }
}
