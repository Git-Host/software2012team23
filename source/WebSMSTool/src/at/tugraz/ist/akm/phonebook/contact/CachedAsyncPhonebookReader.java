package at.tugraz.ist.akm.phonebook.contact;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.trace.LogClient;

public class CachedAsyncPhonebookReader extends Thread implements
        ContactModifiedCallback
{

    private class ThreadInfo
    {
        public boolean isRunning = true;
        public int sleepMs = 100;
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

    public enum StateMachine {
        ALIVE {
            @Override
            StateMachine nextState()
            {
                ALIVE.logNode(this, STARTED);
                return STARTED;
            }
        },
        STARTED {
            @Override
            StateMachine nextState()
            {
                STARTED.logNode(this, READ_DB);
                return READ_DB;
            }
        },
        READ_DB {
            @Override
            StateMachine nextState()
            {
                READ_DB.logNode(this, READ_DB_DONE);
                return READ_DB_DONE;
            }
        },
        READ_DB_DONE {
            @Override
            StateMachine nextState()
            {
                READ_DB_DONE.logNode(this, READ_CONTENTPROVIDER);
                return READ_CONTENTPROVIDER;
            }
        },
        READ_CONTENTPROVIDER {
            @Override
            StateMachine nextState()
            {
                READ_CONTENTPROVIDER.logNode(this, READ_CONTENTPROVIDER_DONE);
                return READ_CONTENTPROVIDER_DONE;
            }
        },
        READ_CONTENTPROVIDER_DONE {
            @Override
            StateMachine nextState()
            {
                READ_CONTENTPROVIDER_DONE.logNode(this, READY_FOR_CHANGES);
                return READY_FOR_CHANGES;
            }
        },
        READY_FOR_CHANGES {
            @Override
            StateMachine nextState()
            {
                READY_FOR_CHANGES.logLeaf(this);
                return READY_FOR_CHANGES;
            }
        },
        STOPPED {
            @Override
            StateMachine nextState()
            {
                STOPPED.logLeaf(this);
                return STOPPED;
            }
        };

        private static LogClient mLog = new LogClient(
                StateMachine.class.getName());

        private static StateMachine mState = StateMachine.ALIVE;


        abstract StateMachine nextState();


        public static StateMachine transit()
        {
            return mState = mState.nextState();
        }


        public static StateMachine reset()
        {
            return mState = StateMachine.ALIVE;
        }


        public static StateMachine state()
        {
            return mState;
        }


        public static void state(StateMachine alternativeState)
        {
            mState = alternativeState;
        }


        private void logNode(StateMachine oldState, StateMachine newState)
        {
            mLog.debug("transition: " + oldState.toString() + " -> "
                    + newState.toString());
        }


        private void logLeaf(StateMachine statusQuo)
        {
            mLog.debug("dead end reached: " + statusQuo.toString());
        }

    }

    private LogClient mLog = new LogClient(
            CachedAsyncPhonebookReader.class.getName());
    private ContactSources mContactSources = new ContactSources();
    private ContactFilter mContactFilter = null;
    private Context mApplicationContext = null;
    private PhonebookCacheDB mPhonebookCacheDB = null;
    private IContactReader mContentproviderContactReader = null;
    private ThreadInfo mThreadInfo = new ThreadInfo();
    private TimingInfo mTimingInfo = new TimingInfo();


    public CachedAsyncPhonebookReader(ContactFilter filter,
            Context applicationContext, IContactReader contactReader)
    {
        mApplicationContext = applicationContext;
        mPhonebookCacheDB = new PhonebookCacheDB(mApplicationContext);
        mContactFilter = filter;
        mContentproviderContactReader = contactReader;
        StateMachine.state(StateMachine.ALIVE);
    }


    public List<Contact> fetchContacts()
    {
        synchronized (mContactSources)
        {
            switch (StateMachine.state())
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
        while (mThreadInfo.isRunning)
        {
            if (StateMachine.state() != StateMachine.READY_FOR_CHANGES)
            {
                readContactsFromCacheAndProvider();
            }

            sleepSilent(100);

        }
    };


    private void sleepSilent(long sleepMs)
    {
        try
        {
            Thread.sleep(mThreadInfo.sleepMs);
        }
        catch (InterruptedException ie) // don't care
        {
        }
    }


    public void finish()
    {
        mThreadInfo.isRunning = false;
        StateMachine.state(StateMachine.STOPPED);
    }


    public void onClose()
    {

        mPhonebookCacheDB.clear();
        for (Contact c : mContactSources.contentProvider)
        {
            mPhonebookCacheDB.cache(c);
        }
        mPhonebookCacheDB.close();
    }


    private void readContactsFromCacheAndProvider()
    {
        StateMachine.transit();

        switch (StateMachine.state())
        {
        case READ_DB:
            synchronized (mContactSources)
            {
                mContactSources.cached = tryReadFromDatabase();
            }
            break;

        case READ_CONTENTPROVIDER:
            synchronized (mContactSources)
            {
                mContactSources.contentProvider = fetchFromContentProvider();
            }
            break;

        case READY_FOR_CHANGES:
            mLog.debug("read cached database in" + mTimingInfo.readDBDurationMs
                    + "[ms]");
            mLog.debug("read ContentProvider in"
                    + mTimingInfo.readContentProvicerDurationMs + "[ms]");
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
        StateMachine currentState = StateMachine.state();
        if (currentState == StateMachine.READ_CONTENTPROVIDER
                || currentState == StateMachine.READ_CONTENTPROVIDER_DONE
                || currentState == StateMachine.READY_FOR_CHANGES)
        {
            StateMachine.state(StateMachine.READ_CONTENTPROVIDER);
        }
    }
}
