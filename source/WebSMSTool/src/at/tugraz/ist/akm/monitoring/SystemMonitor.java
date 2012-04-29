package at.tugraz.ist.akm.monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import at.tugraz.ist.akm.trace.Logable;

public class SystemMonitor extends Logable {

	private Context mContext = null;

	public SystemMonitor(Context c) {
		super(SystemMonitor.class.getSimpleName());
		mContext = c;
	}

	public BatteryStatus getBatteryStatus() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = mContext.registerReceiver(null, filter);
		return new BatteryStatus(mContext, batteryStatus);
	}
}
