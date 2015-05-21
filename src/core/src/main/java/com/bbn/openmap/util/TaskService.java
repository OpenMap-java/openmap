/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Reuse threads instead of making new ones over and over.
 */
public final class TaskService {
    public static TaskService singleton() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final TaskService instance = new TaskService();
    }

    private ExecutorService executor;

    private TaskService() {
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Run a task in a thread.
     */
    public void spawn(Runnable task) {
        executor.execute(task);
    }

    public <T> Future<T> spawn(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Allows more control over how threads can be allocated.
     * 
     * @param eService ExecutorService that allocates and schedules thread
     *        spawning for layers.
     */
    public void setExecutorService(ExecutorService eService) {
        if (eService != null) {
            executor = eService;
        }
    }

    /**
     * If called, will replace the default unbounded executor using a cached
     * thread pool with a fixed thread pool executor, so the maxNumThreads are
     * allocated. If they are all busy, the Runnables will queue.
     * 
     * @param maxNumThreads
     */
    public void setMaxNumThreads(int maxNumThreads) {
        executor = new ThreadPoolExecutor(0, maxNumThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }
}
