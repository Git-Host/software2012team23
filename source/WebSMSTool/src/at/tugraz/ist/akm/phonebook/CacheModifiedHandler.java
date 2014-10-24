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

package at.tugraz.ist.akm.phonebook;

import java.io.Closeable;
import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;

public class CacheModifiedHandler extends Handler implements Closeable
{
    private static class CacheModified
    {
        public static final String KEY = "CacheModifiedName";
        public static final String VALUE = "CacheModifiedValue";
    }

    private LogClient mLog = new LogClient(
            CacheModifiedHandler.class.getCanonicalName(), true);

    private PhonebookBridge mPhonebook = null;


    @SuppressWarnings("unused")
    private CacheModifiedHandler()
    {
    }


    public CacheModifiedHandler(PhonebookBridge phonebook)
    {
        mPhonebook = phonebook;
    }


    @Override
    public void close() throws IOException
    {
        mPhonebook = null;
        mLog = null;
    }


    public Message newCacheModifiedMessage()
    {
        Bundle bundle = new Bundle();
        bundle.putString(CacheModified.KEY, CacheModified.VALUE);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }


    private boolean isCacheModifiedMessage(Message message)
    {
        Bundle bundle = message.getData();
        if (bundle != null)
        {
            String value = bundle.getString(CacheModified.KEY);
            if (value != null && value.compareTo(CacheModified.VALUE) == 0)
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public void handleMessage(Message msg)
    {
        synchronized (this)
        {
            if (mPhonebook != null && isCacheModifiedMessage(msg))
            {
                mLog.debug("processing cache modified in threadID ["
                        + Thread.currentThread().getId() + "]");
                mPhonebook.cacheModifiedCallback();
            }
        }
    }
}
