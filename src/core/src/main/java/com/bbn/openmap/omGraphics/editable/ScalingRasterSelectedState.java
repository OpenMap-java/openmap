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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/ScalingRasterSelectedState.java,v
// $
// $RCSfile: ScalingRasterSelectedState.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/10 22:27:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMScalingRaster;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class ScalingRasterSelectedState extends GraphicSelectedState {

    public ScalingRasterSelectedState(EditableOMScalingRaster eomr) {
        super(eomr);
    }

    /**
     * In this state, we need to change states only if the graphic, or
     * anyplace off the graphic is pressed down on. If the end points
     * are clicked on, then we do nothing except set the moving point
     * and go to edit mode.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg",
                "ScalingRasterStateMachine|selected state|mousePressed");
        // This is added for Rectangles:
        ((EditableOMScalingRaster) graphic).initRectSize();
        return super.mousePressed(e);
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomgdetail",
                "ScalingStateMachine|selected state|mouseMoved");

        GrabPoint mp = graphic.getMovingPoint(e);
        if (mp == null) {
            graphic.fireEvent(EOMGCursors.DEFAULT, "", EOMGEvent.EOMG_UNCHANGED);
        } else {
            graphic.fireEvent(EOMGCursors.EDIT,
                    i18n.get(ScalingRasterSelectedState.class,
                            "Click_and_Drag_to_change_the_graphic.",
                            "Click and Drag to change the graphic."), EOMGEvent.EOMG_UNCHANGED);
        }
        return false;
    }
}