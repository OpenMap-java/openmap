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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyAddNodeState.java,v $
// $RCSfile: PolyAddNodeState.java,v $
// $Revision: 1.3 $
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

public class PolyAddNodeState extends State {
    protected EditableOMGraphic graphic;

    public PolyAddNodeState(EditableOMPoly eomg) {
        graphic = eomg;
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on.  If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|add node state|mouseReleased");

        GrabPoint mp = graphic.getMovingPoint(e);

        // If the graphic itself was clicked on, then just go to selected
        // mode.
        if (mp != null) {
            int index = ((EditableOMPoly)graphic).whichGrabPoint(mp);
            if (index != EditableOMPoly.OFFSET_POINT_INDEX) {
                ((EditableOMPoly)graphic).addPoint(e.getX() + 5, e.getY() + 5, index + 1);
            }
        }

        graphic.getStateMachine().setSelected();
        graphic.redraw(e, true);
        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add node state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);

        if (mp != null) { // Only change the cursor over a node
//      if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
            graphic.fireEvent(EOMGCursors.EDIT, "Click on a node to add a point.");
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, "Click on a node to add a point.");
        }

//      graphic.redraw(e);
        return false;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add node state|mouseDragged");

        if (graphic.getGraphic().distance(e.getX(), e.getY()) < 2) {
            graphic.fireEvent(EOMGCursors.EDIT, "Release on an node to add a node");
        } else {
            graphic.fireEvent(EOMGCursors.DEFAULT, "Release on an node to add a node");
        }

//      graphic.redraw(e);
        return false;
    }
}







