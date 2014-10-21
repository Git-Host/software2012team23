package at.tugraz.ist.akm.test.webservice.requestprocessor.interceptor;

import at.tugraz.ist.akm.webservice.requestprocessor.interceptor.HttpClientBackLog;

public class TestableHttpClientBackLog extends HttpClientBackLog
{

    TestableHttpClientBackLog()
    {
        mMaxClientCapacity = 10;
        mExpiresAfterMs = 10 * 1000;
        mGCMaxClientsAtOnce = 2;
        mGCMinClientCount = mMaxClientCapacity / 3;
        mOnWriteAccessGCInvocation = 10;
    }


    public boolean isAuthExpired(int client)
    {
        return super.isExpired(client);
    }


    public void memorizeClient(int client)
    {
        super.memorize(client);
    }


    public void memorizeClientBeforeExpiration(int client)
    {
        memorize(client, expiresAt() - 1);
    }


    public long expiresAt()
    {
        return System.currentTimeMillis() - mExpiresAfterMs;
    }

}
