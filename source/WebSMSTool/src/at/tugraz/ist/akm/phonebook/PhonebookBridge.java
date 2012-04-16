package at.tugraz.ist.akm.phonebook;

import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.trace.Logable;

public class PhonebookBridge implements ContactModifiedCallback {

	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	private ContactReader mContactReader = null;

	private Cursor mContactContentCursor = null;
	private ContactContentObserver mContactContentObserver = null;
	private ContactModifiedCallback mExternalContactModifiedCallback = null;

	private class ContactContentObserver extends ContentObserver {

		private ContactModifiedCallback mCallback = null;

		public ContactContentObserver(ContactModifiedCallback c) {
			super(null);
			mCallback = c;
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (!selfChange) {
				mCallback.contactModifiedCallback();
			}
		}
	}

	public PhonebookBridge(Activity a) {
		log("starting ...");
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mContactReader = new ContactReader(mContentResolver);
		mContactContentCursor = getContactCursor();
		registerContactChangedObserver();
	}

	public void close() {
		unregisterContactChangedObserver();
	}

	public List<Contact> fetchContacts(ContactReader.ContactFilter filter) {
		List<Contact> contacts = mContactReader.fetchContacts(filter);
		log("fetched [" + contacts.size() + "] cntacts");
		return contacts;
	}

	private Cursor getContactCursor() {
		Uri select = ContactsContract.Contacts.CONTENT_URI;
		String[] as = { ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts._ID };
		String where = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ? ";
		String[] like = { "1" };

		return mActivity.managedQuery(select, as, where, like, null);
	}

	private void registerContactChangedObserver() {
		mContactContentObserver = new ContactContentObserver(this);
		mContactContentCursor.registerContentObserver(mContactContentObserver);
	}

	private void unregisterContactChangedObserver() {
		mContactContentCursor
				.unregisterContentObserver(mContactContentObserver);
		mContactContentObserver = null;
	}

	public void setContactModifiedCallback(ContactModifiedCallback c) {
		log("registered new [ContactModifiedCallback] callback");
		mExternalContactModifiedCallback = c;
	}

	@Override
	public void contactModifiedCallback() {
		if (mExternalContactModifiedCallback != null) {
			mExternalContactModifiedCallback.contactModifiedCallback();
		}
	}

	private void log(final String m) {
		mLog.v(m);
	}
}
