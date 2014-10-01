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
import at.tugraz.ist.akm.trace.AndroidLogSink;
import at.tugraz.ist.akm.trace.ILogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class ThrowingLogableTest extends TestCase
{

    protected LogClient mLog = new LogClient(this);


    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    public void testLogDebug()
    {
        try
        {
            mLog.debug("testLogDebug");
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }


    public void testLogDebug_nullException()
    {
        try
        {
            mLog.debug("testLogDebug_nullException", null);
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }

    public void testLogDebug_exception()
    {
        try
        {
            mLog.debug("testLogDebug_exception", new Throwable("test exception"));
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }
    
    public void testLogError()
    {
        try
        {
            mLog.error("testLogError: If you read this message don't panik - it's just a test!");
        } catch (Throwable throwable)
        {
            // ok
            ILogSink oldSink = TraceService.getSink();
            TraceService.setSink(new AndroidLogSink());
            mLog.error("this log is just for test coverage");
            TraceService.setSink(oldSink);
            return;
        }
        assertTrue(false);
    }


    public void testLogError_nullException()
    {
        try
        {
            mLog.error(
                    "testLogErrorE: If you read this message don't panik - it's just a test!",
                    null);
        } catch (Throwable throwable)
        {
            // ok
            ILogSink oldSink = TraceService.getSink();
            TraceService.setSink(new AndroidLogSink());
            mLog.error("this log is just for test coverage");
            TraceService.setSink(oldSink);
            return;
        }
        assertTrue(false);
    }
    
    
    public void testLogError_eception()
    {
        try
        {
            mLog.error(
                    "testLogErrorE: If you read this message don't panik - it's just a test!",
                    new Throwable("test exception"));
        } catch (Throwable throwable)
        {
            // ok
            ILogSink oldSink = TraceService.getSink();
            TraceService.setSink(new AndroidLogSink());
            mLog.error("this log is just for test coverage");
            TraceService.setSink(oldSink);
            return;
        }
        assertTrue(false);
    }


    public void testLogInfo()
    {
        try
        {
            mLog.info("testLogInfo");
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }


    public void testLogInfo_nullException()
    {
        try
        {
            mLog.info("testLogInfo_nullException", null);
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }
    public void testLogInfo_exception()
    {
        try
        {
            mLog.info("testLogInfo_exception", new Throwable("test exception"));
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }


    public void testLogWarn()
    {
        try
        {
            mLog.warning("testLogWarn");
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }


    public void testLogWarn_nullException()
    {
        try
        {
            mLog.warning("testLogWarn_nullException", null);
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }
    
    public void testLogWarn_exception()
    {
        try
        {
            mLog.warning("testLogWarn_exception", new Throwable("test exception"));
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }
}
