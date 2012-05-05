package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;

public class WebSMSToolTestcase extends InstrumentationTestCase {

	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;

	public WebSMSToolTestcase(final String logTag) {
		Logger.setSink(new ThrowingLogSink());
		mLog = new Logable(logTag);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logV(getName() + ".setUp()");

		mContentResolver = getInstrumentation().getContext()
				.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		logV(getName() + ".tearDown()");
		super.tearDown();
	}

	protected void logD(final String m, Throwable t) {
		mLog.logD(m, t);
	}
	
	protected void logD(final String m) {
		mLog.logD(m);
	}
	
	protected void logE(final String m, Throwable t) {
		mLog.logE(m, t);
	}
	
	protected void logE(final String m) {
		mLog.logE(m);
	}
	
	protected void logI(final String m, Throwable t) {
		mLog.logI(m, t);
	}
	
	protected void logI(final String m) {
		mLog.logI(m);
	}
	
	protected void logV(final String m, Throwable t) {
		mLog.logV(m, t);
	}
	
	protected void logV(final String m) {
		mLog.logV(m);
	}

}
