package at.tugraz.ist.akm.monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import at.tugraz.ist.akm.trace.Logable;

public class SystemMonitor extends Logable {

	// TODO: to be refactored
	public static class BatteryStatus {
		public int batteryLevel = 0;
		public int batteryIconIdSmall = 0;
		public boolean isCharging = false;
		public boolean isFull = false;
		public int chargePlug = 0;
		public boolean usbCharge = false;
		public boolean acCharge = false;

		public BatteryStatus(Intent batteryStatus) {
			int extraStatus = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_STATUS, -1);
			isCharging = (extraStatus == BatteryManager.BATTERY_STATUS_CHARGING || extraStatus == BatteryManager.BATTERY_STATUS_FULL);
			isFull = (extraStatus == BatteryManager.BATTERY_STATUS_FULL);
			chargePlug = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_PLUGGED, -1);
			usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
			acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC);

			batteryIconIdSmall = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_ICON_SMALL, -1);
			batteryLevel = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_LEVEL, -1);
		}
	}

	private Context mContext = null;

	public SystemMonitor(Context c) {
		super(SystemMonitor.class.getSimpleName());
		mContext = c;
	}

	public BatteryStatus getBatteryStatus() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = mContext.registerReceiver(null, filter);
		return new BatteryStatus(batteryStatus);
	}
}
