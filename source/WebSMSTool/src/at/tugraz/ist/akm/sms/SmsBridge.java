package at.tugraz.ist.akm.sms;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBridge implements SmsSentCallback, SmsReceivedCallback {

	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	private SmsSentBroadcastReceiver mSmsSendNotifier = new SmsSentBroadcastReceiver(
			this);
	private SmsReceivedContentObserver mSmsReceivedNotifier = new SmsReceivedContentObserver(
			this);
	private Cursor mSmsInboxContentCursor = null;

	private SmsSend mSmsSink = null;
	private SmsBoxReader mSmsBoxReader = null;
	private SmsBoxWriter mSmsBoxWriter = null;

	private SmsReceivedCallback mExternalSmsReceivedCallback = null;
	private SmsSentCallback mExternalSmsSentCallback = null;

	private class SmsReceivedContentObserver extends ContentObserver {

		private SmsReceivedCallback mCallback = null;

		public SmsReceivedContentObserver(SmsReceivedCallback c) {
			super(null);
			mCallback = c;
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (!selfChange) {
				mCallback.smsReceivedCallback();
			}
		}
	}

	public SmsBridge(Activity a) {
		log("starting ...");
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mSmsSink = new SmsSend(mActivity);
		mSmsBoxReader = new SmsBoxReader(mContentResolver);
		mSmsBoxWriter = new SmsBoxWriter(mContentResolver);

		registerSmsSentNotification();
		registerSmsDeliveredNotification();

		mSmsInboxContentCursor = getSmsInboxCursor();
		registerSmsReceivedObserver();
	}

	public int sendTextMessage(TextMessage message) {
		log("sending message to [" + message.getAddress() + "]");
		return mSmsSink.sendTextMessage(message);
	}

	public List<TextMessage> fetchInbox() {
		List<TextMessage> inbox = mSmsBoxReader.getInbox();
		log("fetched [" + inbox.size() + "] items from inbox");
		return inbox;
	}

	public List<TextMessage> fetchOutbox() {
		List<TextMessage> outbox = mSmsBoxReader.getSentbox();
		log("fetched [" + outbox.size() + "] items from outbox");
		return outbox;
	}

	public void setSmsSentCallback(SmsSentCallback c) {
		log("registered new [SmsSentCallback] callback");
		mExternalSmsSentCallback = c;
	}

	public void setSmsReceivedCallback(SmsReceivedCallback c) {
		log("registered new [SmsReceivedCallback] callback");
		mExternalSmsReceivedCallback = c;
	}

	private void registerSmsSentNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	private void registerSmsReceivedObserver() {
		mSmsInboxContentCursor.registerContentObserver(mSmsReceivedNotifier);
	}

	public void close() {
		log("closing ...");
		mActivity.unregisterReceiver(mSmsSendNotifier);
		mSmsSendNotifier = null;
		mSmsInboxContentCursor.unregisterContentObserver(mSmsReceivedNotifier);
		mSmsReceivedNotifier = null;
	}

	/**
	 * returns a table cursor that is going to be observed later hence we need
	 * no useful columns, just a table
	 * 
	 * @return
	 */
	private Cursor getSmsInboxCursor() {
		Uri select = SmsContent.ContentUri.INBOX_URI;
		String[] as = { SmsContent.Content.ID };
		String where = SmsContent.Content.TYPE + " = ? ";
		String[] like = { SmsContent.Content.TYPE_SMS };

		return mActivity.managedQuery(select, as, where, like, null);
	}

	/**
	 * 1. try to parse the TextMessage and store to content://sms/sent 2.
	 * regardless of the state bypass the event to external audience
	 */
	@Override
	public void smsSentCallback(Context context, Intent intent) {
		String verboseSentState = null;
		boolean sentSuccessfully = false;
		TextMessage sentMessage = parseToTextMessgae(intent);

		switch (mSmsSendNotifier.getResultCode()) {
		case Activity.RESULT_OK:
			verboseSentState = "to address [" + sentMessage.getAddress()
					+ "] on [" + sentMessage.getDate() + "] ("
					+ sentMessage.getBody() + ")";

			mSmsBoxWriter.writeSentboxTextMessage(sentMessage);
			sentSuccessfully = true;
			break;

		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			verboseSentState = "Error.";
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NO_SERVICE:
			verboseSentState = "Error: No service.";
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NULL_PDU:
			verboseSentState = "Error: Null PDU.";
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_RADIO_OFF:
			verboseSentState = "Error: Radio off.";
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;
		}

		if (sentSuccessfully) {
			log("saved message to outbox (" + verboseSentState + ")");
		} else {
			log(verboseSentState);
		}

		if (mExternalSmsSentCallback != null) {
			log("bypassing SmsSentCallback.smsSentCallback()");
			mExternalSmsSentCallback.smsSentCallback(context, intent);
		} else {
			log("no external callback [SmsSentCallback.smsSentCallback()] found - callback ends here");
		}
	}

	/**
	 * bypass the event to external audience
	 */
	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		if (mExternalSmsSentCallback != null) {
			log("bypassing SmsSentCallback.smsDeliveredCallback()");
			mExternalSmsSentCallback.smsDeliveredCallback(context, intent);
		} else {
			log("no external callback [SmsSentCallback.smsDeliveredCallback()] found - callback ends here");
		}
	}

	private TextMessage parseToTextMessgae(Intent intent) {
		try {
			Bundle extrasBundle = intent.getExtras();
			if (extrasBundle != null) {
				Serializable serializedTextMessage = extrasBundle
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);

				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					return sentMessage;
				}

			} else {
				log("couldn't find any text message infos at all :(");
			}
		} catch (Exception e) {
			log("FAILED to gather text message extras from intent");
		}
		return null;
	}

	/**
	 * simply bypass the callback to external listener
	 */
	@Override
	public void smsReceivedCallback() {
		if (mExternalSmsReceivedCallback != null) {
			log("bypassing mExternalSmsReceivedCallback.smsReceivedCallback()");
			mExternalSmsReceivedCallback.smsReceivedCallback();
		} else {
			log("no external callback [mExternalSmsReceivedCallback.smsReceivedCallback()] found - callback ends here");
		}

	}

	private void log(final String m) {
		mLog.log(m);
	}
}
