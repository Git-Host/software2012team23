package at.tugraz.ist.akm.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.sms.SmsRead;
import at.tugraz.ist.akm.sms.SmsSend;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.Logable;

public class ManipulateSmsTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private Logable mLogger = new Logable(this.getClass().getSimpleName());
	private SmsSend mSmsSink = null;

	public ManipulateSmsTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(super.getName() + ".setUp()");

		mActivity = getActivity();
		mContentResolver = mActivity.getContentResolver();
		assertNotNull(mContentResolver);
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
		log(super.getName() + ".tearDown()");
		super.tearDown();
	}

	private void log(String message) {
		mLogger.log(message);
	}
}
