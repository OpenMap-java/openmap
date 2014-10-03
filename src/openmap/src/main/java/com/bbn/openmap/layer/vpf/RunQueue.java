// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/RunQueue.java,v $
// $RCSfile: RunQueue.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement a very basic thread scheduling mechanism.
 * 
 * TerminatingRunnable objects get added to the queue. As the thread
 * gets cpu time, it will sequentially dequeue and run the objects in
 * the queue in a LIFO order. Duplicates are allowed in the queue. Run
 * will get called exactly once for each enqueue call. Only one thread
 * will run at a time. (The next object won't run until the previous
 * one completes.)
 * 
 * @see java.lang.Runnable#run()
 */
public class RunQueue extends java.lang.Thread {
    /** the queue of objects to run */
    final private List<Runnable> queue = new ArrayList<Runnable>(); //fixed, unique
    /** used to give threads unique names */
    static private int tcount = 0;

    /**
     * default constructor. priority and daemon status inherited from
     * the calling thread. This thread is NOT started
     */
    public RunQueue() {
        this(null); //call constructor with ThreadGroup arg
    }

    /**
     * construct this thread in a particular ThreadGroup
     * 
     * @param tg the ThreadGroup to be in. <code>null</code> means
     *        the current ThreadGroup
     */
    public RunQueue(ThreadGroup tg) {
        super((tg != null) ? tg : Thread.currentThread().getThreadGroup(),
              nextThreadName());
    }

    /**
     * construct a thread in the default ThreadGroup with a few
     * options set
     * 
     * @param isDaemon <code>true</code> means be a daemon thread,
     *        <code>false</code> means don't.
     * @param priority the value to use when calling setPriority
     * @param autoStart <code>true</code> means call this.start()
     * @see java.lang.Thread#start()
     */
    public RunQueue(boolean isDaemon, int priority, boolean autoStart) {
        this(null, isDaemon, priority, autoStart);
    }

    /**
     * construct a thread in the default ThreadGroup with a few
     * options set
     * 
     * @param tg the ThreadGroup to be in
     * @param isDaemon <code>true</code> means be a daemon thread,
     *        <code>false</code> means don't.
     * @param priority the value to use when calling setPriority
     * @param autoStart <code>true</code> means call this.start()
     * @see java.lang.Thread#start()
     */
    public RunQueue(ThreadGroup tg, boolean isDaemon, int priority,
            boolean autoStart) {
        super(tg, nextThreadName());
        setDaemon(isDaemon);
        setPriority(priority);
        if (autoStart) {
            start();
        }
    }

    /**
     * start going. This function locks the queue, dequeues and item,
     * unlocks the queue, and calls run on the object that was
     * dequeued. if no object was on the queue, it waits on the queue.
     * this function never exits.
     */
    public void run() {
        while (true) {
            Runnable r = null;
            synchronized (queue) { //lock the queue
                while (queue.isEmpty()) {
                    try {
                        queue.wait(); //wait for an enqueue to happen
                    } catch (java.lang.InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                r = queue.remove(queue.size() - 1);

            }
            try {
                r.run();
            } catch (Exception e) {
                //Since this happens asynchronously to the thread who
                // enqueued
                //the object, there isn't a whole lot we can do...
                e.printStackTrace();
            }
        }
    }

    /**
     * Add an object to the queue of threads to run
     * 
     * @param r the object to add
     */
    public void enqueue(TerminatingRunnable r) {
        synchronized (queue) {
            queue.add(r);
            queue.notifyAll(); //notify the running thread that the
                               // queue
            // has stuff in it now
        }
    }

    /**
     * if we use threadgroups, we need names. So here's where we make
     * them
     */
    private static synchronized String nextThreadName() {
        return ("RunQueueThread-" + tcount++);
    }
}