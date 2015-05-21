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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/SelectionEvent.java,v $
// $RCSfile: SelectionEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;

/**
 * An event that a SelectionProvider gives to a SelectionListener, in
 * order to notify it of OMGraphics that have been selected and
 * deselected.
 */
public class SelectionEvent {

    protected Object source;
    protected OMGraphic graphic;
    protected DrawingToolRequestor requestor;
    protected boolean selected;

    public SelectionEvent(Object source, OMGraphic omg,
            DrawingToolRequestor dtr, boolean selectionStatus) {

        this.source = source;
        graphic = omg;
        requestor = dtr;
        selected = selectionStatus;
    }

    public Object getSource() {
        return source;
    }

    public OMGraphic getOMGraphic() {
        return graphic;
    }

    public DrawingToolRequestor getRequestor() {
        return requestor;
    }

    public boolean isSelected() {
        return selected;
    }

}