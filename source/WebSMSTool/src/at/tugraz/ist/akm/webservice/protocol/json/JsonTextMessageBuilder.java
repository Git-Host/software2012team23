package at.tugraz.ist.akm.webservice.protocol.json;

import java.text.SimpleDateFormat;
import java.util.Date;

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
			
			SimpleDateFormat df = new SimpleDateFormat("d.M.y HH:mm:ss");
			json.put("date", df.format(new Date(Long.parseLong(message.getDate()))));
			
			json.put("person", message.getPerson());
			json.put("status", message.getStatus());
			json.put("seen", message.getSeen());
		} catch (JSONException jsonException) {
			log.logError("Could not create jsonTextMessage Object", jsonException);
		}
		return json;
	}
}
