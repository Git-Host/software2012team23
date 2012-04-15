package at.tugraz.ist.akm.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBoxWriter extends Logable {

	private ContentResolver mContentResolver = null;

	public SmsBoxWriter(ContentResolver c) {
		super(SmsBoxWriter.class.getSimpleName());
		mContentResolver = c;
	}

	/**
	 * Stores sms to SmsContent.ContentUri.OUTBOX_URI.
	 * The OUTBOX_URI contains queued sms.
	 * @param message
	 * @return @see putTextMessageToUri
	 */
	public Uri writeOutboxTextMessage(TextMessage message) {
		return putTextMessageToUri(message, SmsContent.ContentUri.OUTBOX_URI);
	}
	
	/**
	 * Stores sms to SmsContent.ContentUri.SENT_URI.
	 * The SENT_URI contains sent sms.
	 * @param message
	 * @return @see putTextMessageToUri
	 */
	public Uri writeSentboxTextMessage(TextMessage message) {
		return putTextMessageToUri(message, SmsContent.ContentUri.SENT_URI);
	}

	/**
	 * stores a text message to Uri
	 * @param message
	 * @param destination
	 *            to content://sms/*
	 * @return the Uri pointing to the newly inserted text message
	 */
	public Uri putTextMessageToUri(TextMessage message, Uri destination) {
		ContentValues values = new ContentValues();

		values.put(SmsContent.Content.ADDRESS, message.getAddress());
		values.put(SmsContent.Content.BODY, message.getBody());
		values.put(SmsContent.Content.DATE, message.getDate());
		values.put(SmsContent.Content.ERROR_CODE, message.getErrorCode());
		values.put(SmsContent.Content.LOCKED, message.getLocked());
		values.put(SmsContent.Content.SUBJECT, message.getSubject());
		values.put(SmsContent.Content.PERSON, message.getPerson());
		values.put(SmsContent.Content.PROTOCOL, message.getProtocol());
		values.put(SmsContent.Content.READ, message.getRead());
		values.put(SmsContent.Content.REPLY_PATH_PRESENT, message.getReplyPathPresent());
		values.put(SmsContent.Content.SEEN, message.getSeen());
		values.put(SmsContent.Content.SERVICE_CENTER, message.getServiceCenter());
		values.put(SmsContent.Content.STATUS, message.getStatus());
		// the following ones will be auto generated
		// values.put(SmsContent.Content.ID, message.getId());
		// values.put(SmsContent.Content.THREAD_ID, message.getThreadId());
		// values.put(SmsContent.Content.TYPE, message.getType());

		return mContentResolver.insert(destination, values);
	}
}
