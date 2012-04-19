package at.tugraz.ist.akm.sms;

import android.database.ContentObserver;

public class SmsReceivedContentObserver extends ContentObserver {

	private SmsReceivedCallback mCallback = null;

	public SmsReceivedContentObserver(SmsReceivedCallback c) {
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
			mCallback.smsReceivedCallback();
		}
	}
}
