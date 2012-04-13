package at.tugraz.ist.akm.sms;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SmsSend {

	private Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private SmsManager mSmsManager = SmsManager.getDefault();
	private SmsSendCallback mSmsSendCallback = null;



	public SmsSend(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
	}

	public int sendTextMessage(TextMessage message) {
		List<String> parts = mSmsManager.divideMessage(message.getBody());

		PendingIntent sentIntend = PendingIntent.getBroadcast(mActivity
				.getApplicationContext(), 0, new Intent(
				SmsBroadcastReceiver.ACTION_SMS_SENT), 0);
		PendingIntent deliveredIntend = PendingIntent.getBroadcast(mActivity
				.getApplicationContext(), 0, new Intent(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED), 0);

		for (String part : parts) {
			mSmsManager.sendTextMessage(message.getBody(), null, part, sentIntend,
					deliveredIntend);
		}
		return parts.size();
	}

	public void smsSentCallback(Context context, Intent intent) {
		mSmsSendCallback.smsSentCallback(context, intent);
	}

	public void smsDeliveredCallback(Context context, Intent intent) {
		mSmsSendCallback.smsSentCallback(context, intent);
	}
	
	public void registerCallback(SmsSendCallback c) {
		mSmsSendCallback = c;
	}

	public void finalize() {
	}
}
