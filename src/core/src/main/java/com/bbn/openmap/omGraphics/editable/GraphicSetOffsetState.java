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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicSetOffsetState.java,v
// $
// $RCSfile: GraphicSetOffsetState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

public class GraphicSetOffsetState extends State implements EOMGAuxState {

    protected EditableOMGraphic graphic;

    public GraphicSetOffsetState(EditableOMGraphic eomg) {
        graphic = eomg;
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomg",
                "GraphicStateMachine|set offset state|mouseDragged");
        setGrabPoint(e);
        return getMapMouseListenerResponse();
    }
    
    public boolean mouseMoved(MouseEvent e) {
        graphic.fireEvent(EOMGCursors.PUTNODE,
                i18n.get(CircleSetOffsetState.class,
                        "Click_to_place_offset_point.",
                        "Click to place offset point."), EOMGEvent.EOMG_UNCHANGED);
        return getMapMouseListenerResponse();
    }
    
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|set offset state|mousePressed");
        setGrabPoint(e);
        return getMapMouseListenerResponse();
    }

    protected void setGrabPoint(MouseEvent e) {
        // Each editable omgraphic should handle this.
        
    //      OffsetGrabPoint ogb =
    // (OffsetGrabPoint)graphic.getGrabPoint(EditableOMGraphic.OFFSET_POINT_INDEX);
    //      ogb.set(e.getX(), e.getY());
    //      ogb.updateOffsets();

    //      graphic.setMovingPoint(graphic.getGrabPoint(EditableOMGraphic.OFFSET_POINT_INDEX));

    //      graphic.redraw(e);
    //      graphic.fireEvent(EOMGCursors.PUTNODE, "Click to place offset
    // point for graphic.");
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|edit state|mouseReleased");
        graphic.getStateMachine().setSelected();
        graphic.redraw(e, true);
        graphic.setMovingPoint(null);
        graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_SELECTED);
        return getMapMouseListenerResponse();
    }
}

