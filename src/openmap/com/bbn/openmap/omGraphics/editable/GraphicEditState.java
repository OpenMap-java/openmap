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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicEditState.java,v
// $
// $RCSfile: GraphicEditState.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class GraphicEditState extends State implements EOMGEditState {

    protected EditableOMGraphic graphic;

    public GraphicEditState(EditableOMGraphic eomg) {
        graphic = eomg;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|edit state|mouseDragged");
        if (graphic.getMovingPoint() != null) {
            graphic.redraw(e);
        } else {
            graphic.getStateMachine().setSelected();
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|edit state|mouseReleased");

        if ((graphic.getStateMachine()).isOffsetNeeded() == true) {
            graphic.getStateMachine().setOffset();
            graphic.getStateMachine().setOffsetNeeded(false);
        } else {
            graphic.getStateMachine().setSelected();
            GrabPoint mp = graphic.getMovingPoint();
            if (mp == null && !graphic.getCanGrabGraphic()) {
                graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_SELECTED);
            } else {
                graphic.fireEvent(EOMGCursors.EDIT, "", e, EOMGEvent.EOMG_SELECTED);
            }
        }

        graphic.redraw(e, true);
        graphic.setMovingPoint(null);

        return getMapMouseListenerResponse();
    }
}

