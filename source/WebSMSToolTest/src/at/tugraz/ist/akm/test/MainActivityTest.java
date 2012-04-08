package at.tugraz.ist.akm.test;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.MainActivity;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;

	public MainActivityTest() {
		super("at.tugraz.ist.akm", MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void test() {
		System.out.println("test!");

	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}
