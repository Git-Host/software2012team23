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

package at.tugraz.ist.akm.webservice.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestThreadPool
{
    private int mCorePoolSize = 3;
    private int mMaxPoolSize = 6;
    private int mThreadIdleKeepAliveSeconds = 10;
    private int mTaskQueueSize = 5;
    private ArrayBlockingQueue<Runnable> mTaskQueue = new ArrayBlockingQueue<Runnable>(
            mTaskQueueSize);
    private ThreadPoolExecutor mThreadPool = null;

    private int ThreadIdx = 0;


    public RequestThreadPool()
    {
        mThreadPool = new ThreadPoolExecutor(mCorePoolSize, mMaxPoolSize,
                mThreadIdleKeepAliveSeconds, TimeUnit.SECONDS, mTaskQueue,
                newNamedThreadFactory());
    }


    private ThreadFactory newNamedThreadFactory() {
        return new ThreadFactory(){
            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, "HTTPRequestWorkerThread[" + ThreadIdx++ + "]");
            }
        };
    }


    public void executeTask(Runnable runnable)
            throws RejectedExecutionException
    {
        mThreadPool.execute(runnable);
    }


    public void shutdown()
    {
        mThreadPool.shutdown();
    }
}
