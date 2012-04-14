package at.tugraz.ist.akm.sms;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

public class SmsSend {

	private Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private SmsManager mSmsManager = SmsManager.getDefault();

	public SmsSend(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
	}

	public int sendTextMessage(TextMessage message) {
		List<String> parts = mSmsManager.divideMessage(message.getBody());

		PendingIntent deliveredPIntent = PendingIntent.getBroadcast(mActivity
				.getApplicationContext(), 0, new Intent(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED), 0);

		for (String part : parts) {

			Bundle smsBundle = new Bundle();
			smsBundle.putSerializable(
					SmsBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE, message);

			Intent sentIntent = new Intent(SmsBroadcastReceiver.ACTION_SMS_SENT);
			sentIntent.putExtras(smsBundle);

			PendingIntent sentPIntent = PendingIntent.getBroadcast(
					mActivity.getApplicationContext(), 0, sentIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			mSmsManager.sendTextMessage(message.getAddress(), null, part,
					sentPIntent, deliveredPIntent);
		}
		return parts.size();
	}

	public void finalize() {
	}
}
