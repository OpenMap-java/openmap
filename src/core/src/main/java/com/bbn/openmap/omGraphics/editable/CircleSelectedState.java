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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/CircleSelectedState.java,v
// $
// $RCSfile: CircleSelectedState.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/10 22:27:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMCircle;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class CircleSelectedState
        extends GraphicSelectedState {

    public CircleSelectedState(EditableOMCircle eomc) {
        super(eomc);
    }

    /**
     * In this state, we need to change states only if the graphic, or anyplace
     * off the graphic is pressed down on. If the end points are clicked on,
     * then we do nothing except set the moving point and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed");
        GrabPoint mp = graphic.getMovingPoint(e);

        int renderType = graphic.getGraphic().getRenderType();

        // If the latlon circle is so small that all you can see is the center
        // grab point, assume that we're grabbing the edge. We can do this by
        // nulling out the moving point, if it's the same as the center grab
        // point.
        if (renderType == OMGraphic.RENDERTYPE_LATLON && mp == graphic.getGrabPoints()[EditableOMCircle.CENTER_POINT_INDEX]
                && graphic.isMouseEventTouchingTheEdge(e)) {
            mp = null;
        }

        // If no grab point was selected, need to check if the graphic itself
        // was clicked on. If so, then just go to selected mode.
        if (mp == null) {
            if ((renderType != OMGraphic.RENDERTYPE_LATLON && graphic.isMouseEventTouching(e))
                    || graphic.isMouseEventTouchingTheEdge(e)) {

                if (graphic.getCanGrabGraphic()) {
                    // No point was selected, but the graphic was. Get
                    // ready
                    // to move the graphic.
                    Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed - graphic held");
                    graphic.getStateMachine().setEdit();

                    // This is the only difference in this method from
                    // the
                    // GraphicSelectedState.mousePressed method.
                    graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_EDIT);
                    // //////

                    graphic.move(e);
                }
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
                graphic.redraw(e, true);
            }
        } else {
            // else, if the moving point is set, go to edit mode. If
            // the mouse is released, we'll consider ourselves
            // unselected agin.
            graphic.getStateMachine().setEdit();
            graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_EDIT);
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "CircleStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp == null) {
            if (graphic.getCanGrabGraphic() && graphic.isMouseEventTouchingTheEdge(e)) {

                graphic.fireEvent(EOMGCursors.EDIT, i18n.get(CircleSelectedState.class, "Click_and_Drag_edge_to_resize.",
                                                             "Click and Drag edge to resize."), EOMGEvent.EOMG_UNCHANGED);
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
            }

        } else {
            graphic.fireEvent(EOMGCursors.EDIT, i18n.get(CircleSelectedState.class, "Click_and_Drag_to_change_the_graphic.",
                                                         "Click and Drag to change the graphic."), EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}
