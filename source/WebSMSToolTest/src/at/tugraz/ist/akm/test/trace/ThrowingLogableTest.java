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
			mLog.logV("testLogVerbose");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogVerboseE() {
		try {			
			mLog.logV("testLogVerboseE", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogDebug() {
		try {
			mLog.logD("testLogDebug");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogDebugE() {
		try {
			mLog.logD("testLogDebugE", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogError() {
		try {
			mLog.logE("testLogError");
		} catch (Throwable e) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogErrorE() {
		try {
			mLog.logE("testLogErrorE", null);
		} catch (Throwable e) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogInfo() {
		try {
			mLog.logI("testLogInfo");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogInfoE() {
		try {
			mLog.logI("testLogInfoE", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogWarn() {
		try {
			mLog.logW("testLogWarn");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogWarnE() {
		try {
			mLog.logW("testLogWarnE", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
}
