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
import at.tugraz.ist.akm.trace.Logable;

public class SmsBridge implements SmsSendCallback {

	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private SmsBroadcastReceiver mSmsSendNotifier = new SmsBroadcastReceiver(
			this);
	private SmsSend mSmsSink = null;
	private SmsBoxReader mSmsBoxReader = null;
	private SmsBoxWriter mSmsBoxWriter = null;

	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsBridge(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mSmsSink = new SmsSend(mActivity);
		mSmsBoxReader = new SmsBoxReader(mContentResolver);
		registerSmsSentNotification();
		registerSmsDeliveredNotification();
		mSmsBoxWriter = new SmsBoxWriter(mContentResolver);
	}

	public int sendTextMessage(TextMessage message) {
		return mSmsSink.sendTextMessage(message);
	}

	public List<TextMessage> fetchInbox() {
		return mSmsBoxReader.getInbox();
	}

	public List<TextMessage> fetchOutbox() {
		return mSmsBoxReader.getSentbox();
	}
	
	private void registerSmsSentNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	public void close() {
		mActivity.unregisterReceiver(mSmsSendNotifier);
		mSmsSendNotifier = null;
	}

	@Override
	public void smsSentCallback(Context context, Intent intent) {
		String verboseSentState = null;
		boolean sentSuccessfully = false;

		switch (mSmsSendNotifier.getResultCode()) {
		case Activity.RESULT_OK:
			verboseSentState = "Message sent.";
			mSmsBoxWriter.writeOutboxTextMessage(parseToTextMessgae(intent));
			sentSuccessfully = true;
			break;

		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			verboseSentState = "Error.";
			break;

		case SmsManager.RESULT_ERROR_NO_SERVICE:
			verboseSentState = "Error: No service.";
			break;

		case SmsManager.RESULT_ERROR_NULL_PDU:
			verboseSentState = "Error: Null PDU.";
			break;

		case SmsManager.RESULT_ERROR_RADIO_OFF:
			verboseSentState = "Error: Radio off.";
			break;
		}

		if (sentSuccessfully) {
			log("contentprovider: save message to outbox (" + verboseSentState
					+ ")");
		} else {
			log(verboseSentState);
		}
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		String from = "";
		String receiver = "";
		String body = "";
		log("message from [" + from + "] to [" + receiver + "] delivered ("
				+ body + ")");

	}

	private TextMessage parseToTextMessgae(Intent intent) {
		try {
			Bundle extrasBundle = intent.getExtras();
			if (extrasBundle != null) {
				Serializable serializedTextMessage = extrasBundle
						.getSerializable(SmsBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);

				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					StringBuffer infos = new StringBuffer();
					infos.append("SMS to [" + sentMessage.getAddress()
							+ "] sent on [" + sentMessage.getDate() + "] ("
							+ sentMessage.getBody() + ")");
					log(infos.toString());
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

	private void log(final String m) {
		mLog.log(m);
	}
}
