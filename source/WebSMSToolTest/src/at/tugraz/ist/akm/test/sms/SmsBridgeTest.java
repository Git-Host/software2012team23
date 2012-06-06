package at.tugraz.ist.akm.test.sms;

import java.util.List;

import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class SmsBridgeTest extends WebSMSToolActivityTestcase {

	public SmsBridgeTest() {
		super(SmsBridgeTest.class.getSimpleName());
	}

	public void testSmsBridgeSendSms() {

		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			smsBridge.sendTextMessage(SmsHelper.getDummyTextMessage());
			Thread.sleep(1000);
			smsBridge.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSmsBridgeFetchInbox() {
		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.INBOX_URI);
			List<TextMessage> inMessages = smsBridge.fetchTextMessages(filter);
			for (TextMessage message : inMessages) {
				SmsHelper.logTextMessage(message);
			}
			smsBridge.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSmsBridgeFetchOutbox() {
		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.OUTBOX_URI);
			List<TextMessage> inMessages = smsBridge.fetchTextMessages(filter);
			for (TextMessage message : inMessages) {
				SmsHelper.logTextMessage(message);
			}
			smsBridge.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
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
