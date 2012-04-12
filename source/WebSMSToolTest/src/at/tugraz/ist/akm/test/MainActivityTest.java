package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.trace.Logable;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private Solo mSolo;
	private Logable mLogger = null;

	public MainActivityTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
		mLogger = new Logable("this.getClass().getName()");
		mSolo = new Solo(getInstrumentation(), getActivity());
		getActivity().setContentView(R.layout.main);
	}

	protected void test() {
		 log("test(): running empty test");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		 log("setUp()");
	}

	@Override
	protected void tearDown() throws Exception {
		 log("tearDown()");
		mSolo.finishOpenedActivities();
	}

	private void log(String message) {
		mLogger.log(message);
	}

}
