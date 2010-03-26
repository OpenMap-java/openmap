/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Reuse threads instead of making new ones over and over.
 * Use this if you don't need access to the Thread itself.
 * If you do need the Thread, for instance to invoke join,
 * use ThreadPool.
 */
public final class TaskService {
   private static final TaskService singleton = new TaskService();
   
   /**
    * @return the singleton pool.
    */
   public static TaskService singleton() {
      return singleton;
   }
   
   private final ExecutorService executor;
   
   private TaskService() {
      executor = Executors.newFixedThreadPool(10);
   }
   
   /**
    * Run a task in a thread.
    */
   public void spawn(Runnable task) {
      singleton.executor.execute(task);
   }
   
   public <T> Future<T> spawn(Callable<T> task) {
      List<Future<T>> futures;
      try {
         futures = singleton.executor.invokeAll(Collections.singletonList(task));
         return futures.get(0);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
   }
}
