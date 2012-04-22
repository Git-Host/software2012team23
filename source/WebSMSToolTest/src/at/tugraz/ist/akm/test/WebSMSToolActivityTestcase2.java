package at.tugraz.ist.akm.test;

import android.app.Activity;
import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolActivityTestcase2 extends
ActivityInstrumentationTestCase2<MainActivityTest> {
	
	protected Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;
	
	public WebSMSToolActivityTestcase2(final String logTag) {
		 super(MainActivityTest.class);
		mLog = new Logable(logTag);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
		mActivity = getActivity();
		mContentResolver = mActivity.getContentResolver();
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
