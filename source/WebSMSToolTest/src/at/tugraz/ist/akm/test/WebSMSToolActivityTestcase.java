package at.tugraz.ist.akm.test;

import android.app.Activity;
import android.content.ContentResolver;
import android.test.ActivityTestCase;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.trace.Logable;

public class WebSMSToolActivityTestcase extends
ActivityTestCase {
	
	protected Activity mActivity = null;
	protected ContentResolver mContentResolver = null;
	private Logable mLog = null;
	
	public WebSMSToolActivityTestcase(final String logTag) {
		mLog = new Logable(logTag);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
		
		mActivity = new MainActivity();
		super.setActivity(mActivity);
		mContentResolver = getInstrumentation().getContext().getContentResolver();
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
