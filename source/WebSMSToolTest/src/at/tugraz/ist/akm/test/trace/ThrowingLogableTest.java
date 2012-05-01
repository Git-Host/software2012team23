package at.tugraz.ist.akm.test.trace;

import junit.framework.TestCase;
import at.tugraz.ist.akm.trace.Logable;

public class ThrowingLogableTest extends TestCase {

	protected Logable mLog = new ThrowingLogable(ThrowingLogable.class.getSimpleName());
	
	public void testLogVerbose() {
		try {
			mLog.v("");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogVerboseE() {
		try {			
			mLog.v("", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogDebug() {
		try {
			mLog.d("");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogDebugE() {
		try {
			mLog.d("", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogError() {
		try {
			mLog.e("");
		} catch (Throwable e) {
			// expect to throw
			return;
		}
		assertTrue(false);
	}
	
	public void testLogErrorE() {
		try {
			mLog.e("", null);
		} catch (Throwable e) {
			// expect to throw
			return;
		}
		assertTrue(false);
	}
	
	public void testLogInfo() {
		try {
			mLog.i("");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogInfoE() {
		try {
			mLog.i("", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogWarn() {
		try {
			mLog.w("");
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testLogWarnE() {
		try {
			mLog.w("", null);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
}
