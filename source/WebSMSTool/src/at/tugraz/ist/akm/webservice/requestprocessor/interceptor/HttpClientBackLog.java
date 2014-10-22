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

package at.tugraz.ist.akm.webservice.requestprocessor.interceptor;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import my.org.apache.http.HttpInetConnection;
import my.org.apache.http.protocol.ExecutionContext;
import my.org.apache.http.protocol.HttpContext;
import android.annotation.SuppressLint;
import at.tugraz.ist.akm.trace.LogClient;

public class HttpClientBackLog
{
    private LogClient mLog = new LogClient(
            HttpClientBackLog.class.getCanonicalName());
    private DateFormat mTimeFormatter = DateFormat.getTimeInstance();

    protected int mMaxClientCapacity = 64;
    protected long mExpiresAfterMs = 60 * 1000;
    protected int mGCMaxClientsAtOnce = 5;
    protected int mGCMinClientCount = mMaxClientCapacity / 3;
    protected int mOnWriteAccessGCInvocation = (int) (0.12 * mMaxClientCapacity);

    private int mWriteAccessCount = 0;

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Long> mClientBacklog = new HashMap<Integer, Long>();


    public boolean isAuthExpired(HttpContext httpContext)
    {

        return isExpired(getClientHash(httpContext));
    }


    protected boolean isExpired(int clientHash)
    {
        Long clientExpirationTimestamp = mClientBacklog.get(clientHash);
        if (null == clientExpirationTimestamp)
        {
            return true;
        }
        return ((clientExpirationTimestamp.longValue()) < (System
                .currentTimeMillis()));
    }


    private int getClientHash(HttpContext httpContext)
    {
        HttpInetConnection connection = (HttpInetConnection) httpContext
                .getAttribute(ExecutionContext.HTTP_CONNECTION);
        InetAddress inetAddress = connection.getRemoteAddress();

        return inetAddress.getHostAddress().hashCode();
    }


    public void memorizeClient(HttpContext httpContext)
    {
        memorize(getClientHash(httpContext));

    }


    protected void memorize(int clientHash)
    {
        memorize(clientHash, System.currentTimeMillis() + mExpiresAfterMs);
    }


    protected void memorize(int clientHash, long timestamp)
    {
        long nowStamp = System.currentTimeMillis();
        String now = mTimeFormatter.format(nowStamp);
        String then = mTimeFormatter.format(timestamp);

        mWriteAccessCount++;
        if (null != mClientBacklog.put(clientHash, timestamp))
        {
            mLog.debug("client update at [" + now + "] expires at [" + then
                    + "] in [" + (timestamp - nowStamp) + "]ms");
        } else
        {
            mLog.debug("new client memorized at [" + now + "] expires at ["
                    + then + "] in [" + (timestamp - nowStamp) + "]ms");
            if (mClientBacklog.size() > mMaxClientCapacity)
            {
                mLog.debug("warning, container size [" + mClientBacklog.size()
                        + "] bigger than designated [" + mMaxClientCapacity
                        + "]");
            }
        }

        if (mWriteAccessCount >= mOnWriteAccessGCInvocation)
        {
            gc();
            mWriteAccessCount = 0;
        }
    }


    protected int gc()
    {
        String reason = null;
        boolean writeAccessReason = false, softLimitReason = false, maxCapacityReason = false;
        DecimalFormat decimalFormatter = new DecimalFormat("#.##");

        if (mClientBacklog.size() < mMaxClientCapacity)
        {
            reason = "GC_SOFT_LIMIT";
            softLimitReason = true;
        }
        if (mClientBacklog.size() < mGCMinClientCount)
        {
            reason = "GC_WRITE_ACCESS";
            softLimitReason = false;
            writeAccessReason = true;
        } else
        {
            reason = "GC_MAX_CAPACITY";
            maxCapacityReason = true;
        }

        double usage = (100.0 * mClientBacklog.size())
                / (1.0 * mMaxClientCapacity);

        mLog.debug(reason + ": backlog usage " + decimalFormatter.format(usage)
                + "% [" + mClientBacklog.size() + "] of [" + mMaxClientCapacity
                + "] soft limit [" + mGCMinClientCount + "] expiration MS ["
                + mExpiresAfterMs + "] invocation every ["
                + mOnWriteAccessGCInvocation + "] write access");

        int freed = 0;
        if (writeAccessReason)
        {
            return 0;
        } else if (softLimitReason)
        {
            freed = tryFreeAtLeast(mGCMaxClientsAtOnce);
        } else if (maxCapacityReason)
        {
            freed = sweepMap();
            if (0 == freed)
            {
                mLog.debug("failed to free memory; container grows...");
            }
        } else
        {
            mLog.error("serious error freeing client backlog");
            freed = 0;
        }

        usage = (100.0 * mClientBacklog.size()) / (1.0 * mMaxClientCapacity);

        mLog.debug(reason + ": freed [" + freed + "] usage "
                + decimalFormatter.format(usage) + "% ["
                + mClientBacklog.size() + "] of [" + mMaxClientCapacity
                + "] soft limit [" + mGCMinClientCount + "]");

        return freed;
    }


    private int tryFreeAtLeast(int targetCount)
    {
        int freed = 0;
        Iterator<Map.Entry<Integer, Long>> iter = mClientBacklog.entrySet()
                .iterator();

        while (iter.hasNext())
        {
            Map.Entry<Integer, Long> entry = iter.next();
            if (entry.getValue() < System.currentTimeMillis())
            {
                iter.remove();
                freed++;
                if (freed >= targetCount)
                {
                    break;
                }
            }
        }
        return freed;
    }


    private int sweepMap()
    {
        int freed = 0;
        Iterator<Map.Entry<Integer, Long>> iter = mClientBacklog.entrySet()
                .iterator();

        while (iter.hasNext())
        {
            Map.Entry<Integer, Long> entry = iter.next();
            if (entry.getValue() < System.currentTimeMillis())
            {
                iter.remove();
                freed++;
            }
        }
        return freed;
    }


    public int size()
    {
        return mClientBacklog.size();
    }

}
