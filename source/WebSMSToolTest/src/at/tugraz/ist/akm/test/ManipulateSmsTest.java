package at.tugraz.ist.akm.test;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.trace.Logable;

public class ManipulateSmsTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private ContentResolver mContentResolver = null;
	private Logable mLogger = new Logable("sms");

	public static final String ACTION_SMS_SENT = "com.example.android.apis.os.SMS_SENT_ACTION";

	public ManipulateSmsTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
	}

	protected void setUp() {
		log("setUp()");
		mContentResolver = getActivity().getContentResolver();
		assertNotNull(mContentResolver);
		registerTrap();
	}


	

	public void doesNotWorkIfPreviousTestHasBeenRunBefore_testSendSms() {
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		sendSms("01906666", "sexxyy message");
	}

	private void sendSms(String phoneNumber, String message) {
		log("perpare to send message: " + message);
		SmsManager sms = SmsManager.getDefault();
		List<String> parts = sms.divideMessage(message);

		log("split message in " + parts.size() + " parts");

		for (String part : parts) {
			log("sending part: \"" + part + "\"");
			sms.sendTextMessage(phoneNumber, null, part, PendingIntent
					.getBroadcast(getActivity().getApplicationContext(), 0,
							new Intent(ACTION_SMS_SENT), 0), null);
		}
	}

	private void registerTrap() {
		// Register broadcast receivers for SMS sent and delivered intents
		getActivity().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String message = null;
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					message = "Message sent!";
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					message = "Error.";
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					message = "Error: No service.";
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					message = "Error: Null PDU.";
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					message = "Error: Radio off.";
					break;
				}
				log("sending message returned: " + message);
			}
		}, new IntentFilter(ACTION_SMS_SENT));

	}

	// reads incoming and outgoing (all) sms
		public void testReadAllSMS() {
			Cursor conversation = mContentResolver.query(
					Uri.parse("content://sms"), null, null, null, null);

			while (conversation.moveToNext()) {
				String id = conversation.getString(conversation
						.getColumnIndex("thread_id"));
				String person = conversation.getString(conversation
						.getColumnIndex("person"));
				String date = conversation.getString(conversation
						.getColumnIndex("date"));
				String address = conversation.getString(conversation
						.getColumnIndex("address"));
				String seen = conversation.getString(conversation
						.getColumnIndex("seen"));
				String body = conversation.getString(conversation
						.getColumnIndex("body"));
				String read = conversation.getString(conversation
						.getColumnIndex("read"));
				log("id [" + id + "] date [" + date + "] person [" + person
						+ "] seen [" + seen + "] address [" + address + "] read ["
						+ read);
				log("body: " + body);
			}
		}
		
	protected void tearDown() {
		log("tearDown()");
	}

	private void log(String message) {
		mLogger.log(message);
	}
}
