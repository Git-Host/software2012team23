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
			mLog.verbose("testLogVerbose");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogVerboseE() {
		try {			
			mLog.verbose("testLogVerboseE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebug() {
		try {
			mLog.debug("testLogDebug");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogDebugE() {
		try {
			mLog.debug("testLogDebugE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogError() {
		try {
			mLog.error("testLogError: If you read this message don't panik - it's just a test!");
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogErrorE() {
		try {
			mLog.error("testLogErrorE: If you read this message don't panik - it's just a test!", null);
		} catch (Throwable throwable) {
			// ok
			return;
		}
		assertTrue(false);
	}
	
	public void testLogInfo() {
		try {
			mLog.info("testLogInfo");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogInfoE() {
		try {
			mLog.info("testLogInfoE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarn() {
		try {
			mLog.warning("testLogWarn");
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
	
	public void testLogWarnE() {
		try {
			mLog.warning("testLogWarnE", null);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}
}
