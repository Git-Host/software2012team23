package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityActionBarTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Logable mLog = null;
	
	public MainActivityActionBarTest()
	{
		super("at.tugraz.ist.akm", MainActivity.class);
		Logger.setSink(new ThrowingLogSink());
		mLog = new Logable(MainActivityActionBarTest.class.getSimpleName());
	}


	public void testSettingsActivity() throws Exception {
		Solo mSolo = new Solo(getInstrumentation(), getActivity());
		mSolo.assertCurrentActivity("Actual activty is MainActivity", MainActivity.class);

		/* no idea on how to access the action bar menu items to be clicked */
		
		//this should work but i am not sure if only with android 4.
		//getInstrumentation().invokeMenuActionSync(getActivity(), R.id.info, 0);
		
		//Thread.sleep(3000);
		//mSolo.assertCurrentActivity("Actual activty is AboutActivity", AboutActivity.class);
	}
	

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		log(getName() + ".setUp()");
	}

	@Override
	protected void tearDown() throws Exception
	{
		log(getName() + ".tearDown()");
		super.tearDown();
	}

	protected void log(final String m)
	{
		mLog.logVerbose(m);
	}

}
