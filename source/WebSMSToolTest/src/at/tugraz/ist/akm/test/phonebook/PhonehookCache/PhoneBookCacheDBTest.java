package at.tugraz.ist.akm.test.phonebook.PhonehookCache;

import java.util.List;
import java.util.Vector;

import android.net.Uri;
import at.tugraz.ist.akm.phonebook.PhonebookCache.PhonebookCacheDB;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;

public class PhoneBookCacheDBTest extends WebSMSToolActivityTestcase
{
    private PhonebookCacheDB mCache = null;
    static private long ContactCounter = 0;


    public PhoneBookCacheDBTest()
    {
        super(PhoneBookCacheDBTest.class.getName());
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mCache = new PhonebookCacheDB(mContext);
        mCache.clear();
    }


    @Override
    protected void tearDown() throws Exception
    {
        mCache.close();
        mCache = null;
        super.tearDown();
    }


    public void test_cacheContact()
    {
        try
        {
            mCache.cache(newRandomContact());
        }
        catch (Throwable e)
        {
            assertTrue(false);
        }
    }


    public void test_clearCache()
    {
        try
        {
            mCache.clear();
            mCache.clear();
            mCache.cache(newRandomContact());
            mCache.cache(newRandomContact());
            mCache.clear();
        }
        catch (Throwable e)
        {
            assertTrue(false);
        }
    }


    public void test_getNumEntries()
    {
        mCache.cache(newRandomContact());
        assertEquals(mCache.numEntries(), 1L);
        mCache.clear();
        assertEquals(mCache.numEntries(), 0L);
        assertTrue(true);
    }


    public void test_cacheAndReadCached()
    {
        List<Contact> outContacts = new Vector<Contact>();

        assertEquals(0, mCache.numEntries());
        outContacts.add(newRandomContact());
        outContacts.add(newRandomContact());
        outContacts.add(newRandomContact());

        mCache.cache(outContacts.get(0));
        mCache.cache(outContacts.get(1));
        mCache.cache(outContacts.get(2));
        assertTrue(mCache.numEntries() == outContacts.size());

        List<Contact> inContacts = mCache.getCached(null);
        assertTrue(inContacts.get(0).getDisplayName()
                .equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0);
        outContacts.remove(0);
        assertTrue(inContacts.get(0).getDisplayName()
                .equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0);
        outContacts.remove(0);
        assertTrue(inContacts.get(0).getDisplayName()
                .equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0);
        outContacts.remove(0);

        mCache.clear();
        assertTrue(mCache.numEntries() == 0);
        mCache.close();
    }


    private Contact newRandomContact()
    {
        String mutable = Long.toString(ContactCounter);
        Contact contact = new Contact();
        contact.setDisplayName("displayName-" + mutable);
        contact.setId(ContactCounter);
        List<Contact.Number> numbers = new Vector<Contact.Number>();
        contact.setPhoneNumbers(numbers);
        contact.setPhotoBytes(("photo-" + mutable).getBytes());
        Uri uriGeller = android.net.Uri
                .parse("uri://heller/has/bent/his/spoon");
        contact.setPhotoUri(uriGeller);
        contact.setStarred(false);
        ContactCounter++;
        return contact;
    }
}
