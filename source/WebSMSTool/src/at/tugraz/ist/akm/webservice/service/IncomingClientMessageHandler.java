package at.tugraz.ist.akm.webservice.service;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;

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
            Messenger client = null;

            mLog.debug("incoming client message id [" + msg.what + "]");
            switch (msg.what)
            {
            case ServiceConnectionMessageTypes.Client.Request.REGISTER_TO_SERVICE:
                mService.setClient(msg.replyTo);
                break;

            case ServiceConnectionMessageTypes.Client.Request.UNREGISTER_TO_SERVICE:
                mService.setClient(null);
                break;

            case ServiceConnectionMessageTypes.Client.Request.RUNNING_STATE:
                client = mService.getClientMessenger();

                if (client != null)
                {
                    client.send(Message
                            .obtain(null,
                                    ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE,
                                    mService.getRunningState()));
                }
                break;

            case ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE:
                if (mService.getRunningState() == ServiceRunningStates.RUNNING)
                {
                    mService.stopWebSMSToolService();
                    mService.stopSelf();
                }
                ;
                break;

            case ServiceConnectionMessageTypes.Client.Request.HTTP_URL:
                client = mService.getClientMessenger();

                if (client != null)
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append(mService.getServerProtocol()).append("://")
                            .append(mService.getServerAddress()).append(":")
                            .append(mService.getServerPort());

                    client.send(Message
                            .obtain(null,
                                    ServiceConnectionMessageTypes.Service.Response.HTTP_URL,
                                    sb.toString()));
                }

                break;

            default:
                super.handleMessage(msg);
                mLog.debug("unhandled message id [" + msg.what + "]");
                break;
            }
        }
        catch (Throwable e)
        {
            mLog.error("failed reading message id [" + msg.what
                    + "] from client");
            mLog.debug("failed reading message id [" + msg.what
                    + "] from client", e);
        }
    }
}
