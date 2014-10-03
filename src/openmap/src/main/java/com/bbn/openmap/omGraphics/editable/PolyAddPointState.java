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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PolyAddPointState.java,v
// $
// $RCSfile: PolyAddPointState.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class PolyAddPointState
        extends State {
    protected EditableOMGraphic graphic;

    public PolyAddPointState(EditableOMPoly eomg) {
        graphic = eomg;
    }

    /**
     */
    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "PointStateMachine|add point state|mouseReleased");

        if (e.getClickCount() > 1) {
            ((EditableOMPoly) graphic).evaluateEnclosed();
            if ((graphic.getStateMachine()).isOffsetNeeded() == true) {
                graphic.getStateMachine().setOffset();
                graphic.getStateMachine().setOffsetNeeded(false);
            } else {
                graphic.getStateMachine().setSelected();
            }
            // Save state
            graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNDO);
            graphic.redraw(e, true);
            return false;
        }

        // If we are in this state, the moving point should be set to
        // the new point, which actually hasn't been placed yet. So,
        // we need to check the click count. If it is 1, then we need
        // to set the point, and create a new one and stay in this
        // state. If it is more than 1, we need to set the point,
        // then change state to the selected state because we are done
        // drawing the poly.
        ((EditableOMPoly) graphic).addMovingPoint(e.getX(), e.getY());
        graphic.fireEvent(EOMGEvent.EOMG_SELECTED);
        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add point state|mouseMoved");
        graphic.redraw(e);
        return false;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomgdetail", "PolyStateMachine|add point state|mouseDragged");
        graphic.redraw(e);
        return false;
    }
}
