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

import java.util.List;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.LongSparseArray;
import at.tugraz.ist.akm.trace.LogClient;

public class SmsSender extends LogClient implements SentSmsStorage
{

    private LogClient mLog = new LogClient(SmsSender.class.getCanonicalName());
    private Context mContext = null;
    protected ContentResolver mContentResolver = null;
    private SmsManager mSmsManager = SmsManager.getDefault();
    private long mIntentRequestCode = 1;
    private LongSparseArray<TextMessage> mSmsSentStorage = new LongSparseArray<TextMessage>();


    public SmsSender(Context context)
    {
        super(SmsSender.class.getName());
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }


    synchronized public int sendTextMessage(TextMessage message)
    {
        List<String> parts = mSmsManager.divideMessage(message.getBody());

        int partNum = 0;
        for (String part : parts)
        {
            info("sending part [" + partNum++ + "] to [" + message.getAddress()
                    + "] size in chars [" + part.length() + "] (" + part + ")");
            PendingIntent sentPIntent = getSentPendingIntent(message, part);
            mSmsManager.sendTextMessage(message.getAddress(), null, part,
                    sentPIntent, null);
        }
        return parts.size();
    }


    private PendingIntent getSentPendingIntent(TextMessage message, String part)
    {
        return getSmsPendingIntent(message, part,
                SmsSentBroadcastReceiver.ACTION_SMS_SENT);
    }


    private PendingIntent getSmsPendingIntent(TextMessage message, String part,
            String action)
    {
        Intent textMessageIntent = new Intent(action);
        mIntentRequestCode++;
        textMessageIntent.putExtras(getSmsIdBundle(mIntentRequestCode, message,
                part));
        remember(mIntentRequestCode, message, part);
        PendingIntent sentPIntent = PendingIntent.getBroadcast(mContext,
                (int) mIntentRequestCode, textMessageIntent,
                PendingIntent.FLAG_ONE_SHOT);

        return sentPIntent;
    }


    private Bundle getSmsIdBundle(long sentId, TextMessage message, String part)
    {
        Bundle smsBundle = new Bundle();

        smsBundle.putSerializable(
                SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE_ID,
                sentId);
        return smsBundle;
    }


    private void remember(long sentId, TextMessage message, String part)
    {
        TextMessage storageEntry = new TextMessage(message);
        storageEntry.setBody(part);
        mSmsSentStorage.put(sentId, storageEntry);
    }


    @Override
    synchronized public TextMessage takeMessage(long sentId)
    {
        mLog.debug("take sms [" + sentId + "] from storage");
        TextMessage entry = mSmsSentStorage.get(sentId);
        if (entry != null)
        {
            mSmsSentStorage.remove(sentId);
            mLog.debug("sms [" + sentId + "] found and removed");
        } else
        {
            mLog.debug("sms [" + sentId + "] missing");
        }
        return entry;
    }
}
