//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/dnd/ComponentDragGestureListener.java,v $
//$RCSfile: ComponentDragGestureListener.java,v $
//$Revision: 1.3 $
//$Date: 2004/10/14 18:06:25 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.dnd;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

import com.bbn.openmap.util.Debug;

/**
 * A custom DragGestureListener class that accepts DefaultDnDCatcher
 * as one of the parameters and invokes its startDragAction() method
 * on dragGestureRecognized event.
 */

public class ComponentDragGestureListener implements DragGestureListener {
    private DragSourceListener dsl;
    private DefaultDnDCatcher dndCatcher;

    /**
     * Note that DefaultDnDCatcher is a DragSourceListener itself, so
     * dndCatcher and dsl can be the same object.
     */

    public ComponentDragGestureListener(DefaultDnDCatcher dndCatcher,
            DragSourceListener dsl) {
        this.dndCatcher = dndCatcher;
        this.dsl = dsl;
        Debug.message("draggesturelistener",
                "Created> ComponentDragGestureListener");
        Debug.message("draggesturelistener", "dndCatcher=" + dndCatcher);
    }

    /**
     * A <code>DragGestureRecognizer</code> has detected a
     * platform-dependent drag initiating gesture and is notifying
     * this listener in order for it to initiate the action for the
     * user.
     * <P>
     * 
     * @param dge the <code>DragGestureEvent</code> describing the
     *        gesture that has just occurred
     */

    public void dragGestureRecognized(DragGestureEvent dge) {
        Debug.message("draggesturelistener",
                "ComponentDragGestureListener.dragGestureRecognized");
        try {
            dndCatcher.startDragAction(dge, dsl);
        } catch (InvalidDnDOperationException idoe) {
        }
    }
}