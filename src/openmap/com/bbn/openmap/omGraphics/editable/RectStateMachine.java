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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/RectStateMachine.java,v $
// $RCSfile: RectStateMachine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.editable;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.util.Debug;

public class RectStateMachine extends EOMGStateMachine {

    public RectStateMachine(EditableOMRect rect){
	super(rect);
    }

    protected State[] init(){
	State[] states = super.init();
	Debug.message("eomc", "RectStateMachine.init()");

	//  These are the only two states that need something special
	//  to happen.
	states[GRAPHIC_UNDEFINED] = new RectUndefinedState((EditableOMRect)graphic);
	states[GRAPHIC_SELECTED] = new RectSelectedState((EditableOMRect)graphic);
	states[GRAPHIC_SETOFFSET] = new RectSetOffsetState((EditableOMRect)graphic);
	return states;
    }
}
