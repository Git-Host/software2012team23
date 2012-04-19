package at.tugraz.ist.akm.test.phonebook;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase2;

public class PhonebookBridgeTest extends WebSMSToolActivityTestcase2 {

	public PhonebookBridgeTest() {
		super(PhonebookBridgeTest.class.getSimpleName());
	}

	public void testPhonebookBridgeContactChangedCallback() throws Throwable {
		try {
			PhonebookBridge phonebook = new PhonebookBridge(mActivity);
			phonebook.start();
			String[] mrFoo = { "Foo", "Bar", "01906666" };
			Thread.sleep(1000);
			storeContact(mrFoo);
			Thread.sleep(1000);
			deleteContact(mrFoo[2], mrFoo[0] + " " + mrFoo[1]);
			Thread.sleep(1000);
			phonebook.stop();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
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

	private void deleteContact(String phoneNumber, String displayName) {
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

}
