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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProjectionSupport.java,v $
// $RCSfile: ProjectionSupport.java,v $
// $Revision: 1.10 $
// $Date: 2009/02/05 18:46:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.proj.Projection;

/**
 * This is a utility class that can be used by beans that need support for
 * handling ProjectionListeners and firing ProjectionEvents. You can use an
 * instance of this class as a member field of your bean and delegate work to
 * it.
 */
public class ProjectionSupport extends ListenerSupport {

    protected ProjectionChangeNotifier pcNotifier = new ProjectionChangeNotifier();

    /**
     * Construct a ProjectionSupport.
     */
    public ProjectionSupport() {
        this(null);
    }

    /**
     * Construct a ProjectionSupport.
     * 
     * @param aSource source Object
     */
    public ProjectionSupport(Object aSource) {
        super(aSource);
        pcNotifier.start();
    }

    /**
     * Add a ProjectionListener.
     * 
     * @param l ProjectionListener
     */
    public void addProjectionListener(ProjectionListener l) {
        addListener(l);
    }

    /**
     * Remove a ProjectionListener.
     * 
     * @param l ProjectionListener
     */
    public void removeProjectionListener(ProjectionListener l) {
        removeListener(l);
    }

    /**
     * Send a center event to all registered listeners.
     * 
     * @param proj Projection
     */
    public void fireProjectionChanged(Projection proj) {
        if (proj == null || size() == 0)
            return; // no event or no listeners

        if (pcNotifier == null) {
            pcNotifier = new ProjectionChangeNotifier();
            pcNotifier.start();
        }
        pcNotifier.fireProjectionEvent(new ProjectionEvent(getSource(), proj));
    }

    public void dispose() {
        super.removeAll();
        pcNotifier.setTerminated(true);
        pcNotifier.interrupt();
        pcNotifier = null;
    }

    /**
     * A thread that disperses the projection event, instead of letting the
     * Swing thread do it. A new one is created for every projection change, so
     * the current ProjectionEvent object is getting delivered with it.
     */
    protected class ProjectionChangeNotifier extends Thread {

        private final Object lock = new Object();

        /* current projection event */
        protected ProjectionEvent projEvent;

        /* next projection event */
        protected ProjectionEvent nextEvent;

        /* a flag to know if we are terminated. */
        protected boolean terminated = false;

        public ProjectionChangeNotifier() {
            setName("ProjectionSupportThread");
        }

        public boolean isTerminated() {
            return terminated;
        }

        public void setTerminated(boolean terminated) {
            this.terminated = terminated;
        }

        protected boolean isEventInProgress() {
            // synchronized(lock){
            return projEvent != null;
            // }
        }

        public void fireProjectionEvent(ProjectionEvent event) {
            synchronized (lock) {
                nextEvent = event;
                lock.notifyAll(); // wakes up thread if sleeping
            }
        }

        public void run() {
            while (!terminated) { // run while parent mapbean exists
                synchronized (lock) {
                    if (nextEvent != null) {
                        projEvent = nextEvent;
                        nextEvent = null;
                    }
                }

                if (projEvent != null && listeners != null) {
                    for (Object o : (Vector) listeners.clone()) {
                        if (nextEvent != null) {
                            break; // new event has been posted, bail out
                        }
                        if (o instanceof ProjectionListener) {
                            ((ProjectionListener) o).projectionChanged(projEvent);
                        }
                    }
                    // notification is complete
                    synchronized (lock) {
                        projEvent = null;
                    }
                } else { // there is no event
                    // just wait until we are awakened for the next event
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException x) {
                        // do nothing, just reenter loop
                    }
                }
            }
        }
    }

}