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
        public final static String VALUE_CONTENT_TYPE_JSON = "application/json";
    }

}
