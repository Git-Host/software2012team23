package at.tugraz.ist.akm.activities;

import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.ServiceConnectionMessageTypes;

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
            mLog.debug("incoming service message ["
                    + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "]");
            switch (msg.what)
            {

            case ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL:
                mClientFragment
                        .onWebServiceURLChanged(msg
                                .getData()
                                .getString(
                                        ServiceConnectionMessageTypes.Bundle.Key.CONNECTION_URL_STRING));
                break;

            case ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE:
                switch (msg.arg1)
                {
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_BEFORE_SINGULARITY:
                    mClientFragment.onWebServiceRunningStateBeforeSingularity();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_RUNNING:
                    mClientFragment.onWebServiceRunning();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTED_ERRONEOUS:
                    mClientFragment.onWebServiceStartErroneous();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTING:
                    mClientFragment.onWebServiceStarting();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED:
                    mClientFragment.onWebServiceStopped();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED_ERRONEOUS:
                    mClientFragment.onWebServiceStoppedErroneous();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPING:
                    mClientFragment.onWebServiceStopping();
                    break;
                default:
                    mLog.debug("failed handling ["
                            + ServiceConnectionMessageTypes
                                    .getMessageName(ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE)
                            + "] = ["
                            + ServiceConnectionMessageTypes
                                    .getMessageName(msg.arg1) + "]");
                    break;
                }
                break;
            case ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE:
                mClientFragment.onWebServiceClientRegistered();
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
