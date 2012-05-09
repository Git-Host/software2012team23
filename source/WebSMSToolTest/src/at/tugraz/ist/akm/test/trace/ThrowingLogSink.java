package at.tugraz.ist.akm.test.trace;

import android.test.AssertionFailedError;
import at.tugraz.ist.akm.trace.AndroidLogSink;

public class ThrowingLogSink extends AndroidLogSink {

	@Override
	public void e(final String tag, final String message) {
		super.e(tag, message);
		throw new AssertionFailedError();
	}

}
