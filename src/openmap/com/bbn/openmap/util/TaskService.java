/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bbn.openmap.Environment;
import com.bbn.openmap.PropertyHandler;

/**
 * Reuse threads instead of making new ones over and over.
 * Use this if you don't need access to the Thread itself.
 * If you do need the Thread, for instance to invoke join,
 * use ThreadPool.
 */
public final class TaskService {
   /**
    * 
    */
   private static final int DEFAULT_POOL_SIZE = 50;
   private static TaskService singleton;
   
   public static void initialize(PropertyHandler properties) {
      synchronized (TaskService.class) {
         if (singleton != null) {
            throw new RuntimeException("Thread pool has already been initialized.");
         }
         Properties openmapProperties = properties.getProperties(Environment.OpenMapPrefix);
         String poolSizeString = openmapProperties.getProperty(Environment.ThreadPool);
         int poolSize;
         if (poolSizeString != null) {
            poolSize = Integer.parseInt(poolSizeString);
         } else {
            poolSize = DEFAULT_POOL_SIZE;
         }
         singleton = new TaskService(poolSize);
      }
   }
   
   /**
    * @return the singleton pool.
    */
   public static TaskService singleton() {
      synchronized (TaskService.class) {
         if (singleton == null) {
            singleton = new TaskService(DEFAULT_POOL_SIZE);
         }
         return singleton;
      }
   }
   
   private final ExecutorService executor;
   
   private TaskService(int poolSize) {
      executor = Executors.newFixedThreadPool(poolSize);
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
