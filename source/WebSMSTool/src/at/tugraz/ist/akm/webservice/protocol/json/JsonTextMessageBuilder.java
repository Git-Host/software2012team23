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

package at.tugraz.ist.akm.webservice.protocol.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.Contact;
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
        	json.put("address", Contact.Number.cleanNumber(message.getAddress()));
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
