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

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class ServiceMessageBuilder
{

    static public Message newEventClientRegistrationMessage(Messenger client)
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_EVENTS);
        message.replyTo = client;
        return message;
    }


    static public Message newEventClientUnregistrationMessage()
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_EVENTS);
        return message;
    }


    static public Message newManagementClientRegistrationMessage(
            Messenger client)
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.REGISTER_FOR_SERVICE_MANAGEMENT);
        message.replyTo = client;
        return message;
    }


    static public Message newManagementClientUnregistrationMessage()
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Request.UNREGISTER_FROM_SERVICE_MANAGEMENT);
        return message;
    }


    static public Message newStopServiceMessage()
    {
        return Message.obtain(null,
                ServiceConnectionMessageTypes.Client.Request.STOP_SERVICE);
    }


    static public Message newStartServiceMessage(WebserverProtocolConfig config)
    {
        Message message = Message.obtain(null,
                ServiceConnectionMessageTypes.Client.Request.START_WEB_SERVICE);
        Bundle data = new Bundle();
        putServerconfigToBundle(data, config);
        message.setData(data);
        return message;
    }


    static public Message newServiceConfigurationChanged(
            WebserverProtocolConfig config)
    {
        Message message = Message
                .obtain(null,
                        ServiceConnectionMessageTypes.Client.Response.SERVER_SETTINGS_GHANGED);
        Bundle data = new Bundle();
        putServerconfigToBundle(data, config);
        message.setData(data);
        return message;
    }


    static public Message newRepublishStatesMessage()
    {
        return Message.obtain(null,
                ServiceConnectionMessageTypes.Client.Request.REPUBLISH_STATES);
    }


    static private void putServerconfigToBundle(Bundle aBundle,
            WebserverProtocolConfig configToStore)
    {
        aBundle.putBoolean(
                ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_HTTPS,
                configToStore.isHttpsEnabled);
        aBundle.putBoolean(
                ServiceConnectionMessageTypes.Bundle.Key.BOOLEAN_ARG_SERVER_USER_AUTH,
                configToStore.isUserAuthEnabled);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD,
                configToStore.password);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PROTOCOL,
                configToStore.protocolName);
        aBundle.putString(
                ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME,
                configToStore.username);
        aBundle.putInt(
                ServiceConnectionMessageTypes.Bundle.Key.INT_ARG_SERVER_PORT,
                configToStore.port);
    }
}
