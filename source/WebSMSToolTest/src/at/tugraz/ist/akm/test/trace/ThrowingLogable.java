package at.tugraz.ist.akm.test.trace;

import android.test.AssertionFailedError;
import at.tugraz.ist.akm.trace.Logable;

public class ThrowingLogable extends Logable {

	public ThrowingLogable(String tag) {
		super(tag);
	}

	@Override
	public void e(String message) {
		super.e(message);
		throw new AssertionFailedError();
	}

	@Override
	public void e(String message, Throwable t) {
		super.e(message, t);
		throw new AssertionFailedError();
	}

}
