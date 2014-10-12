/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.test.phonebook;

import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.test.testdata.PhonebookTestsHelper;

public class PhonebookBridgeTest extends WebSMSToolActivityTestcase {

	public PhonebookBridgeTest() {
		super(PhonebookBridgeTest.class.getSimpleName());
	}

	public void testPhonebookBridgeContactChangedCallback() throws Throwable {
		try {
			PhonebookBridge phonebook = new PhonebookBridge(mContext);
			phonebook.start();
			String[] mrFoo = { "Foo", "Bar", "01906666" };
			Thread.sleep(1000);
			PhonebookTestsHelper.storeContact(mrFoo, mContentResolver);
			Thread.sleep(1000);
			PhonebookTestsHelper.deleteContact(mrFoo[2], mrFoo[0] + " " + mrFoo[1], mContentResolver);
			Thread.sleep(1000);
			phonebook.stop();
			phonebook.onClose();
		} catch (Exception ex) {
			ex.printStackTrace();
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
