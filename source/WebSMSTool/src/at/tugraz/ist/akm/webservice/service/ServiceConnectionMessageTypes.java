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
            public static final int CONNECTION_URL = 11; // TODO remove
            public static final int REPUBLISH_STATES = 13;
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
        }
    }

    public static class Bundle
    {
        public static class Key
        {
            public static final String CONNECTION_URL_STRING = "CONNECTION_URL";
        }
    }


    public static String getMessageName(int messageId)
    {
        String fieldName = null;

        fieldName = getFieldOfClass(
                ServiceConnectionMessageTypes.Client.Request.class, messageId);
        if (fieldName == null)
        {
            fieldName = getFieldOfClass(
                    ServiceConnectionMessageTypes.Service.Response.class,
                    messageId);
        }

        if (fieldName == null)
        {
            return "invalid message id";
        }
        return fieldName;

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
