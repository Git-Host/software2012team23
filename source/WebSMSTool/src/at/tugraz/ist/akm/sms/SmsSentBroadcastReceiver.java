package at.tugraz.ist.akm.sms;

import java.util.ArrayList;
import java.util.Date;

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

	public SmsSentBroadcastReceiver(SmsIOCallback s)
	{
		mCallback = s;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if (action.compareTo(ACTION_SMS_SENT) == 0)
		{
			mCallback.smsSentCallback(context, intent);

		}
		else if (action.compareTo(ACTION_SMS_DELIVERED) == 0)
		{
			mCallback.smsDeliveredCallback(context, intent);

		}
		else if (action.compareTo(ACTION_SMS_RECEIVED) == 0)
		{
			// we can receive more than one sms at a time
			injectSmsList(intent);
			mCallback.smsReceivedCallback(context, intent);
		}
		else
		{
			Log.v(getClass().getSimpleName(), "unknown action received: " + action);
		}
	}

	private Bundle getSmsBundle(ArrayList<TextMessage> messages)
	{
		Bundle smsBundle = new Bundle();
		smsBundle.putSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGELIST, messages);
		return smsBundle;
	}
	
	private void injectSmsList(Intent intent)
	{
		Bundle bundle = intent.getExtras();

		ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get(EXTRA_BUNDLE_KEY_PDU);

			for (int i = 0; i < pdus.length; ++i)
			{
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
				messages.add(parseToTextMessage(sms));
			}
			intent.putExtras(getSmsBundle(messages));
		}
	}
	
	private TextMessage parseToTextMessage(SmsMessage sms) {
		TextMessage textMessage = new TextMessage();
		textMessage.setDate(Long.toString(new Date().getTime()));
		textMessage.setAddress(sms.getOriginatingAddress());
		textMessage.setBody(sms.getMessageBody());
		return textMessage;
	}

};
