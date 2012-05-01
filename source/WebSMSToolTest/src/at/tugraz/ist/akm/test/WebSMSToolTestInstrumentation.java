package at.tugraz.ist.akm.test;

import android.app.Activity;
import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.test.trace.ThrowingLogable;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolTestInstrumentation extends
ActivityInstrumentationTestCase2<MainActivity> {
	
	protected Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;
	
	public WebSMSToolTestInstrumentation(final String logTag) {
		super("at.tugraz.ist.akm", MainActivity.class);
		mLog = new ThrowingLogable(logTag);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
		mActivity = super.getActivity();
		mContentResolver = mActivity.getContentResolver();
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
