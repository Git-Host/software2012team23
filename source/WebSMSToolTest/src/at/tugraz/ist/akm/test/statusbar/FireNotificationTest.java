package at.tugraz.ist.akm.test.statusbar;

import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase2;

public class FireNotificationTest extends WebSMSToolActivityTestcase2 {

	public FireNotificationTest() {
		super(FireNotificationTest.class.getSimpleName());
	}

	public void testFireNotificationNoExcepton() {
		try {
			FireNotification notify = new FireNotification(super.mActivity);
			FireNotification.NotificationInfo infos = new FireNotification.NotificationInfo();
			infos.text = "http://192.168.1.100:8080";
			infos.title = "WebSMSTool";
			notify.fireStickyInfos(infos);
			Thread.sleep(100);
			notify.cancelAll();
		} catch (Exception e) {
			assertTrue(false);
		}
	}
}
