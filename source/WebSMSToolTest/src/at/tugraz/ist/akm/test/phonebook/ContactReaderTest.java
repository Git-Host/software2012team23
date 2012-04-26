package at.tugraz.ist.akm.test.phonebook;

import java.util.List;

import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactReader;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class ContactReaderTest extends WebSMSToolActivityTestcase {

	private String[][] mTestContacts = null;

	public ContactReaderTest() {
		super(ContactReaderTest.class.getSimpleName());
		mTestContacts = new String[][] { { "First", "Last", "123" },
				{ "Senthon", "L", "12312323" }, { "Therock", "G", "0" },
				{ "Speedy", "R", "0" }, { "", "Baz", "0" }, { "Bar", "", "0" } };
	}

	public void testFetchContacts() {
		try {
			PhonebookHelper.storeContacts(mTestContacts, mContentResolver);
			ContactReader contactReader = new ContactReader(mContentResolver);

			log("get contacts with phone");
			ContactFilter filterWithPhone = new ContactFilter();
			filterWithPhone.setWithPhone(true);
			List<Contact> contacts = contactReader
					.fetchContacts(filterWithPhone);
			PhonebookHelper.logContacts(contacts);

			log("get contacts with phone AND starred");
			ContactFilter filterWithPhoneAndStarred = new ContactFilter();
			filterWithPhoneAndStarred.setWithPhone(true);
			filterWithPhoneAndStarred.setIsStarred(true);
			contacts = contactReader.fetchContacts(filterWithPhoneAndStarred);
			PhonebookHelper.logContacts(contacts);

			log("get starred contacts");
			ContactFilter filterStarred = new ContactFilter();
			filterStarred.setIsStarred(true);
			contacts = contactReader.fetchContacts(filterStarred);
			PhonebookHelper.logContacts(contacts);

			log("get contacts unfiltered");
			ContactFilter noFilter = new ContactFilter();
			contacts = contactReader.fetchContacts(noFilter);
			PhonebookHelper.logContacts(contacts);

			PhonebookHelper.deleteContacts(mTestContacts, mContentResolver);
		} catch (Throwable e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}
