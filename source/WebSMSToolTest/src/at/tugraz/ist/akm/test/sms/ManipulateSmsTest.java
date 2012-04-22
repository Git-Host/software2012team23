package at.tugraz.ist.akm.test.sms;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.sms.SmsBoxReader;
import at.tugraz.ist.akm.sms.SmsBoxWriter;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.SmsSender;
import at.tugraz.ist.akm.sms.SmsSentBroadcastReceiver;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase2;

public class ManipulateSmsTest extends WebSMSToolActivityTestcase2 implements
		SmsIOCallback {

	public ManipulateSmsTest() {
		super(ManipulateSmsTest.class.getSimpleName());
	}

	/**
	 * Prove existence of expected content provider uris
	 */
	public void testGetSmsContentProviderTables() {
		try {
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.INBOX_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.QUEUED_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.BASE_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.DRAFT_URI, null, null, null, null));

			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.FAILED_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.OUTBOX_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.UNDELIVERED_URI, null, null, null,
					null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.ContentUri.SENT_URI, null, null, null, null));

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

	public void testSendExtremLongSms() {
		try {
			SmsSender smsSink = new SmsSender(mActivity);
			SmsSentBroadcastReceiver sentReceiver = new SmsSentBroadcastReceiver(
					this);
			SmsSentBroadcastReceiver deliveredReceiver = new SmsSentBroadcastReceiver(
					this);

			mActivity.registerReceiver(sentReceiver, new IntentFilter(
					SmsSentBroadcastReceiver.ACTION_SMS_SENT));
			mActivity.registerReceiver(deliveredReceiver, new IntentFilter(
					SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
			smsSink.sendTextMessage(SmsHelper.getDummyMultiTextMessage());
			// wait until intent is (hopefully) broadcasted, else it won't
			// trigger the desired callback
			Thread.sleep(1000);

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
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.INBOX_URI);
			List<TextMessage> inbox = smsSource.getTextMessages(filter);
			for (TextMessage m : inbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * (1) write message somewhere to content://sms/* (2) let messaging tool
	 * auto generate ID and THREAD_ID (3) read the newly generated IDs (4) write
	 * some other values to the text message
	 */
	public void testUpdateSms() {
		try {
			SmsBoxWriter writer = new SmsBoxWriter(mContentResolver);
			TextMessage message = SmsHelper.getDummyTextMessage();

			// 1. and 2.
			Uri directMessageUri = writer.writeOutboxTextMessage(message);
			Cursor messageCursor = mContentResolver.query(directMessageUri,
					null, null, null, null);
			messageCursor.moveToNext();

			// 3.
			message.setId(messageCursor.getString(messageCursor
					.getColumnIndex(SmsContent.Content.ID)));
			message.setThreadId(messageCursor.getString(messageCursor
					.getColumnIndex(SmsContent.Content.THREAD_ID)));

			// 4.
			message.setBody(message.getBody()
					+ " - This message has been updated");
			assertTrue(1 == writer.updateTextMessage(message));

			// prove correctness of update
			messageCursor = mContentResolver.query(directMessageUri, null,
					null, null, null);
			messageCursor.moveToNext();
			String messageBodyFromContentprovider = messageCursor
					.getString(messageCursor
							.getColumnIndex(SmsContent.Content.BODY));
			assertTrue(0 == messageBodyFromContentprovider.compareTo(message
					.getBody()));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadOutboxSms() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.OUTBOX_URI);
			List<TextMessage> outbox = smsSource.getTextMessages(filter);
			for (TextMessage m : outbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadThreadIdNoException() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			String address = "1357";
			List<Integer> threadIDs = smsSource.getThreadIds(address);

			for (Integer id : threadIDs) {
				log("fetched sms thread-ID: [" + id + "] for address [" + address + "]");
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
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);
				String part = (String) extrasBundle
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART);
				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					StringBuffer infos = new StringBuffer();
					infos.append("SMS to [" + sentMessage.getAddress()
							+ "] sent on [" + sentMessage.getDate()
							+ "], part: [" + part + "], whole message was: ["
							+ sentMessage.getBody() + "]");
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

	@Override
	public void smsReceivedCallback(Context context, Intent intent) {
		log("sms received (action: " + intent.getAction() + " )");

	}

}
