package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.content.Context;
import android.test.ActivityTestCase;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolActivityTestcase extends ActivityTestCase {

	protected Context mContext = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;

	public WebSMSToolActivityTestcase(final String logTag) {
		mLog = new Logable(logTag);
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

	protected void log(final String m) {
		mLog.log(m);
	}

}
