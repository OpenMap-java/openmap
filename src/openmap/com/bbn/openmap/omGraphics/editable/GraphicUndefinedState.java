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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicUndefinedState.java,v $
// $RCSfile: GraphicUndefinedState.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
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

public class GraphicUndefinedState extends State implements EOMGUndefinedState {

    protected EditableOMGraphic graphic;

    public GraphicUndefinedState(EditableOMGraphic eomg) {
	graphic = eomg;
    }

    /**
     * In this state, we need to draw a graphic from scratch.  So, we
     * listen for a mouse down, and set both points there, and then
     * set the mode to graphic edit.  
     */
    public boolean mousePressed(MouseEvent e){ 
	if (Debug.debugging("eomg")) {
	    Debug.output("GraphicStateMachine|undefined state|mousePressed = " + 
			 graphic.getGraphic().getRenderType());
	}
	
// 	graphic.getGrabPoint(EditableOMGraphic.STARTING_POINT_INDEX).set(e.getX(), e.getY());
//  	graphic.getGrabPoint(EditableOMGraphic.ENDING_POINT_INDEX).set(e.getX(), e.getY());
// 	graphic.setMovingPoint(graphic.getGrabPoint(EditableOMGraphic.ENDING_POINT_INDEX));

	if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
	    graphic.getStateMachine().setOffsetNeeded(true);
	    Debug.message("eomg", "GraphicStateMachine|undefined state| *offset needed*");
	}
	graphic.getStateMachine().setEdit();
	return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
	Debug.message("eomgdetail", "GraphicStateMachine|undefined state|mouseMoved");
	graphic.fireEvent(EOMGCursors.EDIT, "Click and Drag to define graphic.");
	return false;
    }

}
