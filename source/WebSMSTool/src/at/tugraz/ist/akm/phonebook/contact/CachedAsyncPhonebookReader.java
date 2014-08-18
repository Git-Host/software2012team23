package at.tugraz.ist.akm.phonebook.contact;

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
        public List<Contact> mNoContacts = new Vector<Contact>(0);
        public List<Contact> mCached = mNoContacts;
        public List<Contact> mContentProvider = mNoContacts;
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


    public CachedAsyncPhonebookReader(ContactFilter filter,
            Context applicationContext, IContactReader contactReader)
    {
        mApplicationContext = applicationContext;
        mPhonebookCacheDB = new PhonebookCacheDB(mApplicationContext);
        mContactFilter = filter;
        mContentproviderContactReader = contactReader;

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
                return mContactSources.mNoContacts;

            case READ_DB_DONE:
            case READ_CONTENTPROVIDER:
                mLog.debug("requested contacts from cache: "
                        + mContactSources.mCached.size() + " entries");
                return mContactSources.mCached;

            case READ_CONTENTPROVIDER_DONE:
            case READY_FOR_CHANGES:
            case STOPPED:
                mLog.debug("requested contacts from content provider: "
                        + mContactSources.mContentProvider.size() + " entries");
                return mContactSources.mContentProvider;

            default:
                return mContactSources.mNoContacts;
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

            try
            {
                Thread.sleep(mThreadInfo.sleepMs);
            }
            catch (InterruptedException ie) // don't care
            {
            }

        }
    };


    public void finish()
    {
        mThreadInfo.isRunning = false;
        StateMachine.state(StateMachine.STOPPED);
    }

    public void onClose() {
        mPhonebookCacheDB.close();
    }

    private void readContactsFromCacheAndProvider()
    {
        StateMachine.transit();

        switch (StateMachine.state())
        {
        case READ_DB:
            mContactSources.mCached = tryReadFromDatabase();

        case READ_CONTENTPROVIDER:
            mContactSources.mContentProvider = fetchFromContentProvider();

        default:
        }
    }


    private List<Contact> fetchFromContentProvider()
    {
            List<Contact> c = mContentproviderContactReader.fetchContacts(mContactFilter);
            return c;
    }


    private List<Contact> tryReadFromDatabase()
    {
        return mPhonebookCacheDB.getCached(mContactFilter);
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
