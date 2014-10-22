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

package at.tugraz.ist.akm.webservice.service;

import android.os.CountDownTimer;

public class NetworkTrafficPropagationTimer extends CountDownTimer
{
    private int mLastTxBytesPropagated = 0;
    private int mLastRxBytesPropagated = 0;
    private int mPropagationBytesDelta = 2 * (2 ^ 10);
    private WebSMSToolService mCallback;


    public NetworkTrafficPropagationTimer(int durationSeconds, int tickSeconds,
            WebSMSToolService callback)
    {
        super(durationSeconds * 1000, tickSeconds * 1000);
        mCallback = callback;
    }


    private NetworkTrafficPropagationTimer()
    {
        super(0, 0);
    }


    @Override
    public void onTick(long millisUntilFinished)
    {
        if (mCallback == null)
        {
            return;
        }

        int currentTxByteStatus = (int) (mCallback.getSentBytesCount());

        if ((mLastTxBytesPropagated <= 0)
                || (mLastTxBytesPropagated + mPropagationBytesDelta) < currentTxByteStatus)
        {
            mLastTxBytesPropagated = currentTxByteStatus;
            mCallback
                    .onManagementClientRequestSentBytesCount(mLastTxBytesPropagated);
        }

        int currentRxByteStatus = (int) (mCallback.getReceivedBytesCount());
        if ((mLastRxBytesPropagated <= 0)
                || (mLastRxBytesPropagated + mPropagationBytesDelta) < currentRxByteStatus)
        {
            mLastRxBytesPropagated = currentRxByteStatus;
            mCallback
                    .onManagementClientRequestReceivedBytesCount(mLastRxBytesPropagated);
        }
    }


    @Override
    public void onFinish()
    {
        this.start();
    }
};
