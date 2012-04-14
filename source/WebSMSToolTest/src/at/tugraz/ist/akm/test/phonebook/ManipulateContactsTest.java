package at.tugraz.ist.akm.test.phonebook;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactReader;
import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class ManipulateContactsTest extends WebSMSToolTestInstrumentation {

	private String[][] mTestContacts = null;

	public ManipulateContactsTest() {
		super(ManipulateContactsTest.class.getSimpleName());
		mTestContacts = new String[][] { { "First", "Last", "123" },
				{ "Senthon", "L", "12312323" }, { "Therock", "G", "0" },
				{ "Speedy", "R", "0" }, { "", "Baz", "0" }, { "Bar", "", "0" } };
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = super.getActivity();
		mContentResolver = mActivity.getContentResolver();
		assertNotNull(mContentResolver);
	}

	private void storeTestContacts() throws Throwable {
		for (String[] c : mTestContacts) {
			storeContact(c);
		}
	}

	private void deleteTestContacts() {
		for (String[] c : mTestContacts) {
			deleteContact(c[2], c[0] + " " + c[1]);
		}
	}

	private void storeContact(String[] record) throws Throwable {
		// TODO: see also
		// http://saigeethamn.blogspot.com/2009/09/android-developer-tutorial-part-10.html
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = ops.size();

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, null)
				.withValue(RawContacts.ACCOUNT_NAME, null).build());

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						record[0] + " " + record[1])
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
						record[1])
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
						record[0]).build());

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
						record[2]).build());

		ContentProviderResult[] res = mContentResolver.applyBatch(
				ContactsContract.AUTHORITY, ops);

		assertNotNull(res);
	}

	public void deleteContact(String phoneNumber, String displayName) {
		Uri select = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		String[] as = { PhoneLookup.DISPLAY_NAME,
				ContactsContract.Contacts.LOOKUP_KEY };
		String where = PhoneLookup.DISPLAY_NAME + " = '?' ";
		String[] like = { displayName };
		Cursor contact = mContentResolver.query(select, as, where, like, null);

		if (contact != null) {
			while (contact.moveToNext()) {
				String lookupKey = contact.getString(contact
						.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
				Uri uri = Uri
						.withAppendedPath(
								ContactsContract.Contacts.CONTENT_LOOKUP_URI,
								lookupKey);
				mContentResolver.delete(uri, null, null);
				return;
			}
		}
		assertTrue(false);
	}

	public void testFetchContacts() {
		try {
			storeTestContacts();
			ContactReader contactReader = new ContactReader(mContentResolver);

			log("get contacts with phone");
			ContactReader.ContactFilter filterWithPhone = new ContactReader.ContactFilter();
			filterWithPhone.setWithPhone(true);
			List<Contact> contacts = contactReader
					.fetchContacts(filterWithPhone);
			logContacts(contacts);

			log("get contacts with phone AND starred");
			ContactReader.ContactFilter filterWithPhoneAndStarred = new ContactReader.ContactFilter();
			filterWithPhoneAndStarred.setWithPhone(true);
			filterWithPhoneAndStarred.setStarred(true);
			contacts = contactReader.fetchContacts(filterWithPhoneAndStarred);
			logContacts(contacts);

			log("get starred contacts");
			ContactReader.ContactFilter filterStarred = new ContactReader.ContactFilter();
			filterStarred.setStarred(true);
			contacts = contactReader.fetchContacts(filterStarred);
			logContacts(contacts);

			log("get contacts unfiltered");
			ContactReader.ContactFilter noFilter= new ContactReader.ContactFilter();
			contacts = contactReader.fetchContacts(noFilter);
			logContacts(contacts);
			
			deleteTestContacts();
		} catch (Throwable e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private void logContacts(List<Contact> contacts) {
		for (Contact contact : contacts) {
			StringBuffer details = new StringBuffer();
			StringBuffer numbers = new StringBuffer();

			if (contact.getPhoneNumbers() != null) {
				for (Contact.Number number : contact.getPhoneNumbers()) {
					numbers.append(number.getNumber() + ":" + number.getType()
							+ " ");
				}
			}
			details.append("DName: " + contact.getDisplayName() + " FName: "
					+ contact.getFamilyName() + " GName: " + contact.getName()
					+ " PhotoUri: " + contact.getPhotoUri() + " IsStarred: "
					+ contact.isStarred() + " Numbers: " + numbers.toString());
			log(details.toString());
		}

	}

	public void testPhonebookBridgeContactChangedCallback() throws Throwable {
		try {
			PhonebookBridge phonebook = new PhonebookBridge(mActivity);
			String[] mrFoo = { "Foo", "Bar", "01906666" };
			Thread.sleep(1000);
			storeContact(mrFoo);
			Thread.sleep(1000);
			deleteContact(mrFoo[2], mrFoo[0] + " " + mrFoo[1]);
			Thread.sleep(1000);
			phonebook.close();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
