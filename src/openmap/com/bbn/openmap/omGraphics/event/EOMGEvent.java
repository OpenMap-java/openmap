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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/EOMGEvent.java,v $
// $RCSfile: EOMGEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.util.Debug;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class EOMGEvent {
    /** Nothing about the graphic is known or defined. */
    public final static int EOMG_UNDEFINED = 0;
    /**
     * The Graphic is defined and in a stable state without the grab
     * points active. 
     */
    public final static int EOMG_DEFINED = 1;
    /** 
     * The Graphic is defined, in a stable state with the grab points
     * active. 
     */
    public final static int EOMG_SELECTED = 2;
    /**
     * The Graphic is defined, in a state of flux. The GrabPoints are
     * being manipulated, and the graphic parameters are being
     * modified as a result of this. 
     */
    public final static int EOMG_EDIT = 3;
    /**
     * This is a state where something different or extra is being
     * done.  Grab point being added/defined, some other parameter of
     * the graphic being modified where an extra state warrants it.
     * There may be other auxillary states defined, and they should be
     * defined to be greater than EOMG_AUX.
     */
    public final static int EOMG_AUX = 4;

    /** The EOMG in question. */
    protected EditableOMGraphic source;
    /**
     * The Cursor that should be active after this event is received.
     * If null, then the cursor should be unchanged. 
     */
    protected Cursor cursor;
    /**
     * An instructional/error message that should be presented to the
     * user.  If null, nothing should be displayed.  Send an empty
     * string to clear out what is currently being displayed.
     */
    protected String message;
    /**
     * A java MouseEvent that might have caused this EOMGEvent.  Can
     * be used by the listener to determine what type of action to
     * take, to check if an option key was held down, etc.  May be null!
     */
    protected MouseEvent mouseEvent = null;

    /**
     * Create an Event.
     */
    public EOMGEvent(EditableOMGraphic source, Cursor cursor, 
                     String message, MouseEvent me) {
        this.source = source;
        this.cursor = cursor;
        this.message = message;
        this.mouseEvent = me;
    }

    /**
     * Deactivation event.
     */
    public EOMGEvent() {
        this.source = null;
        this.cursor = null;
        this.message = null;
        this.mouseEvent = null;
    }

    public void setSource(EditableOMGraphic eomg) {
        source = eomg;
    }

    public EditableOMGraphic getSource() {
        return source;
    }

    public void setCursor(Cursor cur) {
        cursor = cur;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMouseEvent(MouseEvent me) {
        mouseEvent = me;
    }

    /**
     * @return the MouseEvent that started the EOMG changing.  May be null!
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    public boolean shouldShowGUI() {
        if (mouseEvent != null) {
            return          
                (mouseEvent.isControlDown() || 
                 (mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) > 0);
        }
        return false;
    }

    public boolean shouldDeactivate() {
        return  this.source == null && this.cursor == null &&
            this.message == null && this.mouseEvent == null;
    }

}

