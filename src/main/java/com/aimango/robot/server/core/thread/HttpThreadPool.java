package com.aimango.robot.server.core.thread;

import java.util.concurrent.*;

public class HttpThreadPool {
    private static ExecutorService executorService;
    static {
        executorService=  new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),2*Runtime.getRuntime().availableProcessors(),0, TimeUnit.SECONDS,new LinkedBlockingQueue(),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static <T> Future<T> submit(Callable<T> callable){
        Future<T> future = executorService.submit(callable);
        return future;
    }
}
