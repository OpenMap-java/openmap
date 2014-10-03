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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyAddNodeState.java,v
// $
// $RCSfile: PolyAddNodeState.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/10 22:27:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class PolyAddNodeState
        extends State {
    protected EditableOMGraphic graphic;

    public PolyAddNodeState(EditableOMPoly eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or anyplace
     * off the graphic is pressed down on. If the end points are clicked on,
     * then we do nothing except set the moving point and go to edit mode.
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|add node state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If a node itself was clicked on, then just go to selected mode.
        if (mp == null) {

            /**
             * We need to find the segment that was clicked on, and then add a
             * node between them. So, index needs to point to the grab point of
             * the first grab point of the segment.
             */
            if (graphic.isMouseEventTouching(e)) {
                OMPoly poly = (OMPoly) graphic.getGraphic();
                double x = e.getX();
                double y = e.getY();
                if (poly.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
                    Point2D llp = graphic.getProjection().inverse(x, y);
                    x = llp.getX();
                    y = llp.getY();
                }

                int index = poly.getIndexOfFirstNodeOfSegIntersect(x, y, 2);
                // int index = ((EditableOMPoly) graphic).whichGrabPoint(mp);

                if (index != -1) {
                    ((EditableOMPoly) graphic).addPoint(e.getX(), e.getY(), index + 1);

                    graphic.fireEvent(EOMGEvent.EOMG_SELECTED);
                }
                graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNDO);
            }
        }

        graphic.getStateMachine().setSelected();
        graphic.redraw(e, true);
        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add node state|mouseMoved");

        if (graphic.isMouseEventTouching(e)) { // Only change the cursor over
                                               // graphic between nodes
            graphic.fireEvent(EOMGCursors.EDIT, i18n.get(PolyAddNodeState.class, "Click_between_nodes_to_add_another_node.",
                                                         "Click between nodes to add another node."), e, EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, i18n.get(PolyAddNodeState.class, "Click_between_nodes_to_add_another_node.",
                                                            "Click between nodes to add another node."), e,
                              EOMGEvent.EOMG_UNCHANGED);
        }

        // graphic.redraw(e);
        return false;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add node state|mouseDragged");

        if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
            graphic.fireEvent(EOMGCursors.EDIT, i18n.get(PolyAddNodeState.class, "Release_between_nodes_to_add_a_node.",
                                                         "Release between nodes to add a node."), e, EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, i18n.get(PolyAddNodeState.class, "Release_between_nodes_to_add_a_node.",
                                                            "Release between nodes to add a node."), e, EOMGEvent.EOMG_UNCHANGED);
        }

        // graphic.redraw(e);
        return false;
    }
}
