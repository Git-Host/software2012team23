package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.content.Context;
import android.test.ActivityTestCase;
import at.tugraz.ist.akm.test.trace.ThrowingLogable;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolActivityTestcase extends ActivityTestCase {

	protected Context mContext = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;

	public WebSMSToolActivityTestcase(final String logTag) {
		mLog = new ThrowingLogable(logTag);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
		mContext = getInstrumentation().getTargetContext();
		mContentResolver = mContext.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		log(getName() + ".tearDown()");
		super.tearDown();
	}

	protected void log(final String m, Throwable t) {
		mLog.v(m, t);
	}
	
	protected void log(final String m) {
		mLog.v(m);
	}
	
	protected void logd(final String m, Throwable t) {
		mLog.d(m, t);
	}
	
	protected void logd(final String m) {
		mLog.d(m);
	}
	
	protected void loge(final String m, Throwable t) {
		mLog.e(m, t);
	}
	
	protected void loge(final String m) {
		mLog.e(m);
	}
	
	protected void logi(final String m, Throwable t) {
		mLog.i(m, t);
	}
	
	protected void logi(final String m) {
		mLog.i(m);
	}

}
