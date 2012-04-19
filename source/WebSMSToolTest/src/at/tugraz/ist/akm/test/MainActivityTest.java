package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.trace.Logable;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	private Solo mSolo;
	private Logable mLog = new Logable(getClass().getSimpleName());

	public MainActivityTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
		mSolo = new Solo(getInstrumentation(), getActivity());
		getActivity().setContentView(R.layout.main);
	}

	protected void test() {
		log("running empty test");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		log("tearDown()");
		mSolo.finishOpenedActivities();
	}

	private void log(final String m) {
		mLog.log(m);
	}
}
