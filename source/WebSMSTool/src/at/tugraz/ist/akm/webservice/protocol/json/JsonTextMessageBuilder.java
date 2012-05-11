package at.tugraz.ist.akm.webservice.protocol.json;

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.Logable;

public class JsonTextMessageBuilder implements IJsonBuilder {

	@Override
	public JSONObject build(Object data) {
    	Logable log = new Logable(this.getClass().getSimpleName());
		TextMessage message = (TextMessage) data;
		
        JSONObject json = new JSONObject();
        try {
			json.put("id", message.getId());
			json.put("thread_id", message.getThreadId());
        	json.put("address", message.getAddress());
			json.put("body", message.getBody());
			json.put("date", message.getDate());
			json.put("person", message.getPerson());
			json.put("status", message.getStatus());
			json.put("seen", message.getSeen());
		} catch (JSONException e) {
			log.logE("Could not create jsonTextMessage Object",e);
		}
		return json;
	}
}
