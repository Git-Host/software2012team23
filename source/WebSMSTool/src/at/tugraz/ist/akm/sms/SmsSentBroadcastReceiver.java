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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import at.tugraz.ist.akm.trace.Logable;

public class SmsSentBroadcastReceiver extends BroadcastReceiver
{

	public static final String ACTION_SMS_SENT = "at.tugraz.ist.akm.sms.SMS_SENT_ACTION";
	public static final String ACTION_SMS_DELIVERED = "at.tugraz.ist.akm.sms.SMS_DELIVERED_ACTION";
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	public static final String EXTRA_BUNDLE_KEY_TEXTMESSAGE = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_TEXTMESSAGE_KEY";
	public static final String EXTRA_BUNDLE_KEY_TEXTMESSAGELIST = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_TEXTMESSAGELIST_KEY";

	private static final String EXTRA_BUNDLE_KEY_PDU = "pdus";

	private SmsIOCallback mCallback = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public SmsSentBroadcastReceiver(SmsIOCallback callback)
	{
		mCallback = callback;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		
		if (action.compareTo(ACTION_SMS_SENT) == 0)
		{
			mLog.logDebug("passing callback [ACTION_SMS_SENT]");
			TextMessage message = extractSmsFromIntent(intent);
			List<TextMessage> messages = new ArrayList<TextMessage>();
			messages.add(message);
			mCallback.smsSentCallback(context, messages);

		}
		else if (action.compareTo(ACTION_SMS_DELIVERED) == 0)
		{
			mLog.logDebug("passing callback [ACTION_SMS_DELIVERED]");
			List<TextMessage> messages = new ArrayList<TextMessage>();
			messages.add(extractSmsFromIntent(intent));
			mCallback.smsDeliveredCallback(context, messages);
		}
		else if (action.compareTo(ACTION_SMS_RECEIVED) == 0)
		{
			mLog.logDebug("passing callback [ACTION_SMS_RECEIVED]");
			List<TextMessage> messages = extractSmsListFromIntentPdu(intent);
			mCallback.smsReceivedCallback(context, messages);
		}
		else
		{
			 mLog.logVerbose("unknown action received: " + action);
		}
	}
	
	
	private TextMessage extractSmsFromIntent(Intent intent) {
		try {
			Bundle extrasBundle = intent.getExtras();
			if (extrasBundle != null) {
				Serializable serializedTextMessage = extrasBundle
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);
				
				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					return sentMessage;
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}	
	
	
	private List<TextMessage> extractSmsListFromIntentPdu(Intent intent)
	{
		Bundle bundle = intent.getExtras();

		ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.getSerializable(EXTRA_BUNDLE_KEY_PDU);

			if  ( pdus != null ) {
				for (int idx = 0; idx < pdus.length; ++idx)
				{
					SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[idx]);
					messages.add(parseToTextMessage(sms));
				}
			}
			else {
				mLog.logVerbose("bundle contains no pdu(s)");
			}
		}
		return messages;
	}	
	
		
	private TextMessage parseToTextMessage(SmsMessage sms) {
		TextMessage textMessage = new TextMessage();
		textMessage.setDate(Long.toString(new Date().getTime()));
		textMessage.setAddress(sms.getOriginatingAddress());
		textMessage.setBody(sms.getMessageBody());
		return textMessage;
	}

};
