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

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonFactory
{
    private LogClient log = new LogClient(JsonFactory.class.getName());
    private HashMap<Class<?>, IJsonBuilder> mJsonObjectBuilders = new HashMap<Class<?>, IJsonBuilder>();


    public JsonFactory()
    {
        mJsonObjectBuilders.put(Contact.class, new JsonContactBuilder());
        mJsonObjectBuilders
                .put(TextMessage.class, new JsonTextMessageBuilder());
        mJsonObjectBuilders.put(TelephonySignalStrength.class,
                new JsonTelephonySignalStrengthBuilder());
        mJsonObjectBuilders.put(BatteryStatus.class,
                new JsonBatteryStatusBuilder());
    }


    public JSONObject createJsonObject(Object object)
    {
        IJsonBuilder builder = mJsonObjectBuilders.get(object.getClass());
        if (builder != null)
        {
            return builder.build(object);
        }
        log.warning("no json builder available for object <"
                + object.getClass() + ">");

        return null;
    }


    public JSONObject createJsonObjectFromList(List<?> dataList)
    {
        return null;
    }
}
