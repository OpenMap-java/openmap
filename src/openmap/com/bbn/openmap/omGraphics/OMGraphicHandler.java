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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGraphicHandler.java,v $
// $RCSfile: OMGraphicHandler.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Shape;

/**
 * This interface describes an object that manages OMGraphics. It
 * provides a mechanism to filter what OMGraphics are displayed based
 * on some criteria, and can also return graphics based on those
 * criteria.
 * 
 * It's assumed that when a filter is applied to an OMGraphicHandler,
 * that it stays in place until resetFiltering() is called. Calling
 * multiple filters sets a chain.
 */
public interface OMGraphicHandler {

    /**
     * Filters the OMGraphicHandler graphic list so that graphics
     * within the given shape will be visible. Returns an
     * OMGeometryList with those visible shapes. The returned list
     * should not be assumed to be the same OMGraphicList object that
     * is maintained inside the OMGraphicHandler. Same as calling
     * filter(withinThisShape, true).
     * 
     * @param withinThisShape java.awt.Shape object defining a
     *        boundary.
     * @return OMGraphicList containing OMGraphics that are within the
     *         Shape.
     */
    public OMGraphicList filter(Shape withinThisShape);

    /**
     * Filters the OMGraphicHandler graphic list so that graphics
     * inside or outside the given shape will be visible. Returns an
     * OMGraphicList with those visible shapes. The returned list
     * should not be assumed to be the same OMGraphicList object that
     * is maintained inside the OMGraphicHandler.
     * 
     * @param shapeBoundary java.awt.Shape object defining a boundary.
     * @param getInsideBoundary if true, the filter will look for
     *        shapes inside and contacting the boundary. If false, the
     *        filter will look for shapes outside the boundary.
     * @return OMGraphicList containing OMGraphics that are within the
     *         Shape.
     */
    public OMGraphicList filter(Shape shapeBoundary, boolean getInsideBoundary);

    /**
     * Returns true if the OMGraphicHandler can handle SQL statements
     * for filtering.
     */
    public boolean supportsSQL();

    /**
     * Filters the OMGraphicHandler graphic list so that graphics
     * meeting the SQL query statement will be visible. Returns an
     * OMGraphicList with those visible shapes. The returned list
     * should not be assumed to be the same OMGraphicList object that
     * is maintained inside the OMGraphicHandler.
     * 
     * @param SQLQuery a SELECT SQL statement
     * @return OMGraphicList containing OMGraphics that meet the
     *         SELECT statement criteria.
     */
    public OMGraphicList filter(String SQLQuery);

    /**
     * Allows the OMGraphicHandler to receive graphics or take some
     * action on one.
     * 
     * @param graphic the OMGraphic to do the action on.
     * @param action the OMAction describing what to do to the
     *        graphic.
     * @return true if the action was able to be carried out.
     */
    public boolean doAction(OMGraphic graphic, OMAction action);

    /**
     * Return the graphic list currently being used by the
     * OMGraphicHandler. If filters have been applied, then the
     * OMGraphics that have made it through the filter are visible.
     * 
     * @see OMGraphic#isVisible().
     */
    public OMGraphicList getList();

    /**
     * Indicates if the OMGraphicHandler can have its OMGraphicList
     * set.
     */
    public boolean canSetList();

    /**
     * Set the OMGraphicList within this OMGraphicHandler. Works if
     * canSetGraphicList == true.
     */
    public void setList(OMGraphicList omgl);

    /**
     * Remove all filters, and reset all graphics to be visible.
     */
    public void resetFiltering();
}