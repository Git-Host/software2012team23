package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.sms.SmsSend;
import at.tugraz.ist.akm.trace.Logable;

public class ManipulateSmsTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private ContentResolver mContentResolver = null;
	private Logable mLogger = new Logable(this.getClass().getName());
	private SmsSend mSmsSink = null;

	public ManipulateSmsTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log("setUp()");

		mContentResolver = getActivity().getContentResolver();
		assertNotNull(mContentResolver);
		mSmsSink = new SmsSend(getActivity());

		log("setUp().return");
	}

	public void testSendSms() {
		mSmsSink.sendSms("0123456789", "SmsSend.java");
		// wait till intent is getting broadcasted, else it won't trigger the sent/delivered callback
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void tearDown() throws Exception {
		log("tearDown()");
		mSmsSink.unregisterSmsNotifications();
		log("tearDown().return");
		super.tearDown();
	}

	private void log(String message) {
		mLogger.log(message);
	}
}
