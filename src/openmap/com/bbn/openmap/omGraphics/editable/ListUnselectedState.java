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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/ListUnselectedState.java,v $
// $RCSfile: ListUnselectedState.java,v $
// $Revision: 1.1 $
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

public class ListUnselectedState extends GraphicUnselectedState {

    public ListUnselectedState(EditableOMGraphic eomg) {
	super(eomg);
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * it's grab points are clicked on. If a point is clicked on, then
     * it should become the moving point, and the graphic should be
     * changed to edit mode.
     */
    public boolean mousePressed(MouseEvent e){ 
	Debug.message("eomg", "ListStateMachine|unselected state|mousePressed");
	GrabPoint mp = graphic.getMovingPoint(e);

	if (mp != null) {
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
	Debug.message("eomg", "ListStateMachine|unselected state|mouseReleased");
	GrabPoint mp = graphic.getMovingPoint(e);

	if (mp != null) {
	    graphic.getStateMachine().setSelected();
	    graphic.fireEvent(EOMGCursors.EDIT, "");
	    graphic.setMovingPoint(null);
	}
	return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
	Debug.message("eomgdetail", "ListStateMachine|unselected state|mouseMoved");

	GrabPoint mp = graphic.getMovingPoint(e);
	if (mp != null) {
	    graphic.fireEvent(EOMGCursors.EDIT, "Click to select the graphic.");
	} else {
	    graphic.fireEvent(EOMGCursors.DEFAULT, "");
	}
	return false;
    }
}










