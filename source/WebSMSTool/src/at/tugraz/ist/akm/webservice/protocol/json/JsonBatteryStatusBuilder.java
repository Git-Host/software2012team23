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

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonBatteryStatusBuilder implements IJsonBuilder
{

    private final static String mDefaultEncoding = "UTF8";

    public class BatteryLevelValueNames
    {
        public static final String BATTERY_LEVEL = "battery_level";
        public static final String BATTERY_LEVEL_ICON = "battery_level_icon";
        public static final String IS_CHARGING = "is_charging";
        public static final String IS_AC_CHARGE = "is_ac_charge";
        public static final String IS_USB_CHARGE = "is_usb_charge";
        public static final String IS_FULL = "is_full";
    }


    @Override
    public JSONObject build(Object data)
    {
        LogClient log = new LogClient(this);
        BatteryStatus status = (BatteryStatus) data;

        JSONObject json = new JSONObject();
        try
        {
            json.put(BatteryLevelValueNames.BATTERY_LEVEL,
                    status.getBatteryLevel());
            json.put(BatteryLevelValueNames.BATTERY_LEVEL_ICON, new String(
                    status.getBatteryIconBytes(), mDefaultEncoding));
            json.put(BatteryLevelValueNames.IS_CHARGING, status.getIsCharging());
            json.put(BatteryLevelValueNames.IS_AC_CHARGE,
                    status.getIsAcCharge());
            json.put(BatteryLevelValueNames.IS_USB_CHARGE,
                    status.getIsUsbCharge());
            json.put(BatteryLevelValueNames.IS_FULL, status.getIsFull());
        }
        catch (JSONException jsonException)
        {
            log.error("failed to create jsonBatteryStatus Object",
                    jsonException);
        }
        catch (UnsupportedEncodingException e)
        {
            log.error(
                    "failed to create jsonBatteryStatus Object due to encoding error",
                    e);
        }
        return json;
    }
}
