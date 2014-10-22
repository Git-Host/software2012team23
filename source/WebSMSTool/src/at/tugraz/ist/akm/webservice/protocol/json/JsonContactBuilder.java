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

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.Contact.Number;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonContactBuilder implements IJsonBuilder
{

    private final static String mDefaultEncoding = "UTF8";

    public class ContactValueNames
    {
        public static final String DISPLAY_NAME = "display_name";
        public static final String ID = "id";
        public static final String IMAGE_BASE64 = "image";
        public static final String PHONE_NUMBERS = "phone_numbers";

        public class NumbersValueNames
        {
            public static final String NUMBER = "number";
            public static final String CLEAN_NUMBER = "clean_number";
            public static final String TYPE = "type";
        }
    }


    @Override
    public JSONObject build(Object data)
    {
        LogClient log = new LogClient(this);

        Contact contact = (Contact) data;
        try
        {
            JSONObject json = new JSONObject();
            json.put(ContactValueNames.DISPLAY_NAME, contact.getDisplayName());
            json.put(ContactValueNames.ID, contact.getId());

            byte[] imageBytes = contact.getPhotoBytes();
            if (imageBytes != null)
            {
                byte[] imageEncoded = Base64.encode(imageBytes, Base64.DEFAULT);
                json.put(ContactValueNames.IMAGE_BASE64, new String(
                        imageEncoded, mDefaultEncoding));
            }

            json.put(ContactValueNames.PHONE_NUMBERS,
                    buildPhoneNumbers(contact.getPhoneNumbers()));

            // log.logInfo(json.toString());
            return json;
        }
        catch (JSONException jsonException)
        {
            log.error("failed to create jsonContact Object", jsonException);
        }
        catch (UnsupportedEncodingException encEx)
        {
            log.error(
                    "failed to create jsonContact Object due to encoding error",
                    encEx);
        }
        return null;
    }


    private JSONArray buildPhoneNumbers(List<Number> phoneNumbers)
            throws JSONException
    {
        JSONArray jsonNumberList = new JSONArray();

        for (Contact.Number number : phoneNumbers)
        {
            JSONObject jsonNumber = new JSONObject();
            jsonNumber.put(ContactValueNames.NumbersValueNames.NUMBER,
                    number.getNumber());
            jsonNumber.put(ContactValueNames.NumbersValueNames.TYPE,
                    Integer.toString(number.getType()));
            jsonNumber.put(ContactValueNames.NumbersValueNames.CLEAN_NUMBER,
                    number.getNumber());
            jsonNumberList.put(jsonNumber);
        }

        return jsonNumberList;
    }
}
