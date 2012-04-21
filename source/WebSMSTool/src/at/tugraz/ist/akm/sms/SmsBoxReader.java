package at.tugraz.ist.akm.sms;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.content.query.TextMessageQueryBuilder;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBoxReader {

	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsBoxReader(ContentResolver c) {
		mContentResolver = c;
	}

	public List<TextMessage> getTextMessages(TextMessageFilter filter) throws NullPointerException {
		return getSms(filter);
	}

	/**
	 * Reads text messages (a.k.a. SMS) from uri
	 * 
	 * @param smsBoxUri
	 *            sms box uri, @see SmsContent.Uri
	 * 
	 * @return list of text messages
	 */
	private List<TextMessage> getSms(TextMessageFilter filter) throws NullPointerException {
		List<TextMessage> messages = new ArrayList<TextMessage>();
		TextMessageQueryBuilder qBuild = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters qp = qBuild.getQueryArgs();

		if ( qp.uri == null ) {
			throw new NullPointerException("<null> is no valid URI");
		}
		Cursor inbox = mContentResolver.query(qp.uri, qp.as,
				qp.where, qp.like, qp.sortBy);

		if (inbox != null) {
			while (inbox.moveToNext()) {
				messages.add(parseToTextMessge(inbox));
			}
		}
		
		log("read [" + messages.size() + "] messages from [" + filter.getBox() + "]");
		return messages;
	}

	private TextMessage parseToTextMessge(Cursor sms) {
		TextMessage m = new TextMessage();
		m.setAddress(sms.getString(sms
				.getColumnIndex(SmsContent.Content.ADDRESS)));
		m.setBody(sms.getString(sms.getColumnIndex(SmsContent.Content.BODY)));
		m.setDate(sms.getString(sms.getColumnIndex(SmsContent.Content.DATE)));
		m.setId(sms.getString(sms.getColumnIndex(SmsContent.Content.ID)));
		m.setLocked(sms.getString(sms.getColumnIndex(SmsContent.Content.LOCKED)));
		m.setPerson(sms.getString(sms.getColumnIndex(SmsContent.Content.PERSON)));
		m.setProtocol(sms.getString(sms
				.getColumnIndex(SmsContent.Content.PROTOCOL)));
		m.setRead(sms.getString(sms.getColumnIndex(SmsContent.Content.READ)));
		m.setSeen(sms.getString(sms.getColumnIndex(SmsContent.Content.SEEN)));
		m.setServiceCenter(sms.getString(sms
				.getColumnIndex(SmsContent.Content.SERVICE_CENTER)));
		m.setStatus(sms.getString(sms.getColumnIndex(SmsContent.Content.STATUS)));
		m.setThreadId(sms.getString(sms
				.getColumnIndex(SmsContent.Content.THREAD_ID)));
		m.setType(sms.getString(sms.getColumnIndex(SmsContent.Content.MESSAGE_TYPE)));
		return m;
	}
	
	private void log(final String m) {
		mLog.log(m);
	}

}
