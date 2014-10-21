package at.tugraz.ist.akm.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.ui.UiEvent;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceConnectionMessageTypes;

public class EventClientIncomingServiceMessageHandler extends Handler
{
    LogClient mLog = new LogClient(
            EventClientIncomingServiceMessageHandler.class.getCanonicalName());
    EventFragment mClientFragment = null;


    public EventClientIncomingServiceMessageHandler(EventFragment client)
    {
        mClientFragment = client;
    }


    public void onClose()
    {
        mClientFragment = null;
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

            case ServiceConnectionMessageTypes.Service.Response.SERVICE_EVENT:
                extractParcelableEvents(msg.getData());
                break;

            case ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE_MANAGEMENT:
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


    private void extractParcelableEvents(Bundle data)
    {
        data.setClassLoader(UiEvent.class.getClassLoader());
        boolean hasExtractedEvent = false;
        try
        {
            UiEvent event = data
                    .getParcelable(ServiceConnectionMessageTypes.Bundle.Key.PARCELABLE_UI_EVENT);
            if (event != null)
            {
                mLog.debug("received single event");
                mClientFragment.onWebServiceEvent(event);
                hasExtractedEvent = true;
            } else
            {
                mLog.debug("failed unmarshalling single event, trying ArrayList ...");
            }
        }
        catch (Throwable e)
        {
            mLog.debug("failed unmarshalling single event", e);
        }

        if (hasExtractedEvent == false)
        {
            try
            {
                ArrayList<UiEvent> events = data
                        .getParcelableArrayList(ServiceConnectionMessageTypes.Bundle.Key.PARCELABLE_UI_EVENT_LIST);
                if (events != null)
                {
                    mLog.debug("received [" + events.size() + "] events");
                    mClientFragment.onWebServiceEvent(events);
                } else
                {
                    mLog.debug("failed unmarshalling events from ArrayList");
                }
            }
            catch (Throwable f)
            {
                mLog.debug("failed unmarshalling events from ArrayList", f);
            }
        }
    }
}
