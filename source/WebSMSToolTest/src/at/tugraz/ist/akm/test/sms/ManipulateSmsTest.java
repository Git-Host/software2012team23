package at.tugraz.ist.akm.test.sms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.tugraz.ist.akm.sms.SmsRead;
import at.tugraz.ist.akm.sms.SmsSend;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class ManipulateSmsTest extends WebSMSToolTestInstrumentation {

	private SmsSend mSmsSink = null;

	public ManipulateSmsTest() {
		super(ManipulateSmsTest.class.getSimpleName());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mSmsSink = new SmsSend(getActivity());
	}

	public void testSendSms() {
		TextMessage m = new TextMessage();
		m.setAddress("01234567");
		m.setBody("test messgage: prepared by " + getClass().getSimpleName()
				+ " at " + getDateNowString());
		mSmsSink.sendTextMessage(m);
		// wait till intent is getting broadcasted, else it won't trigger the
		// sent/delivered callback
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void testReadInboxSms() {
		SmsRead smsSource = new SmsRead(mContentResolver);
		List<TextMessage> inbox = smsSource.getInbox();

		int msgIdx = 0;
		for (TextMessage m : inbox) {
			log("got inbox message [" + msgIdx++ + "/" + inbox.size()
					+ "] from [" + m.getAddress() + "]: " + m.getBody());
		}
	}

	public void testReadOutboxSms() {
		SmsRead smsSource = new SmsRead(mContentResolver);
		List<TextMessage> outbox = smsSource.getOutbox();

		int msgIdx = 0;
		for (TextMessage m : outbox) {
			log("got outbox message [" + msgIdx++ + "/" + outbox.size()
					+ "] from [" + m.getAddress() + "]: " + m.getBody());
		}
	}

	private String getDateNowString() {
		Date dateNow = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm dd.MM.yyyy");
		StringBuilder now = new StringBuilder(dateformat.format(dateNow));
		return now.toString();

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
