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

package at.tugraz.ist.akm.test.phonebook.PhonehookCache;

import java.util.List;
import java.util.Vector;

import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.PhonebookCache.CacheStateMachine;
import at.tugraz.ist.akm.phonebook.PhonebookCache.CacheStates;
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
        super(CachedAsyncPhonebookReaderTest.class.getName());
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mDefaultInserter = new DefaultContactSetInserter(mContentResolver);
        mDefaultInserter.insertDefaultContacts();
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
        TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                filter, null, null);

        assertEquals(reader.state(), CacheStates.ALIVE);
        reader.fetchContacts();
        assertEquals(reader.state(), CacheStates.ALIVE);

        reader.transit();
        assertEquals(reader.state(), CacheStates.STARTED);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READ_DB);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READ_DB_DONE);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READ_CONTENTPROVIDER);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READ_CONTENTPROVIDER_DONE);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READY_FOR_CHANGES);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READY_FOR_CHANGES);
        reader.transit();
        assertEquals(reader.state(), CacheStates.READY_FOR_CHANGES);
    }


    public void test_state_transitions_from_STOP()
    {
        CacheStateMachine stateMachine = new CacheStateMachine();

        stateMachine.state(CacheStates.STOP);
        assertEquals(stateMachine.state(), CacheStates.STOP);
        stateMachine.transit();
        assertEquals(stateMachine.state(), CacheStates.STOPPED);
        stateMachine.transit();
        assertEquals(stateMachine.state(), CacheStates.STOPPED);
    }


    public void test_dead_end_transision()
    {
        ContactFilter filter = new ContactFilter();
        TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                filter, null, null);

        reader.state(CacheStates.STOPPED);
        assertTrue(reader.state() == CacheStates.STOPPED);
        reader.fetchContacts();
        assertTrue(reader.state() == CacheStates.STOPPED);
        reader.fetchContacts();
        assertTrue(reader.state() == CacheStates.STOPPED);
        reader.fetchContacts();
        assertTrue(reader.state() == CacheStates.STOPPED);

    }


    public void test_construct_async_reader_noException_noErrorlog()
    {
        try
        {
            ContactFilter filter = new ContactFilter();
            filter.getWithPhone();
            ContactReader contactReader = new ContactReader(mContentResolver);
            TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                    filter, mContext, contactReader);
            reader.start();

            while (reader.state() != CacheStates.READY_FOR_CHANGES)
            {
                sleepSilent(100);
            }

            reader.finish();

            while (reader.state() != CacheStates.STOPPED)
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
        TestableCachedAsyncPhonebookReader reader = new TestableCachedAsyncPhonebookReader(
                filter, mContext, contactReader);

        assertEquals(CacheStates.ALIVE, reader.state());
        List<Contact> nsaWatchlist = reader.fetchContacts();
        assertEquals(CacheStates.ALIVE, reader.state());

        assertEquals(0, nsaWatchlist.size());

        reader.start();
        sleepSilent(100);

        for (int i = 0; i < 20; i++)
        {
            nsaWatchlist = reader.fetchContacts();
            printContactsAndState(i, reader.state(), nsaWatchlist);
            sleepSilent(20);
        }

        reader.finish();
    }


    private void printContactsAndState(int readCount, CacheStates state,
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
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(CacheStates.READ_DB_DONE);

        List<Contact> dbContacts = reader.fetchContacts();
        String[][] defaultContacts = new DefaultContacts().getDefaultRecords();

        assertTrue(defaultContacts.length <= dbContacts.size());

        for (String[] record : defaultContacts)
        {
            Contact c = contactFromRecord(record);

            assertTrue(dbContacts.contains(c));
        }

        synchronized (reader)
        {
            reader.notify();
        }
        waitForReaderToBeReady(reader);
        reader.finish();
    }


    private void waitForReaderToBeReady(
            TestableCachedAsyncPhonebookReader reader)
    {
        while (reader.state() != CacheStates.READY_FOR_CHANGES)
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
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(CacheStates.READ_CONTENTPROVIDER_DONE);

        List<Contact> providerContacts = reader.fetchContacts();
        String[][] defaultContacts = new DefaultContacts().getDefaultRecords();

        assertTrue(defaultContacts.length <= providerContacts.size());

        for (String[] record : defaultContacts)
        {
            Contact c = contactFromRecord(record);
            assertTrue(providerContacts.contains(c));
        }

        synchronized (reader)
        {
            reader.notify();
        }
        waitForReaderToBeReady(reader);
        reader.finish();
    }


    private TestableCachedAsyncPhonebookReader getHaltedReader(
            CacheStates breakPoint)
    {
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
                CacheStates.ALIVE, CacheStates.ALIVE);
    }


    public void test_post_contactModified_state_STARTED()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                CacheStates.STARTED, CacheStates.STARTED);
    }


    public void test_post_contactModified_state_READ_DB()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                CacheStates.READ_DB, CacheStates.READ_DB);
    }


    public void test_post_contactModified_state_READ_DB_DONE()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                CacheStates.READ_DB_DONE, CacheStates.READ_DB_DONE);
    }


    public void test_post_contactModified_state_READ_CONTENTPROVIDER()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                CacheStates.READ_CONTENTPROVIDER,
                CacheStates.READ_CONTENTPROVIDER);
    }


    public void test_post_contactModified_state_READ_CONTENTPROVIDER_DONE()
    {
        assert_paused_reader_recieving_contactModified_having_states(
                CacheStates.READ_CONTENTPROVIDER_DONE,
                CacheStates.READ_CONTENTPROVIDER);
    }


    private void assert_paused_reader_recieving_contactModified_having_states(
            CacheStates haltedState, CacheStates stateAfterCallback)
    {
        TestableCachedAsyncPhonebookReader reader = getHaltedReader(haltedState);
        assertEquals(haltedState, reader.state());
        reader.contactModifiedCallback();
        assertEquals(stateAfterCallback, reader.state());
        reader.disableBreakpoint();
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
        waitForReaderToBeReady(reader);

    }
}
