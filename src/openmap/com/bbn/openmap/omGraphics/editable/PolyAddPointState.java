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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyAddPointState.java,v $
// $RCSfile: PolyAddPointState.java,v $
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

public class PolyAddPointState extends State {
    protected EditableOMGraphic graphic;

    public PolyAddPointState(EditableOMPoly eomg) {
	graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on.  If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mouseReleased(MouseEvent e) {
	Debug.message("eomg", "GraphicStateMachine|add point state|mouseReleased");

	if (e.getClickCount() > 1) {
	    ((EditableOMPoly)graphic).evaluateEnclosed();
	    if ((graphic.getStateMachine()).isOffsetNeeded() == true) {
		graphic.getStateMachine().setOffset();
		graphic.getStateMachine().setOffsetNeeded(false);
	    } else {
		graphic.getStateMachine().setSelected();
	    }
//  	    ((EditableOMPoly)graphic).enablePolygonButton(false);
	    graphic.redraw(e, true);
	    return false;
	}

	// If we are in this state, the moving point should be set to
	// the new point, which actually hasn't been placed yet.  So,
	// we need to check the click count.  If it is 1, then we need
	// to set the point, and create a new one and stay in this
	// state.  If it is more than 1, we need to set the point,
	// then change state to the selected state because we are done
	// drawing the poly.
	((EditableOMPoly)graphic).addMovingPoint(e.getX(), e.getY());

	return false;
    }

    public boolean mouseMoved(MouseEvent e) {
	Debug.message("eomgdetail", "PolyStateMachine|add point state|mouseMoved");
	graphic.redraw(e);
	return false;
    }

    public boolean mouseDragged(MouseEvent e) {
	Debug.message("eomgdetail", "PolyStateMachine|add point state|mouseDragged");
	graphic.redraw(e);
	return false;
    }
}







