package at.tugraz.ist.akm.webservice;

public class WebServerConfig {
    /*
     * 
     * XML Configuration
     */
    public final static class XML {
        public final static String ATTRIBUTE_DATA_FILE = "dataFile";
        public final static String ATTRIBUTE_CONTENT_TYPE = "contentType";
        public final static String ATTRIBUTE_URI_PATTERN = "uriPattern";
        public final static String ATTRIBUTE_CLASS = "class";

        public final static String TAG_REQUEST = "request";
        public final static String TAG_REQUEST_HANDLER = "requestHandler";
        
        public final static String TAG_REQUEST_INTERCEPTORS = "requestInterceptors";
        public final static String TAG_INTERCEPTOR = "interceptor";
    }

    /*
     * 
     * File Paths
     */
    public final static class RES {
        public final static String BASE_PATH = "web";
        public final static String WEB_XML = BASE_PATH + "/web.xml";
    }

    public final static class HTTP {
        public final static String KEY_CONTENT_TYPE = "Content-Type";
        public final static String CONTENT_TYPE_JSON = "application/json";
        public final static String REQUEST_TYPE_POST = "POST";
        public final static String HEADER_COOKIE = "Cookie";
        public final static String HEADER_SET_COOKIE = "Set-Cookie";
    }

    public final static class JSON {
        public final static String METHOD = "method";
        public final static String PARAMS = "params";
        public final static String STATE_SUCCESS = "success";
        public final static String STATE_ERROR = "error";
        public final static String STATE_SESSION_COOKIE_EXPIRED = "session_cookie_expired";
        public final static String STATE = "state";
        public final static String ERROR_MSG = "error_msg";
    }

}
