package com.bbn.openmap.util;


/**
 * @param <T> The type of value computed by the task.
 * 
 * Use thread pool to run tasks that compute a value.
 * 
 */
public abstract class PooledSwingWorker<T> 
      implements ISwingWorker<T> {
   private T value;
   private boolean interrupted;
   private boolean completed;
   private Exception executionException;
   private final Runnable task;
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
         task = new Runnable() {
            public void run() {
               value = construct();
               completed = true;
               TaskService.singleton().spawn(doFinished);
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
         if (!interrupted) {
            TaskService.singleton().spawn(task);
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
      synchronized (lock) {
         while (!interrupted && !completed) {
            try {
               lock.wait(100);
            } catch (InterruptedException e) {
               // don't care.
            }
         }
         return value;
      }
   }

   /**
    * A new method that interrupts the worker thread. Call this method to force
    * the worker to stop what it's doing.
    */
   public void interrupt() {
      synchronized (lock) {
         interrupted = true;
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
  
}