/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

/**
 * Interface that describes kinds of SwingWorkers that layers might want to use.
 *
 * @author rshapiro
 */
public interface ISwingWorker<T> {

    /**
     * Compute the value to be returned by the <code>get</code> method.
     * 
     * @return the get method return
     */
   public T construct();

   /**
    * Called on the event dispatching thread (not on the worker thread) after
    * the <code>construct</code> method has returned.
    */
   public void finished();

   /**
    * A new method that interrupts the worker thread. Call this method to force
    * the worker to stop what it's doing.
    */
   public void interrupt();

   public boolean isInterrupted();

   /**
    * Return the value created by the <code>construct</code> method. Returns
    * null if either the constructing thread or the current thread was
    * interrupted before a value was produced.
    * 
    * @return the value created by the <code>construct</code> method
    */
   public T get();

   /**
    * Start the worker thread.
    */
   public void start();

   /**
    * For compatibility with old versions of SwingWorker, calls start().
    */
   public void execute();

}