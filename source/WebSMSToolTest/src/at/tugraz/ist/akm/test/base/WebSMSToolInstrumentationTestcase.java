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

package at.tugraz.ist.akm.test.base;

import android.content.ContentResolver;
import android.content.Context;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class WebSMSToolInstrumentationTestcase extends InstrumentationTestCase {

    protected Context mContext = null;
    protected ContentResolver mContentResolver = null;
	private LogClient mLog = null;
	private String mLogTag = null;

	public WebSMSToolInstrumentationTestcase(String logTag) {
		TraceService.setSink(new ExceptionThrowingLogSink());
		mLogTag = new String(logTag);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mLog = new LogClient(mLogTag);
		logVerbose(getName() + ".setUp()");

		mContext = getInstrumentation().getTargetContext();
		mContentResolver = mContext.getContentResolver();
		assertTrue(mContentResolver != null);
	}

	@Override
	protected void tearDown() throws Exception {
		logVerbose(getName() + ".tearDown()");
	    mContext = null;
	    mContentResolver = null;
	    mLog = null;
		super.tearDown();
	}

	protected void logDebug(final String message, Throwable throwable) {
		mLog.debug(message, throwable);
	}
	
	protected void logDebug(final String message) {
		mLog.debug(message);
	}
	
	protected void logError(final String message, Throwable throwable) {
		mLog.error(message, throwable);
	}
	
	protected void logError(final String message) {
		mLog.error(message);
	}
	
	protected void logInfo(final String message, Throwable throwable) {
		mLog.info(message, throwable);
	}
	
	protected void logInfo(final String message) {
		mLog.info(message);
	}
	
	protected void logVerbose(final String message, Throwable throwable) {
		mLog.info(message, throwable);
	}
	
	protected void logVerbose(final String message) {
		mLog.info(message);
	}

}
