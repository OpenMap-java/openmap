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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PointUndefinedState.java,v $
// $RCSfile: PointUndefinedState.java,v $
// $Revision: 1.2 $
// $Date: 2003/11/14 20:50:27 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.editable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.util.Debug;

public class PointUndefinedState extends GraphicUndefinedState {

    public PointUndefinedState(EditableOMPoint eomp) {
	super(eomp);
    }

    /**
     * In this state, we need to draw a rect from scratch.  So, we
     * listen for a mouse down, and set both points there, and then
     * set the mode to rect edit.  
     */
    public boolean mousePressed(MouseEvent e){ 
	Debug.message("eomg", "PointStateMachine|undefined state|mousePressed = " + 
		      graphic.getGraphic().getRenderType());
	
	GrabPoint gb = graphic.getGrabPoint(EditableOMPoint.CENTER_POINT_INDEX);
	gb.set(e.getX(), e.getY());
	graphic.setMovingPoint(gb);

	if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
 	    graphic.getGrabPoint(EditableOMPoint.OFFSET_POINT_INDEX).set(e.getX(), e.getY());
	    graphic.getStateMachine().setOffsetNeeded(true);
	    Debug.message("eomg", "PointStateMachine|undefined state| *offset needed*");
	}

 	graphic.getStateMachine().setEdit();
	return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
	graphic.fireEvent(EOMGCursors.EDIT, "Click to define the point location.");
	return false;
    }
}
