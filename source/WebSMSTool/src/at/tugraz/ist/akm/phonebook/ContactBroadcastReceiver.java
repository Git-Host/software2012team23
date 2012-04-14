package at.tugraz.ist.akm.phonebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.tugraz.ist.akm.trace.Logable;

public class ContactBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_CONTACT_MODIFIED = android.content.Intent.ACTION_EDIT;
	public static final String ACTION_CONTACT_CREATED = android.content.Intent.ACTION_INSERT;
	private ContactChangedCallback mCallback = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public ContactBroadcastReceiver(ContactChangedCallback c) {
		mCallback = c;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		log("catched explicit intent");
		String action = intent.getAction();
		if (action.compareTo(ACTION_CONTACT_CREATED) == 0) {
			mCallback.contactCreatedCallback(context, intent);
		} else if (action.compareTo(ACTION_CONTACT_MODIFIED) == 0) {
			mCallback.contactModifiedCallback(context, intent);
		}
	}

	private void log(final String m) {
		mLog.log(m);
	}
}
