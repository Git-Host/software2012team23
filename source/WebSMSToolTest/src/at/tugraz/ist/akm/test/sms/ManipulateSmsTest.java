/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.test.sms;

import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.sms.SmsBoxReader;
import at.tugraz.ist.akm.sms.SmsBoxWriter;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.SmsSender;
import at.tugraz.ist.akm.sms.SmsSentBroadcastReceiver;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;

public class ManipulateSmsTest extends WebSMSToolActivityTestcase implements
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
					SmsContent.Uri.INBOX_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.QUEUED_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.BASE_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.DRAFT_URI, null, null, null, null));

			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.FAILED_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.OUTBOX_URI, null, null, null, null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.UNDELIVERED_URI, null, null, null,
					null));
			SmsHelper.logCursor(mContentResolver.query(
					SmsContent.Uri.SENT_URI, null, null, null, null));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testWriteSMSToOutbox() {
		try {
			TextMessage textMessage = SmsHelper.getDummyTextMessage();
			SmsBoxWriter smsWriter = new SmsBoxWriter(mContentResolver);
			smsWriter.writeOutboxTextMessage(textMessage);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}

	}

	public void testSendExtremLongSms() {
		try {
			SmsSender smsSink = new SmsSender(mContext);
			SmsSentBroadcastReceiver sentReceiver = new SmsSentBroadcastReceiver(
					this);
			SmsSentBroadcastReceiver deliveredReceiver = new SmsSentBroadcastReceiver(
					this);

			mContext.registerReceiver(sentReceiver, new IntentFilter(
					SmsSentBroadcastReceiver.ACTION_SMS_SENT));
			mContext.registerReceiver(deliveredReceiver, new IntentFilter(
					SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
			smsSink.sendTextMessage(SmsHelper.getDummyMultiTextMessage());
			// wait until intent is (hopefully) broadcasted, else it won't
			// trigger the desired callback
			Thread.sleep(1000);

			mContext.unregisterReceiver(sentReceiver);
			mContext.unregisterReceiver(deliveredReceiver);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadInboxSms() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.Uri.INBOX_URI);
			List<TextMessage> inbox = smsSource.getTextMessages(filter);
			for (TextMessage m : inbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
					.getColumnIndex(SmsContent.Column.ID)));
			message.setThreadId(messageCursor.getString(messageCursor
					.getColumnIndex(SmsContent.Column.THREAD_ID)));

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
							.getColumnIndex(SmsContent.Column.BODY));
			assertTrue(0 == messageBodyFromContentprovider.compareTo(message
					.getBody()));
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadOutboxSms() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.Uri.OUTBOX_URI);
			List<TextMessage> outbox = smsSource.getTextMessages(filter);
			for (TextMessage m : outbox) {
				SmsHelper.logTextMessage(m);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadThreadIdNoException() {
		try {
			SmsBoxReader smsSource = new SmsBoxReader(mContentResolver);
			String address = "1357";
			List<Integer> threadIDs = smsSource.getThreadIds(address);

			for (Integer id : threadIDs) {
				logVerbose("fetched sms thread-ID: [" + id + "] for address ["
						+ address + "]");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
	public void smsSentCallback(Context context, List<TextMessage> messages) {
		logVerbose("sms sent (list size: " + messages.size() + " )");
	}

	@Override
	public void smsDeliveredCallback(Context context, List<TextMessage> messages) {
		logVerbose("sms delivered (list size: " + messages.size() + " )");
	}

	@Override
	public void smsReceivedCallback(Context context, List<TextMessage> messages) {
		logVerbose("sms received (list size: " + messages.size() + " )");
	}

	@Override
	public void smsSentErrorCallback(Context context, List<TextMessage> messages) {
		logVerbose("sms sent erroneous (list size: " + messages.size() + " )");
	}

}
