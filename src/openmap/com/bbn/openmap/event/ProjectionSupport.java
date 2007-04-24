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
// $Revision: 1.8 $
// $Date: 2007/04/24 19:53:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.util.Iterator;

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

        pcNotifier.fireProjectionEvent(new ProjectionEvent(getSource(), proj));
        /*
         * ProjectionEvent evt = new ProjectionEvent(getSource(), proj);
         * 
         * ProjectionSupportII.ProjectionChangedRunnable pcr = new
         * ProjectionSupportII.ProjectionChangedRunnable(evt); new
         * Thread(pcr).start();
         */
    }

    /**
     * A Runnable class that disperses the projection, instead of letting the
     * Swing thread do it. A new one is created for every projection change, so
     * the current ProjectionEvent object is getting delivered with it.
     */
//    protected class ProjectionChangedRunnable implements Runnable {
//        protected ProjectionEvent projEvent;
//
//        public ProjectionChangedRunnable(ProjectionEvent pe) {
//            projEvent = pe;
//        }
//
//        public void run() {
//            ProjectionListener target = null;
//            Iterator it = iterator();
//            while (it.hasNext()) {
//                target = (ProjectionListener) it.next();
//                if (Debug.debugging("mapbean")) {
//                    Debug.output("ProjectionChangeRunnable: firing projection change, target is: "
//                            + target);
//                }
//                target.projectionChanged(projEvent);
//            }
//        }
//    };

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

        /* a flag to kneow if we are terminated (which is never ) */
        protected boolean terminated = false;

        public ProjectionChangeNotifier() {}

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
            while (!terminated) { // run forever
                synchronized (lock) {
                    if (nextEvent != null) {
                        projEvent = nextEvent;
                        nextEvent = null;
                    }
                }
                if (projEvent != null) {
                    for (Iterator it = listeners.iterator(); it.hasNext();) {
                        Object o = it.next();
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
                    // just wait until we are woken up for the next event
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