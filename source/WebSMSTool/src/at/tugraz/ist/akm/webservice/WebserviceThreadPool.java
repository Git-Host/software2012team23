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
