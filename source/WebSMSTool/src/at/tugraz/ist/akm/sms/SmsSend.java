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

		for (String part : parts) {
			PendingIntent sentPIntent = getSentPendingIntent(message, part);
			PendingIntent deliveredPIntent = getDeliveredPendingIntent(message,
					part);
			mSmsManager.sendTextMessage(message.getAddress(), null, part,
					sentPIntent, deliveredPIntent);
		}
		return parts.size();
	}

	private PendingIntent getSentPendingIntent(TextMessage message, String part) {
		Intent sentIntent = new Intent(SmsSentBroadcastReceiver.ACTION_SMS_SENT);
		sentIntent.putExtras(getBundle(message, part));
		PendingIntent sentPIntent = PendingIntent.getBroadcast(
				mActivity.getApplicationContext(), 0, sentIntent, 0);
		
		return sentPIntent;
	}

	private PendingIntent getDeliveredPendingIntent(TextMessage message,
			String part) {
		Intent deliveredIntent = new Intent(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED);
		deliveredIntent.putExtras(getBundle(message, part));

		PendingIntent deliveredPIntent = PendingIntent.getBroadcast(
				mActivity.getApplicationContext(), 0, deliveredIntent, 0);
		
		return deliveredPIntent;
	}

	private Bundle getBundle(TextMessage message, String part) {
		Bundle smsBundle = new Bundle();
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE, message);
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART, part);
		
		return smsBundle;
	}

}
