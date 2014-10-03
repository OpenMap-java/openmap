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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicUnselectedState.java,v
// $
// $RCSfile: GraphicUnselectedState.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/10 22:27:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class GraphicUnselectedState extends State implements EOMGDefinedState {

    protected EditableOMGraphic graphic;

    public GraphicUnselectedState(EditableOMGraphic eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or it's grab
     * points are clicked on. If a point is clicked on, then it should become
     * the moving point, and the graphic should be changed to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg",
                "GraphicStateMachine|unselected state|mousePressed");
        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            if (graphic.isMouseEventTouching(e)) {
                if (graphic.getCanGrabGraphic()) {
                    // OK, so a grab point has not been selected, but
                    // the graphic has been, and the button is down.
                    // We should be moving the end grab points of the
                    // graphic. If the graphic is not a
                    // RENDERTYPE_OFFSET graphic, then the
                    // OffsetGrabPoint is available to use for this.
                    // If the graphic is RENDERTYPE_OFFSET, then we
                    // need to create a OffsetGrabPoint to use
                    // temporarily to move just the end points.
                    Debug.message("eomg",
                            "GraphicStateMachine|unselected state|mousePressed - graphic held");
                    graphic.getStateMachine().setEdit();
                    graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_EDIT);
                    // Prepare the graphic to move
                    graphic.move(e);
                } else {
                    graphic.getStateMachine().setSelected();
                    graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_SELECTED);
                }
                // Clean up the map and redraw.
                graphic.redraw(e, true);
            }
        } else {
            // Else, set the moving point, and go to edit mode. If
            // the mouse is released, we'll consider ourselves
            // unselected again.
            graphic.getStateMachine().setEdit();
            graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_EDIT);
            graphic.redraw(e, true);
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg",
                "GraphicStateMachine|unselected state|mouseReleased");
        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            if (graphic.isMouseEventTouching(e)) {
                graphic.getStateMachine().setSelected();
                graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_SELECTED);
            } else {
                graphic.setMovingPoint(new GrabPoint(e.getX(), e.getY()));
                // OK, we're done, or at a crossroad. Give the
                // listeners
                // the MouseEvent so they can determine what to do, to
                // end, or provide options...
                graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNCHANGED);
                graphic.setMovingPoint(null);
            }
            graphic.redraw(e, true);
        } else {
            graphic.setMovingPoint(null);
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail",
                "GraphicStateMachine|unselected state|mouseMoved");

        if (graphic.isMouseEventTouching(e)) {
            graphic.fireEvent(EOMGCursors.EDIT,
                    i18n.get(GraphicUnselectedState.class,
                            "Click_to_select_the_graphic.",
                            "Click to select the graphic."), EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}
