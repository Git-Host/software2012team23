package at.tugraz.ist.akm.test.sms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import at.tugraz.ist.akm.sms.SmsBoxReader;
import at.tugraz.ist.akm.sms.SmsBoxWriter;
import at.tugraz.ist.akm.sms.SmsBridge;
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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Prove existence of expected content provider uris
	 */
	public void testGetSmsContentProviderTables() {
		try {
			logCursor(mContentResolver.query(SmsContent.ContentUri.INBOX_URI,
					null, null, null, null));
			logCursor(mContentResolver.query(SmsContent.ContentUri.QUEUED_URI,
					null, null, null, null));
			logCursor(mContentResolver.query(SmsContent.ContentUri.BASE_URI,
					null, null, null, null));
			logCursor(mContentResolver.query(SmsContent.ContentUri.DRAFT_URI,
					null, null, null, null));

			logCursor(mContentResolver.query(SmsContent.ContentUri.FAILED_URI,
					null, null, null, null));
			logCursor(mContentResolver.query(SmsContent.ContentUri.OUTBOX_URI,
					null, null, null, null));
			logCursor(mContentResolver.query(
					SmsContent.ContentUri.UNDELIVERED_URI, null, null, null,
					null));
			logCursor(mContentResolver.query(SmsContent.ContentUri.SENT_URI,
					null, null, null, null));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private void logCursor(Cursor table) {
		table.moveToNext();
		StringBuffer cols = new StringBuffer();
		for (String col : table.getColumnNames()) {
			cols.append(col + " ");
		}
		log("cursor has [" + table.getCount() + "] entries and ["
				+ table.getColumnCount() + "] cols: " + cols.toString());
	}

	private void logTextMessage(TextMessage message) {
		StringBuffer info = new StringBuffer();
		info.append("address: " + message.getAddress());
		info.append(" body: " + message.getBody());
		info.append(" date: " + message.getDate());
		info.append(" errorCode: " + message.getErrorCode());
		info.append(" locked: " + message.getLocked());
		info.append(" subject: " + message.getSubject());
		info.append(" person: " + message.getPerson());
		info.append(" protocol: " + message.getProtocol());
		info.append(" read: " + message.getRead());
		info.append(" replyPathPresent: " + message.getReplyPathPresent());
		info.append(" seen: " + message.getSeen());
		info.append(" serviceCenter: " + message.getServiceCenter());
		info.append(" status: " + message.getStatus());
		info.append(" id: " + message.getId());
		info.append(" threadId: " + message.getThreadId());
		info.append(" type: " + message.getType());
		log("test message {" + info.toString() + "}");
	}

	public void testWriteSMSToOutbox() {
		try {
			TextMessage m = getDummyTextMessage();
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
			smsSink.sendTextMessage(getDummyTextMessage());
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
				logTextMessage(m);
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
				logTextMessage(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSmsBridgeSendSms() {
		try {
			SmsBridge s = new SmsBridge(mActivity);
			s.sendTextMessage(getDummyTextMessage());
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private String getDateNowString() {
		Date dateNow = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm dd.MM.yyyy");
		StringBuilder now = new StringBuilder(dateformat.format(dateNow));
		return now.toString();

	}

	private TextMessage getDummyTextMessage() {
		String methodName = Thread.currentThread().getStackTrace()[3]
				.getMethodName();
		TextMessage m = new TextMessage();
		m.setAddress("1357");
		m.setBody(methodName + ": Dummy texting generated on "
				+ getDateNowString() + ".");
		m.setDate(Long.toString(new Date().getTime()));
		return m;
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
