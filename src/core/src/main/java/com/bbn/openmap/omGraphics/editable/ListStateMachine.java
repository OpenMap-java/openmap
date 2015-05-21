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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/ListStateMachine.java,v
// $
// $RCSfile: ListStateMachine.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.util.Iterator;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMGraphicList;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;
public class ListStateMachine extends EOMGStateMachine {

    public ListStateMachine(EditableOMGraphicList list) {
        super(list);
    }

    protected State[] init() {
        Debug.message("eomg", "ListStateMachine.init()");
        State[] states = super.init();
        states[GRAPHIC_SELECTED] = new ListSelectedState(graphic);
        states[GRAPHIC_UNSELECTED] = new ListUnselectedState(graphic);
        return states;
    }

    /**
     * Check to see if the graphic offset point needs to be defined.
     */
    public boolean isOffsetNeeded() {
        return offsetNeeded;
    }

    public void setState(int state) {
        super.setState(state);
        if (Debug.debugging("eomg")) {
            Debug.output("ListStateMachine.setState: Setting state to "
                    + states.elementAt(state).getClass().getName());
        }
        for (Iterator it = ((EditableOMGraphicList) graphic).getEditables()
                .iterator(); it.hasNext();) {
            try {
                EditableOMGraphic editable = (EditableOMGraphic) it.next();
                editable.getStateMachine().setState(state);

                if (Debug.debugging("eomg")) {
                    Debug.debugging("  on " + editable.getClass().getName());
                }
            } catch (NullPointerException npe) {
                Debug.output("ListStateMachine: something's not right setting ListStateMachine.setState()");
            }
        }
    }
}