// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.3 $
// $Date: 2003/10/08 21:29:17 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProjectionListeners and firing ProjectionEvents.  You
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
     * @param aSource source Object
     */
    public ProjectionSupport(Object aSource) {
	super(aSource);
    }

    /**
     * Add a ProjectionListener.
     * @param l ProjectionListener
     */
    public void addProjectionListener(ProjectionListener l) {
	addListener(l);
    }


    /**
     * Remove a ProjectionListener.
     * @param l ProjectionListener
     */
    public void removeProjectionListener(ProjectionListener l) {
	removeListener(l);
    }

    /**
     * Send a center event to all registered listeners.
     * @param proj Projection
     */
    public void fireProjectionChanged(Projection proj) {
	ProjectionListener target;

	if (size() == 0) return;

	ProjectionEvent evt = new ProjectionEvent(getSource(), proj);

	Iterator it = iterator();
	while (it.hasNext()) {
	    target = (ProjectionListener)it.next();
	    if (Debug.debugging("mapbean")) {
		Debug.output("ProjectionSupport.fireProjectionChanged(): " +
			     "target is: " + target);
	    }
	    target.projectionChanged(evt);
	}
    }
}
