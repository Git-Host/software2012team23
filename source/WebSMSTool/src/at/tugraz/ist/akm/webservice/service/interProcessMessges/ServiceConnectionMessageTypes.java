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

import java.lang.reflect.Field;

public class ServiceConnectionMessageTypes
{
    public static class Client
    {
        public static class Request
        {
            public static final int REGISTER_FOR_SERVICE_MANAGEMENT = 1;
            public static final int UNREGISTER_FROM_SERVICE_MANAGEMENT = 3;
            public static final int CURRENT_RUNNING_STATE = 5;
            public static final int STOP_SERVICE = 7;
            public static final int CONNECTION_URL = 9; // TODO remove unused
                                                        // call
            public static final int REPUBLISH_STATES = 11;
            public static final int HTTP_USERNAME = 13;
            public static final int HTTP_PASSWORD = 15;
            public static final int HTTP_ACCESS_RESCRICTION_ENABLED = 17;
            public static final int START_WEB_SERVICE = 19;

            public static final int REGISTER_FOR_SERVICE_EVENTS = 21;
            public static final int UNREGISTER_FROM_SERVICE_EVENTS = 23;
        }

        public static class Response
        {
            public static final int SERVER_SETTINGS_GHANGED = 101;
        }
    }

    public static class Service
    {
        public static class Response
        {
            public static final int REGISTERED_TO_SERVICE_MANAGEMENT = 2;
            public static final int CURRENT_RUNNING_STATE = 4;
            public static final int RUNNING_STATE_BEFORE_SINGULARITY = 6;
            public static final int RUNNING_STATE_STOPPED = 8;
            public static final int RUNNING_STATE_STARTING = 10;
            public static final int RUNNING_STATE_STARTED_ERRONEOUS = 12;
            public static final int RUNNING_STATE_RUNNING = 14;
            public static final int RUNNING_STATE_STOPPING = 16;
            public static final int RUNNING_STATE_STOPPED_ERRONEOUS = 18;
            public static final int CONNECTION_URL = 20;
            public static final int SMS_SENT = 22;
            public static final int SMS_SENT_ERRONEOUS = 24;
            public static final int SMS_DELIVERED = 26;
            public static final int SMS_RECEIVED = 28;
            public static final int NETWORK_TRAFFIC_TX_BYTES = 30;
            public static final int NETWORK_TRAFFIC_RX_BYTES = 32;
            public static final int HTTP_USERNAME = 34;
            public static final int HTTP_PASSWORD = 36;
            public static final int HTTP_ACCESS_RESCRICTION_ENABLED = 38;
            public static final int NETWORK_NOT_AVAILABLE = 40;

            public static final int REGISTERED_TO_SERVICE_EVENTS = 42;
            public static final int SERVICE_EVENT = 44;

        }
    }

    public static class Bundle
    {
        public static class Key
        {
            public static final String STRING_ARG_CONNECTION_URL = "STRING_ARG_CONNECTION_URL";
            public static final String STRING_ARG_SERVER_USERNAME = "STRING_ARG_SERVER_USERNAME";
            public static final String STRING_ARG_SERVER_PASSWORD = "STRING_ARG_SERVER_PASSWORD";
            public static final String STRING_ARG_SERVER_PROTOCOL = "STRING_ARG_SERVER_PROTOCOL";
            public static final String INT_ARG_SERVER_PORT = "INT_ARG_SERVER_PORT";
            public static final String BOOLEAN_ARG_SERVER_HTTPS = "BOOLEAN_ARG_SERVER_HTTPS";
            public static final String BOOLEAN_ARG_SERVER_USER_AUTH = "BOOLEAN_ARG_SERVER_USER_AUTH";
            public static final String PARCELABLE_UI_EVENT = "PARCELABLE_UI_EVENT";
            public static final String PARCELABLE_UI_EVENT_LIST = "PARCELABLE_UI_EVENT_LIST";
        }
    }


    public static String getMessageName(int messageId)
    {
        String fieldName = null;

        fieldName = getFieldOfClass(
                ServiceConnectionMessageTypes.Client.Request.class, messageId);
        if (fieldName != null)
        {
            return fieldName;
        }

        fieldName = getFieldOfClass(
                ServiceConnectionMessageTypes.Client.Response.class, messageId);
        if (fieldName != null)
        {
            return fieldName;
        }

        fieldName = getFieldOfClass(
                ServiceConnectionMessageTypes.Service.Response.class, messageId);
        if (fieldName != null)
        {
            return fieldName;
        }

        return null;
    }


    private static String getFieldOfClass(Class<?> clasz, int value)
    {
        for (Field field : clasz.getDeclaredFields())
        {
            try
            {
                if (value == field.getInt(null))
                {
                    return field.getName();
                }
            }
            catch (IllegalAccessException e)
            {
            }
        }
        return null;
    }
}
