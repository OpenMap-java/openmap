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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/CircleSelectedState.java,v $
// $RCSfile: CircleSelectedState.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:13 $
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

public class CircleSelectedState extends GraphicSelectedState {

    public CircleSelectedState(EditableOMCircle eomc) {
        super(eomc);
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on.  If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed");
        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to selected
        // mode.
        if (mp == null) {
            if ((graphic.getGraphic().getRenderType() != OMGraphic.RENDERTYPE_LATLON &&
                 graphic.getGraphic().distance(e.getX(), e.getY()) <= 2) ||
                graphic.getGraphic().distanceToEdge(e.getX(), e.getY()) <= 2) {

                if (graphic.getCanGrabGraphic()) {
                    // No point was selected, but the graphic was.  Get ready
                    // to move the graphic.
                    Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed - graphic held");
                    graphic.getStateMachine().setEdit();

                    // This is the only difference in this method from the
                    // GraphicSelectedState.mousePressed method.
                    graphic.fireEvent(EOMGCursors.EDIT, "");
                    ////////

                    graphic.move(e);
                }
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "");
                graphic.redraw(e, true);
            }
        } else {
            // else, if the moving point is set, go to edit mode.  If
            // the mouse is released, we'll consider ourselves
            // unselected agin.
            graphic.getStateMachine().setEdit();
            graphic.fireEvent(EOMGCursors.EDIT, "");
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "CircleStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp == null) {
            if (graphic.getCanGrabGraphic() &&
                graphic.getGraphic().distanceToEdge(e.getX(), e.getY()) < 2) {

                graphic.fireEvent(EOMGCursors.EDIT, "Click and Drag edge to resize.");
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "");
            }

        } else {
            graphic.fireEvent(EOMGCursors.EDIT, "Click and Drag to change the graphic.");
        }
        return false;
    }
}







