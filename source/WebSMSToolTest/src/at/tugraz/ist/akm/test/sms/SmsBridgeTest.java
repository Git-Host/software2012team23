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

package at.tugraz.ist.akm.test.sms;

import java.util.List;

import android.test.AssertionFailedError;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.AndroidLogSink;
import at.tugraz.ist.akm.trace.LogSink;
import at.tugraz.ist.akm.trace.Logger;

public class SmsBridgeTest extends WebSMSToolActivityTestcase {

	public SmsBridgeTest() {
		super(SmsBridgeTest.class.getSimpleName());
	}

	public void testSmsBridgeSendSms() 
	{
		android.telephony.ServiceState voiceService = new android.telephony.ServiceState();
		LogSink oldSink = Logger.getSink();
		
		if ( android.telephony.ServiceState.STATE_IN_SERVICE != voiceService.getState() ) {
			Logger.setSink(new AndroidLogSink());
		}
		
		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			smsBridge.sendTextMessage(SmsHelper.getDummyTextMessage());
			Thread.sleep(1000);
			smsBridge.stop();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
		finally {
			Logger.setSink(oldSink);
		}
	}

	public void testSmsBridgeFetchInbox() {
		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.INBOX_URI);
			List<TextMessage> inMessages = smsBridge.fetchTextMessages(filter);
			for (TextMessage message : inMessages) {
				SmsHelper.logTextMessage(message);
			}
			smsBridge.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSmsBridgeFetchOutbox() {
		try {
			SmsBridge smsBridge = new SmsBridge(mContext);
			smsBridge.start();
			TextMessageFilter filter = new TextMessageFilter();
			filter.setBox(SmsContent.ContentUri.OUTBOX_URI);
			List<TextMessage> inMessages = smsBridge.fetchTextMessages(filter);
			for (TextMessage message : inMessages) {
				SmsHelper.logTextMessage(message);
			}
			smsBridge.stop();
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
