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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicSelectedState.java,v
// $
// $RCSfile: GraphicSelectedState.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.util.Debug;

public class GraphicSelectedState extends State implements EOMGSelectedState {

    protected EditableOMGraphic graphic;

    public GraphicSelectedState(EditableOMGraphic eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on. If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            float distance = graphic.getGraphic().distance(e.getX(), e.getY());
            if (distance <= 2) {
                if (graphic.getCanGrabGraphic()) {

                    // No point was selected, but the graphic was. Get
                    // ready
                    // to move the graphic.
                    Debug.message("eomg",
                            "GraphicStateMachine|selected state|mousePressed - graphic held");
                    graphic.getStateMachine().setEdit();
                    graphic.fireEvent(EOMGCursors.MOVE, "");
                    graphic.move(e);
                } else {
                    Debug.message("eomg",
                            "GraphicStateMachine|selected state|mousePressed - graphic can't be held");
                }
                graphic.fireEvent(EOMGCursors.DEFAULT, "");
            } else {
                Debug.message("eomg",
                        "GraphicStateMachine|selected state|mousePressed - click off graphic, "
                                + distance + " away");
                graphic.fireEvent(EOMGCursors.DEFAULT, "");
                // Preparing for deactivation, why bother
                // repainting...
                //              graphic.redraw(e, true);
            }
        } else {
            // else, if the moving point is set, go to edit mode. If
            // the mouse is released, we'll consider ourselves
            // unselected agin.
            graphic.getStateMachine().setEdit();
            graphic.fireEvent(EOMGCursors.EDIT, "");
        }
        return getMapMouseListenerResponse();
    }

    /**
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg",
                "GraphicStateMachine|selected state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            if (graphic.getGraphic().distance(e.getX(), e.getY()) <= 2) {
                if (graphic.getCanGrabGraphic()) {

                    graphic.fireEvent(EOMGCursors.EDIT, "", e);
                    graphic.redraw(e, true);
                } else {
                    graphic.fireEvent(EOMGCursors.DEFAULT, "", e);
                }
            } else {
                Debug.message("eomg", " deactivating with fired event");
                // If the graphic isn't picked, then need to
                // deactivate with a deactivation event.
                graphic.fireEvent(new com.bbn.openmap.omGraphics.event.EOMGEvent());
            }
        } else {
            // If the moving point was valid, just stay in selected
            // mode.
            graphic.fireEvent(EOMGCursors.EDIT, "", e);
            graphic.redraw(e, true);
        }

        graphic.setMovingPoint(null);
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail",
                "GraphicStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
                graphic.fireEvent(EOMGCursors.EDIT,
                        "Click and Drag to move the graphic.");
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "");
            }
        } else {
            graphic.fireEvent(EOMGCursors.EDIT,
                    "Click and Drag to change the graphic.");
        }
        return false;
    }
}

