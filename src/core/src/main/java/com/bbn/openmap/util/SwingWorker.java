package com.bbn.openmap.util;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * This class used to be based on the 3rd version of SwingWorker. But Carsten
 * provided a more modern version that's a lot cleaner.
 * 
 * @param <T> The type of value computed by the task.
 */
public abstract class SwingWorker<T> implements ISwingWorker<T> {

    private Exception executionException;

    private FutureTask<T> future = null;

    /**
     * Start a thread that will call the <code>construct</code> method and then
     * exit.
     */
    public SwingWorker() {
        final Callable<T> doWork = new Callable<T>() {
            public T call() {
                return construct();
            }
        };
        future = new FutureTask<T>(doWork) {
            @Override
            protected void done() {
                finished();
            }
        };
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract T construct();

    /**
     * For compatibility with old versions of SwingWorker, calls start().
     */
    public void execute() {
        start();
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        new Thread(future).start();
    }

    /**
     * Return the value created by the <code>construct</code> method. Returns
     * null if either the constructing thread or the current thread was
     * interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public T get() {
        try {
            return future.get();
        } catch (Exception e) {
            executionException = e;
        }
        return null;
    }

    /**
     * A new method that interrupts the worker thread. Call this method to force
     * the worker to stop what it's doing.
     */
    public void interrupt() {
        future.cancel(true);
    }

    public boolean isInterrupted() {
        return future.isCancelled();
    }

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * @return the executionException
     */
    public Exception getExecutionException() {
        return executionException;
    }

    /**
     * @param executionException the executionException to set
     */
    public void setExecutionException(Exception executionException) {
        this.executionException = executionException;
    }

    /**
     * @return the future
     */
    public FutureTask<T> getFuture() {
        return future;
    }

    /**
     * @param future the future to set
     */
    public void setFuture(FutureTask<T> future) {
        this.future = future;
    }

}