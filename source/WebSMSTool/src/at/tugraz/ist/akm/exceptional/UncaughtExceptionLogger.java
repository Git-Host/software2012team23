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

package at.tugraz.ist.akm.exceptional;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;

import at.tugraz.ist.akm.trace.LogClient;

public class UncaughtExceptionLogger
{

    LogClient mLog = null;
    UncaughtExceptionHandler mDefaultHandler = null;


    @SuppressWarnings("unused")
    private UncaughtExceptionLogger()
    {
    }


    public UncaughtExceptionLogger(LogClient logger)
    {
        mLog = logger;
    }


    public void register()
    {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        try
        {

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e)
                {
                    printRunningThreads();
                    mLog.error(
                            "uncaught exception detected in thread["
                                    + t.getId() + "}", e);
                }
            });
        }
        catch (SecurityException e)
        {
            mLog.error("could not set default UncaughtExceptionHandler", e);
        }
    }


    public void unregister()
    {
        Thread.setDefaultUncaughtExceptionHandler(mDefaultHandler);
    }


    public void printRunningThreads()
    {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        for (Thread t : threads.keySet())
        {
            mLog.debug("*[" + t.getName() + "][" + t.getId() + "]["
                    + t.getThreadGroup() + "] ");
        }
    }
}
