package at.tugraz.ist.akm.test.phonebook.contact;

import java.util.List;
import java.util.Vector;

import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader.StateMachine;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.Contact.Number;
import at.tugraz.ist.akm.phonebook.contact.ContactReader;
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
            sleepSilent(100);
            reader.onClose();
            sleepSilent(100);
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
        reader.onClose();
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
            Contact c = new Contact();

            c.setDisplayName(record[0] + " " + record[1]);
            Vector<Number> numbers = new Vector<Number>();
            numbers.add(new Number(record[2],
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
            c.setPhoneNumbers(numbers);
            c.setDisplayName(record[0] + " " + record[1]);

            assertTrue(dbContacts.contains(c));
        }

    }
    
    public void test_verify_contacts_from_ContentProvider()
    {
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(StateMachine.READ_CONTENTPROVIDER_DONE);

        List<Contact> providerContacts = reader.fetchContacts();
        String[][] defaultContacts = new DefaultContacts().getDefaultRecords();

        assertEquals(defaultContacts.length, providerContacts.size());

        for (String[] record : defaultContacts)
        {
            Contact c = new Contact();

            c.setDisplayName(record[0] + " " + record[1]);
            Vector<Number> numbers = new Vector<Number>();
            numbers.add(new Number(record[2],
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
            c.setPhoneNumbers(numbers);
            c.setDisplayName(record[0] + " " + record[1]);

            assertTrue(providerContacts.contains(c));
        }

    }


    private TestableCachedAsyncPhonebookReader getHaltedReader(StateMachine breakPoint)
    {
        test_construct_async_reader_noException_noErrorlog();

        ContactFilter filter = new ContactFilter();
        ContactReader contactReader = new ContactReader(mContentResolver);
        TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                filter, mContext, contactReader);

        reader.setNextBreakpoint(breakPoint);
        reader.start();

        while (StateMachine.state() != breakPoint)
        {
            sleepSilent(100);
        }
        return reader;
    }
}
