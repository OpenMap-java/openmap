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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicUnselectedState.java,v $
// $RCSfile: GraphicUnselectedState.java,v $
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

public class GraphicUnselectedState extends State implements EOMGDefinedState {

    protected EditableOMGraphic graphic;

    public GraphicUnselectedState(EditableOMGraphic eomg) {
	graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * it's grab points are clicked on. If a point is clicked on, then
     * it should become the moving point, and the graphic should be
     * changed to edit mode.
     */
    public boolean mousePressed(MouseEvent e){ 
	Debug.message("eomg", "GraphicStateMachine|unselected state|mousePressed");
	GrabPoint mp = graphic.getMovingPoint(e);

	// If the graphic itself was clicked on, then just go to selected
	// mode.
	if (mp == null) {
	    if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
		if (graphic.getCanGrabGraphic()) {
		    //  OK, so a grab point has not been selected, but
		    //  the graphic has been, and the button is down.
		    //  We should be moving the end grab points of the
		    //  graphic.  If the graphic is not a
		    //  RENDERTYPE_OFFSET graphic, then the
		    //  OffsetGrabPoint is available to use for this.
		    //  If the graphic is RENDERTYPE_OFFSET, then we
		    //  need to create a OffsetGrabPoint to use
		    //  temporarily to move just the end points.
		    Debug.message("eomg", "GraphicStateMachine|unselected state|mousePressed - graphic held");
		    graphic.getStateMachine().setEdit();
		    graphic.fireEvent(EOMGCursors.EDIT, "");
		    // Prepare the graphic to move
		    graphic.move(e);
		} else {
		    graphic.getStateMachine().setSelected();
		    graphic.fireEvent(EOMGCursors.DEFAULT, "");
		}
		// Clean up the map and redraw.
		graphic.redraw(e, true);
	    }
	} else {
	    // Else, set the moving point, and go to edit mode.  If
	    // the mouse is released, we'll consider ourselves
	    // unselected again.
	    graphic.getStateMachine().setEdit();
	    graphic.fireEvent(EOMGCursors.EDIT, "");
	    graphic.redraw(e, true);
	}
	return getMapMouseListenerResponse();
    }

    public boolean mouseReleased(MouseEvent e) {
	Debug.message("eomg", "GraphicStateMachine|unselected state|mouseReleased");
	GrabPoint mp = graphic.getMovingPoint(e);

	// If the graphic itself was clicked on, then just go to selected
	// mode.
	if (mp == null) {
	    if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
		graphic.getStateMachine().setSelected();
		graphic.fireEvent(EOMGCursors.EDIT, "");
	    } else {
		graphic.setMovingPoint(new GrabPoint(e.getX(), e.getY()));
		// OK, we're done, or at a crossroad.  Give the listeners 
		// the MouseEvent so they can determine what to do, to
		// end, or provide options...
		graphic.fireEvent(EOMGCursors.DEFAULT, "", e);
		graphic.setMovingPoint(null);
	    }
	    graphic.redraw(e, true);
	} else {
	    graphic.setMovingPoint(null);
	}
	return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
	Debug.message("eomgdetail", "GraphicStateMachine|unselected state|mouseMoved");

	if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
	    graphic.fireEvent(EOMGCursors.EDIT, "Click to select the graphic.");
	} else {
	    graphic.fireEvent(EOMGCursors.DEFAULT, "");
	}
	return false;
    }
}










