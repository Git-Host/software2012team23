package at.tugraz.ist.akm.sms;

import at.tugraz.ist.akm.trace.Logable;

public class SmsWrite {

	private Logable mLog = new Logable(getClass().getSimpleName());

	public boolean writeOutboxTextMessage(TextMessage message) {
		
		return false;
	}
	
	private void log(final String m) {
		mLog.log(m);
	}
}
