package at.tugraz.ist.akm.webservice.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceConnectionMessageTypes;

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
            case ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_MANAGEMENT:
                mService.onManagementClientRequestRegisterForManagement(msg.replyTo);
                break;

            case ServiceConnectionMessageTypes.Client.Request.UNREGISTER_FROM_SERVICE_MANAGEMENT:
                mService.onManagementClientRequestRegisterForManagement(null);
                break;

            case ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_EVENTS:
                mService.onEventClientRequestRegisterForEvents(msg.replyTo);
                break;
                
            case ServiceConnectionMessageTypes.Client.Request.UNREGISTER_FROM_SERVICE_EVENTS:
                mService.onEventClientRequestRegisterForEvents(null);
                break;

            case ServiceConnectionMessageTypes.Client.Request.CURRENT_RUNNING_STATE:
                mService.onManagementClientRequestCurrentRunningState();
                break;

            case ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE:
                mService.onManagementClientRequestStopWEBService();
                break;

            case ServiceConnectionMessageTypes.Client.Request.START_WEB_SERVICE:
                mService.onManagementClientRequestStartWEBService();
                break;

            case ServiceConnectionMessageTypes.Client.Request.CONNECTION_URL:
                mService.onManagementClientRequestConnectionUrl();
                break;

            case ServiceConnectionMessageTypes.Client.Request.REPUBLISH_STATES:
                mService.onManagementClientRequestRepublishStates();
                break;

            case ServiceConnectionMessageTypes.Client.Request.HTTP_PASSWORD:
                mService.onManagementClientRequestHttpPassword();
                break;

            case ServiceConnectionMessageTypes.Client.Request.HTTP_USERNAME:
                mService.onManagementClientRequestHttpUsername();
                break;

            case ServiceConnectionMessageTypes.Client.Response.SERVER_SETTINGS_GHANGED:
                WebserverProtocolConfig newSettings = newServerSettingsFromBundle(msg
                        .getData());
                mService.onManagementClientResponseServerSettingsChanged(newSettings);
                break;

            default:
                super.handleMessage(msg);
                mLog.debug("unhandled message id [" + msg.what + "]");
                break;
            }
        }
        catch (Throwable e)
        {
            mLog.error("failed reading message id ["
                    + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "]");
            mLog.debug("failed reading message id ["
                    + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "] from client", e);
        }
    }


    private WebserverProtocolConfig newServerSettingsFromBundle(Bundle data)
    {

        WebserverProtocolConfig newSettings = new WebserverProtocolConfig();

        newSettings.isHttpsEnabled = data
                .getBoolean(ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_HTTPS);
        newSettings.isUserAuthEnabled = data
                .getBoolean(ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_USER_AUTH);
        newSettings.password = data
                .getString(ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD);
        newSettings.protocolName = data
                .getString(ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PROTOCOL);
        newSettings.username = data
                .getString(ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME);
        newSettings.port = data
                .getInt(ServiceConnectionMessageTypes.Bundle.Key.INT_ARG_SERVER_PORT);

        return newSettings;
    }
}
