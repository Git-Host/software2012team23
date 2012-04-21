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
import at.tugraz.ist.akm.sms.SmsReceivedCallback;
import at.tugraz.ist.akm.sms.SmsSentCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.Logable;

public class TextingAdapter extends Logable implements TextingInterface, SmsReceivedCallback, SmsSentCallback,
		ContactModifiedCallback {

	private Activity mActivity = null;

	private SmsBridge mSmsBridge = null;
	private PhonebookBridge mPhoneBook = null;
	
	private SmsSentCallback mExternalMessageSentCallback = null;
	private SmsReceivedCallback mExternalMessageReceiedCallback = null;
	private ContactModifiedCallback mExternalPhonebookModifiedCallback = null;

	public TextingAdapter(Activity a, SmsSentCallback ms,
			SmsReceivedCallback mr, ContactModifiedCallback cm) {
		super(TextingAdapter.class.getSimpleName());
		mActivity = a;
		
		mExternalMessageSentCallback = ms;
		mExternalMessageReceiedCallback = mr;
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
	
	/* (non-Javadoc)
	 * @see at.tugraz.ist.akm.texting.TextingInterface#sendTextMessage(at.tugraz.ist.akm.sms.TextMessage)
	 */
	@Override
	public int sendTextMessage(TextMessage m) {
		return mSmsBridge.sendTextMessage(m);
	}

	/* (non-Javadoc)
	 * @see at.tugraz.ist.akm.texting.TextingInterface#fetchTextMessages(at.tugraz.ist.akm.content.query.TextMessageFilter)
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
		log("sms sent");
		if (mExternalMessageSentCallback != null) {
			mExternalMessageSentCallback.smsSentCallback(context, intent);
		}
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		log("sms delivered");
		if (mExternalMessageSentCallback != null) {
			mExternalMessageSentCallback.smsDeliveredCallback(context, intent);
		}

	}

	@Override
	public void smsReceivedCallback() {
		log("sms received");
		if (mExternalMessageReceiedCallback != null) {
			mExternalMessageReceiedCallback.smsReceivedCallback();
		}
	}

	@Override
	public void contactModifiedCallback() {
		log("contact modified");
		if (mExternalPhonebookModifiedCallback != null) {
			mExternalPhonebookModifiedCallback.contactModifiedCallback();
		}
	}

	private void registerSmsCallbacks() {
		mSmsBridge.setSmsReceivedCallback(this);
		mSmsBridge.setSmsSentCallback(this);
	}

	private void registerPhonebookCallbacks() {
		mPhoneBook
				.setContactModifiedCallback(mExternalPhonebookModifiedCallback);
	}

	@Override
	public int updateTextMessage(TextMessage message) {
		return mSmsBridge.updateTextMessage(message);
	}
}
