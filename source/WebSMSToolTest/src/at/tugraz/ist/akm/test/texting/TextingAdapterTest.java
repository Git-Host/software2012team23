package at.tugraz.ist.akm.test.texting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsReceivedCallback;
import at.tugraz.ist.akm.sms.SmsSentCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase2;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.texting.TextingInterface;

public class TextingAdapterTest extends WebSMSToolActivityTestcase2 implements
		SmsSentCallback, SmsReceivedCallback, ContactModifiedCallback {

	private Activity mActivity = null;
	private int mCountSent = 0;

	public TextingAdapterTest() {
		super(TextingAdapterTest.class.getSimpleName());
	}

	public void testSendNoFail() throws Exception {
		TextingInterface texting = new TextingAdapter(mActivity, this, this,
				this);
		texting.start();
		TextMessage m = new TextMessage();
		m.setAddress("01234");
		m.setBody("foobar foo baz");
		texting.sendTextMessage(m);
		Thread.sleep(1000);
		assertTrue(mCountSent > 0);
		texting.stop();
	}

	public void testFetchContactsNoFail() {
		TextingInterface texting = new TextingAdapter(mActivity, this, this,
				this);
		texting.start();
		// ContactFilter filter = new ContactFilter();
		// filter.setId(1L);
		// texting.fetchContacts(filter);
		texting.stop();
	}

	public void testFetchMessagesNoFail() {
		TextingInterface texting = new TextingAdapter(mActivity, this, this,
				this);
		texting.start();
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.INBOX_URI);
		filter.setAddress("01906666");
		texting.fetchTextMessages(filter);
		texting.stop();
	}

	@Override
	public void contactModifiedCallback() {
		log("TODO Auto-generated method stub");

	}

	@Override
	public void smsReceivedCallback() {
		log("TODO Auto-generated method stub");

	}

	@Override
	public void smsSentCallback(Context context, Intent intent) {
		++mCountSent;
		log("TODO Auto-generated method stub");

	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		log("TODO Auto-generated method stub");

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
