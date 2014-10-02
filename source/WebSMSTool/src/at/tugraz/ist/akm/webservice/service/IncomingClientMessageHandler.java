package at.tugraz.ist.akm.webservice.service;

import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;

class IncomingClientMessageHandler extends Handler
{
    private WebSMSToolService mService = null;

    private LogClient mLog = new LogClient(
            IncomingClientMessageHandler.class.getCanonicalName());


    public IncomingClientMessageHandler(WebSMSToolService service)
    {
        mService = service;
    }


    @Override
    public void handleMessage(Message msg)
    {
        try
        {
            mLog.debug("incoming client message ["
                    + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "]");
            switch (msg.what)
            {
            case ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE:
                mService.onClientRequestRegister(msg.replyTo);
                break;

            case ServiceConnectionMessageTypes.Client.Request.UNREGISTER_TO_SERVICE:
                mService.onClientRequestRegister(null);
                break;

            case ServiceConnectionMessageTypes.Client.Request.CURRENT_RUNNING_STATE:
                mService.onClientRequestCurrentRunningState();
                break;

            case ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE:
                mService.onClientRequstStopService();
                break;

            case ServiceConnectionMessageTypes.Client.Request.CONNECTION_URL:
                mService.onClientRequestConnectionUrl();
                break;

            case ServiceConnectionMessageTypes.Client.Request.REPUBLISH_STATES:
                mService.onClientRequestRepublishStates();
                break;

            default:
                super.handleMessage(msg);
                mLog.debug("unhandled message id [" + msg.what + "]");
                break;
            }
        }
        catch (Throwable e)
        {
            mLog.error("failed reading message id [" + ServiceConnectionMessageTypes.getMessageName(msg.what) + "]");
            mLog.debug("failed reading message id [" + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "] from client", e);
        }
    }
}
