package at.tugraz.ist.akm.sms;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBridge extends Logable implements SmsIOCallback {

	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;

	private SmsSender mSmsSink = null;
	private SmsBoxReader mSmsBoxReader = null;
	private SmsBoxWriter mSmsBoxWriter = null;

	private SmsSentBroadcastReceiver mSmsSentNotifier = new SmsSentBroadcastReceiver(
			this);
	private SmsIOCallback mExternalSmsSentCallback = null;

	public SmsBridge(Activity a) {
		super(SmsBridge.class.getSimpleName());
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mSmsSink = new SmsSender(mActivity);
		mSmsBoxReader = new SmsBoxReader(mContentResolver);
		mSmsBoxWriter = new SmsBoxWriter(mContentResolver);
	}

	public int sendTextMessage(TextMessage message) {
		log("sending message to [" + message.getAddress() + "]");
		return mSmsSink.sendTextMessage(message);
	}

	public List<TextMessage> fetchTextMessages(TextMessageFilter filter) {
		List<TextMessage> messages = mSmsBoxReader.getTextMessages(filter);
		log("fetched [" + messages.size() + "] messages");
		return messages;
	}

	public int updateTextMessage(TextMessage message) {
		return mSmsBoxWriter.updateTextMessage(message);
	}

	public List<Integer> fetchThreadIds(final String address) {
		return mSmsBoxReader.getThreadIds(address);
	}
	
	public void setSmsSentCallback(SmsIOCallback c) {
		log("registered new [SmsSentCallback] callback");
		mExternalSmsSentCallback = c;
	}

	public void start() {
		registerSmsSentNotification();
		registerSmsDeliveredNotification();
		registerSmsReceivedNotification();
	}

	public void stop() {
		mActivity.unregisterReceiver(mSmsSentNotifier);
		mSmsSentNotifier = null;
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
		switch (mSmsSentNotifier.getResultCode()) {
		case Activity.RESULT_OK:
			verboseSentState = "to address [" + sentMessage.getAddress()
					+ "] on [" + sentMessage.getDate() + "] ("
					+ sentMessage.getBody() + ")";

			mSmsBoxWriter.writeSentboxTextMessage(sentMessage);
			sentSuccessfully = true;
			break;

		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			verboseSentState = "Error.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NO_SERVICE:
			verboseSentState = "Error: No service.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NULL_PDU:
			verboseSentState = "Error: Null PDU.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_RADIO_OFF:
			verboseSentState = "Error: Radio off.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
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

	/**
	 * simply bypass the callback to external listener
	 */
	@Override
	public void smsReceivedCallback(Context context, Intent intent) {
		if (mExternalSmsSentCallback != null) {
			log("bypassing mExternalSmsReceivedCallback.smsReceivedCallback()");
		} else {
			log("no external callback [mExternalSmsReceivedCallback.smsReceivedCallback()] found - callback ends here");
		}

	}

	private void registerSmsSentNotification() {
		log("registered new IntentFilter [ACTION_SMS_SENT]");
		mActivity.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		log("registered new IntentFilter [ACTION_SMS_DELIVERED]");
		mActivity.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	private void registerSmsReceivedNotification() {
		log("registered new IntentFilter [ACTION_SMS_SENT]");
		mActivity.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_RECEIVED));
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
}
