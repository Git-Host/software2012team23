package at.tugraz.ist.akm.test.trace;

import android.test.AssertionFailedError;
import at.tugraz.ist.akm.trace.AndroidLogSink;

public class ThrowingLogSink extends AndroidLogSink {

	@Override
	public void error(final String tag, final String message) {
		super.error(tag, message);
		throw new AssertionFailedError();
	}

}
