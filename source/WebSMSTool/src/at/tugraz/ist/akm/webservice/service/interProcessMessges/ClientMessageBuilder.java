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

package at.tugraz.ist.akm.webservice.service.interProcessMessges;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import at.tugraz.ist.akm.trace.ui.UiEvent;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;

public class ClientMessageBuilder
{
    static public Message newRegisteredToServiceEventsMessage()
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE_EVENTS);
    }


    static public Message newRegisteredToServiceManagementMessage()
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE_MANAGEMENT);
    }


    static public Message newSmsSentMessage(int totalSentCount)
    {
        return Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.SMS_SENT,
                totalSentCount, 0);
    }


    static public Message newSmsSentErroneousMessage(int totalSentCount)
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.SMS_SENT_ERRONEOUS,
                        totalSentCount, 0);
    }


    static public Message newSmsDeliveredMessage(int totalSentCount)
    {
        return Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.SMS_DELIVERED,
                totalSentCount, 0);
    }


    static public Message newSmsReceivedMessage(int totalSentCount)
    {
        return Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.SMS_RECEIVED,
                totalSentCount, 0);
    }


    static public Message newHttpAccessRestrictionMessage(
            Boolean isRestrictionEnabled)
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.HTTP_ACCESS_RESCRICTION_ENABLED,
                        (isRestrictionEnabled) ? 1 : 0, 0);
    }


    static public Message newHttpPasswordMessage(String password)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD);

        Bundle data = new Bundle();
        data.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD,
                password);
        message.setData(data);
        return message;
    }


    static public Message newHttpUsernameMessage(String username)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME);
        Bundle data = new Bundle();
        data.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME,
                username);
        message.setData(data);
        return message;
    }


    static public Message newReceivedBytesCountMessage(int rxBytes)
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_RX_BYTES,
                        rxBytes, 0);
    }


    static public Message newSentBytesCountMessage(int txBytes)
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_TX_BYTES,
                        txBytes, 0);
    }


    static public Message newNetworkNotAvailableMessage()
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.NETWORK_NOT_AVAILABLE);
    }


    static public Message newConnectionUrlMessage(String url)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL);
        Bundle bundle = new Bundle();
        bundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_CONNECTION_URL,
                url);
        message.setData(bundle);
        return message;
    }


    static public Message newCurrentRunningStateMessage(
            ServiceRunningStates runningState)
    {
        return Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE,
                        translateRunningStateToInt(runningState), 0);
    }


    static public Message newServiceEventMessage(UiEvent event)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.SERVICE_EVENT);
        Bundle data = new Bundle();
        data.putParcelable(
                ServiceConnectionMessageTypes.Bundle.Key.PARCELABLE_UI_EVENT,
                event);
        message.setData(data);
        return message;
    }


    static public Message newServiceEventMessage(ArrayList<UiEvent> events)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Service.Response.SERVICE_EVENT);
        Bundle data = new Bundle();
        data.putParcelableArrayList(
                ServiceConnectionMessageTypes.Bundle.Key.PARCELABLE_UI_EVENT_LIST,
                events);
        message.setData(data);
        return message;
    }


    static public Message newEventClientRegistrationMessage(Messenger client)
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_EVENTS);
        message.replyTo = client;
        return message;
    }


    public static int translateRunningStateToInt(ServiceRunningStates state)
    {
        switch (state)
        {
        case BEFORE_SINGULARITY:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_BEFORE_SINGULARITY;
        case STARTED_ERRONEOUS:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTED_ERRONEOUS;
        case RUNNING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_RUNNING;
        case STARTING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTING;
        case STOPPED:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED;
        case STOPPED_ERRONEOUS:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED_ERRONEOUS;
        case STOPPING:
            return ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPING;
        default:
            return -1;
        }
    }

}
