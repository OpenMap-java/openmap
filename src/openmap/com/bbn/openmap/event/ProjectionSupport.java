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
// $Revision: 1.7 $
// $Date: 2005/08/09 17:37:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProjectionListeners and firing ProjectionEvents. You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class ProjectionSupport extends ListenerSupport {

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

        if (size() == 0)
            return;

        ProjectionEvent evt = new ProjectionEvent(getSource(), proj);

        ProjectionChangedRunnable pcr = new ProjectionChangedRunnable(evt);
        new Thread(pcr).start();
    }

    /**
     * A Runnable class that disperses the projection, instead of
     * letting the Swing thread do it. A new one is created for every
     * projection change, so the current ProjectionEvent object is
     * getting delivered with it.
     */
    protected class ProjectionChangedRunnable implements Runnable {
        protected ProjectionEvent projEvent;

        public ProjectionChangedRunnable(ProjectionEvent pe) {
            projEvent = pe;
        }

        public void run() {
            ProjectionListener target = null;
            Iterator it = iterator();
            while (it.hasNext()) {
                target = (ProjectionListener) it.next();
                if (Debug.debugging("mapbean")) {
                    Debug.output("ProjectionChangeRunnable: firing projection change, target is: "
                            + target);
                }
                target.projectionChanged(projEvent);
            }
        }
    };

}