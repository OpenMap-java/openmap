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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/RectSetOffsetState.java,v $
// $RCSfile: RectSetOffsetState.java,v $
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

public class RectSetOffsetState extends GraphicSetOffsetState {

    public RectSetOffsetState(EditableOMRect eomc) {
	super(eomc);
    }

    protected void setGrabPoint(MouseEvent e) {
	OffsetGrabPoint ogb = (OffsetGrabPoint)graphic.getGrabPoint(EditableOMRect.OFFSET_POINT_INDEX);
	ogb.set(e.getX(), e.getY());
	ogb.updateOffsets();

	graphic.setMovingPoint(graphic.getGrabPoint(EditableOMRect.OFFSET_POINT_INDEX));
	graphic.redraw(e);
	graphic.fireEvent(EOMGCursors.PUTNODE, "Click to place offset point.");
    }
}










