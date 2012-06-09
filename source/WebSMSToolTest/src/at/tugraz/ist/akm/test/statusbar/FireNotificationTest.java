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

package at.tugraz.ist.akm.test.statusbar;

import at.tugraz.ist.akm.statusbar.FireNotification;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class FireNotificationTest extends WebSMSToolActivityTestcase {

	public FireNotificationTest() {
		super(FireNotificationTest.class.getSimpleName());
	}

	public void testFireNotificationNoExcepton() {
		try {
			FireNotification notify = new FireNotification(mContext);
			FireNotification.NotificationInfo infos = new FireNotification.NotificationInfo();
			infos.text = "http://192.168.1.100:8080";
			infos.title = "WebSMSTool";
			notify.fireStickyInfos(infos);
			Thread.sleep(100);
			notify.cancelAll();
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
}
