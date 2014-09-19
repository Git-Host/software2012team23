package at.tugraz.ist.akm.phonebook.PhonebookCache;

import at.tugraz.ist.akm.trace.LogClient;

public enum CacheStates {
    ALIVE {
        @Override
        CacheStates nextState()
        {
            ALIVE.logNodeTransition(this, STARTED);
            return STARTED;
        }
    },
    STARTED {
        @Override
        CacheStates nextState()
        {
            STARTED.logNodeTransition(this, READ_DB);
            return READ_DB;
        }
    },
    READ_DB {
        @Override
        CacheStates nextState()
        {
            READ_DB.logNodeTransition(this, READ_DB_DONE);
            return READ_DB_DONE;
        }
    },
    READ_DB_DONE {
        @Override
        CacheStates nextState()
        {
            READ_DB_DONE.logNodeTransition(this, READ_CONTENTPROVIDER);
            return READ_CONTENTPROVIDER;
        }
    },
    READ_CONTENTPROVIDER {
        @Override
        CacheStates nextState()
        {
            READ_CONTENTPROVIDER.logNodeTransition(this,
                    READ_CONTENTPROVIDER_DONE);
            return READ_CONTENTPROVIDER_DONE;
        }
    },
    READ_CONTENTPROVIDER_DONE {
        @Override
        CacheStates nextState()
        {
            READ_CONTENTPROVIDER_DONE
                    .logNodeTransition(this, READY_FOR_CHANGES);
            return READY_FOR_CHANGES;
        }
    },
    READY_FOR_CHANGES {
        @Override
        CacheStates nextState()
        {
            READY_FOR_CHANGES.logLeafTransition(this);
            return READY_FOR_CHANGES;
        }
    },
    STOP {
        @Override
        CacheStates nextState()
        {
            STOP.logNodeTransition(this, STOPPED);
            return STOPPED;
        }
    },
    STOPPED {
        @Override
        CacheStates nextState()
        {
            STOPPED.logLeafTransition(this);
            return STOPPED;
        }
    };

    private static LogClient mLog = new LogClient(CacheStates.class.getName());


    private void logNodeTransition(CacheStates oldState, CacheStates newState)
    {
        mLog.debug("transition: " + oldState.toString() + " -> "
                + newState.toString());
    }


    private void logLeafTransition(CacheStates statusQuo)
    {
        mLog.debug("dead end reached: " + statusQuo.toString());
    }


    abstract CacheStates nextState();
}
