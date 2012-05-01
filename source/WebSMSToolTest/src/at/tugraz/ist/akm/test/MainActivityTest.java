package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.test.trace.ThrowingLogable;
import at.tugraz.ist.akm.trace.Logable;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	    
	private Logable mLog = null;

	public MainActivityTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
		mLog = new ThrowingLogable(MainActivityTest.class.getSimpleName());
	}

	/**
	 * just show that the main activity starts without crashing
	 */
	public void testMainActivityStart() {
		assertTrue( null != getActivity());	
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		log(getName() + ".setUp()");
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
