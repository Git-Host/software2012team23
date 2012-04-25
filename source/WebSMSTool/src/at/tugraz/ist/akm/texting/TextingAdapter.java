package at.tugraz.ist.akm.texting;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.Logable;

public class TextingAdapter extends Logable implements TextingInterface,
		SmsIOCallback, ContactModifiedCallback {

	private Activity mActivity = null;

	private SmsBridge mSmsBridge = null;
	private PhonebookBridge mPhoneBook = null;

	private SmsIOCallback mExternalTextMessageCallback = null;
	private ContactModifiedCallback mExternalPhonebookModifiedCallback = null;
	
	private VolatileOutgoingReport mOutgoingStatistic = new VolatileOutgoingReport();

	public TextingAdapter(Activity a, SmsIOCallback ms,
			ContactModifiedCallback cm) {
		super(TextingAdapter.class.getSimpleName());
		mActivity = a;

		mExternalTextMessageCallback = ms;
		mExternalPhonebookModifiedCallback = cm;

		mSmsBridge = new SmsBridge(mActivity);
		mPhoneBook = new PhonebookBridge(mActivity.getApplicationContext());
	}

	public void start() {
		log("power up ...");
		registerSmsCallbacks();
		registerPhonebookCallbacks();
		mSmsBridge.start();
		mPhoneBook.start();
	}

	public void stop() {
		log("power down ...");
		mPhoneBook.stop();
		mSmsBridge.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tugraz.ist.akm.texting.TextingInterface#sendTextMessage(at.tugraz.
	 * ist.akm.sms.TextMessage)
	 */
	@Override
	public int sendTextMessage(TextMessage m) {
		mOutgoingStatistic.numPending++;
		return mSmsBridge.sendTextMessage(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tugraz.ist.akm.texting.TextingInterface#fetchTextMessages(at.tugraz
	 * .ist.akm.content.query.TextMessageFilter)
	 */
	@Override
	public List<TextMessage> fetchTextMessages(TextMessageFilter filter) {
		return mSmsBridge.fetchTextMessages(filter);
	}

	@Override
	public List<Contact> fetchContacts(ContactFilter filter) {
		return mPhoneBook.fetchContacts(filter);
	}

	@Override
	public void smsSentCallback(Context context, Intent intent) {
		log("sms sent successfully");
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentCallback(context, intent);
		}
		mOutgoingStatistic.numPending--;
	}
	
	@Override
	public void smsSentErrorCallback(Context context, Intent intent) {
		log("failed to send sms");
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentErrorCallback(context, intent);
		}
		mOutgoingStatistic.numErroneous++;
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		log("sms delivered");
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsDeliveredCallback(context, intent);
		}

	}

	@Override
	public void smsReceivedCallback(Context context, Intent intent) {
		log("sms received");
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsReceivedCallback(context, intent);
		}
	}

	@Override
	public void contactModifiedCallback() {
		log("contact modified");
		if (mExternalPhonebookModifiedCallback != null) {
			mExternalPhonebookModifiedCallback.contactModifiedCallback();
		}
	}

	@Override
	public int updateTextMessage(TextMessage message) {
		return mSmsBridge.updateTextMessage(message);
	}

	@Override
	public List<Integer> fetchThreadIds(final String address) {
		return mSmsBridge.fetchThreadIds(address);
	}

	/**
	 * @return A brief information about the outgoing state.
	 */
	@Override
	public VolatileOutgoingReport getOutgoingReport() {
		return new VolatileOutgoingReport(mOutgoingStatistic);
	}
	
	private void registerSmsCallbacks() {
		mSmsBridge.setSmsSentCallback(this);
	}

	private void registerPhonebookCallbacks() {
		mPhoneBook
				.setContactModifiedCallback(mExternalPhonebookModifiedCallback);
	}

}
