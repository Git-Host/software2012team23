package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>
{

	private Logable mLog = null;

	public MainActivityTest()
	{
		super("at.tugraz.ist.akm", MainActivity.class);
		Logger.setSink(new ThrowingLogSink());
		mLog = new Logable(MainActivityTest.class.getSimpleName());
	}

	/**
	 * just show that the main activity starts without crashing
	 */
	public void testMainActivityStart() throws Exception
	{
		MainActivity activity = getActivity();
		assertTrue(null != activity);
	}
	
	
	
	/**
	 * Test start button (state after it should be checked) and click again to stop service
	 * (state should be not checked)
	 */
	public void testStartStopButton(){
		Solo solo = new Solo(getInstrumentation(), getActivity());
		ToggleButton startStop = (ToggleButton) getActivity().findViewById(R.id.start_stop_server);
		solo.clickOnView(startStop);		
		assertTrue(startStop.isChecked());
		solo.clickOnView(startStop);
		assertFalse(startStop.isChecked());
	}
	
	
	/**
	 * Check if ip address is only displayed if the service has been started
	 * @throws InterruptedException 
	 */
	public void testIpAdressOutputOnServiceStart() throws InterruptedException{
		Solo solo = new Solo(getInstrumentation(), getActivity());
		ToggleButton startStop = (ToggleButton) getActivity().findViewById(R.id.start_stop_server);
		TextView ipView = (TextView) getActivity().findViewById(R.id.ip_data_field);
		
		assertTrue(ipView.getText().length() == 0);
		solo.clickOnView(startStop);	
		Thread.sleep(1000);
		assertTrue(ipView.getText().length() > 0);
		solo.clickOnView(startStop);
		Thread.sleep(1000);
		assertTrue(ipView.getText().length() == 0);
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
