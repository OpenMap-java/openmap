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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/TextStateMachine.java,v
// $
// $RCSfile: TextStateMachine.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.util.stateMachine.State;
public class TextStateMachine extends EOMGStateMachine {

    public TextStateMachine(EditableOMText text) {
        super(text);
    }

    protected State[] init() {
        State[] states = super.init();

        //  These are the only three states that need something special
        // to happen.
        states[GRAPHIC_UNDEFINED] = new TextUndefinedState((EditableOMText) graphic);
        states[GRAPHIC_EDIT] = new TextEditState((EditableOMText) graphic);
        states[GRAPHIC_SETOFFSET] = new TextSetOffsetState((EditableOMText) graphic);
        return states;
    }
}