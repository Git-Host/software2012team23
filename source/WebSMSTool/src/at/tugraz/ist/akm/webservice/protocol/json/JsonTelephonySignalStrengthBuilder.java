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

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.trace.Logable;

public class JsonTelephonySignalStrengthBuilder implements IJsonBuilder {

    @Override
    public JSONObject build(Object data) {
    	Logable log = new Logable(this.getClass().getSimpleName());
    	
        TelephonySignalStrength signal = (TelephonySignalStrength) data;
        try {
            JSONObject json = new JSONObject();
            json.put("signal_strength", signal.getSignalStrength());
            json.put("signal_icon", new String(signal.getSignalStrengthIconBytes()));
            json.put("level", signal.getLevel());
            json.put("cdma_level", signal.getCdmaLevel());
            json.put("gsm_level", signal.getGsmLevel());
            json.put("evdo_level", signal.getEvdoLevel());
            return json;
        } catch (JSONException jsonException) {
			log.logError("Could not create jsonTelephonySignalStrength Object",jsonException);
        }
        return null;
    }

}
