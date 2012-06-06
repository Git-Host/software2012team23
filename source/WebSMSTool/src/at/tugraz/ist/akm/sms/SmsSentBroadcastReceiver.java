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
import android.util.Log;

public class SmsSentBroadcastReceiver extends BroadcastReceiver
{

	public static final String ACTION_SMS_SENT = "at.tugraz.ist.akm.sms.SMS_SENT_ACTION";
	public static final String ACTION_SMS_DELIVERED = "at.tugraz.ist.akm.sms.SMS_DELIVERED_ACTION";
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	public static final String EXTRA_BUNDLE_KEY_TEXTMESSAGE = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_TEXTMESSAGE_KEY";
	public static final String EXTRA_BUNDLE_KEY_TEXTMESSAGELIST = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_TEXTMESSAGELIST_KEY";
	public static final String EXTRA_BUNDLE_KEY_PART = "at.tugraz.ist.akm.sms.EXTRA_BUNDLE_PART_KEY";

	private static final String EXTRA_BUNDLE_KEY_PDU = "pdus";

	private SmsIOCallback mCallback = null;

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
			TextMessage message = extractSmsFromIntent(intent);
			List<TextMessage> messages = new ArrayList<TextMessage>();
			messages.add(message);
			mCallback.smsSentCallback(context, messages);

		}
		else if (action.compareTo(ACTION_SMS_DELIVERED) == 0)
		{
			List<TextMessage> messages = extractSmsListFromIntent(intent);
			mCallback.smsDeliveredCallback(context, messages);

		}
		else if (action.compareTo(ACTION_SMS_RECEIVED) == 0)
		{
			List<TextMessage> messages = extractSmsListFromIntent(intent);
			mCallback.smsReceivedCallback(context, messages);
		}
		else
		{
			Log.v(getClass().getSimpleName(), "unknown action received: " + action);
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
	
	
	
	private List<TextMessage> extractSmsListFromIntent(Intent intent)
	{
		Bundle bundle = intent.getExtras();

		ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get(EXTRA_BUNDLE_KEY_PDU);

			for (int idx = 0; idx < pdus.length; ++idx)
			{
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[idx]);
				messages.add(parseToTextMessage(sms));
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
