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
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonTextMessageBuilder implements IJsonBuilder
{

    public class TextMessageValueNames
    {
        public static final String ID = "id";
        public static final String THREAD_ID = "thread_id";
        public static final String ADDRESS = "address";
        public static final String BODY = "body";
        public static final String DATE = "date";
        public static final String PERSON = "person";
        public static final String STATUS = "status";
        public static final String SEEN = "seen";

    }


    @Override
    public JSONObject build(Object data)
    {
        LogClient log = new LogClient(this);
        TextMessage message = (TextMessage) data;

        JSONObject json = new JSONObject();
        try
        {
            json.put(TextMessageValueNames.ID, message.getId());
            json.put(TextMessageValueNames.THREAD_ID, message.getThreadId());
            json.put(TextMessageValueNames.ADDRESS,
                    Contact.Number.cleanNumber(message.getAddress()));
            json.put(TextMessageValueNames.BODY, message.getBody());

            SimpleDateFormat df = new SimpleDateFormat("d.M.y HH:mm:ss",
                    Locale.getDefault());
            json.put(TextMessageValueNames.DATE,
                    df.format(new Date(Long.parseLong(message.getDate()))));

            json.put(TextMessageValueNames.PERSON, message.getPerson());
            json.put(TextMessageValueNames.STATUS, message.getStatus());
            json.put(TextMessageValueNames.SEEN, message.getSeen());
        }
        catch (JSONException jsonException)
        {
            log.error("Could not create jsonTextMessage Object", jsonException);
        }
        return json;
    }
}
