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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/GestureResponsePolicy.java,v $
// $RCSfile: GestureResponsePolicy.java,v $
// $Revision: 1.10 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.awt.Component;
import java.util.List;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * The GestureResponsePolicy interface describes an object that
 * receives interpreted events from a MapMouseInterpreter. The
 * interpreter receives the MouseEvents and does the work of deciding
 * what has happened concerning the OMGraphics on an
 * OMGraphicHandlerLayer, and notifies the GestureResponsePolicy what
 * it thinks happened. The GRP is free to respond as it needs.
 * 
 * @see com.bbn.openmap.omGraphics.event.MapMouseInterpreter
 * @see com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer
 */
public interface GestureResponsePolicy {

    /**
     * A query from the MapMouseInterpreter wondering if it should ask
     * any questions about the given OMGraphic concerning mouse
     * movement and mouse dragged gestures. Highlighting is usually
     * means reactions in anticipation of selection.
     */
    public boolean isHighlightable(OMGraphic omgr);

    /**
     * A query from the MapMouseInterpreter wondering if the OMGraphic
     * is selectable. Returns true if the OMGraphic is able to be
     * selected for modification, deletion or movement. Selection may
     * happen to an OMGraphic by itself or in a group of other
     * OMGraphics.
     */
    public boolean isSelectable(OMGraphic omgr);

    /**
     * A query from the MapMouseInterpreter wondering if the
     * GestureResponsePolicy wants events pertaining to mouse
     * movements over the map that are not over an OMGraphic. If the
     * GestureResponsePolicy responds true, then the mouseOver and
     * leftClick methods will be called on the GestureResponsePolicy
     * by the interpreter. There is no rightClick method that is
     * called, because a right click will always cause a
     * getItemsForMapMenu() method to be called.
     */
    public boolean receivesMapEvents();

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
     * (deleted and returned) from the list and deselected. If the GRP
     * doesn't want to provide that capability, it should return null.
     */
    public OMGraphicList cut(OMGraphicList omgl);

    /**
     * A notification that the OMGraphics on the list should be copied
     * (duplicated and returned) and deselected. If the GRP doesn't
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
     * an OMGraphic. If a tool tip should not be displayed, null
     * should be returned.
     */
    public String getToolTipTextFor(OMGraphic omg);

    /**
     * A request for a string to be provided to use in the information
     * line of the InformationDelegator, for instance. If no
     * information should be presented on behalf of this OMGraphic,
     * null should be returned.
     */
    public String getInfoText(OMGraphic omg);

    /**
     * Return a JMenu with contents applicable to a popup menu for a
     * location over the map. The popup doesn't concern any
     * OMGraphics, and should be presented for a click on the map
     * background.
     * 
     * @param mme a MapMouseEvent describing the location over where
     *        the menu items should apply, in case different options
     *        are appropriate for different places.
     * @return a List containing java.awt.Component options over the
     *         provided place on the map. Return null or empty List if
     *         no input required.
     */
    public List<Component> getItemsForMapMenu(MapMouseEvent mme);

    /**
     * Return a java.util.List containing input for a JMenu with
     * contents applicable to a popup menu for a location over an
     * OMGraphic.
     * 
     * @return a List containing java.awt.Component options for the
     *         given OMGraphic. Return null or empty list if there are
     *         no options.
     */
    public List<Component> getItemsForOMGraphicMenu(OMGraphic omg);

    /**
     * A notification that the mouse cursor has been moved over the
     * map, not over any of the OMGraphics on the
     * GestureResponsePolicy. This only gets called if the response to
     * receivesMapEvents is true.
     * 
     * @param mme MapMouseEvent describing the location of the mouse.
     * @return true of this information is to be considered consumed
     *         and should not be passed to anybody else.
     */
    public boolean mouseOver(MapMouseEvent mme);

    /**
     * A notification that the mouse has been clicked with the left
     * mouse button on the map, and not on any of the OMGraphics. This
     * only gets called if the response to receivesMapEvents is true.
     * Right clicks on the map are always reported to the
     * getItemsForMapMenu method.
     * 
     * @param mme MapMouseEvent describing the location of the mouse.
     * @return true of this information is to be considered consumed
     *         and should not be passed to anybody else.
     */
    public boolean leftClick(MapMouseEvent mme);

}