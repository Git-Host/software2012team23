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

package at.tugraz.ist.akm.test.webservice.requestprocessor.interceptor;

import junit.framework.TestCase;
import android.util.SparseArray;
import android.util.SparseIntArray;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class HttpClientBacklogTest extends TestCase
{

    LogClient mLog = new LogClient(
            HttpClientBacklogTest.class.getCanonicalName());


    public HttpClientBacklogTest()
    {
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    public void test_build_backlog_no_exception()
    {
        TestableHttpClientBackLog backlog = new TestableHttpClientBackLog();
        long backlogCount = 100;

        for (long i = 0; i < backlogCount; i++)
        {
            backlog.memorizeClient(Long.toString(i).hashCode());
        }
    }


    public void test_expired_and_max_capacity()
    {
        TestableHttpClientBackLog backlog = new TestableHttpClientBackLog();
        long backlogCount = 10;

        SparseIntArray clientReference = new SparseIntArray();
        SparseArray<Long> clientExpirationLimit = new SparseArray<Long>();

        for (int i = 0; i < backlogCount; i++)
        {
            int client = Integer.toString(i).hashCode();
            backlog.memorizeClientBeforeExpiration(client);
            clientReference.put(i, Integer.valueOf(client));
            clientExpirationLimit.put(i, Long.valueOf(backlog.expiresAt()));
        }

        for (int idx = 0; idx < clientReference.size(); idx++)
        {
            assertTrue(
                    "assert failed at client [" + idx + "] expiration [~"
                            + clientExpirationLimit.get(idx) + "] now is ["
                            + System.currentTimeMillis() + "]",
                    backlog.isAuthExpired(clientReference.get(idx)));
        }
        assertEquals(0, backlog.size());

    }


    public void test_not_expired_and_max_capacity_reached()
    {
        TestableHttpClientBackLog backlog = new TestableHttpClientBackLog();
        long backlogCount = 11;

        SparseIntArray clientReference = new SparseIntArray();

        for (int i = 0; i < backlogCount; i++)
        {
            int client = Integer.toString(i).hashCode();
            backlog.memorizeClient(client);
            clientReference.put(i, Integer.valueOf(client));
        }

        for (int idx = 0; idx < clientReference.size(); idx++)
        {
            assertFalse("assert failed at client [" + idx + "] now is ["
                    + System.currentTimeMillis() + "]",
                    backlog.isAuthExpired(clientReference.get(idx)));
        }
        assertEquals((int) backlogCount, backlog.size());

    }


    public void test_remove_client()
    {
        TestableHttpClientBackLog backlog = new TestableHttpClientBackLog();
        long backlogCount = 5;

        SparseIntArray clientReference = new SparseIntArray();

        for (int i = 0; i < backlogCount; i++)
        {
            int client = (Integer.toString(i)+ "xxx").hashCode();
            backlog.memorizeClient(client);
            clientReference.put(i, Integer.valueOf(client));
        }

        for (int idx = 0; idx < clientReference.size(); idx++)
        {
            assertFalse("assert failed at client [" + idx + "] now is ["
                    + System.currentTimeMillis() + "]",
                    backlog.isAuthExpired(clientReference.get(idx)));
        }
        assertEquals((int) backlogCount, backlog.size());

        assertTrue(backlog.forgetClient(clientReference.get(0)));
        assertEquals(backlogCount - 1, backlog.size());
        assertFalse(backlog.forgetClient(999));
        assertFalse(backlog.forgetClient(999));
    }
}
