package at.tugraz.ist.akm.sms;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.trace.Logable;

public class SmsRead {

	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsRead(ContentResolver c) {
		mContentResolver = c;
	}

	public List<TextMessage> getInbox() {
		return getSms(SmsContent.Uri.SMS_INBOX);
	}
	
	public List<TextMessage> getOutbox() {
		return getSms(SmsContent.Uri.SMS_OUTBOX);
	}
	
	/**
	 * Reads text messages (a.k.a. SMS) from uri
	 * @param smsBoxUri sms box uri, see SmsContent.Uri
	 * @return list of text messages
	 */
	private List<TextMessage> getSms(String smsBoxUri) {
		List<TextMessage> messages = new ArrayList<TextMessage>();
		Uri inboxUri = Uri.parse(smsBoxUri);
		Cursor inbox = mContentResolver.query(inboxUri, null, null, null, null);

		if (inbox != null) {
			while (inbox.moveToNext()) {
				messages.add(parseToTextMessge(inbox));
			}
		}
		log("read ["+ messages.size() + "] messages from ["+ smsBoxUri + "]");
		return messages;
	}

	private TextMessage parseToTextMessge(Cursor sms) {
		TextMessage m = new TextMessage();
		m.setAddress(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.ADDRESS)));
		m.setBody(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.BODY)));
		m.setDate(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.DATE)));
		m.setId(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.ID)));
		m.setLocked(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.LOCKED)));
		m.setPerson(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.PERSON)));
		m.setProtocol(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.PROTOCOL)));
		m.setRead(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.READ)));
		m.setSeen(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.SEEN)));
		m.setServiceCenter(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.SERVICE_CENTER)));
		m.setStatus(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.STATUS)));
		m.setThreadId(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.THREAD_ID)));
		m.setType(sms.getString(sms.getColumnIndex(SmsContent.MessageColumns.TYPE)));
		return m;
	}

	private void log(final String m) {
		mLog.log(m);
	}

}
