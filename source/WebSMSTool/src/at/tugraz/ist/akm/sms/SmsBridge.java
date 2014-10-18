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

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.trace.LogClient;

public class SmsBridge extends LogClient implements ISmsIOCallback, Closeable
{

    private Context mContext = null;
    private ContentResolver mContentResolver = null;

    private SmsSender mSmsSink = null;
    private SmsBoxReader mSmsBoxReader = null;
    private SmsBoxWriter mSmsBoxWriter = null;

    private SmsSentBroadcastReceiver mSmsSentNotifier = null;

    private ISmsIOCallback mExternalSmsSentCallback = null;


    public SmsBridge(Context context)
    {
        super(SmsBridge.class.getName());
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mSmsSink = new SmsSender(mContext);
        mSmsSentNotifier = new SmsSentBroadcastReceiver(this, mSmsSink);
        mSmsBoxReader = new SmsBoxReader(mContentResolver);
        mSmsBoxWriter = new SmsBoxWriter(mContentResolver);
    }


    public int sendTextMessage(TextMessage message)
    {
        debug("sending message to [" + message.getAddress() + "]");
        message.setDate(Long.toString(new Date().getTime()));
        return mSmsSink.sendTextMessage(message);
    }


    public List<TextMessage> fetchTextMessages(TextMessageFilter filter)
    {
        List<TextMessage> messages = mSmsBoxReader.getTextMessages(filter);
        debug("fetched [" + messages.size() + "] messages");
        return messages;
    }


    public int updateTextMessage(TextMessage message)
    {
        return mSmsBoxWriter.updateTextMessage(message);
    }


    public List<Integer> fetchThreadIds(final String phoneNumber)
    {
        return mSmsBoxReader.getThreadIds(phoneNumber);
    }


    synchronized public void setSmsSentCallback(ISmsIOCallback callback)
    {
        debug("registered new [SmsSentCallback] callback");
        mExternalSmsSentCallback = callback;
    }


    public void start()
    {
        registerSmsSentNotification();
        // registerSmsDeliveredNotification();
        // registerSmsReceivedNotification();
    }


    public void stop()
    {
        mContext.unregisterReceiver(mSmsSentNotifier);
        mSmsSentNotifier = null;
    }


    @Override
    public void close() throws IOException
    {
        mContext = null;
        mContentResolver = null;
        mSmsSink = null;
        mSmsBoxReader = null;
        mSmsBoxWriter = null;
        mExternalSmsSentCallback = null;
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
    synchronized public void smsSentCallback(Context context,
            List<TextMessage> messages)
    {
        boolean sentSuccessfully = storeMessageToCorrectBox(messages);

        if (mExternalSmsSentCallback != null)
        {

            if (sentSuccessfully)
            {
                debug("bypassing SmsSentCallback.smsSentCallback()");
                mExternalSmsSentCallback.smsSentCallback(context, messages);
            } else
            {
                debug("bypassing SmsSendErrorCallback.smsSentCallback()");
                mExternalSmsSentCallback
                        .smsSentErrorCallback(context, messages);
            }
        } else
        {
            debug("no external callback [SmsSentCallback.smsSentCallback()] found - callback ends here");
        }
    }


    /**
     * Note, on {@link SmsSentBroadcastReceiver.ACTION_SMS_SENT}: Since
     * SmsSentBroadcastReceiver never can get the result code (getResultCode()),
     * this interface method will be never called from SmsSentBroadcastReceiver.
     */
    @Override
    synchronized public void smsSentErrorCallback(Context context,
            List<TextMessage> messages)
    {
        error("failed to send [" + messages.size() + "] messages");
    }


    /**
     * bypass the event to external audience
     */
    @Override
    synchronized public void smsDeliveredCallback(Context context,
            List<TextMessage> messages)
    {
        if (mExternalSmsSentCallback != null)
        {
            debug("bypassing SmsSentCallback.smsDeliveredCallback()");
            mExternalSmsSentCallback.smsDeliveredCallback(context, messages);
        } else
        {
            debug("no external callback [SmsSentCallback.smsDeliveredCallback()] found - callback ends here");
        }
    }


    /**
     * simply bypass the callback to external listener
     */
    @Override
    synchronized public void smsReceivedCallback(Context context,
            List<TextMessage> messages)
    {
        if (mExternalSmsSentCallback != null)
        {
            debug("bypassing mExternalSmsReceivedCallback.smsReceivedCallback()");
            mExternalSmsSentCallback.smsReceivedCallback(context, messages);
        } else
        {
            debug("no external callback [mExternalSmsReceivedCallback.smsReceivedCallback()] found - callback ends here");
        }

    }


    private void registerSmsSentNotification()
    {
        debug("registered new IntentFilter [ACTION_SMS_SENT]");
        mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
                SmsSentBroadcastReceiver.ACTION_SMS_SENT));
    }


    // private void registerSmsDeliveredNotification() {
    // debug("registered new IntentFilter [ACTION_SMS_DELIVERED]");
    // mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
    // SmsSentBroadcastReceiver.ACTION_SMS_DELIVERED));
    // }

    // private void registerSmsReceivedNotification() {
    // debug("registered new IntentFilter [ACTION_SMS_RECEIVED]");
    // mContext.registerReceiver(mSmsSentNotifier, new IntentFilter(
    // SmsSentBroadcastReceiver.ACTION_SMS_RECEIVED));
    // }

    private boolean storeMessageToCorrectBox(List<TextMessage> messages)
    {
        boolean isSuccessfullySent = false;

        String verboseSentState = null;

        for (TextMessage sentMessage : messages)
        {
            int resultCode = mSmsSentNotifier.getResultCode();
            switch (resultCode)
            {
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
            default:
                verboseSentState = "uncaught state: [" + resultCode + "]";
                break;
            }
        }

        if (isSuccessfullySent)
        {
            debug("text message sent successfully (" + verboseSentState + ")");
        } else
        {
            error(verboseSentState);
        }

        return isSuccessfullySent;
    }
}
