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

package at.tugraz.ist.akm.trace;

public class Logger {

    private static boolean mEnabled = TraceSettings.ENABLE_TRACE;
	private static Logger mLogger = null;
	private LogSink mSink = new AndroidLogSink();

	public enum LogLevel {
		ERROR, WARNING, INFO, DEBUG, VERBOSE
	}
	
	public static void log(final LogLevel level, final String tag, final String message, final Throwable t) {
		if (mEnabled) {
		    getInstance().distributeLog(level, tag, message, t);
		}
	}

	public static void setSink(LogSink newSink) {
		getInstance().mSink = newSink;
	}
	
	private static Logger getInstance() {
		if (mLogger == null ) {
			mLogger = new Logger();
		}
		return mLogger;
	}
	
	private void distributeLog(final LogLevel level, final String tag,
			final String message, final Throwable t) {
		if (mEnabled) {
    	    switch (level) {
    		case ERROR:
    			if (TraceSettings.ENABLE_TRACE_ERROR)
    				mSink.error(tag, formatMessage(message, t));
    			break;
    		case WARNING:
    			if (TraceSettings.ENABLE_TRACE_WARNING)
    				mSink.warning(tag, formatMessage(message, t));
    			break;
    	
    		case INFO:
    			if (TraceSettings.ENABLE_TRACE_INFO)
    				mSink.info(tag, formatMessage(message, t));
    			break;
    	
    		case DEBUG:
    			if (TraceSettings.ENABLE_TRACE_DEBUG)
    				mSink.debug(tag, formatMessage(message, t));
    			break;
    	
    		case VERBOSE:
    			if (TraceSettings.ENABLE_TRACE_VERBOSE)
    				mSink.verbose(tag, formatMessage(message, t));
    			break;
    		}
		}
	}

	private String formatMessage(final String message, final Throwable t) {
		StringBuilder sb = new StringBuilder(message);
		if (t != null) {
			sb.append(" => ").append("exception <").append(t.getMessage())
					.append(">");
		}
		return sb.toString();
	}

}
