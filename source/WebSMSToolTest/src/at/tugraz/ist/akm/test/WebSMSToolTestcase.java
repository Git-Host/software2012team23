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
		logVerbose(getName() + ".setUp()");

		mContentResolver = getInstrumentation().getContext()
				.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		logVerbose(getName() + ".tearDown()");
		super.tearDown();
	}

	protected void logDebug(final String message, Throwable throwable) {
		mLog.logDebug(message, throwable);
	}
	
	protected void logDebug(final String message) {
		mLog.logDebug(message);
	}
	
	protected void logError(final String message, Throwable throwable) {
		mLog.logError(message, throwable);
	}
	
	protected void logError(final String message) {
		mLog.logError(message);
	}
	
	protected void logInfo(final String message, Throwable throwable) {
		mLog.logInfo(message, throwable);
	}
	
	protected void logInfo(final String message) {
		mLog.logInfo(message);
	}
	
	protected void logVerbose(final String message, Throwable throwable) {
		mLog.logVerbose(message, throwable);
	}
	
	protected void logVerbose(final String message) {
		mLog.logVerbose(message);
	}

}
