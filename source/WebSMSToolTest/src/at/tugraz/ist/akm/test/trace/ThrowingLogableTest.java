package at.tugraz.ist.akm.test.trace;

import junit.framework.TestCase;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;

public class ThrowingLogableTest extends TestCase {

	protected Logable mLog = new Logable(ThrowingLogSink.class.getSimpleName());
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		Logger.setSink(new ThrowingLogSink());
	}
	
	public void testLogVerbose() {
		try {
			mLog.logVerbose("testLogVerbose");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogVerboseE() {
		try {			
			mLog.logVerbose("testLogVerboseE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebug() {
		try {
			mLog.logDebug("testLogDebug");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebugE() {
		try {
			mLog.logDebug("testLogDebugE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogError() {
		try {
			mLog.logError("testLogError: If you read this message don't panik - it's just a test!");
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogErrorE() {
		try {
			mLog.logError("testLogErrorE: If you read this message don't panik - it's just a test!", null);
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogInfo() {
		try {
			mLog.logInfo("testLogInfo");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogInfoE() {
		try {
			mLog.logInfo("testLogInfoE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarn() {
		try {
			mLog.logWarning("testLogWarn");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarnE() {
		try {
			mLog.logWarning("testLogWarnE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
}
