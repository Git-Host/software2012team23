package at.tugraz.ist.akm.phonebook.contact.PhonebookCache;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.CacheModifiedHandler;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;
import at.tugraz.ist.akm.phonebook.contact.PhonebookCacheDB;
import at.tugraz.ist.akm.trace.LogClient;

public class CachedAsyncPhonebookReader extends Thread implements
        IContactModifiedCallback
{

    private class ThreadInfo
    {
        public int breathingPauseMs = 750;
    }

    private class ContactSources
    {
        public List<Contact> noContacts = new Vector<Contact>(0);
        public List<Contact> cached = noContacts;
        public List<Contact> contentProvider = noContacts;
    }

    private class TimingInfo
    {
        public long readDBDurationMs = 0;
        public long readContentProvicerDurationMs = 0;
    }

    private static final long CACHE_READY_MESSAGE_DELAY_MS = 5 * 1000;

    private LogClient mLog = new LogClient(
            CachedAsyncPhonebookReader.class.getCanonicalName());
    private ContactSources mContactSources = new ContactSources();
    private ContactFilter mContactFilter = null;
    private Context mApplicationContext = null;
    private PhonebookCacheDB mPhonebookCacheDB = null;
    private IContactReader mContentproviderContactReader = null;
    private ThreadInfo mThreadInfo = new ThreadInfo();
    private TimingInfo mTimingInfo = new TimingInfo();
    private Object mWaitMonitor = new Object();
    private CacheModifiedHandler mCacheModifiedHandler = null;
    protected CacheStateMachine mStateMachine = new CacheStateMachine();


    public CachedAsyncPhonebookReader(ContactFilter filter,
            Context applicationContext, IContactReader contactReader)
    {
        this.setName(CachedAsyncPhonebookReader.class.getSimpleName());
        mApplicationContext = applicationContext;
        mPhonebookCacheDB = new PhonebookCacheDB(mApplicationContext);
        mContactFilter = filter;
        mContentproviderContactReader = contactReader;
        mStateMachine.state(CacheStates.ALIVE);
    }


    public void registerCacheModifiedHandler(CacheModifiedHandler handler)
    {
        mCacheModifiedHandler = handler;
    }


    public void unregisterCacheModifiedHandler()
    {
        mCacheModifiedHandler = null;
    }


    public List<Contact> fetchContacts()
    {
        synchronized (mContactSources)
        {
            switch (mStateMachine.state())
            {
            case ALIVE:
            case STARTED:
            case READ_DB:
                mLog.debug("requested contacts from uncomplete cache: 0 entries");
                return mContactSources.noContacts;

            case READ_DB_DONE:
            case READ_CONTENTPROVIDER:
                mLog.debug("requested contacts from cache: "
                        + mContactSources.cached.size() + " entries");
                return mContactSources.cached;

            case READ_CONTENTPROVIDER_DONE:
            case READY_FOR_CHANGES:
            case STOP:
            case STOPPED:
                mLog.debug("requested contacts from content provider: "
                        + mContactSources.contentProvider.size() + " entries");
                return mContactSources.contentProvider;

            default:
                return mContactSources.noContacts;
            }
        }
    }


    @Override
    public void run()
    {
        while (mStateMachine.state() != CacheStates.STOPPED)
        {
            tick();
            breatingPause();
        }
    }


    protected synchronized void tick()
    {
        switch (mStateMachine.state())
        {
        case ALIVE:
        case STARTED:
        case READ_DB:
        case READ_DB_DONE:
        case READ_CONTENTPROVIDER:
            readContactsFromCacheAndProvider();
            break;

        case READ_CONTENTPROVIDER_DONE:
            double speedupRatio = mTimingInfo.readContentProvicerDurationMs
                    / (mTimingInfo.readDBDurationMs + 1);
            mLog.debug("cache speedup " + speedupRatio
                    + "[times] - read cached database in "
                    + mTimingInfo.readDBDurationMs
                    + " [ms] - ContentProvider in "
                    + mTimingInfo.readContentProvicerDurationMs + " [ms]");
            break;

        case READY_FOR_CHANGES:
            break;

        case STOP:
            onClose();
            break;

        default:
            mLog.warning("ignoring unacceptable state");
            sleepSilent(mThreadInfo.breathingPauseMs);
            break;
        }
        mStateMachine.transit();
    }


    protected void breatingPause()
    {
        if (mStateMachine.state() == CacheStates.READY_FOR_CHANGES)
        {
            synchronized (mWaitMonitor)
            {
                try
                {
                    mWaitMonitor.wait();
                }
                catch (InterruptedException e)
                {
                    // don't care
                    mLog.debug("ignoring interrupted exception");
                }
            }
        }
    }


    private void sleepSilent(long sleepMs)
    {
        try
        {
            Thread.sleep(sleepMs);
        }
        catch (InterruptedException ie)
        {
            // don't care
            mLog.debug("ignored interrupted exception");
        }
    }


    public synchronized void finish()
    {
        synchronized (mWaitMonitor)
        {
            mStateMachine.state(CacheStates.STOP);
            mWaitMonitor.notify();
            mLog.debug("cache stopped");
        }
    }


    private void onClose()
    {
        mPhonebookCacheDB.clear();
        for (Contact c : mContactSources.contentProvider)
        {
            mPhonebookCacheDB.cache(c);
        }
        mPhonebookCacheDB.close();
        mLog.debug("cache closed");
    }


    private void sendcacheModified()
    {
        sendMessage(0);
    }


    private void sendDelayedCacheModified()
    {
        sendMessage(CACHE_READY_MESSAGE_DELAY_MS);
    }


    private void sendMessage(long msDelay)
    {
        if (mCacheModifiedHandler != null)
        {
            mLog.debug("sending cache modified in threadID ["
                    + Thread.currentThread().getId() + "]");
            mCacheModifiedHandler.sendMessageDelayed(
                    mCacheModifiedHandler.newCacheModifiedMessage(), msDelay);
        }
    }


    private void readContactsFromCacheAndProvider()
    {
        switch (mStateMachine.state())
        {
        case READ_DB:
            synchronized (mContactSources)
            {
                mContactSources.cached = tryReadFromDatabase();
                sendcacheModified();
            }
            break;

        case READ_CONTENTPROVIDER:
            synchronized (mContactSources)
            {
                mContactSources.contentProvider = fetchFromContentProvider();
                sendDelayedCacheModified();
            }
            break;
        default:
        }
    }


    private List<Contact> fetchFromContentProvider()
    {
        Date start = new Date();
        List<Contact> watchlist = mContentproviderContactReader
                .fetchContacts(mContactFilter);
        mTimingInfo.readContentProvicerDurationMs = new Date().getTime()
                - start.getTime();
        mLog.debug("found contacts " + watchlist.size()
                + " in content provider");
        return watchlist;
    }


    private List<Contact> tryReadFromDatabase()
    {
        Date start = new Date();
        List<Contact> watchlist = mPhonebookCacheDB.getCached(mContactFilter);
        mTimingInfo.readDBDurationMs = new Date().getTime() - start.getTime();

        mLog.debug("found contacts " + watchlist.size() + " in cache DB");
        return watchlist;
    }


    @Override
    public void contactModifiedCallback()
    {
        synchronized (this)
        {
            CacheStates currentState = mStateMachine.state();
            if (currentState == CacheStates.READ_CONTENTPROVIDER
                    || currentState == CacheStates.READ_CONTENTPROVIDER_DONE
                    || currentState == CacheStates.READY_FOR_CHANGES)
            {
                mStateMachine.state(CacheStates.READ_CONTENTPROVIDER);
                synchronized (mWaitMonitor)
                {
                    mWaitMonitor.notifyAll();
                }
            }
        }
    }

}
