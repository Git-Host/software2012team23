package at.tugraz.ist.akm.phonebook;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import at.tugraz.ist.akm.trace.Logable;

public class PhonebookBridge implements ContactChangedCallback {

	private Logable mLog = new Logable(getClass().getSimpleName());
	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private ContactReader mContactReader = null;
	private Cursor mContactContentCursor = null;
	private ContactContentObserver mContactContentObserver = null;

	private class ContactContentObserver extends ContentObserver {

		private ContactChangedCallback mCallback = null;

		public ContactContentObserver(ContactChangedCallback c) {
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
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mContactReader = new ContactReader(mContentResolver);
		mContactContentCursor = getContactCursor();
		registerContactChangedObserver();
	}

	public void close() {
		unregisterContactChangedObserver();
	}

	public void fetchContacts(ContactReader.ContactFilter filter) {
		mContactReader.fetchContacts(filter);
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

	@Override
	public void contactModifiedCallback() {
		log("Contact modified callback triggered. Unfortunately we don't know which contact is involved");
	}

	private void log(final String m) {
		mLog.log(m);
	}
}
