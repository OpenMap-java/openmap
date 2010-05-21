package com.bbn.openmap.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @param <T> The type of value computed by the task.
 * 
 *        Use thread pool to run tasks that compute a value.
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
   private Future<T> futureList = null;

   /**
    * Start a thread that will call the <code>construct</code> method and then
    * exit.
    */
   public PooledSwingWorker() {
      final Callable<T> doWork = new Callable<T>() {
         public T call() {
            return construct();
         }
      };
      synchronized (lock) {
         task = new Runnable() {
            public void run() {
               completed = false;

               if (!interrupted) {

                  futureList = TaskService.singleton().spawn(doWork);
                  try {

//                     while (!futureList.isDone()) {
//                        synchronized (lock) {
//                           if (interrupted) {
//                              /*
//                               * Calling cancel on futureList seems to really mess up the whole
//                               * thread handling - it causes finished to be
//                               * called earlier and often, and many threads get
//                               * created. Seems better to just let the Callable
//                               * return and move on.
//                               */
//                              // future.cancel(true);
//                           }
//                        }
//                     }
                     value = futureList.get();
                     completed = !interrupted;
                  } catch (ExecutionException ee) {
                     interrupted = true;
                  } catch (InterruptedException e) {
                     interrupted = true;
                  } catch (CancellationException ce) {
                     interrupted = true;
            }
               }
               finished();
            }
         };

         /*
          * task.run() should use doFinished, which is a callable, to do
          * construct(). When passed to the TaskService, it will return a
          * Future<T>, which can be a member variable of the PooledSwingWorker.
          * When interrupt is called below, Future.cancel(true) can be called.
          */
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
               // don't care
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
         // if (future != null) {
         // future.cancel(true);
         // }
         lock.notifyAll();
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