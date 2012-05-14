package at.tugraz.ist.akm.sms;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.trace.Logable;

public class SmsBridge extends Logable implements SmsIOCallback {

	private Context mContext = null;
	private ContentResolver mContentResolver = null;

	private SmsSender mSmsSink = null;
	private SmsBoxReader mSmsBoxReader = null;
	private SmsBoxWriter mSmsBoxWriter = null;

	private SmsSentBroadcastReceiver mSmsSentNotifier = new SmsSentBroadcastReceiver(
			this);
	private SmsIOCallback mExternalSmsSentCallback = null;

	public SmsBridge(Context c) {
		super(SmsBridge.class.getSimpleName());
		mContext = c;
		mContentResolver = mContext.getContentResolver();
		mSmsSink = new SmsSender(mContext);
		mSmsBoxReader = new SmsBoxReader(mContentResolver);
		mSmsBoxWriter = new SmsBoxWriter(mContentResolver);
	}

	public int sendTextMessage(TextMessage message) {
		logV("sending message to [" + message.getAddress() + "]");
		message.setDate(Long.toString(new Date().getTime()));
		return mSmsSink.sendTextMessage(message);
	}

	public List<TextMessage> fetchTextMessages(TextMessageFilter filter) {
		List<TextMessage> messages = mSmsBoxReader.getTextMessages(filter);
		logV("fetched [" + messages.size() + "] messages");
		return messages;
	}

	public int updateTextMessage(TextMessage message) {
		return mSmsBoxWriter.updateTextMessage(message);
	}

	public List<Integer> fetchThreadIds(final String address) {
		return mSmsBoxReader.getThreadIds(address);
	}

	public void setSmsSentCallback(SmsIOCallback c) {
		logV("registered new [SmsSentCallback] callback");
		mExternalSmsSentCallback = c;
	}

	public void start() {
		registerSmsSentNotification();
		registerSmsDeliveredNotification();
		registerSmsReceivedNotification();
	}

	public void stop() {
		mContext.unregisterReceiver(mSmsSentNotifier);
		mSmsSentNotifier = null;
	}

	/**
	 * 1st: try to parse the TextMessage and store to content://sms/sent 2nd:
	 * regardless of the state bypass the event to external audience but
	 * separate erroneous states from good ones. Note, on
	 * {@link SmsSentBroadcastReceiver.ACTION_SMS_SENT}: Since
	 * SmsSentBroadcastReceiver never can get the result code (getResultCode()),
	 * only this interface method will be ever called from
	 * SmsSentBroadcastReceiver.
	 */
	@Override
	public void smsSentCallback(Context context, Intent intent) {
		boolean sentSuccessfully = storeMessageToCorrectBox(intent);

		if (mExternalSmsSentCallback != null) {

			if (sentSuccessfully) {
				logV("bypassing SmsSentCallback.smsSentCallback()");
				mExternalSmsSentCallback.smsSentCallback(context, intent);
			} else {
				logV("bypassing SmsSendErrorCallback.smsSentCallback()");
				mExternalSmsSentCallback.smsSentErrorCallback(context, intent);
			}
		} else {
			logV("no external callback [SmsSentCallback.smsSentCallback()] found - callback ends here");
		}
	}

	/**
	 * Note, on {@link SmsSentBroadcastReceiver.ACTION_SMS_SENT}: Since
	 * SmsSentBroadcastReceiver never can get the result code (getResultCode()),
	 * this interface method will be never called from SmsSentBroadcastReceiver.
	 */
	@Override
	public void smsSentErrorCallback(Context context, Intent intent) {
	}

	/**
	 * bypass the event to external audience
	 */
	@Override
	public void smsDeliveredCallback(Context context, Intent intent) {
		if (mExternalSmsSentCallback != null) {
			logV("bypassing SmsSentCallback.smsDeliveredCallback()");
			mExternalSmsSentCallback.smsDeliveredCallback(context, intent);
		} else {
			logV("no external callback [SmsSentCallback.smsDeliveredCallback()] found - callback ends here");
		}
	}

	/**
	 * simply bypass the callback to external listener
	 */
	@Override
	public void smsReceivedCallback(Context context, Intent intent) {
		if (mExternalSmsSentCallback != null) {
			logV("bypassing mExternalSmsReceivedCallback.smsReceivedCallback()");
			mExternalSmsSentCallback.smsReceivedCallback(context, intent);
		} else {
			logV("no external callback [mExternalSmsReceivedCallback.smsReceivedCallback()] found - callback ends here");
		}

	}

	private void registerSmsSentNotification() {
		logV("registered new IntentFilter [ACTION_SMS_SENT]");
		mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_SENT));
	}

	private void registerSmsDeliveredNotification() {
		logV("registered new IntentFilter [ACTION_SMS_DELIVERED]");
		mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
	}

	private void registerSmsReceivedNotification() {
		logV("registered new IntentFilter [ACTION_SMS_SENT]");
		mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
				SmsSentBroadcastReceiver.ACTION_SMS_RECEIVED));
	}

	private TextMessage parseToTextMessgae(Intent intent) {
		try {
			Bundle extrasBundle = intent.getExtras();
			if (extrasBundle != null) {
				Serializable serializedTextMessage = extrasBundle
						.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);

				if (serializedTextMessage != null) {
					TextMessage sentMessage = (TextMessage) serializedTextMessage;
					return sentMessage;
				}

			} else {
				logV("couldn't find any text message infos at all :(");
			}
		} catch (Exception e) {
			logV("FAILED to gather text message extras from intent");
		}
		return null;
	}

	/**
	 * Is being called when the send state of a TextMessage is clear. If state
	 * is OK, then store message to sent-box. On error place the TextMessage to
	 * out-box (box for pending or not sent messages).
	 * 
	 * @param intent
	 *            where to parse the TextMessage from
	 * @return true if correctly sent else false
	 */
	private boolean storeMessageToCorrectBox(Intent intent) {
		boolean isSuccessfullySent = false;
		TextMessage sentMessage = parseToTextMessgae(intent);
		
		String verboseSentState = null;

		switch (mSmsSentNotifier.getResultCode()) {
		case Activity.RESULT_OK:
			verboseSentState = "to address [" + sentMessage.getAddress()
					+ "] on [" + sentMessage.getDate() + "] ("
					+ sentMessage.getBody() + ")";

			mSmsBoxWriter.writeSentboxTextMessage(sentMessage);
			isSuccessfullySent = true;
			break;

		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			verboseSentState = "Error.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NO_SERVICE:
			verboseSentState = "Error: No service.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_NULL_PDU:
			verboseSentState = "Error: Null PDU.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;

		case SmsManager.RESULT_ERROR_RADIO_OFF:
			verboseSentState = "Error: Radio off.";
			sentMessage.setLocked("");
			sentMessage.setErrorCode("");
			mSmsBoxWriter.writeOutboxTextMessage(sentMessage);
			break;
		}

		if (isSuccessfullySent) {
			logV("text message sent successfully (" + verboseSentState + ")");
		} else {
			logV(verboseSentState);
		}

		return isSuccessfullySent;
	}
}