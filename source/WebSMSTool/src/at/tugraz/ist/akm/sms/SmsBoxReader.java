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

package at.tugraz.ist.akm.sms;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.content.query.TextMessageQueryBuilder;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBoxReader {

	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsBoxReader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	public List<TextMessage> getTextMessages(TextMessageFilter filter)
			throws NullPointerException {
		return getSms(filter);
	}

	public List<Integer> getThreadIds(final String address) {
		Uri select = SmsContent.ContentUri.BASE_URI;
		String[] as = { SmsContent.Content.THREAD_ID };
		String where = SmsContent.Content.ADDRESS
				+ " = ? ) GROUP BY ( thread_id ";
		String[] like = { address };
		String sortBy = SmsContent.Content.DATE + " ASC";
		Cursor threadIDs = mContentResolver.query(select, as, where, like,
				sortBy);

		List<Integer> threadIdList = new ArrayList<Integer>();
		if (threadIDs != null) {
			while (threadIDs.moveToNext()) {
				threadIdList.add(Integer.parseInt(threadIDs.getString(threadIDs
						.getColumnIndex(SmsContent.Content.THREAD_ID))));
			}
		}

		return threadIdList;
	}

	/**
	 * Reads text messages (a.k.a. SMS) from uri
	 * 
	 * @param smsBoxUri
	 *            sms box uri, @see SmsContent.Uri
	 * 
	 * @return list of text messages
	 */
	private List<TextMessage> getSms(TextMessageFilter filter)
			throws NullPointerException {
		List<TextMessage> messages = new ArrayList<TextMessage>();
		TextMessageQueryBuilder qBuild = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters qp = qBuild.getQueryArgs();

		if (qp.uri == null) {
			throw new NullPointerException("<null> is no valid URI");
		}
		Cursor inbox = mContentResolver.query(qp.uri, qp.as, qp.where, qp.like,
				qp.sortBy);

		if (inbox != null) {
			while (inbox.moveToNext()) {
				messages.add(parseToTextMessge(inbox));
			}
		}

		log("read [" + messages.size() + "] messages from [" + filter.getBox()
				+ "]");
		return messages;
	}

	private TextMessage parseToTextMessge(Cursor sms) {
		TextMessage message = new TextMessage();
		message.setAddress(sms.getString(sms
				.getColumnIndex(SmsContent.Content.ADDRESS)));
		message.setBody(sms.getString(sms.getColumnIndex(SmsContent.Content.BODY)));
		message.setDate(sms.getString(sms.getColumnIndex(SmsContent.Content.DATE)));
		message.setId(sms.getString(sms.getColumnIndex(SmsContent.Content.ID)));
		message.setLocked(sms.getString(sms.getColumnIndex(SmsContent.Content.LOCKED)));
		message.setPerson(sms.getString(sms.getColumnIndex(SmsContent.Content.PERSON)));
		message.setProtocol(sms.getString(sms
				.getColumnIndex(SmsContent.Content.PROTOCOL)));
		message.setRead(sms.getString(sms.getColumnIndex(SmsContent.Content.READ)));
		message.setSeen(sms.getString(sms.getColumnIndex(SmsContent.Content.SEEN)));
		message.setServiceCenter(sms.getString(sms
				.getColumnIndex(SmsContent.Content.SERVICE_CENTER)));
		message.setStatus(sms.getString(sms.getColumnIndex(SmsContent.Content.STATUS)));
		message.setThreadId(sms.getString(sms
				.getColumnIndex(SmsContent.Content.THREAD_ID)));
		message.setType(sms.getString(sms
				.getColumnIndex(SmsContent.Content.MESSAGE_TYPE)));
		return message;
	}

	private void log(final String message) {
		mLog.logVerbose(message);
	}

}
