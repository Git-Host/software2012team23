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

package at.tugraz.ist.akm.test.phonebook;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.trace.LogClient;

public class PhonebookHelper {

    private static LogClient mLog = new LogClient(PhonebookHelper.class);
    
	public static void storeContact(String[] record,
			ContentResolver contentResolver) throws Throwable {
		
		// for a better solution see also:
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

		ContentProviderResult[] res = contentResolver.applyBatch(
				ContactsContract.AUTHORITY, ops);

		assert (res != null);
	}

	public static void storeContacts(String [][] contacts, ContentResolver contentResolver) throws Throwable {
		for (String[] c : contacts) {
			PhonebookHelper.storeContact(c, contentResolver);
		}
	}
	
	public static void deleteContact(String phoneNumber, String displayName,
			ContentResolver contentResolver) {
		Uri select = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		String[] as = { PhoneLookup.DISPLAY_NAME,
				ContactsContract.Contacts.LOOKUP_KEY };
		String where = PhoneLookup.DISPLAY_NAME + " = '?' ";
		String[] like = { displayName };
		Cursor contact = contentResolver.query(select, as, where, like, null);

		if (contact != null) {
			while (contact.moveToNext()) {
				String lookupKey = contact.getString(contact
						.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
				Uri uri = Uri
						.withAppendedPath(
								ContactsContract.Contacts.CONTENT_LOOKUP_URI,
								lookupKey);
				contentResolver.delete(uri, null, null);
				return;
			}
		}
		assert (false);
	}

	public static void deleteContacts(String [][]contacts, ContentResolver contentResolver) {
		for (String[] c : contacts) {
			deleteContact(c[2], c[0] + " " + c[1], contentResolver);
		}
	}
	
	public static void logContacts(List<Contact> contacts) {
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
			mLog.logVerbose(details.toString());
		}

	}
}
