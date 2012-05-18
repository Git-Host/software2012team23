package at.tugraz.ist.akm.webservice.handler.interceptor;

import my.org.apache.http.HttpRequest;
import my.org.apache.http.HttpResponse;

public interface IRequestInterceptor {
    /**
     * !!!DO NOT retrieve the entity from the given httpRequest. <br/>
     * The desired data is provided by the parameter <code>requestData</code>
     * 
     * @param httpRequest
     * @param requestData
     * @param httpResponse
     * @return
     */
    public boolean process(HttpRequest httpRequest, String requestData, HttpResponse httpResponse);
}
