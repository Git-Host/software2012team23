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

package at.tugraz.ist.akm.test.trace;

import junit.framework.TestCase;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class ThrowingLogableTest extends TestCase {

	protected LogClient mLog = new LogClient(this);
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		TraceService.setSink(new ThrowingLogSink());
	}
	
	public void testLogVerbose() {
		try {
			mLog.logVerbose("testLogVerbose");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogVerboseE() {
		try {			
			mLog.logVerbose("testLogVerboseE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebug() {
		try {
			mLog.logDebug("testLogDebug");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebugE() {
		try {
			mLog.logDebug("testLogDebugE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogError() {
		try {
			mLog.logError("testLogError: If you read this message don't panik - it's just a test!");
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogErrorE() {
		try {
			mLog.logError("testLogErrorE: If you read this message don't panik - it's just a test!", null);
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogInfo() {
		try {
			mLog.logInfo("testLogInfo");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogInfoE() {
		try {
			mLog.logInfo("testLogInfoE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarn() {
		try {
			mLog.logWarning("testLogWarn");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarnE() {
		try {
			mLog.logWarning("testLogWarnE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
}
