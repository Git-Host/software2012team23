package at.tugraz.ist.akm.sms;

import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBridge implements SmsSendCallback {

	private Activity mActivity = null;
	private ContentResolver mContentResolver= null;
	private SmsBroadcastReceiver mSmsSendNotifier = new SmsBroadcastReceiver(
			this);
	private SmsSend mSmsSink = null;
	private SmsRead mLocalSmsReader = null;
	
	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsBridge(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mSmsSink = new SmsSend(mActivity);
		mLocalSmsReader = new SmsRead(mContentResolver);
		registerSmsSentNotification();
		registerSmsDeliveredNotification();
	}

	private void registerSmsSentNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		mActivity.registerReceiver(mSmsSendNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	public void unregisterSmsNotifications() {
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
		// TODO: also set intent.putExtra() bevore so that we now have needed
		// params available
		String from = "";
		String receiver = "";
		String body = "";
		log("message from [" + from + "] to [" + receiver + "] delivered ("
				+ body + ")");

	}

	public int sendTextMessage(TextMessage message) {
		return mSmsSink.sendTextMessage(message);
	}

	public List<TextMessage> fetchInbox() {
		return mLocalSmsReader.getInbox();
	}
	
	public List<TextMessage> fetchOutbox() {
		return mLocalSmsReader.getOutbox();
	}
	
	private void log(final String m) {
		mLog.log(m);
	}
}
