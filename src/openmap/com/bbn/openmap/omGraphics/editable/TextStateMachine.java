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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/TextStateMachine.java,v $
// $RCSfile: TextStateMachine.java,v $
// $Revision: 1.1 $
// $Date: 2003/10/24 15:34:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.layer.util.stateMachine.State;
import com.bbn.openmap.util.Debug;

public class TextStateMachine extends EOMGStateMachine {

    public TextStateMachine(EditableOMText text) {
	super(text);
    }


    protected State[] init() {
	State[] states = super.init();

	//  These are the only two states that need something special
	//  to happen.
	states[GRAPHIC_UNDEFINED] = new TextUndefinedState((EditableOMText)graphic);
//	states[GRAPHIC_SELECTED] = new TextSelectedState((EditableOMText)graphic);
	states[GRAPHIC_SETOFFSET] = new TextSetOffsetState((EditableOMText)graphic);
	return states;
    }
}
