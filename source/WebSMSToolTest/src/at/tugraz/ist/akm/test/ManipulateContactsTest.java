package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import at.tugraz.ist.akm.MainActivity;

public class ManipulateContactsTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private ContentResolver mContentResolver = null;

	public ManipulateContactsTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log("setUp()");
		mContentResolver = super.getActivity().getContentResolver();
		assertNotNull(mContentResolver);
	}

	public void testGetContacts() {
		/*
		 * log("testGetContacts()"); Uri uri =
		 * Uri.parse("content://contacts/people"); Intent intent = new
		 * Intent(Intent.ACTION_EDIT, uri);
		 * super.getActivity().startActivity(intent);
		 */

		Cursor contactRecord = mContentResolver.query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (contactRecord.moveToNext()) {
			printContactDetails(contactRecord);
			printContactPhones(contactRecord);
		}
	}

	private void printContactDetails(Cursor contact) {
		String cName = contact.getString(contact
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		log("Contact Name:  " + cName);
	}

	private void printContactPhones(Cursor contact) {

		int phoneColNr = contact
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int idColNr = contact.getColumnIndex(ContactsContract.Contacts._ID);

		String hasPhone = contact.getString(phoneColNr);
		log("has phone: " + hasPhone);

		String contactId = contact.getString(idColNr);

		if (!hasPhone.equals("0")) {
			// You know it has a number so now query it like this
			Cursor phones = mContentResolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			log("found " + phones.getCount() + " numbers");
			while (phones.moveToNext()) {
				String phoneNumber = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				log(phoneNumber);
			}
			phones.close();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		log("tearDown()");
	}

	private void log(String message) {
		Log.d("contacts", message);
	}

}
