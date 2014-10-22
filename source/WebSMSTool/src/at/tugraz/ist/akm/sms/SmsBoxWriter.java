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
import android.content.ContentValues;
import at.tugraz.ist.akm.content.SmsContentConstants;
import at.tugraz.ist.akm.trace.LogClient;

public class SmsBoxWriter extends LogClient
{

    private ContentResolver mContentResolver = null;


    public SmsBoxWriter(ContentResolver contentResolver)
    {
        super(SmsBoxWriter.class.getName());
        mContentResolver = contentResolver;
    }


    /**
     * Stores sms to SmsContent.ContentUri.OUTBOX_URI. The OUTBOX_URI contains
     * queued sms.
     * 
     * @param message
     * @return @see putTextMessageToUri
     */
    public android.net.Uri writeOutboxTextMessage(TextMessage message)
    {
        return putTextMessageToUri(message, SmsContentConstants.Uri.OUTBOX_URI);
    }


    /**
     * Stores sms to SmsContent.ContentUri.SENT_URI. The SENT_URI contains sent
     * sms.
     * 
     * @param message
     * @return @see putTextMessageToUri
     */
    public android.net.Uri writeSentboxTextMessage(TextMessage message)
    {
        return putTextMessageToUri(message, SmsContentConstants.Uri.SENT_URI);
    }


    /**
     * Update the (non-auto-generated) fields of a TextMessage stored in
     * content://sms
     * 
     * @param message
     *            Threaad-id and message-id must be set correctly to match the
     *            message that has to be updated
     * @return amount of affected rows; usually 1 else 0
     */
    public int updateTextMessage(TextMessage message)
    {
        return updateTextMessage(message, SmsContentConstants.Uri.BASE_URI);
    }


    /**
     * stores a text message to Uri
     * 
     * @param message
     * @param destination
     *            to content://sms/*
     * @return the Uri pointing to the newly inserted text message
     */
    private android.net.Uri putTextMessageToUri(TextMessage message,
            android.net.Uri destination)
    {
        return mContentResolver.insert(destination,
                textMessageToValues(message));
    }


    /**
     * @param message
     *            The text message to be updated. The update query depends on
     *            message id AND thread-id.
     * @param destination
     *            content://sms/*
     * @return number of affected rows; normally 0 or 1
     */
    private int updateTextMessage(TextMessage message,
            android.net.Uri destination)
    {
        StringBuffer where = new StringBuffer();
        List<String> likeArgs = new ArrayList<String>();

        where.append(SmsContentConstants.Column.ID + " = ? AND ");
        where.append(SmsContentConstants.Column.THREAD_ID + " = ? ");

        likeArgs.add(message.getId());
        likeArgs.add(message.getThreadId());

        String[] like = new String[likeArgs.size()];
        like = likeArgs.toArray(like);
        int rows = mContentResolver.update(destination,
                textMessageToValues(message), where.toString(), like);

        info("Updated [" + rows + "] rows on [" + destination.toString() + "]");
        return rows;
    }


    /**
     * Puts the fields from TextMessage to ContentValues, where the value keys
     * are the same as the column names of content://sms/*
     * 
     * @param message
     * @return
     */
    private ContentValues textMessageToValues(TextMessage message)
    {
        ContentValues values = new ContentValues();
        values.put(SmsContentConstants.Column.ADDRESS, message.getAddress());
        values.put(SmsContentConstants.Column.BODY, message.getBody());
        values.put(SmsContentConstants.Column.DATE, message.getDate());
        values.put(SmsContentConstants.Column.ERROR_CODE,
                message.getErrorCode());
        values.put(SmsContentConstants.Column.LOCKED, message.getLocked());
        values.put(SmsContentConstants.Column.SUBJECT, message.getSubject());
        values.put(SmsContentConstants.Column.PERSON, message.getPerson());
        values.put(SmsContentConstants.Column.PROTOCOL, message.getProtocol());
        values.put(SmsContentConstants.Column.READ, message.getRead());
        values.put(SmsContentConstants.Column.REPLY_PATH_PRESENT,
                message.getReplyPathPresent());
        values.put(SmsContentConstants.Column.SEEN, message.getSeen());
        values.put(SmsContentConstants.Column.SERVICE_CENTER,
                message.getServiceCenter());
        values.put(SmsContentConstants.Column.STATUS, message.getStatus());
        // the following ones will be auto generated
        // values.put(SmsContent.Content.ID, message.getId());
        // values.put(SmsContent.Content.THREAD_ID, message.getThreadId());
        // values.put(SmsContent.Content.TYPE, message.getType());
        return values;
    }
}
