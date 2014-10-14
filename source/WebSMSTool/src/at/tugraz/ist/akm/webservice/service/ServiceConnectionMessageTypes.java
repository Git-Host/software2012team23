package at.tugraz.ist.akm.webservice.service;

import java.lang.reflect.Field;

public class ServiceConnectionMessageTypes
{
    public static class Client
    {
        public static class Request
        {
            public static final int REGISTER_TO_SERVICE = 1;
            public static final int UNREGISTER_TO_SERVICE = 3;
            public static final int CURRENT_RUNNING_STATE = 5;
            public static final int STOP_SERVICE = 7;
            public static final int CONNECTION_URL = 11; // TODO remove unused
                                                         // call
            public static final int REPUBLISH_STATES = 13;
            public static final int HTTP_USERNAME = 15;
            public static final int HTTP_PASSWORD = 17;
            public static final int HTTP_ACCESS_RESCRICTION_ENABLED = 19;
            public static final int START_WEB_SERVICE = 23;
        }

        public static class Response
        {
            public static final int SERVER_SETTINGS_GHANGED = 21;
        }
    }

    public static class Service
    {
        public static class Response
        {
            public static final int REGISTERED_TO_SERVICE = 20;
            public static final int CURRENT_RUNNING_STATE = 2;
            public static final int RUNNING_STATE_BEFORE_SINGULARITY = 18;
            public static final int RUNNING_STATE_STOPPED = 6;
            public static final int RUNNING_STATE_STARTING = 8;
            public static final int RUNNING_STATE_STARTED_ERRONEOUS = 10;
            public static final int RUNNING_STATE_RUNNING = 12;
            public static final int RUNNING_STATE_STOPPING = 14;
            public static final int RUNNING_STATE_STOPPED_ERRONEOUS = 16;
            public static final int CONNECTION_URL = 4;
            public static final int SMS_SENT = 22;
            public static final int SMS_SENT_ERRONEOUS = 28;
            public static final int SMS_DELIVERED = 30;
            public static final int SMS_RECEIVED = 24;
            public static final int NETWORK_TRAFFIC_TX_BYTES = 26;
            public static final int NETWORK_TRAFFIC_RX_BYTES = 32;
            public static final int HTTP_USERNAME = 34;
            public static final int HTTP_PASSWORD = 36;
            public static final int HTTP_ACCESS_RESCRICTION_ENABLED = 38;
            public static final int NETWORK_NOT_AVAILABLE = 40;
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
