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
import at.tugraz.ist.akm.trace.Logable;

public class SmsSender extends Logable {

	private Context mContext = null;

	protected ContentResolver mContentResolver = null;
	private SmsManager mSmsManager = SmsManager.getDefault();

	private int mIntentRequestCode = 1;

	public SmsSender(Context context) {
		super(SmsSender.class.getSimpleName());
		mContext = context;
		mContentResolver = mContext.getContentResolver();
	}

	public int sendTextMessage(TextMessage message) {
		List<String> parts = mSmsManager.divideMessage(message.getBody());

		int partNum = 0;
		for (String part : parts) {
			logVerbose("sending part [" + partNum++ + "] to [" + message.getAddress()
					+ "] (" + part + ")");
			PendingIntent sentPIntent = getSentPendingIntent(message, part);
			PendingIntent deliveredPIntent = getDeliveredPendingIntent(message,
					part);
			mSmsManager.sendTextMessage(message.getAddress(), null, part,
					sentPIntent, deliveredPIntent);
		}
		return parts.size();
	}

	private PendingIntent getSentPendingIntent(TextMessage message, String part) {
		Intent sentIntent = new Intent(SmsSentBroadcastReceiver.ACTION_SMS_SENT);
		sentIntent.putExtras(getSmsBundle(message, part));
		PendingIntent sentPIntent = PendingIntent.getBroadcast(mContext,
				mIntentRequestCode++, sentIntent, PendingIntent.FLAG_ONE_SHOT);

		return sentPIntent;
	}

	private PendingIntent getDeliveredPendingIntent(TextMessage message,
			String part) {
		Intent deliveredIntent = new Intent(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED);
		deliveredIntent.putExtras(getSmsBundle(message, part));

		PendingIntent deliveredPIntent = PendingIntent.getBroadcast(mContext,
				mIntentRequestCode++, deliveredIntent,
				PendingIntent.FLAG_ONE_SHOT);

		return deliveredPIntent;
	}

	private Bundle getSmsBundle(TextMessage message, String part) {
		Bundle smsBundle = new Bundle();
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE, new TextMessage(message));
		smsBundle.putSerializable(
				SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART, new String(part));
		
		// TODO: try to fix issue 24
		// workaround [1], answer Mar 9 '11 at 23:52
		// [1] http://stackoverflow.com/questions/5252841/classnotfoundexception-on-serializable-class-during-activity-start
		@SuppressWarnings("unused")
		TextMessage tm = (TextMessage) smsBundle.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);
		@SuppressWarnings("unused")
		String pt = (String) smsBundle.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART);
				
		return smsBundle;
	}

}
