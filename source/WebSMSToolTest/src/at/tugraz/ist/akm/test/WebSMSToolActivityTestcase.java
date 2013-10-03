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

package at.tugraz.ist.akm.test;

import android.content.ContentResolver;
import android.content.Context;
import android.test.ActivityTestCase;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class WebSMSToolActivityTestcase extends ActivityTestCase {

	protected Context mContext = null;
	protected ContentResolver mContentResolver = null;
	private LogClient mLog = null;

	public WebSMSToolActivityTestcase(String foo) {
		TraceService.setSink(new ThrowingLogSink());
		mLog = new LogClient(this);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logVerbose(getName() + ".setUp()");
		mContext = getInstrumentation().getTargetContext();
		assertTrue(mContext != null);
		mContentResolver = mContext.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		logVerbose(getName() + ".tearDown()");
		super.tearDown();
	}
	
	protected void logDebug(final String message, Throwable throwable) {
		mLog.logDebug(message, throwable);
	}
	
	protected void logDebug(final String message) {
		mLog.logDebug(message);
	}
	
	protected void logError(final String message, Throwable throwable) {
		mLog.logError(message, throwable);
	}
	
	protected void logError(final String message) {
		mLog.logError(message);
	}
	
	protected void logInfo(final String message, Throwable throwable) {
		mLog.logInfo(message, throwable);
	}
	
	protected void logInfo(final String message) {
		mLog.logInfo(message);
	}
	
	protected void logVerbose(final String message, Throwable throwable) {
		mLog.logVerbose(message, throwable);
	}
	
	protected void logVerbose(final String message) {
		mLog.logVerbose(message);
	}
	
	protected void logWarning(final String message, Throwable throwable) {
		mLog.logWarning(message, throwable);
	}
	
	protected void logWarning(final String message) {
		mLog.logWarning(message);
	}
}
