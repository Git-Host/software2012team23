package at.tugraz.ist.akm.test.monitoring;

import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class SystemMonitorTest extends WebSMSToolActivityTestcase {

	public SystemMonitorTest() {
		super(SystemMonitorTest.class.getSimpleName());
	}

	public void testSystemMonitorBatteryState() {
		try {
			SystemMonitor sm = new SystemMonitor(mContext);
			BatteryStatus bStats = sm.getBatteryStatus();
			logBStats(bStats);
			assertTrue(bStats.getBatteryIconId() > 0);
			byte [] bytes = bStats.getBatteryIconBytes();
			assertTrue(bytes.length > 0);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	private void logBStats(BatteryStatus bStats) {
		logV(" BatteryStatus: batteryIconIdSmall [" + bStats.getBatteryIconId()
				+ "] isCharging [" + bStats.getIsCharging() + "] isFull["
				+ bStats.getIsFull() + "] chargePlug[" + bStats.getChargePlug()
				+ "]usbCharge [" + bStats.getIsUsbCharge() + "] acCharge["
				+ bStats.getIsAcCharge() + "]");
	}

}
