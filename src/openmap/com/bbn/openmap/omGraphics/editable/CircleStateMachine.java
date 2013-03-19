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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/CircleStateMachine.java,v
// $
// $RCSfile: CircleStateMachine.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMCircle;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;
public class CircleStateMachine
        extends EOMGStateMachine {

    public CircleStateMachine(EditableOMCircle circle) {
        super(circle);
    }

    protected State[] init() {
        State[] states = super.init();
        Debug.message("eomc", "CircleStateMachine.init()");

        // These are the only two states that need something special
        // to happen.
        states[GRAPHIC_UNDEFINED] = new CircleUndefinedState((EditableOMCircle) graphic);
        states[GRAPHIC_SELECTED] = new CircleSelectedState((EditableOMCircle) graphic);
        states[GRAPHIC_SETOFFSET] = new CircleSetOffsetState((EditableOMCircle) graphic);
        states[GRAPHIC_EDIT] = new GraphicUndefinedEditState((EditableOMCircle) graphic);
        states[GRAPHIC_EDIT] = new GraphicEditState((EditableOMCircle) graphic);
        return states;
    }

    protected void setInitialEdit() {
        setEdit();
        if (getState(GRAPHIC_EDIT) instanceof GraphicUndefinedEditState) {
            ((GraphicUndefinedEditState)getState(GRAPHIC_EDIT)).needAnotherPoint = true;
        }
    }
}