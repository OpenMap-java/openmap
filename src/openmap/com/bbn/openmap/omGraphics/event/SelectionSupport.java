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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/SelectionSupport.java,v $
// $RCSfile: SelectionSupport.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/23 20:46:44 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import java.util.Vector;

/**
 * A class to help SelectionProviders manage SelectionListeners, and
 * to help with firing SelectionEvents to those listeners.
 */
public class SelectionSupport {

    transient private Vector listeners;
    private Object source;

    public SelectionSupport(Object src) {
	source = src;
    }

    /**
     * Add a SelectionListener to the listener list.
     *
     * @param listener  The SelectionListener to be added
     */
    public synchronized void addSelectionListener(SelectionListener listener) {
	if (listeners == null) {
	    listeners = new Vector();
	}
	listeners.addElement(listener);
    }

    /**
     * Remove a SelectionListener from the listener list.
     *
     * @param listener  The SelectionListener to be removed
     */
    public synchronized void removeSelectionListener(SelectionListener listener) {
	if (listeners != null) {
	    listeners.removeElement(listener);
	}
    }

    /**
     * Remove all listeners from the list.
     */
    public synchronized void clearSelectionListeners() {
	if (listeners != null) {
	    listeners.clear();
	}
    }

    /**
     * Send a selection event to all registered listeners.
     * @param omg the graphic in the selection event
     * @param dtr the drawingtoolrequestor in the selection event
     * @param isSelected the selection state in the selection event
     * @see SelectionEvent
     */
    public void fireSelection(OMGraphic omg, DrawingToolRequestor dtr, boolean isSelected) {

	Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }
	    targets = (java.util.Vector) listeners.clone();
	}

        SelectionEvent evt = new SelectionEvent(source, omg, dtr, isSelected);

	for (int i = 0; i < targets.size(); i++) {
	    SelectionListener target = (SelectionListener)targets.elementAt(i);
	    target.selectionNotification(evt);
	}
    }

}
