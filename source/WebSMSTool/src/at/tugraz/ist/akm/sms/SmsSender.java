package at.tugraz.ist.akm.sms;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.trace.Logable;

public class SmsSender extends Logable {

	private Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private SmsManager mSmsManager = SmsManager.getDefault();

	private int mSentRequestCode = 0;
	private int mDeliveredRequestCode = 0;

	public SmsSender(Activity a) {
		super(SmsSender.class.getSimpleName());
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
	}

	public int sendTextMessage(TextMessage message) {
		List<String> parts = mSmsManager.divideMessage(message.getBody());

		int partNum = 0;
		for (String part : parts) {
			log("sending part [" + partNum++ + "] to [" + message.getAddress()
					+ "] (" + part + ")");
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
		sentIntent.putExtras(getSmsBundle(message, part));
		PendingIntent sentPIntent = PendingIntent.getBroadcast(
				mActivity.getApplicationContext(), mSentRequestCode++, sentIntent,
				PendingIntent.FLAG_ONE_SHOT);

		return sentPIntent;
	}

	private PendingIntent getDeliveredPendingIntent(TextMessage message,
			String part) {
		Intent deliveredIntent = new Intent(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED);
		deliveredIntent.putExtras(getSmsBundle(message, part));

		PendingIntent deliveredPIntent = PendingIntent.getBroadcast(
				mActivity.getApplicationContext(), mDeliveredRequestCode++, deliveredIntent,
				PendingIntent.FLAG_ONE_SHOT);

		return deliveredPIntent;
	}

	private Bundle getSmsBundle(TextMessage message, String part) {
		Bundle smsBundle = new Bundle();
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE, message);
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART, part);

		return smsBundle;
	}

}
