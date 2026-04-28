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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/LineSetOffsetState.java,v
// $
// $RCSfile: LineSetOffsetState.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/10 22:27:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMLine;
import com.bbn.openmap.omGraphics.OffsetGrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;

public class LineSetOffsetState extends GraphicSetOffsetState {

    public LineSetOffsetState(EditableOMLine eoml) {
        super(eoml);
    }

    protected void setGrabPoint(MouseEvent e) {
        OffsetGrabPoint ogb = (OffsetGrabPoint) graphic.getGrabPoint(EditableOMLine.OFFSET_POINT_INDEX);
        ogb.set(e.getX(), e.getY());
        ogb.updateOffsets();

        graphic.setMovingPoint(graphic.getGrabPoint(EditableOMLine.OFFSET_POINT_INDEX));
        graphic.redraw(e);
        graphic.fireEvent(EOMGCursors.PUTNODE,
                i18n.get(LineSetOffsetState.class,
                        "Click_to_place_offset_point_for_line.",
                        "Click to place offset point for line."), EOMGEvent.EOMG_UNCHANGED);
    }
}
