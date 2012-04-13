package at.tugraz.ist.akm.test;

import java.util.List;

import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactsRead;
import at.tugraz.ist.akm.trace.Logable;

public class ManipulateContactsTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private ContentResolver mContentResolver = null;
	private String[][] mTestContacts = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public ManipulateContactsTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
		mTestContacts = new String[][] { { "First", "Last", "123" },
				{ "Senthon", "L", "12312323" }, { "Therock", "G", "22222" },
				{ "Speedy", "R", "333333" } };
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
		mContentResolver = super.getActivity().getContentResolver();
		assertNotNull(mContentResolver);

		// removeContacts();

		// for (String[] record : mTestContacts) {
		// log("insert contact " + record[0] + "-" + record[1] + "-"
		// + record[2]);
		// insertContact(record);
		// }

	}

	// private void insertContact(String[] record) throws RemoteException,
	// OperationApplicationException {
	// ArrayList<ContentProviderOperation> ops = new
	// ArrayList<ContentProviderOperation>();
	// int rawContactInsertIndex = ops.size();
	//
	// ops.add(ContentProviderOperation
	// .newInsert(ContactsContract.RawContacts.CONTENT_URI)
	// .withValue(RawContacts.ACCOUNT_TYPE, null)
	// .withValue(RawContacts.ACCOUNT_NAME, null).build());
	//
	// ops.add(ContentProviderOperation
	// .newInsert(ContactsContract.Data.CONTENT_URI)
	// .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
	// rawContactInsertIndex)
	// .withValue(
	// ContactsContract.Data.MIMETYPE,
	// ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	// .withValue(
	// ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
	// record[0] + " " + record[1])
	// .withValue(
	// ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
	// record[1])
	// .withValue(
	// ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
	// record[0]).build());
	//
	// ops.add(ContentProviderOperation
	// .newInsert(ContactsContract.Data.CONTENT_URI)
	// .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
	// rawContactInsertIndex)
	// .withValue(
	// ContactsContract.Data.MIMETYPE,
	// ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
	// .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
	// record[2]).build());
	//
	// ContentProviderResult[] res = mContentResolver.applyBatch(
	// ContactsContract.AUTHORITY, ops);
	//
	// assertNotNull(res);
	// }

	// private void removeContacts() throws OperationApplicationException {
	// for (String[] record : mTestContacts) {
	// log("remove contact " + record[0] + "-" + record[1] + "-"
	// + record[2]);
	// removeContact(record);
	// }
	// }

	// private void removeContact(String[] record)
	// throws OperationApplicationException {
	// String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
	// String[] params = new String[] { record[0] + " " + record[1] };
	//
	// ArrayList<ContentProviderOperation> ops = new
	// ArrayList<ContentProviderOperation>();
	// ops.add(ContentProviderOperation
	// .newDelete(ContactsContract.RawContacts.CONTENT_URI)
	// .withSelection(where, params).build());
	// try {
	// mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
	// } catch (RemoteException e) {
	//
	// }
	// }

	public void testFetchContacts() {
		ContactsRead contactReader = new ContactsRead(mContentResolver);
		List<Contact> contacts = contactReader.fetchContactsWithPhone();

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
					+ contact.getFamilyName() + " GName: " + contact.getName() + " PhotoUri: "
					+ contact.getPhotoUri() + " Numbers: " + numbers.toString());
			log(details.toString());
		}
	}

	//
	// private String getContactDisplayName(Cursor contact) {
	// String cName = contact.getString(contact
	// .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	// return (cName == null) ? "--null--" : cName;
	// }
	//
	// private String getContactPhones(Cursor contact) {
	// String phoneNumbers = new String();
	// int phoneColNr = contact
	// .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
	// int idColNr = contact.getColumnIndex(ContactsContract.Contacts._ID);
	//
	// String hasPhone = contact.getString(phoneColNr);
	//
	// String contactId = contact.getString(idColNr);
	//
	// if (!hasPhone.equals("0")) {
	// // You know it has a number so now query it like this
	// Cursor phones = mContentResolver.query(
	// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
	// ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
	// + contactId, null, null);
	// while (phones.moveToNext()) {
	// phoneNumbers += phones
	// .getString(phones
	// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
	// + " ";
	// }
	// phones.close();
	// }
	// return phoneNumbers;
	// }

	@Override
	protected void tearDown() throws Exception {
		log(getName() + ".tearDown()");
		// removeContacts();
		super.tearDown();
	}

	private void log(String message) {
		mLog.log(message);
	}

}
