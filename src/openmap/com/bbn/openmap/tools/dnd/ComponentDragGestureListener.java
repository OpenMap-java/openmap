package com.bbn.openmap.tools.dnd;

import java.awt.dnd.*;
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
     *  Note that DefaultDnDCatcher is a DragSourceListener itself, 
     *  so dndCatcher and dsl can be the same object.
     */

    public ComponentDragGestureListener(DefaultDnDCatcher dndCatcher, DragSourceListener dsl) {
        this.dndCatcher = dndCatcher;
        this.dsl = dsl;
        Debug.message("draggesturelistener", "Created> ComponentDragGestureListener");
        Debug.message("draggesturelistener", "dndCatcher=" + dndCatcher);
    }
    /**
     * A <code>DragGestureRecognizer</code> has detected 
     * a platform-dependent drag initiating gesture and 
     * is notifying this listener
     * in order for it to initiate the action for the user.
     * <P>
     * @param dge the <code>DragGestureEvent</code> describing 
     * the gesture that has just occurred
     */

    public void dragGestureRecognized(DragGestureEvent dge) {
        Debug.message("draggesturelistener", "ComponentDragGestureListener.dragGestureRecognized");
        try {
            dndCatcher.startDragAction(dge, dsl);
        } catch (InvalidDnDOperationException idoe) {
        }
    }
}
