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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyStateMachine.java,v $
// $RCSfile: PolyStateMachine.java,v $
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

public class PolyStateMachine extends EOMGStateMachine {

    public static final int POLY_ADD_POINT = DEFAULT_NUMBER_STATES;
    public static final int POLY_ADD_NODE = DEFAULT_NUMBER_STATES + 1;
    public static final int POLY_DELETE_NODE = DEFAULT_NUMBER_STATES + 2;

    public PolyStateMachine(EditableOMPoly p){
	super(p);
    }

    protected State[] init(){
	Debug.message("eomg", "PolyStateMachine.init()");
	NUMBER_STATES = DEFAULT_NUMBER_STATES + 3;

	State[] states = new State[NUMBER_STATES];

	states[GRAPHIC_UNDEFINED] = new PolyUndefinedState((EditableOMPoly)graphic);
	states[GRAPHIC_UNSELECTED] = new GraphicUnselectedState(graphic);
	states[GRAPHIC_SELECTED] = new GraphicSelectedState((EditableOMPoly)graphic);
	states[GRAPHIC_EDIT] = new GraphicEditState(graphic);
	states[GRAPHIC_SETOFFSET] = new PolySetOffsetState((EditableOMPoly)graphic);
	states[POLY_ADD_POINT] = new PolyAddPointState((EditableOMPoly) graphic);
	states[POLY_ADD_NODE] = new PolyAddNodeState((EditableOMPoly) graphic);
	states[POLY_DELETE_NODE] = new PolyDeleteNodeState((EditableOMPoly) graphic);
	return states;

    }

    /**
     * State where points are added to the end of the polyline.
     */
    public void setAddPoint() {
	setState(POLY_ADD_POINT);
    }
    
    /**
     * State where a node is duplicated if you click on it.
     */
    public void setAddNode() {
	setState(POLY_ADD_NODE);
    }

    /**
     * State where a node is deleted if you click on it.
     */
    public void setDeleteNode() {
	setState(POLY_DELETE_NODE);
    }

    public void setSelected() {
	super.setSelected();
	((EditableOMPoly)graphic).enablePolygonEditButtons(true);
    }

    public void setUnselected() {
	super.setUnselected();
	((EditableOMPoly)graphic).enablePolygonEditButtons(false);
    }
}