/* **********************************************************************
 * 
 * This file is from JavaSoft.  See the URL in the class documentation
 * for more information.
 * 
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/SwingWorker.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:49 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

package com.bbn.openmap.util;

import javax.swing.SwingUtilities;
import com.bbn.openmap.Layer;

/**
 * An abstract class that you subclass to perform
 * GUI-related work in a dedicated thread.
 * For instructions on using this class, see 
 * http://java.sun.com/products/jfc/swingdoc-current/threads2.html
 */
public abstract class SwingWorker {
    private Object value;
    private Thread thread;

    /** 
     * Used to set the value for derived classes.
     * @param sv object set as a result of the construct method
     * */
    public void setValue(Object sv){
	value = sv;
    }

    /** 
     * Used to launch the thread for derived classes.  If the thread
     * is not null, it is automatically started.
     * @param t the thread.
     * */
    public void setThread(Thread t){
	thread = t;
	if (thread != null) thread.start();
    }

    /** 
     * Compute the value to be returned by the <code>get</code> method. 
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to abort what it's doing.
     */
    public void interrupt() {
        Thread t = thread;
        if (t != null) {
            t.interrupt();
        }
        thread = null;
    }

    /**
     * Return the value created by the <code>construct</code> method.  
     */
    public Object get() {
        while (true) {  // keep trying if we're interrupted
            Thread t;
            synchronized (SwingWorker.this) {
                t = thread;
                if (t == null) {
                    return value;
                }
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public void execute() {
        final Runnable doFinished = new Runnable() {
           public void run() { finished(); }
        };

        Runnable doConstruct = new Runnable() { 
            public void run() {
                synchronized(SwingWorker.this) {

		    try {
			value = construct();
		    } catch (Exception e) {
			e.printStackTrace(Debug.err);
			value = null;
		    }
                    thread = null;
                }

		// This is key, if the layer is going to be used with
		// the GIFMapBean.  If the AWT is not available, then
		// we need to finish up everything in this thread.
		// Otherwise, finish everything up in the AWT thread.
		// Assuming that if the AWT is not available for one
		// layer, it's not available for any of them.
		if (Layer.isAWTAvailable())
		    SwingUtilities.invokeLater(doFinished);
		else finished();
            }
        };

        thread = new Thread(doConstruct);
        thread.start();
    }

    public SwingWorker() {
    }
}
