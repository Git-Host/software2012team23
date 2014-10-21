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
}
