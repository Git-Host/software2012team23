package at.tugraz.ist.akm.test.sms;

import java.util.List;

import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class SmsBridgeTest extends WebSMSToolTestInstrumentation  {

	public SmsBridgeTest () {
		super(SmsBridgeTest.class.getSimpleName());
	}
	
	public void testSmsBridgeSendSms() {
		try {
			SmsBridge s = new SmsBridge(mActivity);
			s.sendTextMessage(SmsHelper.getDummyTextMessage());
			Thread.sleep(3000);
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSmsBridgeFetchInbox() {
		try {
			SmsBridge s = new SmsBridge(mActivity);
			List<TextMessage> inMessages = s.fetchInbox();
			for (TextMessage m : inMessages) {
				SmsHelper.logTextMessage(m);
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	public void testSmsBridgeFetchOutbox() {
		try {
			SmsBridge s = new SmsBridge(mActivity);
			List<TextMessage> inMessages = s.fetchInbox();
			for (TextMessage m : inMessages) {
				SmsHelper.logTextMessage(m);
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
