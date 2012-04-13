package at.tugraz.ist.akm.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_SMS_SENT = "at.tugraz.ist.akm.sms.SMS_SENT_ACTION";
	public static final String ACTION_SMS_DELIVERED = "at.tugraz.ist.akm.sms.SMS_DELIVERED_ACTION";

	private SmsSendCallback mCallback = null;

	public SmsBroadcastReceiver(SmsSendCallback s) {
		mCallback = s;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.compareTo(ACTION_SMS_SENT) == 0) {
			mCallback.smsSentCallback(context, intent);
		} else if (action.compareTo(ACTION_SMS_DELIVERED) == 0) {
			mCallback.smsDeliveredCallback(context, intent);
		}
	}
};
