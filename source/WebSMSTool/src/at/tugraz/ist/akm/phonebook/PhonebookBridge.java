package at.tugraz.ist.akm.phonebook;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.trace.Logable;

public class PhonebookBridge implements ContactModifiedCallback {

	private Context mContext = null;
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

	public PhonebookBridge(Context c) {
		mContext = c;
		mContentResolver = mContext.getContentResolver();
		mContactReader = new ContactReader(mContentResolver);
		mContactContentCursor = getContactCursor();
	}
	
	public void start() {
		registerContactChangedObserver();
	}

	public void stop() {
		unregisterContactChangedObserver();
		mContactContentCursor.close();
	}

	public List<Contact> fetchContacts(ContactFilter filter) {
		return mContactReader.fetchContacts(filter);
	}

	private Cursor getContactCursor() {
		Uri select = ContactsContract.Contacts.CONTENT_URI;
		String[] as = { ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts._ID };
		String where = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ? ";
		String[] like = { "1" };

		return mContentResolver.query(select, as, where, like, null);
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
		mLog.log(m);
	}
}
