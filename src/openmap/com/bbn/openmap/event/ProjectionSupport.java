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

import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.proj.Projection;

/**
 * This is a utility class that can be used by beans that need support for
 * handling ProjectionListeners and firing ProjectionEvents. You can use an
 * instance of this class as a member field of your bean and delegate work to
 * it.
 */
public class ProjectionSupport extends ListenerSupport<ProjectionListener> {

    static Logger logger = Logger.getLogger("com.bbn.openmap.event.ProjectionSupport");
    private static final long serialVersionUID = 1L;
    protected ProjectionChangeNotifier pcNotifier;
    protected boolean useNotifier;

    /**
     * Construct a ProjectionSupport.
     */
    public ProjectionSupport(boolean useNotifier) {
        this(null, useNotifier);
    }

    /**
     * Construct a ProjectionSupport.
     * 
     * @param aSource source Object
     */
    public ProjectionSupport(Object aSource, boolean useNotifier) {
        super(aSource);
        this.useNotifier = useNotifier;
    }

    /**
     * Send a center event to all registered listeners.
     * 
     * @param proj Projection
     */
    public void fireProjectionChanged(Projection proj) {
        if (proj == null || isEmpty())
            return; // no event or no listeners

        if (useNotifier && pcNotifier == null) {
            pcNotifier = new ProjectionChangeNotifier();
            pcNotifier.start();
        }

        ProjectionEvent event = new ProjectionEvent(getSource(), proj);

        if (pcNotifier != null) {
            pcNotifier.fireProjectionEvent(event);
        } else {
            for (ProjectionListener listener : this) {
                listener.projectionChanged(event);
            }
        }
    }

    /**
     * Call when getting rid of the ProjectionSupport, it kills the
     * ProjectionSupport thread. // <-- CJS
     */
    public void dispose() {
        super.clear();
        if (pcNotifier != null) {
            pcNotifier.setTerminated(true);
            pcNotifier.fireProjectionEvent(null);
            pcNotifier.interrupt();
            pcNotifier = null;
        }
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
            setName("ProjectionSupportThread " + getName());
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

                if (projEvent != null && !isEmpty()) {

                    // Instead of going top of map to bottom, go bottom to top:

                    // Use this try/catch to deal with any problems getting
                    // clone of
                    // listeners, in case listener list is being changed while
                    // clone
                    // is being made, etc.
                    try {
                        ListIterator<ProjectionListener> li = ProjectionSupport.this.listIterator();
                        while (li.hasPrevious()) {
                            ProjectionListener listener = li.previous();

                            // This is going from top to bottom
                            // for (ProjectionListener listener :
                            // ProjectionSupport.this)
                            // {

                            if (nextEvent != null) {
                                break; // new event has been posted, bail out
                            }

                            // Use this try/catch to eliminate problems from
                            // individual
                            // layers - just blow them off.
                            try {
                                listener.projectionChanged(projEvent);
                            } catch (Exception e) {
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.info("ProjectionListener not handling projection well: "
                                            + listener.getClass().getName() + " : "
                                            + e.getClass().getName() + " : " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (Exception e) {
                        logger.fine("caught exception: " + e.getClass().getName() + " : "
                                + e.getMessage());
                    }

                    // notification is complete
                    synchronized (lock) {
                        projEvent = null;
                    }
                } else { // there is no event
                    // just wait until we are awakened for the next event
                    try {
                        synchronized (lock) {
                            if (nextEvent == null) {
                                lock.wait();
                            }
                        }
                    } catch (InterruptedException x) {
                        // do nothing, just reenter loop
                    }
                }
            }

            logger.fine("Projection notifier thread " + getName() + " done running");
        }
    }

}