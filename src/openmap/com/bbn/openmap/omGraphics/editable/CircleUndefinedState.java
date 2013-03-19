// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/CircleUndefinedState.java,v
// $
// $RCSfile: CircleUndefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMCircle;
import com.bbn.openmap.omGraphics.EditableOMLine;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class CircleUndefinedState extends ClckOrDrgUndefinedState {

    public CircleUndefinedState(EditableOMCircle eomc) {
        super(eomc);

    }

    /**
     * In this state, we need to draw a circle from scratch. So, we listen for a
     * mouse down, and set both points there, and then set the mode to circle
     * edit. This method is overridden because of weirdness with drawing
     * circles and that EditableOMCircle/CircleStateMachine.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomc", "CircleStateMachine|undefined state|mousePressed = "
                + graphic.getGraphic().getRenderType());
        if (point1 == null) {
            if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
                indexOfFirstPoint = EditableOMCircle.CENTER_POINT_INDEX;
                indexOfSecondPoint = EditableOMCircle.RADIUS_POINT_INDEX;
            } else if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
                indexOfFirstPoint = EditableOMCircle.OFFSET_POINT_INDEX;
                indexOfSecondPoint = EditableOMCircle.SE_POINT_INDEX;
            } else {
                indexOfFirstPoint = EditableOMCircle.CENTER_POINT_INDEX;
                indexOfSecondPoint = EditableOMCircle.SE_POINT_INDEX;
            }
        }
        return super.mousePressed(e);
    }
}