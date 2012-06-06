package at.tugraz.ist.akm.test.monitoring;

import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class SystemMonitorTest extends WebSMSToolActivityTestcase {

	public SystemMonitorTest() {
		super(SystemMonitorTest.class.getSimpleName());
	}

	public void testSystemMonitorBatteryState() {
		try {
			SystemMonitor sm = new SystemMonitor(mContext);
			sm.start();
			BatteryStatus bStats = sm.getBatteryStatus();
			assertTrue(null != bStats);
			logBStats(bStats);
			assertTrue(bStats.getBatteryIconId() > 0);
			byte[] bytes = bStats.getBatteryIconBytes();
			assertTrue(bytes.length > 0);
			sm.stop();
		} catch (Exception ex) {
			assertTrue(false);
		}
	}

	public void testSystemMonitorSignalStrength() {
		logWarning("WARNING: one test ist still being skipped [testSystemMonitorSignalStrength()]");
		return;
//				
//		try {
//			SystemMonitor sm = new SystemMonitor(mContext);
//			sm.start();
//			// we don't get any updates so far
//			Thread.sleep(1000);
//			TelephonySignalStrength sStats = sm.getSignalStrength();
//			assertTrue(null != sStats);
//			logSStats(sStats);
//			assertTrue(sStats.getSignalStrength() > 0);
//			assertTrue(sStats.getSignalIconId() > 0);
//			byte[] bytes = sStats.getSignalStrengthIconBytes();
//			assertTrue(bytes.length > 0);
//			sm.stop();
//		} catch (Exception e) {
//			assertTrue(false);
//		}
	}

	private void logBStats(BatteryStatus bStats) {
		logVerbose(" BatteryStatus: batteryIconId [" + bStats.getBatteryIconId()
				+ "] isCharging [" + bStats.getIsCharging() + "] isFull["
				+ bStats.getIsFull() + "] chargePlug[" + bStats.getChargePlug()
				+ "]usbCharge [" + bStats.getIsUsbCharge() + "] acCharge["
				+ bStats.getIsAcCharge() + "]");
	}

	
	@SuppressWarnings("unused")
	private void logSStats(TelephonySignalStrength sStats) {
		logVerbose(" TelephonySignalStrength: signal icon id ["
				+ sStats.getSignalIconId() + "] signal strength ["
				+ sStats.getSignalStrength() + "%]");
	}

}
