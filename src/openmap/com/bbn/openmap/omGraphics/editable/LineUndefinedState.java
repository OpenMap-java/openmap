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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/LineUndefinedState.java,v $
// $RCSfile: LineUndefinedState.java,v $
// $Revision: 1.2 $
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

public class LineUndefinedState extends GraphicUndefinedState {

    public LineUndefinedState(EditableOMLine eoml) {
        super(eoml);
    }

    /**
     * In this state, we need to draw a line from scratch.  So, we
     * listen for a mouse down, and set both points there, and then
     * set the mode to line edit.  
     */
    public boolean mousePressed(MouseEvent e){ 
        Debug.message("eoml", "LineStateMachine|undefined state|mousePressed = " + 
                      graphic.getGraphic().getRenderType());
        
        graphic.getGrabPoint(EditableOMLine.STARTING_POINT_INDEX).set(e.getX(), e.getY());
        graphic.getGrabPoint(EditableOMLine.ENDING_POINT_INDEX).set(e.getX(), e.getY());
        graphic.setMovingPoint(graphic.getGrabPoint(EditableOMLine.ENDING_POINT_INDEX));

        if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
            graphic.getStateMachine().setOffsetNeeded(true);
            Debug.message("eoml", "LineStateMachine|undefined state| *offset needed*");
        }
        graphic.getStateMachine().setEdit();
        return getMapMouseListenerResponse();
    }

}
