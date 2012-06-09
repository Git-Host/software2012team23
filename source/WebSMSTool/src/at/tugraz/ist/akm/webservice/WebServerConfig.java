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
        public final static String UNAUTHORIZED = BASE_PATH + "/unauthorized.html";
    }

    public final static class HTTP {
        public final static String KEY_CONTENT_TYPE = "Content-Type";
        public final static String CONTENT_TYPE_JSON = "application/json";
        public final static String CONTENTY_TYPE_TEXT_HTML = "text/html";
        public final static String REQUEST_TYPE_POST = "POST";

        public final static String HEADER_AUTHENTICATION = "Authorization";
        public final static String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
        public final static String AUTHENTICATION_REALM = "Web SMS Tool";
        
        public final static int HTTP_CODE_OK = 200;
        public final static int HTTP_CODE_UNAUTHORIZED = 401;
        
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
