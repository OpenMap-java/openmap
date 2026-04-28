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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStack.java,v $
// $RCSfile: ProjectionStack.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.OMComponent;

/**
 * Provides a stack of actions that can be undone/redone. The UndoEvent objects
 * contain the information needed to implement the actions. The UndoStack is
 * told to push UndoEvents on their source objects by UndoStackTriggers using a
 * regular ActionListener/ActionEvent mechanism. The UndoStack is an
 * ActionListener to the triggers (GUI buttons/menu items that say Undo or
 * Redo).
 */
public class UndoStack
        extends OMComponent
        implements ActionListener {

    private final static Logger logger = Logger.getLogger("com.bbn.openmap.event.UndoStack");

    public final static int DEFAULT_MAX_SIZE = 10;
    public final static int REMEMBER_ALL = -1;

    /**
     * The notion of the current state is important. When the user does
     * something, a component that is interested in undo/redo should push a
     * current state to this stack. This current state reflects the state of
     * some object at the current time. If another state gets set, then the old
     * state is now put on the undo stack. If the undo command is given to this
     * stack, then an event is popped off the undo stack, set to be the current
     * state. The old state gets pushed to the redo stack.
     */
    protected transient UndoEvent currentState;

    protected int stackSize = DEFAULT_MAX_SIZE;

    public final static transient String UndoCmd = "undo";
    public final static transient String RedoCmd = "redo";
    public final static transient String ClearUndoCmd = "clearUndoStack";
    public final static transient String ClearRedoCmd = "clearRedoStack";
    public final static transient String ClearCmd = "clearStacks";

    protected final Stack<UndoEvent> undoStack;
    protected final Stack<UndoEvent> redoStack;

    protected final UndoStackSupport triggers;

    /**
     */
    public UndoStack() {
        redoStack = new Stack<UndoEvent>();
        undoStack = new Stack<UndoEvent>();
        triggers = new UndoStackSupport();
    }

    /**
     * Sets the current state of some object on the stack. The stack doesn't
     * care what that event/state represents, since it has all the info needed
     * to tell components how to get back to this state later. Anyway, this
     * current state kinda hangs out in limbo. If another event/state comes in,
     * the current state gets pushed on the undo stack. The redo stack gets
     * cleared, since a new state path forward has been established.
     * 
     * @param event
     */
    public void setTheWayThingsAre(UndoEvent event) {
        if (currentState != null) {
            rememberLastThing(currentState);
        }
        currentState = event;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("making (" + currentState.getDescription() + ") current state");
        }

        // We have a new path forward, undefined, so clear out old path forward
        redoStack.clear();
        fireStackStatus();
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand().intern();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Received command: " + command);
        }

        if (UndoCmd.equalsIgnoreCase(command) && undoStack != null && !undoStack.empty()) {

            undo();

        } else if (RedoCmd.equalsIgnoreCase(command) && redoStack != null && !redoStack.empty()) {

            redo();

        } else {
            clearStacks((ClearUndoCmd.equalsIgnoreCase(command) || ClearCmd.equalsIgnoreCase(command)),
                        (ClearRedoCmd.equalsIgnoreCase(command) || ClearCmd.equalsIgnoreCase(command)));
        }
    }

    /**
     * Put a new UndoEvent on the backStack, to remember for later in case we
     * need to back up.
     * 
     * @param event UndoEvent.
     */
    protected synchronized void rememberLastThing(UndoEvent event) {

        if (undoStack.size() >= stackSize) {
            undoStack.removeElementAt(0);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("remembering (" + event.getDescription() + ")");
        }

        undoStack.push(event);
    }

    /**
     * Take a UndoEvent off the backStack, and push it on the forward stack, and
     * invoke the new currentState so the source component gets modified.
     */
    protected synchronized void undo() {
        if (currentState != null) {
            while (redoStack.size() >= stackSize) {
                redoStack.removeElementAt(0);
                logger.info("making room for " + currentState.getDescription());
            }

            redoStack.push(currentState);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("making last current state (" + currentState.getDescription() + ") on redo stack");
            }
        }

        currentState = undoStack.pop();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("making top undo state (" + currentState.getDescription() + ") current state");
        }

        if (currentState != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("calling setState on " + currentState.getDescription());
            }
            currentState.setState();
        }

        fireStackStatus();
    }

    /**
     * Take a UndoEvent off the forwardStack, and push it on the backStack.
     */
    protected synchronized void redo() {
        if (currentState != null) {
            while (undoStack.size() >= stackSize) {
                undoStack.removeElementAt(0);
            }

            undoStack.push(currentState);
        }

        currentState = redoStack.pop();

        if (currentState != null) {
            currentState.setState();
        }

        fireStackStatus();
    }

    /**
     * Clear out the chosen undo stacks and fire an event to update the triggers
     * on stack status. Also sets the current state, which isn't held by either
     * stack, to null.
     * 
     * @param clearUndoStack clear out the undo stack.
     * @param clearRedoStack clear out the redo stack.
     */
    public synchronized void clearStacks(boolean clearUndoStack, boolean clearRedoStack) {

        if (clearUndoStack && undoStack != null) {
            undoStack.clear();
        }

        if (clearRedoStack && redoStack != null) {
            redoStack.clear();
        }

        currentState = null;

        fireStackStatus();
    }

    public void fireStackStatus() {
        if (triggers != null) {
            UndoEvent undoEvent = getWhatWillHappenNextFromStack(undoStack);
            UndoEvent redoEvent = getWhatWillHappenNextFromStack(redoStack);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("back enabled: " + (undoEvent != null) + ", forward enabled: " + (redoEvent != null));
            }
            triggers.fireStackStatus(undoEvent, redoEvent);
        }
    }

    protected UndoEvent getWhatWillHappenNextFromStack(Stack<UndoEvent> stack) {
        UndoEvent nextThing = null;
        try {
            nextThing = stack.peek();
        } catch (EmptyStackException ese) {
            // Noop, event will stay null, that's OK and acceptable.
        }
        return nextThing;
    }

    /**
     * UndoStackTriggers should call this method to add themselves for stack
     * notifications, and all will be well.
     */
    public void addUndoStackTrigger(UndoStackTrigger trigger) {
        trigger.addActionListener(this);

        UndoEvent undoEvent = getWhatWillHappenNextFromStack(undoStack);
        UndoEvent redoEvent = getWhatWillHappenNextFromStack(redoStack);

        triggers.add(trigger);

        trigger.updateUndoStackStatus(undoEvent, redoEvent);
    }

    /**
     * UndoStackTriggers should call this method to remove themselves from stack
     * notifications, and all will be well.
     */
    public void removeUndoStackTrigger(UndoStackTrigger trigger) {
        trigger.removeActionListener(this);
        if (triggers != null) {
            triggers.remove(trigger);
        }
    }

}
