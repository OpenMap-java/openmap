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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/TextSetOffsetState.java,v
// $
// $RCSfile: TextSetOffsetState.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/10 22:27:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.omGraphics.OffsetGrabPoint;
import com.bbn.openmap.omGraphics.event.EOMGEvent;

public class TextSetOffsetState extends GraphicSetOffsetState {

    public TextSetOffsetState(EditableOMText eomc) {
        super(eomc);
    }

    protected void setGrabPoint(MouseEvent e) {
        OffsetGrabPoint ogb = (OffsetGrabPoint) graphic.getGrabPoint(EditableOMText.OFFSET_POINT_INDEX);
        ogb.set(e.getX(), e.getY());
        ogb.updateOffsets();

        graphic.setMovingPoint(graphic.getGrabPoint(EditableOMText.OFFSET_POINT_INDEX));
        graphic.redraw(e);
        graphic.fireEvent(EOMGCursors.PUTNODE,
                i18n.get(TextSetOffsetState.class,
                        "Click_to_place_offset_point.",
                        "Click to place offset point."), EOMGEvent.EOMG_UNCHANGED);
    }
}
