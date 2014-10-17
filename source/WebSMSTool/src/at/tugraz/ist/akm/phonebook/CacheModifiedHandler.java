package at.tugraz.ist.akm.phonebook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;

public class CacheModifiedHandler extends Handler
{
    private static class CacheModified
    {
        public static final String KEY = "CacheModifiedName";
        public static final String VALUE = "CacheModifiedValue";
    }

    private LogClient mLog = new LogClient(
            CacheModifiedHandler.class.getCanonicalName());

    private PhonebookBridge mPhonebook = null;


    @SuppressWarnings("unused")
    private CacheModifiedHandler()
    {
    }


    public CacheModifiedHandler(PhonebookBridge phonebook)
    {
        mPhonebook = phonebook;
    }

    public void onClose() {
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
