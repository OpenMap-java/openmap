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
// $Revision: 1.2 $
// $Date: 2003/02/27 23:58:27 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProjectionListeners and firing ProjectionEvents.  You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class ProjectionSupport implements java.io.Serializable {

    transient private java.util.Vector listeners;
    transient private Object source;

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
	source = aSource;
    }

    /**
     * Set the source object.
     * @param aSource source Object
     */
    public synchronized void setSource(Object aSource) {
	source = aSource;
    }

    /**
     * Get the source object.
     * @return Object source
     */
    public synchronized Object getSource() {
	return source;
    }

    /**
     * Add a ProjectionListener.
     * @param l ProjectionListener
     */
    public synchronized void addProjectionListener(ProjectionListener l) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	if (!listeners.contains(l)){
	    listeners.addElement(l);
	}
    }


    /**
     * Remove a ProjectionListener.
     * @param l ProjectionListener
     */
    public synchronized void removeProjectionListener(ProjectionListener l) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(l);
    }

    /**
     * Return a cloned list of Listeners.
     * @return Vector of listeners, null if none have been added.
     */
    public synchronized java.util.Vector getListeners() {
	if (listeners == null) {
	    return null;
	}

	return (java.util.Vector) listeners.clone();
    }

    /**
     * Send a center event to all registered listeners.
     * @param proj Projection
     */
    public void fireProjectionChanged(com.bbn.openmap.proj.Projection proj) {
	java.util.Vector targets;
	ProjectionListener target;
	Object theSource = getSource();

	targets = getListeners();

	if (listeners == null) {
	    return;
	}

	int nTargets = targets.size();

	if (nTargets == 0) return;

	ProjectionEvent evt = new ProjectionEvent(theSource, proj);

	for (int i = 0; i < nTargets; i++) {
	    target = (ProjectionListener)targets.elementAt(i);
	    if (com.bbn.openmap.util.Debug.debugging("mapbean")) {
		System.out.println(
			"ProjectionSupport.fireProjectionChanged(): " +
			"target is: " + target);
	    }
	    target.projectionChanged(evt);
	}
    }
}
