package com.bbn.openmap.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @param <T> The type of value computed by the task.
 * 
 * Use thread pool to run tasks that compute a value.
 * 
 */
public abstract class PooledSwingWorker<T> {

   private T value;
   private boolean interrupted = false;
   private Callable<T> task;
   private Future<T> future;
   private Exception executionException;
   private final Object lock = new Object();

   /**
    * Start a thread that will call the <code>construct</code> method and then
    * exit.
    */
   public PooledSwingWorker() {
      final Runnable doFinished = new Runnable() {
         public void run() {
            finished();
         }
      };
      synchronized (lock) {
         task = new Callable<T>() {
            @Override
            public T call() {
               T value = construct();
               TaskService.singleton().spawn(doFinished);
               return value;
            }
         };
      }
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
      synchronized (lock) {
         if (task != null) {
            future = TaskService.singleton().spawn(task);
         }
      }
   }
   
   public Exception getExecutionException() {
      return executionException;
   }

   /**
    * Return the value created by the <code>construct</code> method. Returns
    * null if either the constructing thread or the current thread was
    * interrupted before a value was produced.
    * 
    * @return the value created by the <code>construct</code> method
    */
   public T get() {
      while (!interrupted) {
         synchronized (lock) {
            if (future.isDone()) {
               try {
                  value = future.get();
                  break;
               } catch (InterruptedException e) {
                  break;
               } catch (ExecutionException e) {
                  // execution got an exception.
                  executionException = e;
                  break;
               }
            } else {
               try {
                  wait(100);
               } catch (InterruptedException e) {
                  // don't care.
               }
            }
         }
      }
      return value;
   }

   /**
    * A new method that interrupts the worker thread. Call this method to force
    * the worker to stop what it's doing.
    */
   public void interrupt() {
      synchronized (lock) {
         interrupted = true;
         task = null;
         if (future != null) {
            future.cancel(true);
         }
         lock.notify();
      }
   }

   public boolean isInterrupted() {
      return interrupted;
   }

   /**
    * Called on the event dispatching thread (not on the worker thread) after
    * the <code>construct</code> method has returned.
    */
   public void finished() {
   }

   /**
    * Get the value produced by the worker thread, or null if it hasn't been
    * constructed yet.
    */
   protected T getValue() {
      return value;
   }
}