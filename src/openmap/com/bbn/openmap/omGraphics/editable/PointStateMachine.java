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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PointStateMachine.java,v $
// $RCSfile: PointStateMachine.java,v $
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

public class PointStateMachine extends EOMGStateMachine {

    public PointStateMachine(EditableOMPoint point){
	super(point);
    }


    protected State[] init(){
	State[] states = super.init();
	Debug.message("eomc", "PointStateMachine.init()");

	//  These are the only two states that need something special
	//  to happen.
	states[GRAPHIC_UNDEFINED] = new PointUndefinedState((EditableOMPoint)graphic);
	states[GRAPHIC_SETOFFSET] = new PointSetOffsetState((EditableOMPoint)graphic);
	return states;
    }
}
