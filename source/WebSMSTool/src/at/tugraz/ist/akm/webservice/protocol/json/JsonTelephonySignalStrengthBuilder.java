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
            json.put("signal_icon", signal.getSignalStrengthIconBytes());
            json.put("level", signal.getLevel());
            json.put("cdma_level", signal.getCdmaLevel());
            json.put("gsm_level", signal.getGsmLevel());
            json.put("evdo_level", signal.getEvdoLevel());
            return json;
        } catch (JSONException e) {
			log.logE("Could not create jsonTelephonySignalStrength Object",e);
        }
        return null;
    }

}
