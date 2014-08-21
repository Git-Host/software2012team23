package at.tugraz.ist.akm.test.phonebook.contact;

import java.util.List;
import java.util.Vector;

import android.net.NetworkInfo.State;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader.StateMachine;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.Contact.Number;
import at.tugraz.ist.akm.phonebook.contact.ContactReader;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.test.testdata.DefaultContactSetInserter;
import at.tugraz.ist.akm.test.testdata.DefaultContacts;

public class CachedAsyncPhonebookReaderTest extends WebSMSToolActivityTestcase
{
    private DefaultContactSetInserter mDefaultInserter = null;


    public CachedAsyncPhonebookReaderTest()
    {
        super(CachedAsyncPhonebookReader.class.getName());
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mDefaultInserter = new DefaultContactSetInserter(mContentResolver);
        mDefaultInserter.insertDefaultContacts();
        StateMachine.reset();

    }


    @Override
    protected void tearDown() throws Exception
    {
        mDefaultInserter.clearDefaultContacts();
        super.tearDown();

    };


    public void test_default_state_transitions()
    {
        ContactFilter filter = new ContactFilter();
        CachedAsyncPhonebookReader reader = new CachedAsyncPhonebookReader(
                filter, null, null);

        assertEquals(StateMachine.state(), StateMachine.ALIVE);
        reader.fetchContacts();
        assertEquals(StateMachine.state(), StateMachine.ALIVE);

        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.STARTED);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READ_DB);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READ_DB_DONE);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READ_CONTENTPROVIDER);
        StateMachine.transit();
        assertEquals(StateMachine.state(),
                StateMachine.READ_CONTENTPROVIDER_DONE);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READY_FOR_CHANGES);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READY_FOR_CHANGES);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.READY_FOR_CHANGES);
    }


    public void test_state_transitions_from_STOP()
    {
        StateMachine.state(StateMachine.STOP);
        assertEquals(StateMachine.state(), StateMachine.STOP);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.STOPPED);
        StateMachine.transit();
        assertEquals(StateMachine.state(), StateMachine.STOPPED);
    }


    public void test_dead_end_transision()
    {
        ContactFilter filter = new ContactFilter();
        CachedAsyncPhonebookReader reader = new CachedAsyncPhonebookReader(
                filter, null, null);

        StateMachine.state(StateMachine.STOPPED);
        assertTrue(StateMachine.state() == StateMachine.STOPPED);
        reader.fetchContacts();
        assertTrue(StateMachine.state() == StateMachine.STOPPED);
        reader.fetchContacts();
        assertTrue(StateMachine.state() == StateMachine.STOPPED);
        reader.fetchContacts();
        assertTrue(StateMachine.state() == StateMachine.STOPPED);

    }


    public void test_construct_async_reader_noException_noErrorlog()
    {
        try
        {
            ContactFilter filter = new ContactFilter();
            filter.getWithPhone();
            ContactReader contactReader = new ContactReader(mContentResolver);
            CachedAsyncPhonebookReader reader = new CachedAsyncPhonebookReader(
                    filter, mContext, contactReader);
            reader.start();

            while (StateMachine.state() != StateMachine.READY_FOR_CHANGES)
            {
                sleepSilent(100);
            }

            reader.finish();

            while (StateMachine.state() != StateMachine.STOPPED)
            {
                sleepSilent(100);
            }
        }
        catch (Throwable e)
        {
            assertTrue(false);
        }
    }


    public void test_read_contacts_async_and_cached()
    {
        ContactFilter filter = new ContactFilter();
        ContactReader contactReader = new ContactReader(mContentResolver);
        CachedAsyncPhonebookReader reader = new CachedAsyncPhonebookReader(
                filter, mContext, contactReader);

        assertEquals(StateMachine.ALIVE, StateMachine.state());
        List<Contact> nsaWatchlist = reader.fetchContacts();
        assertEquals(StateMachine.ALIVE, StateMachine.state());

        assertEquals(0, nsaWatchlist.size());

        reader.start();
        sleepSilent(100);

        for (int i = 0; i < 20; i++)
        {
            nsaWatchlist = reader.fetchContacts();
            printContactsAndState(i, StateMachine.state(), nsaWatchlist);
            sleepSilent(20);
        }

        reader.finish();
    }


    private void printContactsAndState(int readCount, StateMachine state,
            List<Contact> watchlist)
    {
        logDebug("found " + watchlist.size()
                + " terrrrific contacts in state: " + state + " [" + readCount
                + "]");
        for (Contact terrorist : watchlist)
        {
            logDebug("" + terrorist.getDisplayName());
        }
    }


    private void sleepSilent(long durationMs)
    {
        try
        {
            Thread.sleep(durationMs);
        }
        catch (InterruptedException e)
        {
        }
    }


    public void test_verify_contacts_from_cache()
    {
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(StateMachine.READ_DB_DONE);

        List<Contact> dbContacts = reader.fetchContacts();
        String[][] defaultContacts = new DefaultContacts().getDefaultRecords();

        assertEquals(defaultContacts.length, dbContacts.size());

        for (String[] record : defaultContacts)
        {
            Contact c = contactFromRecord(record);

            assertTrue(dbContacts.contains(c));
        }

        synchronized (reader)
        {
            reader.notify();
        }
        reader.finish();
        waitForReaderToBeFinished();
    }


    private void waitForReaderToBeFinished()
    {
        while (StateMachine.state() != StateMachine.STOPPED)
        {
            sleepSilent(100);
        }
    }


    private Contact contactFromRecord(String[] record)
    {
        Contact c = new Contact();

        c.setDisplayName(record[0] + " " + record[1]);
        Vector<Contact.Number> numbers = new Vector<Number>();
        numbers.add(new Contact.Number(record[2],
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
        c.setPhoneNumbers(numbers);
        c.setDisplayName(record[0] + " " + record[1]);
        return c;
    }


    public void test_verify_contacts_from_ContentProvider()
    {
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(StateMachine.READ_CONTENTPROVIDER_DONE);

        List<Contact> providerContacts = reader.fetchContacts();
        String[][] defaultContacts = new DefaultContacts().getDefaultRecords();

        assertEquals(defaultContacts.length, providerContacts.size());

        for (String[] record : defaultContacts)
        {
            Contact c = contactFromRecord(record);
            assertTrue(providerContacts.contains(c));
        }

        synchronized (reader)
        {
            reader.notify();
        }
        reader.finish();
        waitForReaderToBeFinished();

    }


    private TestableCachedAsyncPhonebookReader getHaltedReader(
            StateMachine breakPoint)
    {
        // test_construct_async_reader_noException_noErrorlog();

        ContactFilter filter = new ContactFilter();
        ContactReader contactReader = new ContactReader(mContentResolver);
        TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                filter, mContext, contactReader);

        reader.setNextBreakpoint(breakPoint);
        reader.start();

        while (!reader.isThreadWaiting())
        {
            sleepSilent(100);
        }

        return reader;
    }


    public void test_post_contactModified_state_ALIVE()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.ALIVE, StateMachine.ALIVE);
    }


    public void test_post_contactModified_state_STARTED()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.STARTED, StateMachine.STARTED);
    }


    public void test_post_contactModified_state_READ_DB()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.READ_DB, StateMachine.READ_DB);
    }


    public void test_post_contactModified_state_READ_DB_DONE()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.READ_DB_DONE, StateMachine.READ_DB_DONE);
    }


    public void test_post_contactModified_state_READ_CONTENTPROVIDER()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.READ_CONTENTPROVIDER,
                StateMachine.READ_CONTENTPROVIDER);
    }


    public void test_post_contactModified_state_READ_CONTENTPROVIDER_DONE()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                StateMachine.READ_CONTENTPROVIDER_DONE,
                StateMachine.READ_CONTENTPROVIDER_DONE);
    }


    private void assert_paused_reader_recieving_contactModified_having_states(
            StateMachine haltedState, StateMachine stateAfterCallback)
    {
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(haltedState);
        reader.contactModifiedCallback();
        assertEquals(stateAfterCallback, StateMachine.state());
        wakeupReader(reader);
        reader.finish();
    }


    private void wakeupReader(TestableCachedAsyncPhonebookReader reader)
    {
        synchronized (reader)
        {
            reader.notify();
        }

        while (reader.isThreadWaiting())
        {
            sleepSilent(100);
        }
        waitForReaderToBeFinished();

    }
}
