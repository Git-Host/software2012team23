package at.tugraz.ist.akm.test.sms;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import at.tugraz.ist.akm.sms.SmsBoxReader;
import at.tugraz.ist.akm.sms.SmsBoxWriter;
import at.tugraz.ist.akm.sms.SmsBroadcastReceiver;
import at.tugraz.ist.akm.sms.SmsContent;
import at.tugraz.ist.akm.sms.SmsSend;
import at.tugraz.ist.akm.sms.SmsSendCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class ManipulateSmsTest extends WebSMSToolTestInstrumentation implements
		SmsSendCallback {

	public ManipulateSmsTest() {
		super(ManipulateSmsTest.class.getSimpleName());
	}

	/**
	 * Prove existence of expected content provider uris
	 */
	public void testGetSmsContentProviderTables() {
		try {
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.INBOX_URI,
					null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.QUEUED_URI,
					null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.BASE_URI,
					null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.DRAFT_URI,
					null, null, null, null));

			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.FAILED_URI,
					null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.OUTBOX_URI,
					null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.UNDELIVERED_URI, null, null, null,
					null));
			SmsHelper.logCursor(mContentResolver.query(SmsContent.ContentUri.SENT_URI,
					null, null, null, null));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testWriteSMSToOutbox() {
		try {
			TextMessage m = SmsHelper.getDummyTextMessage();
			SmsBoxWriter smsWriter = new SmsBoxWriter(mContentResolver);
			smsWriter.writeOutboxTextMessage(m);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

	}

	public void testSendSms() {
		try {
			SmsSend smsSink = new SmsSend(mActivity);
			SmsBroadcastReceiver sentReceiver = new SmsBroadcastReceiver(this);
			SmsBroadcastReceiver deliveredReceiver = new SmsBroadcastReceiver(
					this);

			mActivity.registerReceiver(sentReceiver, new IntentFilter(
					SmsBroadcastReceiver.ACTION_SMS_SENT));
			mActivity.registerReceiver(deliveredReceiver, new IntentFilter(
					SmsBroadcastReceiver.ACTION_SMS_DELIVERED));
			smsSink.sendTextMessage(SmsHelper.getDummyTextMessage());
			// wait until intent is (hopefully) broadcasted, else it won't
			// trigger the desired callback
			Thread.sleep(3000);

			mActivity.unregisterReceiver(sentReceiver);
			mActivity.unregisterReceiver(deliveredReceiver);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadInboxSms() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			List<TextMessage> inbox = smsSource.getInbox();
			for (TextMessage m : inbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadOutboxSms() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			List<TextMessage> outbox = smsSource.getSentbox();
			for (TextMessage m : outbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	public void smsSentCallback(Context context, Intent intent) {
		log("sms sent. unpacking text message extras ...");

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
				}

			} else {
				log("couldn't find any text message infos at all :(");
			}
		} catch (Exception e) {
			log("FAILED to gather text message extras from intent");
		}
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		log("sms delivered (action: " + intent.getAction() + " )");

	}

}
