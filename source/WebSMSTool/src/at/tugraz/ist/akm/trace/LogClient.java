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

public class LogClient
{
    private String mTag = "<notag>";


    public LogClient(String tag)
    {
        mTag = extractClassName(tag);
    }


    public LogClient(Object o)
    {
        mTag = extractClassName(o.getClass().getName());
    }


    public LogClient(String tag, boolean enableDetailedTag)
    {
        if (enableDetailedTag)
        {
            mTag = new String(tag);
        } else
        {
            mTag = extractClassName(tag);
        }
    }


    public LogClient(Object o, boolean enableDetailedTag)
    {
        if (enableDetailedTag)
        {
            mTag = o.getClass().getName();
        } else
        {
            mTag = extractClassName(o.getClass().getName());
        }
    }


    private String extractClassName(String classWithPackagePrefix)
    {
        int lastDotIndex = classWithPackagePrefix.lastIndexOf(".");
        if (lastDotIndex <= 0)
        {
            lastDotIndex = 0;
        } else
        {
            lastDotIndex++;
        }
        return classWithPackagePrefix.substring(lastDotIndex,
                classWithPackagePrefix.length());
    }


    public void error(String message)
    {
        error(message, null);
    }


    public void error(String message, Throwable t)
    {
        TraceService.log(TraceService.LogLevel.ERROR, mTag, message, t);
    }


    public void warning(String message)
    {
        warning(message, null);
    }


    public void warning(String message, Throwable t)
    {
        TraceService.log(TraceService.LogLevel.WARNING, mTag, message, t);
    }


    public void info(String message)
    {
        info(message, null);
    }


    public void info(String message, Throwable t)
    {
        TraceService.log(TraceService.LogLevel.INFO, mTag, message, t);
    }


    public void debug(String message)
    {
        debug(message, null);
    }


    public void debug(String message, Throwable t)
    {
        TraceService.log(TraceService.LogLevel.DEBUG, mTag, message, t);
    }


    public void verbose(String message)
    {
        verbose(message, null);
    }


    public void verbose(String message, Throwable t)
    {
        TraceService.log(TraceService.LogLevel.VERBOSE, mTag, message, t);
    }
}
