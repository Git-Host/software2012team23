package at.tugraz.ist.akm.test.phonebook;

import java.util.List;
import java.util.Vector;

import android.net.Uri;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.PhonebookCache;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.trace.LogClient;

public class PhoneBookCacheTest extends WebSMSToolActivityTestcase 
{
    private PhonebookCache mCache = null;
    static private long ContactCounter = 0;

    public PhoneBookCacheTest()
    {
        super(PhoneBookCacheTest .class.getName());
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mCache = new PhonebookCache(mContext);
        mCache.clear();
    }

    @Override
    protected void tearDown() throws Exception
    {
        mCache.onClose();
        mCache = null;
        super.tearDown();
    }
    
    public void test_cacheContact()
    {
        mCache.cache(newRandomContact());
    }
    
    public void test_clearCache()
    {
        mCache.clear();
        mCache.clear();
        mCache.cache(newRandomContact());
        mCache.cache(newRandomContact());
        mCache.clear();
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
        
        assert(mCache.numEntries() == 0);
        outContacts.add(newRandomContact());
        outContacts.add(newRandomContact());
        outContacts.add(newRandomContact());
        
        mCache.cache(outContacts.get(0));
        mCache.cache(outContacts.get(1));
        mCache.cache(outContacts.get(2));
        assert(mCache.numEntries() == outContacts.size());
        
        List<Contact> inContacts = mCache.getCached(null);
        assertTrue(inContacts.get(0).getDisplayName().equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0); outContacts.remove(0);
        assertTrue(inContacts.get(0).getDisplayName().equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0); outContacts.remove(0);
        assertTrue(inContacts.get(0).getDisplayName().equals(outContacts.get(0).getDisplayName()));
        inContacts.remove(0); outContacts.remove(0);
    
        mCache.clear();
        assert(mCache.numEntries() == 0);
    }
    
    
    private Contact newRandomContact() 
    {
        String mutable = Long.toString(ContactCounter);
        Contact contact = new Contact();
        contact.setDisplayName("displayName-" + mutable);
        contact.setFamilyName("familyName-" + mutable);
        contact.setId(ContactCounter);
        contact.setName("name-" + mutable);
        List<Contact.Number> numbers = new Vector<Contact.Number>();
        contact.setPhoneNumbers(numbers);
        contact.setPhotoBytes(("photo-" + mutable).getBytes());
        Uri uriGeller = android.net.Uri.parse("uri://heller/has/bent/his/spoon");
        contact.setPhotoUri(uriGeller);
        contact.setStarred(false);
        ContactCounter++;
        return contact;
    }
}
