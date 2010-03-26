/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reuse threads instead of making new ones over and over.
 */
public final class ThreadPool {
   private static final ThreadPool singleton = new ThreadPool();
   
   /**
    * @return the singleton pool.
    */
   public static ThreadPool singleton() {
      return singleton;
   }
   
   private final ExecutorService executor;
   
   private ThreadPool() {
      executor = Executors.newFixedThreadPool(10);
   }
   
   /**
    * Run a task in a thread.
    */
   public void spawn(Runnable task) {
      singleton.executor.execute(task);
   }
}
