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

   private static TaskService singleton;

   /**
    * @return the singleton pool.
    */
   public static TaskService singleton() {
      synchronized (TaskService.class) {
         if (singleton == null) {
            singleton = new TaskService();
         }
         return singleton;
      }
   }

   private final ExecutorService executor;

   private TaskService() {
      executor = Executors.newCachedThreadPool();
   }

   /**
    * Run a task in a thread.
    */
   public void spawn(Runnable task) {
      singleton.executor.execute(task);
   }

   public <T> Future<T> spawn(Callable<T> task) {
      return singleton.executor.submit(task);
   }
}
