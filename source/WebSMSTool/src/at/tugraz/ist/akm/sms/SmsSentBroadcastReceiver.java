package at.tugraz.ist.akm.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsSentBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_SMS_SENT = "at.tugraz.ist.akm.sms.SMS_SENT_ACTION";
	public static final String ACTION_SMS_DELIVERED = "at.tugraz.ist.akm.sms.SMS_DELIVERED_ACTION";
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	public static final String EXTRA_BUNDLE_KEY_TEXTMESSAGE = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_TEXTMESSAGE_KEY";
	public static final String EXTRA_BUNDLE_KEY_PART = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_PART_KEY";

	private SmsSentCallback mCallback = null;

	public SmsSentBroadcastReceiver(SmsSentCallback s) {
		mCallback = s;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.compareTo(ACTION_SMS_SENT) == 0) {
			mCallback.smsSentCallback(context, intent);
			
		} else if (action.compareTo(ACTION_SMS_DELIVERED) == 0) {
			mCallback.smsDeliveredCallback(context, intent);
			
		} else if (action.compareTo(ACTION_SMS_RECEIVED) == 0) {
			mCallback.smsReceivedCallback(context, intent);
		} else {
			Log.v(getClass().getSimpleName(), "unknown action received: "
					+ action);
		}

	}
};
