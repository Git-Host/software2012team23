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

package at.tugraz.ist.akm.webservice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebserviceThreadPool {
    private ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(5);

    private final ThreadPoolExecutor threadPool;

    public WebserviceThreadPool() {
        threadPool = new ThreadPoolExecutor(3, 6, 10, TimeUnit.SECONDS, taskQueue);
    }

    public void executeTask(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}
