package com.aimango.robot.server.core.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class HttpThreadPool {
    private static final Logger logger= LoggerFactory.getLogger(HttpThreadPool.class);

    private static volatile ExecutorService executorService;

    private static void init(){
        if (executorService==null){
            synchronized (HttpThreadPool.class){
                if (executorService==null){
                    executorService=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors()*2,1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2048),
                            new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }
    }

    public static <T> Future<T> submit(Callable<T> callable){
        if (executorService==null){
            init();
        }
        Future<T> future=executorService.submit(callable);
        return future;
    }

    public static int activeCount(){
        if (executorService==null){
            init();
        }
        ThreadPoolExecutor threadPoolExecutor=(ThreadPoolExecutor)executorService;
        int activeCount = threadPoolExecutor.getActiveCount();
        return activeCount;
    }

    public static int queueSize(){
        if (executorService==null){
            init();
        }
        ThreadPoolExecutor threadPoolExecutor=(ThreadPoolExecutor)executorService;
        int size = threadPoolExecutor.getQueue().size();
        return size;
    }

    public static long completedTaskCount(){
        if (executorService==null){
            init();
        }
        ThreadPoolExecutor threadPoolExecutor=(ThreadPoolExecutor)executorService;
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        return completedTaskCount;
    }

    public static long taskCount(){
        if (executorService==null){
            init();
        }
        ThreadPoolExecutor threadPoolExecutor=(ThreadPoolExecutor)executorService;
        long taskCount = threadPoolExecutor.getTaskCount();
        return taskCount;
    }
}
