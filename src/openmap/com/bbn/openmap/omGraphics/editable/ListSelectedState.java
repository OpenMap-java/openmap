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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/ListSelectedState.java,v
// $
// $RCSfile: ListSelectedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;

public class ListSelectedState extends GraphicSelectedState {

    public ListSelectedState(EditableOMGraphic eomg) {
        super(eomg);
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on. If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "ListStateMachine|selected state|mousePressed");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp == null) {
            // List is making a GrabPoint with a distance check, so if
            // we get here, we don't have to do anything.
            if (Debug.debugging("eomg")) {
                float distance = graphic.getGraphic().distance(e.getX(),
                        e.getY());
                Debug.output("ListStateMachine|selected state|mousePressed - click off list, "
                        + distance + " away");
            }
            graphic.fireEvent(EOMGCursors.DEFAULT, "");
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
        Debug.message("eomg", "ListStateMachine|selected state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp == null) {
            Debug.message("eomg", " deactivating with fired event");
            // If the graphic isn't picked, then need to
            // deactivate with a deactivation event.
            graphic.fireEvent(new com.bbn.openmap.omGraphics.event.EOMGEvent());
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
                "ListStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp == null) {
            graphic.fireEvent(EOMGCursors.DEFAULT, "");
        } else {
            graphic.fireEvent(EOMGCursors.EDIT,
                    "Click and Drag to change the graphic.");
        }
        return false;
    }
}

