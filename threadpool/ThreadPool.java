package com.bobsystem.exercise.commons.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);

    //region CONSTANT
    // 普通任务池，用于获取消费者信息
    private static final
        ExecutorService THREAD_POOL = Executors.newFixedThreadPool(5);

    // 定时任务池，用于获取所有设备在线信息
    private static final
        ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(10);
    //endregion

    //region static volatile fields
    public static volatile boolean threadPoolIsInterrupted = false;
    public static volatile boolean scheduledPoolIsInterrupted = false;
    //endregion

    //region static methods
    //region schedule()
    //region 单次执行前延迟固定时间
    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return SCHEDULED_POOL.schedule(command, delay, unit);
    }

    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
                                                  TimeUnit unit) {
        return SCHEDULED_POOL.schedule(callable, delay, unit);
    }
    //endregion

    //region 周期执行
    /*
     * 单次任务结束时间如果超出执行周期，紧接着执行下一次任务
     * @param period 周期
     */
    public static ScheduledFuture<?> schedule(Runnable command, long initialDelay,
                                              long period, TimeUnit unit) {
        return SCHEDULED_POOL.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /*
     * 单次任务结束后，延时固定的时间执行下一次任务
     */
    public static ScheduledFuture<?> delay(Runnable command, long initialDelay,
                                           long delay, TimeUnit unit) {
        return SCHEDULED_POOL.scheduleAtFixedRate(command, initialDelay, delay, unit);
    }
    //endregion
    //endregion

    //region submit() 单次立即执行
    public static Future<?> submit(Runnable task) {
        return THREAD_POOL.submit(task);
    }

    public static <T> Future<T> submit(Runnable task, T result) {
        return THREAD_POOL.submit(task, result);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return THREAD_POOL.submit(task);
    }
    //endregion

    //region shtutdown
    public static void shutdown() {
        shutdownScheduledPool_();
        shutdownThreadPool_();
        while (true) {
            if (THREAD_POOL.isTerminated()) break;
            ThreadKit.sleep(300);
        }
        while (true) {
            if (SCHEDULED_POOL.isTerminated()) break;
            ThreadKit.sleep(300);
        }
    }

    private static void shutdownScheduledPool_() {
        // shutdown 并设置 interrupted 状态尝试关闭
        scheduledPoolIsInterrupted = true;
        SCHEDULED_POOL.shutdownNow();
    }

    private static void shutdownThreadPool_() {
        threadPoolIsInterrupted = true;
        THREAD_POOL.shutdownNow();
    }
    //endregion

    //region private methods
    //endregion
    //endregion
}
