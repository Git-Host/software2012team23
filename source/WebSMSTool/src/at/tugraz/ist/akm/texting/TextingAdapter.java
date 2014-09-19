/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.texting;

import java.util.List;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.reports.VolatileIncomingReport;
import at.tugraz.ist.akm.texting.reports.VolatileOutgoingReport;
import at.tugraz.ist.akm.texting.reports.VolatilePhonebookReport;
import at.tugraz.ist.akm.trace.LogClient;

public class TextingAdapter extends LogClient implements TextingInterface,
		SmsIOCallback, IContactModifiedCallback {

	private Context mContext = null;
	private SmsBridge mSmsBridge = null;
	private PhonebookBridge mPhoneBook = null;

	private SmsIOCallback mExternalTextMessageCallback = null;
	private IContactModifiedCallback mExternalPhonebookModifiedCallback = null;

	private VolatileOutgoingReport mOutgoingStatistics = new VolatileOutgoingReport();
	private VolatileIncomingReport mIncomingStatistics = new VolatileIncomingReport();
	private VolatilePhonebookReport mPhonebookStatistics = new VolatilePhonebookReport();

	public TextingAdapter(Context context, SmsIOCallback smsIOCallback,
			IContactModifiedCallback contactModifiedCallback) {
		super(TextingAdapter.class.getName());
		mContext = context;

		mExternalTextMessageCallback = smsIOCallback;
		mExternalPhonebookModifiedCallback = contactModifiedCallback;

		mSmsBridge = new SmsBridge(mContext);
		mPhoneBook = new PhonebookBridge(mContext);
	}

	public void start() {
	    info("starting " + getClass().getSimpleName());
		registerSmsCallbacks();
		registerPhonebookCallbacks();
		mSmsBridge.start();
		mPhoneBook.start();
	}

	public void stop() {
	    info("stopping " + getClass().getSimpleName());
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
	synchronized public int sendTextMessage(TextMessage message) {
		mOutgoingStatistics
				.setNumPending(mOutgoingStatistics.getNumPending() + 1);
		return mSmsBridge.sendTextMessage(message);
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
	public void smsSentCallback(Context context, List<TextMessage> message) {
	    info("sms sent successfully");
		mOutgoingStatistics
				.setNumPending(mOutgoingStatistics.getNumPending() - 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentCallback(context, message);
		}
	}

	@Override
	public void smsSentErrorCallback(Context context, List<TextMessage> message) {
	    info("failed to send sms");
		mOutgoingStatistics.setNumErroneous(mOutgoingStatistics
				.getNumErroneous() + 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentErrorCallback(context, message);
		}
	}

	@Override
	public void smsDeliveredCallback(Context context, List<TextMessage> messages) {
		TextMessage message = messages.get(0);
		info("sms was delivered (to address ["+message.getAddress() +"] on ["+message.getDate()+"] text ["+message.getBody()+"])");
		
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsDeliveredCallback(context, messages);
		}

	}

	@Override
	public void smsReceivedCallback(Context context, List<TextMessage> message) {
	    info("sms received");
		mIncomingStatistics
				.setNumReceived(mIncomingStatistics.getNumReceived() + 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsReceivedCallback(context, message);
		}
	}

	@Override
	public void contactModifiedCallback() {
	    info("contact modified");
		mPhonebookStatistics
				.setNumChanges(mPhonebookStatistics.getNumChanges() + 1);
		if (mExternalPhonebookModifiedCallback != null) {
			mExternalPhonebookModifiedCallback.contactModifiedCallback();
		}
	}

	@Override
	public int updateTextMessage(TextMessage message) {
		return mSmsBridge.updateTextMessage(message);
	}

	/**
	 * return all tread IDs for the specified address
	 * 
	 * @param phoneNumber
	 *            the phone number
	 */
	@Override
	public List<Integer> fetchThreadIds(final String phoneNumber) {
		return mSmsBridge.fetchThreadIds(phoneNumber);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the outgoing state.
	 */
	@Override
	public VolatileOutgoingReport getOutgoingReport() {
		return new VolatileOutgoingReport(mOutgoingStatistics);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the incoming state.
	 */
	@Override
	public VolatileIncomingReport getIncomingReport() {
		return new VolatileIncomingReport(mIncomingStatistics);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the contact (changes, updated) state.
	 */
	@Override
	public VolatilePhonebookReport getPhonebookReport() {
		return new VolatilePhonebookReport(mPhonebookStatistics);
	}

	private void registerSmsCallbacks() {
		mSmsBridge.setSmsSentCallback(this);
	}

	private void registerPhonebookCallbacks() {
		mPhoneBook
				.setContactModifiedCallback(this);
	}

}
