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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PaintListenerSupport.java,v $
// $RCSfile: PaintListenerSupport.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.util.Debug;

import java.util.Vector;

/**
 * This is a utility class that can be used by beans that need support
 * for handling PaintListeners and calling the PaintListener.paint()
 * method.  You can use an instance of this class as a member field of
 * your bean and delegate work to it.  
 */
public class PaintListenerSupport implements java.io.Serializable {

    transient private Vector listeners;
    transient private Object source;

    /**
     * Construct a PaintListenerSupport.
     */
    public PaintListenerSupport() {
	this(null);
    }

    /**
     * Construct a PaintListenerSupport.
     * @param aSource source Object
     */
    public PaintListenerSupport(Object source) {
	setSource(source);
    }

    /**
     * Set the source object.
     * @param graphic source EditableOMGraphic
     */
    public synchronized void setSource(Object source) {
	this.source = source;
    }

    /**
     * Get the source object.
     * @return source Object
     */
    public synchronized Object getSource() {
	return source;
    }

    /**
     * Add a PaintListener.
     * @param l PaintListener
     */
    public synchronized void addPaintListener(PaintListener l) {
	if (listeners == null) {
	    listeners = new Vector();
	}
	if (!listeners.contains(l)){
	    listeners.addElement(l);
	}
    }

    /**
     * Remove a PaintListener.
     * @param l PaintListener
     */
    public synchronized void removePaintListener(PaintListener l) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(l);
    }

    /**
     * Return the number of listeners.
     */
    public int size() {
	int size = 0;
	if (listeners == null) {
	    size = listeners.size();
	}
	return size;
    }

    /**
     * Return a cloned list of Listeners.
     * @return Vector of listeners, null if none have been added.
     */
    public synchronized Vector getListeners(){
	if (listeners == null){
	    return null;
	}

	return (Vector) listeners.clone();
    }

    /**
     * Send a Paint event to all registered listeners.
     *
     * @param event PaintEvent
     */
    public void paint(java.awt.Graphics graphics) {
	Vector targets;
	PaintListener target;

	targets = getListeners();
	if (targets == null || source == null) return;
	int nTargets = targets.size();
	if (nTargets == 0) return;

	for (int i = 0; i < nTargets; i++) {
	    target = (PaintListener)targets.elementAt(i);
	    if (Debug.debugging("paint")) {
		Debug.output("PaintListenerSupport.paint(): target is: " + 
			     target);
	    }
	    target.paint(graphics);
	}
    }

}
