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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyUndefinedState.java,v
// $
// $RCSfile: PolyUndefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class PolyUndefinedState extends GraphicUndefinedState {

    public PolyUndefinedState(EditableOMPoly eomp) {
        super(eomp);
    }

    int initX;
    int initY;

    /**
     * In this state, we need to draw a poly from scratch. So, we
     * listen for a mouse down, and set both points there, and then
     * set the mode to poly edit.
     */
    public boolean mousePressed(MouseEvent e) {
        if (Debug.debugging("eomg")) {
            Debug.output("PolyStateMachine|undefined state|mousePressed = "
                    + graphic.getGraphic().getRenderType());
        }

        // Need to set these up for the polygon
        if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
            graphic.getStateMachine().setOffsetNeeded(true);
            Debug.message("eoml",
                    "PolyStateMachine|undefined state| *offset needed*");
        }

        initX = e.getX();
        initY = e.getY();

        ((EditableOMPoly) graphic).addPoint(initX, initY);
        graphic.fireEvent(EOMGEvent.EOMG_SELECTED);
        return getMapMouseListenerResponse();
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on. If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg",
                "GraphicStateMachine|undefined state|mouseReleased");

        ((EditableOMPoly) graphic).addMovingPoint(e.getX(), e.getY());
        ((PolyStateMachine) graphic.getStateMachine()).setAddPoint();
        graphic.fireEvent(EOMGEvent.EOMG_AUX);
        graphic.redraw(e);
        return false;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomgdetail",
                "PolyStateMachine|undefined state|mouseDragged");

        // Set another point if the mouse was dragged a little
        // before releasing. Assume that the motion can be a click
        // and mouseMove, or a press and mouseDrag. Either way, we
        // want the number of nodes to be the right one.
        if ((Math.abs(e.getX() - initX) > 2)
                || (Math.abs(e.getY() - initY) > 2)) {

            ((EditableOMPoly) graphic).addMovingPoint(e.getX(), e.getY());
            ((PolyStateMachine) graphic.getStateMachine()).setAddPoint();
        }

        graphic.redraw(e);
        return false;
    }
}