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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.Contact.Number;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonContactBuilder implements IJsonBuilder {

    @Override
    public JSONObject build(Object data) {
    	LogClient log = new LogClient(this);
    	
        Contact contact = (Contact) data;
        try {
            JSONObject json = new JSONObject();
            json.put("display_name", contact.getDisplayName());
            json.put("last_name", contact.getFamilyName());
            json.put("name", contact.getName());
            json.put("id", contact.getId());
            
            byte[] imageBytes = contact.getPhotoBytes();
            if(imageBytes != null){
            	byte[] imageEncoded = Base64.encode(imageBytes, Base64.DEFAULT);
                json.put("image", new String(imageEncoded));       
            }
            
            json.put("phone_numbers", buildPhoneNumbers(contact.getPhoneNumbers()));
            
            //log.logInfo(json.toString());
            return json;
        } catch (JSONException jsonException) {
			log.error("Could not create jsonContact Object",jsonException);
        }
        return null;
    }

    private JSONArray buildPhoneNumbers(List<Number> phoneNumbers) throws JSONException {
        JSONArray jsonNumberList = new JSONArray();

        for (Contact.Number number : phoneNumbers) {
            JSONObject jsonNumber = new JSONObject();
            jsonNumber.put("number", number.getNumber());
            jsonNumber.put("type", Integer.toString(number.getType()));
            jsonNumber.put("clean_number", number.getNumber());
            jsonNumberList.put(jsonNumber);
        }

        return jsonNumberList;
    }
}
