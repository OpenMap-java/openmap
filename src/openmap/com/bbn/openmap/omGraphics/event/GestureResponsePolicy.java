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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/GestureResponsePolicy.java,v $
// $RCSfile: GestureResponsePolicy.java,v $
// $Revision: 1.3 $
// $Date: 2003/09/23 22:46:24 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

public interface GestureResponsePolicy {

    ////// Queries

    public boolean isHighlightable(OMGraphic omgr);

    public boolean isSelectable(OMGraphic omgr);

    public OMGraphicList getSelected();

    ////// Reactions
    public void select(OMGraphicList omgl);

    public void deselect(OMGraphicList omgl);

    public OMGraphicList cut(OMGraphicList omgl);

    public OMGraphicList copy(OMGraphicList omgl);

    public void paste(OMGraphicList omgl);

    /** Fleeting change of appearance. */
    public void highlight(OMGraphic omg);

    public void unhighlight(OMGraphic omg);

    public String getToolTipTextFor(OMGraphic omg);

    public String getInfoText(OMGraphic omg);

    public JPopupMenu modifyPopupMenuFor(OMGraphic omg, JPopupMenu jpm);

}
