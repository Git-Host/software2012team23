/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.activities;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.ui.UiEvent;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceConnectionMessageTypes;

public class EventClientIncomingServiceMessageHandler extends Handler implements
        Closeable
{
    LogClient mLog = new LogClient(
            EventClientIncomingServiceMessageHandler.class.getCanonicalName());
    EventFragment mClientFragment = null;


    public EventClientIncomingServiceMessageHandler(EventFragment client)
    {
        mClientFragment = client;
    }


    @Override
    public void close() throws IOException
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
