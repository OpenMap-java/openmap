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
// $Revision: 1.7 $
// $Date: 2005/08/10 22:27:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class GraphicSelectedState
        extends State
        implements EOMGSelectedState {

    protected EditableOMGraphic graphic;

    public GraphicSelectedState(EditableOMGraphic eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or anyplace
     * off the graphic is pressed down on. If the end points are clicked on,
     * then we do nothing except set the moving point and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            if (graphic.isMouseEventTouching(e)) {
                if (graphic.getCanGrabGraphic()) {

                    // No point was selected, but the graphic was. Get
                    // ready
                    // to move the graphic.
                    Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed - graphic held");
                    graphic.getStateMachine().setEdit();
                    graphic.fireEvent(EOMGCursors.MOVE, "", EOMGEvent.EOMG_EDIT);
                    graphic.move(e);
                } else {
                    Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed - graphic can't be held");
                }
                graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
            } else {
                Debug.message("eomg", "GraphicStateMachine|selected state|mousePressed - click off graphic");
                graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
                // Preparing for deactivation, why bother
                // repainting...
                // graphic.redraw(e, true);
            }
        } else {
            // else, if the moving point is set, go to edit mode. If
            // the mouse is released, we'll consider ourselves
            // unselected again.
            graphic.getStateMachine().setEdit();
            graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_EDIT);
        }
        return getMapMouseListenerResponse();
    }

    /**
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|selected state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just stay in
        // selected mode.
        if (mp == null) {
            if (graphic.isMouseEventTouching(e) || SwingUtilities.isRightMouseButton(e)) {
                if (graphic.getCanGrabGraphic()) {

                    graphic.fireEvent(EOMGCursors.EDIT, "", e, EOMGEvent.EOMG_UNCHANGED);
                    graphic.redraw(e, true);
                } else {
                    graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNCHANGED);
                }
            } else {
                if (graphic.isPopupIsUp()) {
                    graphic.setPopupIsUp(false);
                } else {
                    Debug.message("eomg", " deactivating with fired event");
                    // If the graphic isn't picked, then need to
                    // deactivate with a deactivation event.
                    graphic.fireEvent(new EOMGEvent());
                }
            }
        } else {
            // If the moving point was valid, just stay in selected
            // mode.
            graphic.fireEvent(EOMGCursors.EDIT, "", e, EOMGEvent.EOMG_UNCHANGED);
            graphic.redraw(e, true);
        }

        graphic.setMovingPoint(null);
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "GraphicStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp == null) {
            if (graphic.isMouseEventTouching(e)) {
                graphic.fireEvent(EOMGCursors.EDIT, i18n.get(GraphicSelectedState.class, "Click_and_Drag_to_move_the_graphic.",
                                                             "Click and Drag to move the graphic."), EOMGEvent.EOMG_UNCHANGED);
            } else {
                graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
            }
        } else {
            graphic.fireEvent(EOMGCursors.EDIT, i18n.get(GraphicSelectedState.class, "Click_and_Drag_to_change_the_graphic.",
                                                         "Click and Drag to change the graphic."), EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}
