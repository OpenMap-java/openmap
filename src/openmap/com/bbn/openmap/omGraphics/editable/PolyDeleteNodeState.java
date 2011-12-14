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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyDeleteNodeState.java,v
// $
// $RCSfile: PolyDeleteNodeState.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/10 22:27:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class PolyDeleteNodeState
        extends State {
    protected EditableOMGraphic graphic;

    public PolyDeleteNodeState(EditableOMPoly eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or anyplace
     * off the graphic is pressed down on. If the end points are clicked on,
     * then we do nothing except set the moving point and go to edit mode.
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|delete node state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to
        // selected
        // mode.
        if (mp != null) {
            int index = ((EditableOMPoly) graphic).whichGrabPoint(mp);
            if (index != EditableOMPoly.OFFSET_POINT_INDEX) {
                ((EditableOMPoly) graphic).deletePoint(index);
                graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNDO);
                graphic.fireEvent(EOMGEvent.EOMG_SELECTED);
            }
        }

        graphic.getStateMachine().setSelected();
        graphic.redraw(e, true);

        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|delete node state|mouseMoved");
        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp != null) { // Only change the cursor over a node
            // if (graphic.getGraphic().distance(e.getX(), e.getY()) <
            // 2)
            // {
            graphic.fireEvent(EOMGCursors.EDIT,
                              i18n.get(PolyDeleteNodeState.class, "Click_a_node_to_delete_it.", "Click a node to delete it."),
                              EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT,
                              i18n.get(PolyDeleteNodeState.class, "Click_a_node_to_delete_it.", "Click a node to delete it."),
                              EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|delete node state|mouseDragged");
        if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
            graphic.fireEvent(EOMGCursors.EDIT, i18n.get(PolyDeleteNodeState.class, "Release_over_a_node_to_delete_it.",
                                                         "Release over a node to delete it."), EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, i18n.get(PolyDeleteNodeState.class, "Release_over_a_node_to_delete_it.",
                                                            "Release over a node to delete it."), EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}
