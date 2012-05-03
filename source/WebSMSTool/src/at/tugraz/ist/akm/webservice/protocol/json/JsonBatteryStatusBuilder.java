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
			json.put("battery_level_icon", status.getBatteryIconBytes());
			json.put("is_charging", status.getIsCharging());
        	json.put("is_ac_charge", status.getIsAcCharge());
			json.put("is_usb_charge", status.getIsUsbCharge());
			json.put("is_full", status.getIsFull());
		} catch (JSONException e) {
			log.logE("Could not create jsonBatteryStatus Object",e);
		}
		return json;
	}
}
