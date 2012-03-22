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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/LineStateMachine.java,v
// $
// $RCSfile: LineStateMachine.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMLine;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;
public class LineStateMachine extends EOMGStateMachine {

    public LineStateMachine(EditableOMLine l) {
        super(l);
    }

    protected State[] init() {
        State[] states = super.init();
        Debug.message("eoml", "LineStateMachine.init()");

        //  These are the only two states that need something special
        //  to happen.
        states[GRAPHIC_UNDEFINED] = new LineUndefinedState((EditableOMLine) graphic);
        states[GRAPHIC_SETOFFSET] = new LineSetOffsetState((EditableOMLine) graphic);
        return states;
    }
}