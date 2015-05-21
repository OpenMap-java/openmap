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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/EOMGStateMachine.java,v
// $
// $RCSfile: EOMGStateMachine.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;
import com.bbn.openmap.util.stateMachine.StateMachine;
public class EOMGStateMachine extends StateMachine {

    public EditableOMGraphic graphic;
    protected boolean offsetNeeded = false;

    /** The state when a graphic is not there yet, about to be drawn. */
    public final static int GRAPHIC_UNDEFINED = EOMGEvent.EOMG_UNDEFINED; // 0
    /**
     * The state when an existing graphic is present, but has not been
     * selected. At this point, the point nodes should not be visible,
     * but the graphic is.
     */
    public final static int GRAPHIC_UNSELECTED = EOMGEvent.EOMG_DEFINED; // 1
    /**
     * The state when an existing graphic is selected, ready for
     * change given the correct input. The point nodes and the graphic
     * are visible.
     */
    public final static int GRAPHIC_SELECTED = EOMGEvent.EOMG_SELECTED; // 2
    /**
     * The state when the graphic points are in the process of
     * changing.
     */
    public final static int GRAPHIC_EDIT = EOMGEvent.EOMG_EDIT; // 3
    /** The state where an offset point needs to be defined. */
    public final static int GRAPHIC_SETOFFSET = EOMGEvent.EOMG_AUX; // 4

    public final static int DEFAULT_NUMBER_STATES = 5;

    public int NUMBER_STATES = DEFAULT_NUMBER_STATES;

    public EOMGStateMachine(EditableOMGraphic graphic) {
        this.graphic = graphic;
        setStates(init());

        // set reset state
        setResetState(GRAPHIC_UNSELECTED);
        reset();
        setMapMouseListenerResponses(true);

        Debug.message("eomg", "EOMGStateMachine created");
    }

    protected State[] init() {
        Debug.message("eomg", "EOMGStateMachine.init()");
        State[] states = new State[NUMBER_STATES];

        states[GRAPHIC_UNDEFINED] = new GraphicUndefinedState(graphic);
        states[GRAPHIC_UNSELECTED] = new GraphicUnselectedState(graphic);
        states[GRAPHIC_SELECTED] = new GraphicSelectedState(graphic);
        states[GRAPHIC_EDIT] = new GraphicEditState(graphic);
        states[GRAPHIC_SETOFFSET] = new GraphicSetOffsetState(graphic);
        return states;
    }

    /**
     * Check to see if the graphic offset point needs to be defined.
     */
    public boolean isOffsetNeeded() {
        return offsetNeeded;
    }

    /**
     * The state machine, from the edit mode, tests this to see if an
     * offset point needs to be defined. This is set to true when the
     * OMGraphic (offset rendertype) is undefined. Can't be set every
     * time the state machine gets in edit mode, because we may be
     * just moving the graphic. Set to true if you want to define the
     * offset point after defining the actual graphic, which should be
     * done only for offset graphics.
     */
    public void setOffsetNeeded(boolean set) {
        offsetNeeded = set;
    }

    public void setUndefined() {
        setState(GRAPHIC_UNDEFINED);
    }

    public void setUnselected() {
        setState(GRAPHIC_UNSELECTED);
    }

    public void setSelected() {
        setState(GRAPHIC_SELECTED);
    }

    public void setEdit() {
        setState(GRAPHIC_EDIT);
    }

    public void setOffset() {
        setState(GRAPHIC_SETOFFSET);
    }
}