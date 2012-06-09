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

import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.trace.Logable;

public class JsonBatteryStatusBuilder implements IJsonBuilder {

	@Override
	public JSONObject build(Object data) {
    	Logable log = new Logable(this.getClass().getSimpleName());
		BatteryStatus status = (BatteryStatus) data;
		
        JSONObject json = new JSONObject();
        try {
			json.put("battery_level", status.getBatteryLevel());
			json.put("battery_level_icon", new String(status.getBatteryIconBytes()));
			json.put("is_charging", status.getIsCharging());
        	json.put("is_ac_charge", status.getIsAcCharge());
			json.put("is_usb_charge", status.getIsUsbCharge());
			json.put("is_full", status.getIsFull());
		} catch (JSONException jsonException) {
			log.logError("Could not create jsonBatteryStatus Object",jsonException);
		}
		return json;
	}
}
