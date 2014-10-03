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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/SelectionSupport.java,v $
// $RCSfile: SelectionSupport.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.util.Vector;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;

/**
 * A class to help SelectionProviders manage SelectionListeners, and
 * to help with firing SelectionEvents to those listeners.
 */
public class SelectionSupport {

    transient private Vector<SelectionListener> listeners;
    private Object source;

    public SelectionSupport(Object src) {
        source = src;
    }

    /**
     * Add a SelectionListener to the listener list.
     * 
     * @param listener The SelectionListener to be added
     */
    public synchronized void addSelectionListener(SelectionListener listener) {
        if (listeners == null) {
            listeners = new Vector<SelectionListener>();
        }
        listeners.addElement(listener);
    }

    /**
     * Remove a SelectionListener from the listener list.
     * 
     * @param listener The SelectionListener to be removed
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
     * 
     * @param omg the graphic in the selection event
     * @param dtr the drawingtoolrequestor in the selection event
     * @param isSelected the selection state in the selection event
     * @see SelectionEvent
     */
    public void fireSelection(OMGraphic omg, DrawingToolRequestor dtr,
                              boolean isSelected) {

        Vector<SelectionListener> targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = (java.util.Vector<SelectionListener>) listeners.clone();
        }

        SelectionEvent evt = new SelectionEvent(source, omg, dtr, isSelected);

        for (int i = 0; i < targets.size(); i++) {
            SelectionListener target = targets.elementAt(i);
            target.selectionNotification(evt);
        }
    }

}