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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/EOMGListenerSupport.java,v $
// $RCSfile: EOMGListenerSupport.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.util.Debug;

import java.util.Vector;

/**
 * This is a utility class that can be used by beans that need support
 * for handling EOMGListeners and calling the EOMGListener method.  You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class EOMGListenerSupport implements java.io.Serializable {

    transient private Vector listeners;
    transient private EditableOMGraphic eomg;

    /**
     * Construct a EOMGListenerSupport.
     */
    public EOMGListenerSupport() {
	this(null);
    }

    /**
     * Construct a EOMGListenerSupport.
     * @param aSource source Object
     */
    public EOMGListenerSupport(EditableOMGraphic graphic) {
	setEOMG(graphic);
    }

    /**
     * Set the source object.
     * @param graphic source EditableOMGraphic
     */
    public synchronized void setEOMG(EditableOMGraphic graphic) {
	eomg = graphic;
    }

    /**
     * Get the source object.
     * @return EditableOMGraphic
     */
    public synchronized EditableOMGraphic getEOMG() {
	return eomg;
    }

    /**
     * Add a EOMGListener.
     * @param l EOMGListener
     */
    public synchronized void addEOMGListener(EOMGListener l) {
	if (listeners == null) {
	    listeners = new Vector();
	}
	if (!listeners.contains(l)){
	    listeners.addElement(l);
	}
    }

    /**
     * Remove a EOMGListener.
     * @param l EOMGListener
     */
    public synchronized void removeEOMGListener(EOMGListener l) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(l);
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
     * Send a eomgChanged event to all registered listeners.
     *
     * @param event EOMGEvent
     */
    public void fireEvent(EOMGEvent event) {
	Vector targets;
	EOMGListener target;

	targets = getListeners();
	if (targets == null || eomg == null) return;
	int nTargets = targets.size();
	if (nTargets == 0) return;

	for (int i = 0; i < nTargets; i++) {
	    target = (EOMGListener)targets.elementAt(i);
	    if (Debug.debugging("eomgdetail")) {
		System.out.println(
			"EOMGListenerSupport.fireStatusChanged(): " +
			"target is: " + target);
	    }
	    target.eomgChanged(event);
	}
    }

}
