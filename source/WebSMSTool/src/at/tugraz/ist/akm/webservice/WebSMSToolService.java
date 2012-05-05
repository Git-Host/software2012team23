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
    private int mPort = 8887;

    public WebSMSToolService() {
    }

    public WebSMSToolService(int port) {
        mPort = port;
    }

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
        LOG.v("Try to start webserver.");
        mServer = new SimpleWebServer(this);

        try {
            mServer.startServer(mPort);
            LOG.i("Web service has been started on port <" + mPort + ">");
        } catch (Exception e) {
            LOG.e("Couldn't start web service on port <" + mPort + ">", e);
        }

        // try {
        // SSLContext sc = SSLContext.getInstance("TLS");
        // KeyManagerFactory keyFactory =
        // KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // KeyStore kStore = KeyStore.getInstance(KeyStore.getDefaultType());
        //
        // InputStream is = this.getResources().openRawResource(R.raw.websms);
        // kStore.load(is, "foobar64".toCharArray());
        //
        // keyFactory.init(kStore, "foobar64".toCharArray());
        // KeyManager[] keyManager = keyFactory.getKeyManagers();
        // sc.init(keyManager, null, new SecureRandom());
        //
        // mServer = new LocalTestServer(this, sc);
        // } catch(Exception e) {
        // LOG.e("Error creating sslsocket!", e);
        // }
        //
        // try {
        // mServer.start(mPort);
        // LOG.i("Web service has been started on port <" + mPort + ">");
        // } catch (Exception e) {
        // LOG.e("Couldn't start web service on port <" + mPort + ">", e);
        // }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            mServer.stopServer();
        } catch (Exception e) {
            LOG.e("Error while stopping server!", e);
        }
        super.onDestroy();
    }
}