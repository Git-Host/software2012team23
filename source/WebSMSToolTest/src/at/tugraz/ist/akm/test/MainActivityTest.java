package at.tugraz.ist.akm.test;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
		WebSMSToolTestInstrumentation {

	private Solo mSolo;

	public MainActivityTest() {
		super(MainActivityTest.class.getSimpleName());
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
}
