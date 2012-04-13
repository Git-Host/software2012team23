package at.tugraz.ist.akm.phonebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.trace.Logable;

public class ContactsRead {

	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public ContactsRead(ContentResolver c) {
		mContentResolver = c;
	}

	public List<Contact> fetchContactsWithPhone() {
		log("perfom fetching contacts with phone");

		List<Contact> contacts = new Vector<Contact>();
		Uri select = ContactsContract.Contacts.CONTENT_URI;
		String[] as = { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String where = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?";
		String[] like = { "1" };

		Cursor people = mContentResolver.query(select, as, where, like, null);

		if (people != null) {
			while (people.moveToNext()) {
				contacts.add(parseToContact(people));
			}
			people.close();
		}

		log("fetched [" + contacts.size() + "] contacts with phone number(s)");
		return contacts;
	}

	private Contact parseToContact(Cursor person) {
		Contact contact = new Contact();

		String contactId = person.getString(person
				.getColumnIndex(ContactsContract.Contacts._ID));
		String displayName = person.getString(person
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		boolean starred = Boolean.parseBoolean(person.getString(person
				.getColumnIndex(ContactsContract.Contacts.STARRED)));
		
		contact.setDisplayName(displayName);
		contact.setId(Integer.parseInt(contactId));
		contact.setStarred(starred);
		contact.setPhotoUri(getPhotoUri(contactId));
		collectPhoneNumberDetails(contact, contactId);
		collectStructuredNameDetails(contact, contactId);

		return contact;
	}

	private void collectPhoneNumberDetails(Contact contact, String contactId) {
		String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
				+ " = ?";
		String[] as = { ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.TYPE };
		String[] like = { contactId };
		Cursor phoneNumbers = mContentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, as, where,
				like, null);

		if (phoneNumbers != null) {
			List<Contact.Number> phoneNumberList = new ArrayList<Contact.Number>();
			while (phoneNumbers.moveToNext()) {
				phoneNumberList
						.add(new Contact.Number(
								phoneNumbers
										.getString(phoneNumbers
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),Integer.parseInt(
								phoneNumbers.getString(phoneNumbers
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)))));

			}
			contact.setPhoneNumbers(phoneNumberList);
			phoneNumbers.close();
		}
	}

	private void collectStructuredNameDetails(Contact contact, String contactId) {

		Uri selectFrom = ContactsContract.Data.CONTENT_URI;
		String[] as = {
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME };
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ? ";
		String[] like = new String[] {
				contactId,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

		Cursor structuredName = mContentResolver.query(selectFrom, as, where,
				like, null);

		if (structuredName != null) {
			if (structuredName.moveToNext()) {
				String givenName = structuredName
						.getString(structuredName
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
				String familyName = structuredName
						.getString(structuredName
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
				contact.setName(givenName);
				contact.setFamilyName(familyName);
			}
			structuredName.close();
		}

	}

	private Uri getPhotoUri(String contactId) {
		Uri select = ContactsContract.Data.CONTENT_URI;
		String[] as = { ContactsContract.Data.CONTACT_ID };
		String where = ContactsContract.Data.CONTACT_ID + "= ? " + " AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";

		String[] like = { contactId,
				ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE };
		Cursor cur = mContentResolver.query(select, as, where, like, null);

		Uri photoUri = null;
		if (cur != null) {
			if (cur.moveToFirst()) {
				Uri person = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI,
						Long.parseLong(contactId));
				return Uri.withAppendedPath(person,
						ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
			}
		}
		return photoUri;
	}

	private void log(final String m) {
		mLog.log(m);
	}
}
