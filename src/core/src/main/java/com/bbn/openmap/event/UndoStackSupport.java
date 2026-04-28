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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStackSupport.java,v $
// $RCSfile: ProjectionStackSupport.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.event;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a utility class that can be used by beans that need support for
 * notifying gui components that undo/redo actions are available. Use an
 * instance of this class as a member field of your bean and delegate work to
 * it. Used by the UndoStack.
 */
public class UndoStackSupport
        implements java.io.Serializable {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.event.UndoStackSupport");
    transient private ArrayList<UndoStackTrigger> triggers;

    /**
     * Construct a ProjectionStackSupport.
     */
    public UndoStackSupport() {
    }

    /**
     * Add a ProjectionStackTrigger.
     * 
     * @param pt ProjectionStackTrigger
     */
    public synchronized void add(UndoStackTrigger pt) {
        if (triggers == null) {
            triggers = new ArrayList<UndoStackTrigger>();
        }

        if (!triggers.contains(pt)) {
            triggers.add(pt);
        }
    }

    /**
     * Remove a ProjectionStackTrigger.
     * 
     * @param pt ProjectionStackTrigger
     */
    public synchronized void remove(UndoStackTrigger pt) {
        if (triggers == null) {
            return;
        }
        triggers.remove(pt);
    }

    /**
     * Return a cloned list of Triggers.
     * 
     * @return Vector of triggers, null if none have been added.
     */
    public synchronized ArrayList<UndoStackTrigger> getTriggers() {
        if (triggers == null) {
            return null;
        }

        return (ArrayList<UndoStackTrigger>) triggers.clone();
    }

    public int size() {
        if (triggers == null) {
            return 0;
        }
        return triggers.size();
    }

    /**
     * Send a status to all registered triggers.
     * 
     * @param undoEvent the next event for undo, so the GUI can be updated with
     *        what will happen on undo.
     * @param redoEvent the next event for redo, so the GUI can be updated with
     *        what will happen on redo.
     */
    public void fireStackStatus(UndoEvent undoEvent, UndoEvent redoEvent) {

        ArrayList<UndoStackTrigger> targets = getTriggers();

        if (triggers == null) {
            return;
        }

        for (UndoStackTrigger target : targets) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("target is: " + target);
            }

            target.updateUndoStackStatus(undoEvent, redoEvent);
        }
    }
}