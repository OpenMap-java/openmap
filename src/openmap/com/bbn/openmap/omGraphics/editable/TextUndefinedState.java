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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/TextUndefinedState.java,v $
// $RCSfile: TextUndefinedState.java,v $
// $Revision: 1.1 $
// $Date: 2003/10/24 15:34:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;

public class TextUndefinedState extends GraphicUndefinedState {

    public TextUndefinedState(EditableOMText eomc) {
	super(eomc);
    }

    public boolean mousePressed(MouseEvent e) { 
	
	GrabPoint gb;
	gb = graphic.getGrabPoint(EditableOMText.CENTER_POINT_INDEX);
	gb.set(e.getX(), e.getY());
	graphic.setMovingPoint(gb);

	if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
 	    graphic.getGrabPoint(EditableOMText.OFFSET_POINT_INDEX).set(e.getX(), e.getY());
	    graphic.getStateMachine().setOffsetNeeded(true);
	}

 	graphic.getStateMachine().setEdit();
	return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
	graphic.fireEvent(EOMGCursors.EDIT, "Click to define graphic.");
	return false;
    }
}
