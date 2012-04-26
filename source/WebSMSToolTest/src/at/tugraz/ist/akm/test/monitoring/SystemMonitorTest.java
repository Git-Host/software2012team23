package at.tugraz.ist.akm.test.monitoring;

import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class SystemMonitorTest extends WebSMSToolActivityTestcase {

	public SystemMonitorTest() {
		super(SystemMonitorTest.class.getSimpleName());
	}

	public void testSystemMonitorBatteryState() {
		try {
			SystemMonitor sm = new SystemMonitor(mContext);
			SystemMonitor.BatteryStatus bStats = sm.getBatteryStatus();
			logBStats(bStats);
			assertTrue(bStats.batteryIconIdSmall > 0);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	private void logBStats(SystemMonitor.BatteryStatus bStats) {
		log(" BatteryStatus: batteryIconIdSmall [" + bStats.batteryIconIdSmall
				+ "] isCharging [" + bStats.isCharging + "] isFull["
				+ bStats.isFull + "] chargePlug[" + bStats.chargePlug
				+ "]usbCharge [" + bStats.usbCharge + "] acCharge["
				+ bStats.acCharge + "]");
	}

}
