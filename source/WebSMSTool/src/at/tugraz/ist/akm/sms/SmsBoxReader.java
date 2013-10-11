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
import at.tugraz.ist.akm.content.SmsContentConstants;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.content.query.TextMessageQueryBuilder;
import at.tugraz.ist.akm.trace.LogClient;

public class SmsBoxReader {

	private ContentResolver mContentResolver = null;
	private LogClient mLog = new LogClient(this);

	public SmsBoxReader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	public List<TextMessage> getTextMessages(TextMessageFilter filter)
			throws NullPointerException {
		return getSms(filter);
	}

	public List<Integer> getThreadIds(final String address) {
	    android.net.Uri select = SmsContentConstants.Uri.BASE_URI;
		String[] as = { SmsContentConstants.Column.THREAD_ID };
		String where = SmsContentConstants.Column.ADDRESS
				+ " = ? ) GROUP BY ( thread_id ";
		String[] like = { address };
		String sortBy = SmsContentConstants.Column.DATE + " ASC";
		Cursor threadIDs = mContentResolver.query(select, as, where, like,
				sortBy);

		List<Integer> threadIdList = new ArrayList<Integer>();
		if (threadIDs != null) {
			while (threadIDs.moveToNext()) {
				threadIdList.add(Integer.parseInt(threadIDs.getString(threadIDs
						.getColumnIndex(SmsContentConstants.Column.THREAD_ID))));
			}
			threadIDs.close();
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
			inbox.close();
			
		}

		log("read [" + messages.size() + "] messages from [" + filter.getBox()
				+ "]");
		return messages;
	}

	private TextMessage parseToTextMessge(Cursor sms) {
		TextMessage message = new TextMessage();
		message.setAddress(sms.getString(sms
				.getColumnIndex(SmsContentConstants.Column.ADDRESS)));
		message.setBody(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.BODY)));
		message.setDate(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.DATE)));
		message.setId(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.ID)));
		message.setLocked(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.LOCKED)));
		message.setPerson(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.PERSON)));
		message.setProtocol(sms.getString(sms
				.getColumnIndex(SmsContentConstants.Column.PROTOCOL)));
		message.setRead(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.READ)));
		message.setSeen(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.SEEN)));
		message.setServiceCenter(sms.getString(sms
				.getColumnIndex(SmsContentConstants.Column.SERVICE_CENTER)));
		message.setStatus(sms.getString(sms.getColumnIndex(SmsContentConstants.Column.STATUS)));
		message.setThreadId(sms.getString(sms
				.getColumnIndex(SmsContentConstants.Column.THREAD_ID)));
		message.setType(sms.getString(sms
				.getColumnIndex(SmsContentConstants.Column.MESSAGE_TYPE)));
		return message;
	}

	private void log(final String message) {
		mLog.info(message);
	}

}
