package com.bbn.openmap.util;

/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread. For instructions on using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * 
 * Note that the API changed slightly in the 3rd version: You must now invoke
 * start() on the SwingWorker after creating it.
 */
public abstract class SwingWorker<T> implements ISwingWorker<T> {

    private Object value; // see getValue(), setValue()

    private boolean interrupted = false;

    /**
     * Class to maintain reference to current worker thread under separate
     * synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;

        ThreadVar(Thread t) {
            thread = t;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }

    private ThreadVar threadVar;

    /**
     * Get the value produced by the worker thread, or null if it hasn't been
     * constructed yet.
     */
    protected synchronized Object getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     */
    private synchronized void setValue(Object x) {
        value = x;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract T construct();

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has returned.
     */
    public void finished() {}

    /**
     * A new method that interrupts the worker thread. Call this method to force
     * the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
        interrupted = true;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    /**
     * Return the value created by the <code>construct</code> method. Returns
     * null if either the constructing thread or the current thread was
     * interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public T get() {
        while (true) {
            Thread t = threadVar.get();
            if (t == null) {
                return (T) getValue();
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                // 2006.05.09 Attempting to fix synchro bug
                // Thread.currentThread().interrupt(); // propagate
                // System.out.println("OMSwingWorker interrupted : " + this);
                t.interrupt();
                interrupted = true;
                return null;
            }
        }
    }

    /**
     * Start a thread that will call the <code>construct</code> method and
     * then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                Object value = null;
                try {
                    value = construct();
                    // setValue(construct());
                } finally {
                    if (!Thread.currentThread().isInterrupted()) { // TW
                        setValue(value);
                    } else {
                        setValue(null);
                    }

                    // The original version called for the invokeLater method to
                    // be used to launch the finishing thread, but that moves
                    // the work back to the AWT thread. That seems like overkill
                    // if every layer does this, and the performance of layers
                    // greatly improves when this is moved to a different thread.
                    // SwingUtilities.invokeLater(doFinished); // TW
                    Thread fT = new Thread(doFinished);
                    fT.start();
                    threadVar.clear();
                }
            }
        };

        Thread t = new Thread(doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }

    /**
     * For compatibility with old versions of SwingWorker, calls start().
     */
    public void execute() {
        start();
    }
}