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

   private final ExecutorService executor;

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
}
