package at.tugraz.ist.akm.sms;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.trace.Logable;

public class SmsSend {

	private Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLogger = new Logable(this.getClass().getName());
	private SmsManager mSmsManager = SmsManager.getDefault();
	private BroadcastReceiver mSmsSentNotifier;

	class SmsBroadcastReceiver extends BroadcastReceiver {

		public static final String ACTION_SMS_SENT = "at.tugraz.ist.akm.sms.SMS_SENT_ACTION";
		public static final String ACTION_SMS_DELIVERED = "at.tugraz.ist.akm.sms.SMS_DELIVERED_ACTION";

		private SmsSend mCallback = null;

		public SmsBroadcastReceiver(SmsSend s) {
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

	public class ContentProvider {
		public class Sms {
			public class Uri {
				public static final String SMS = "content://sms";
				public static final String SMS_INBOX = "content://sms/inbox";
				public static final String SMS_OUTBOX = "content://sms/outbox";
			};
	
			public class Columns {
				public static final String ID = "_id";
				public static final String THREAD_ID = "thread_id";
				public static final String PERSON = "person";
				public static final String DATE = "date";
				public static final String ADDRESS = "address";
				public static final String SEEN = "seen";
				public static final String READ = "read";
				public static final String BODY = "body";
				public static final String PROTOCOL = "protocol";
				public static final String STATUS = "status";
				public static final String TYPE = "type";
				public static final String SERVICE_CENTER = "service_center";
				public static final String LOCKED = "locked";
			};
		};
	};

	public SmsSend(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mSmsSentNotifier = new SmsBroadcastReceiver(this);
		registerSmsSentNotification();
		registerSmsDeliveredNotification();
	}

	public int sendSms(final String phoneNumber, final String message) {
		List<String> parts = mSmsManager.divideMessage(message);

		log("prepare to send SMS splitted in [" + parts.size() + "] parts");

		PendingIntent sentIntend = PendingIntent.getBroadcast(mActivity
				.getApplicationContext(), 0, new Intent(
				SmsBroadcastReceiver.ACTION_SMS_SENT), 0);
		PendingIntent deliveredIntend = PendingIntent.getBroadcast(mActivity
				.getApplicationContext(), 0, new Intent(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED), 0);

		for (String part : parts) {
			mSmsManager.sendTextMessage(phoneNumber, null, part, sentIntend,
					deliveredIntend);
		}
		return parts.size();
	}

	public void smsSentCallback(Context context, Intent intent) {

		String verboseSentState = null;
		boolean sentSuccessfully = false;

		switch (mSmsSentNotifier.getResultCode()) {
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

	public void smsDeliveredCallback(Context context, Intent intent) {

		String from = "";
		String receiver = "";
		String body = "";
		log("message from [" + from + "] to [" + receiver + "] delivered ("
				+ body + ")");
	}

	private void registerSmsSentNotification() {
		mActivity.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		mActivity.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	public void unregisterSmsNotifications() {
		mActivity.unregisterReceiver(mSmsSentNotifier);
		mSmsSentNotifier = null;
	}

	private void log(final String message) {
		mLogger.log(message);
	}

	public void finalize() {
	}
}
