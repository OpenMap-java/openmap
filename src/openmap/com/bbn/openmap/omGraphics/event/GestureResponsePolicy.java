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
// $Revision: 1.5 $
// $Date: 2003/10/08 21:34:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * The GestureResponsePolicy interface describes an object that
 * receives interpreted events from a MapMouseInterpreter.  The
 * interpreter receives the MouseEvents and does the work of deciding
 * what has happened concerning the OMGraphics on an
 * OMGraphicHandlerLayer, and notifies the GestureResponsePolicy what
 * it thinks happened.  The GRP is free to respond as it needs.
 *
 * @see com.bbn.openmap.omGraphics.event.MapMouseInterpreter
 * @see com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer
 */
public interface GestureResponsePolicy {

    /**
     * A query from the MapMouseInterpreter wondering if it should ask
     * any questions about the given OMGraphic concerning mouse
     * movement and mouse dragged gestures.  Highlighting is usually
     * means reactions in anticipation of selection.
     */
    public boolean isHighlightable(OMGraphic omgr);

    /**
     * A query from the MapMouseInterpreter wondering if the OMGraphic
     * is selectable, i.e. able to be selected for modification,
     * deletion or movement.  Selection may happen to an OMGraphic by
     * itself or in a group of other OMGraphics.
     */
    public boolean isSelectable(OMGraphic omgr);

    /**
     * A query to get a list of all the OMGraphics that are current
     * selected.
     */
    public OMGraphicList getSelected();

    /**
     * A notification that the OMGraphics on the list should be
     * considered to be selected.
     */
    public void select(OMGraphicList omgl);

    /**
     * A notification that the OMGraphics on the list should be
     * considered to be deselected.
     */
    public void deselect(OMGraphicList omgl);

    /**
     * A notification that the OMGraphics on the list should be cut
     * (deleted and returned) from the list and deselected.  If the
     * GRP doesn't want to provide that capability, it should return
     * null.
     */
    public OMGraphicList cut(OMGraphicList omgl);

    /**
     * A notification that the OMGraphics on the list should be copied
     * (duplicated and returned) and deselected.  If the GRP doesn't
     * want to provide that capability, it should return null.
     */
    public OMGraphicList copy(OMGraphicList omgl);

    /**
     * A notification that the OMGraphics on the list should be added
     * to the list and selected.
     */
    public void paste(OMGraphicList omgl);

    /** 
     * A notification that the OMGraphic should be highlighted in some
     * way if the layer wants, to give the impression that something
     * would happen to the OMGraphic if it were clicked upon or that a
     * tooltip or information line information applies to this
     * specific OMGraphic.
     */
    public void highlight(OMGraphic omg);

    /**
     * A notification that the OMGraphic is no longer needed to be
     * highlighted and that its appearance can go back to normal.
     */
    public void unhighlight(OMGraphic omg);

    /**
     * A request for a string to be provided to use as a tool tip for
     * an OMGraphic.  If a tool tip should not be displayed, null
     * should be returned.
     */
    public String getToolTipTextFor(OMGraphic omg);

    /**
     * A request for a string to be provided to use in the information
     * line of the InformationDelegator, for instance.  If no
     * information should be presented on behalf of this OMGraphic,
     * null should be returned.
     */
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
