package com.bbn.openmap.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @param <T> The type of value computed by the task.
 * 
 *        Use thread pool to run tasks that compute a value.
 * 
 * Thanks to Carsten for cleaning this up.
 */
public abstract class PooledSwingWorker<T> extends SwingWorker<T> {

    /**
     * Start a thread that will call the <code>construct</code> method and then
     * exit.
     */
    public PooledSwingWorker() {
        super();
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        TaskService.singleton().spawn(getFuture());
    }
}