package at.tugraz.ist.akm.test.phonebook;

import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase2;

public class PhonebookBridgeTest extends WebSMSToolActivityTestcase2 {

	public PhonebookBridgeTest() {
		super(PhonebookBridgeTest.class.getSimpleName());
	}

	public void testPhonebookBridgeContactChangedCallback() throws Throwable {
		try {
			PhonebookBridge phonebook = new PhonebookBridge(mActivity);
			phonebook.start();
			String[] mrFoo = { "Foo", "Bar", "01906666" };
			Thread.sleep(1000);
			PhonebookHelper.storeContact(mrFoo, mContentResolver);
			Thread.sleep(1000);
			PhonebookHelper.deleteContact(mrFoo[2], mrFoo[0] + " " + mrFoo[1], mContentResolver);
			Thread.sleep(1000);
			phonebook.stop();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
