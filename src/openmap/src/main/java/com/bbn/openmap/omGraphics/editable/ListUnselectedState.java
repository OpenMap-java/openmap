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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/ListUnselectedState.java,v
// $
// $RCSfile: ListUnselectedState.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/10 22:27:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class ListUnselectedState extends GraphicUnselectedState {

    public ListUnselectedState(EditableOMGraphic eomg) {
        super(eomg);
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * it's grab points are clicked on. If a point is clicked on, then
     * it should become the moving point, and the graphic should be
     * changed to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "ListStateMachine|unselected state|mousePressed");
        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp != null) {
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
        Debug.message("eomg", "ListStateMachine|unselected state|mouseReleased");
        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp != null) {
            graphic.getStateMachine().setSelected();
            graphic.fireEvent(EOMGCursors.EDIT, "", EOMGEvent.EOMG_SELECTED);
            graphic.setMovingPoint(null);
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail",
                "ListStateMachine|unselected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);
        if (mp != null) {
            graphic.fireEvent(EOMGCursors.EDIT,
                    i18n.get(ListUnselectedState.class,
                            "Click_to_select_the_graphic.",
                            "Click to select the graphic."), EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}
