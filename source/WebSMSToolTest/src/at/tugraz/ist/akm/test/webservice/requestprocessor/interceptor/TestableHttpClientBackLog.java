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
