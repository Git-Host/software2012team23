package at.tugraz.ist.akm.webservice.protocol.json;

import org.json.JSONObject;

public interface IJsonBuilder {

    public JSONObject build(Object data);
}
