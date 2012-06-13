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

public class Logable
{
    private static boolean mEnabled = true;
	private String mTag = "<notag>";

	public Logable()
	{
	}

	public Logable(String tag)
	{
		mTag = new String(tag);
	}

	public void logVerbose(String message)
	{
	    if (mEnabled)
	        logVerbose(message, null);
	}

	public void logVerbose(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.VERBOSE, mTag, message, t);
	}

	public void logError(String message)
	{
	    if (mEnabled)
	        logError(message, null);
	}

	public void logError(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.ERROR, mTag, message, t);
	}

	public void logWarning(String message)
	{
	    if (mEnabled)
	        logWarning(message, null);
	}

	public void logWarning(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.WARNING, mTag, message, t);
	}

	public void logInfo(String message)
	{
	    if (mEnabled)
	        logInfo(message, null);
	}

	public void logInfo(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.INFO, mTag, message, t);
	}

	public void logDebug(String message)
	{
	    if (mEnabled)
	        logDebug(message, null);
	}

	public void logDebug(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.DEBUG, mTag, message, t);
	}
}
