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
// $Revision: 1.4 $
// $Date: 2003/10/06 19:28:21 $
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

    /**
     * If applicable, add contents to a popup menu for a location over
     * the map.
     * @return the provided JPopupMenu if no modifications are to be
     * made, null if no popup should be displayed.  You can create a
     * different JPopupMenu if you want and return that instead.
     * Returns null by default.
     */
    public JPopupMenu modifyPopupMenuForMap(JPopupMenu jpm);

    /**
     * If applicable, add contents to a popup menu for a location over
     * an OMGraphic.  
     * @return the provided JPopupMenu if no modifications are to be
     * made.You can create a different JPopupMenu if you want and
     * return that instead. Returns null by default.
     */
    public JPopupMenu modifyPopupMenuFor(OMGraphic omg, JPopupMenu jpm);

}
