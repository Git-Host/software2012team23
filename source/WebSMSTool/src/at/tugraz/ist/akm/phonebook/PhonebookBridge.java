package at.tugraz.ist.akm.phonebook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import at.tugraz.ist.akm.trace.Logable;

public class PhonebookBridge implements ContactChangedCallback {

	private Logable mLog = new Logable(getClass().getSimpleName());
	private Activity mActivity = null;
	private ContentResolver mContentResolver = null;
	private ContactBroadcastReceiver mContactBroadcastReceiver = null;
	private ContactReader mContactReader = null;

	public PhonebookBridge(Activity a) {
		mActivity = a;
		mContentResolver = mActivity.getContentResolver();
		mContactReader = new ContactReader(mContentResolver);
		mContactBroadcastReceiver = new ContactBroadcastReceiver(this);
		registerContactChangedListener();
	}

	public void close() {
		unregisterContactChangedListener();
	}

	public void fetchContacts() {
		mContactReader.fetchContactsWithPhone();
	}

	private void registerContactChangedListener() {
		mActivity.registerReceiver(mContactBroadcastReceiver, new IntentFilter(
				ContactBroadcastReceiver.ACTION_CONTACT_CREATED));
		mActivity.registerReceiver(mContactBroadcastReceiver, new IntentFilter(
				ContactBroadcastReceiver.ACTION_CONTACT_MODIFIED));
	}

	private void unregisterContactChangedListener() {
		mActivity.unregisterReceiver(mContactBroadcastReceiver);
	}

	@Override
	public void contactModifiedCallback(Context context, Intent intent) {
		log("contact modified callback triggered");
	}

	@Override
	public void contactCreatedCallback(Context context, Intent intent) {
		log("contact created callback triggered");
	}

	private void log(final String m) {
		mLog.log(m);
	}
}
