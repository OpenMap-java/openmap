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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PointStateMachine.java,v
// $
// $RCSfile: PointStateMachine.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;
public class PointStateMachine extends EOMGStateMachine {

    public PointStateMachine(EditableOMPoint point) {
        super(point);
    }

    protected State[] init() {
        State[] states = super.init();
        Debug.message("eomg", "PointStateMachine.init()");

        //  These are the only two states that need something special
        //  to happen.
        states[GRAPHIC_EDIT] = new PointEditState((EditableOMPoint) graphic);
        states[GRAPHIC_UNDEFINED] = new PointUndefinedState((EditableOMPoint) graphic);
        states[GRAPHIC_SETOFFSET] = new PointSetOffsetState((EditableOMPoint) graphic);
        return states;
    }
}