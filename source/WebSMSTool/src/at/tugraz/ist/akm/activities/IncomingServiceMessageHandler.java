package at.tugraz.ist.akm.activities;

import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.ServiceConnectionMessageTypes;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;

public class IncomingServiceMessageHandler extends Handler
{
    LogClient mLog = new LogClient(
            IncomingServiceMessageHandler.class.getCanonicalName());
    StartServiceFragment mClientFragment = null;


    public IncomingServiceMessageHandler(StartServiceFragment client)
    {
        mClientFragment = client;
    }


    @Override
    public void handleMessage(Message msg)
    {
        try
        {
            mLog.debug("incoming service message id [" + msg.what + "]");
            switch (msg.what)
            {

            case ServiceConnectionMessageTypes.Service.Response.HTTP_URL:
                mClientFragment.setHttpUrl((String) msg.obj);
                break;

            case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE:
                mClientFragment
                        .setServiceRunningState((WebSMSToolService.ServiceRunningStates) msg.obj);
                break;

            default:
                super.handleMessage(msg);
                break;
            }
        }
        catch (NullPointerException e)
        {
            mLog.error("failed reading message from client");
            mLog.debug("failed reading message from client", e);
        }
    }
}
