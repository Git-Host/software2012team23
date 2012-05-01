package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.akm.test.trace.ThrowingLogable;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolTestcase extends InstrumentationTestCase {

	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;

	public WebSMSToolTestcase(final String logTag) {
		mLog = new ThrowingLogable(logTag);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");

		mContentResolver = getInstrumentation().getContext()
				.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		log(getName() + ".tearDown()");
		super.tearDown();
	}

	protected void log(final String m) {
		mLog.v(m);
	}

}
