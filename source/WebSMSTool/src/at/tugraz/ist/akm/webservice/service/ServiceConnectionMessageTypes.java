package at.tugraz.ist.akm.webservice.service;

public class ServiceConnectionMessageTypes
{
    public static class Client
    {
        public static class Request
        {
            public static final int REGISTER_TO_SERVICE = 1;
            public static final int UNREGISTER_TO_SERVICE = 3;
            public static final int RUNNING_STATE = 5;
            public static final int STOP_SERVICE = 7;
            public static final int HTTP_URL = 11;
        }
    }

    public static class Service
    {
        public static class Response
        {
            public static final int RUNNING_STATE = 2;
            public static final int HTTP_URL = 4;
        }
    }
}
