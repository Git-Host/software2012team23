package at.tugraz.ist.akm.trace;

import android.util.Log;

public class Logable {

	protected String mTag = null;

	public Logable(String tag) {
		mTag = new String(tag);
	}

	public Logable() {
		mTag = new String("<notag>");
	}

	public void log(String message) {
		Log.d(mTag, message);
	}
}
