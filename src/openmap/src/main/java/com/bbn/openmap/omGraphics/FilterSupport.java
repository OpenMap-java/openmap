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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/FilterSupport.java,v $
// $RCSfile: FilterSupport.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Shape;
import java.awt.geom.Area;
import java.io.Serializable;

import com.bbn.openmap.util.Debug;

/**
 * This class provides support for implementing the OMGraphicHandler
 * interface. If you already calculate an OMGraphicList, you can use
 * this class to apply filtering to it. The graphics on the list you
 * provide it will be made visible or not depending on whether they
 * meet the filter criteria.
 * <p>
 * 
 * The visibility of the graphics is affected when a filter is
 * applied, and visibility is used as the test if whether a graphic is
 * added to a returned list. Use resetFiltering() to turn visibility
 * back on for all the OMGraphics. If a graphic is not visible when a
 * filter is applied, then the filter test will automatically fail.
 */
public class FilterSupport implements OMGraphicHandler, Serializable {

    /**
     * The source graphic list.
     */
    protected OMGraphicList list = null;

    /**
     * A flag to use the Area.intersect(Area) test, which may be a
     * performance hit.
     */
    protected boolean precise = true;

    protected boolean DEBUG = Debug.debugging("list");

    public FilterSupport() {}

    public FilterSupport(OMGraphicList omgl) {
        setList(omgl);
    }

    /**
     * Filters the OMGraphicHandler graphic list so that graphics
     * within the given shape will be visible. Returns an
     * OMGraphicList with those visible shapes. The returned list
     * should not be assumed to be the same OMGraphicList object that
     * is maintained inside the OMGraphicHandler. Same as calling
     * filter(withinThisShape, true).
     * 
     * @param withinThisShape java.awt.Shape object defining a
     *        boundary.
     * @return OMGraphicList containing OMGraphics that are within the
     *         Shape.
     */
    public OMGraphicList filter(Shape withinThisShape) {
        return filter(withinThisShape, true);
    }

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
    public OMGraphicList filter(Shape shapeBoundary, boolean getInsideBoundary) {
        Area area = null;
        if (shapeBoundary != null) {
            area = new Area(shapeBoundary);
        }

        if (Debug.debugging("filtersupportdetail")) {
            Debug.output(getList().getDescription());
        }

        return filterList(getList(), area, getInsideBoundary);
    }

    /**
     * Method that provides a recursive mechanism to go through
     * OMGraphicsLists to filter out areas, inside or outside another.
     */
    protected OMGraphicList filterList(OMGraphicList omgl, Area area,
                                       boolean getInsideArea) {
        OMGraphicList ret = new OMGraphicList();
        boolean DEBUG_DETAIL = Debug.debugging("filtersupportdetail");
        boolean DEBUG = Debug.debugging("filtersupport") || DEBUG_DETAIL;

        if (DEBUG) {
            Debug.output("FilterSupport.filterList");
        }

        int count = 0; // for debugging

        if (area != null && omgl != null) { // just checking
            
            for (OMGraphic omg : omgl) {

                if (DEBUG) {
                    Debug.output("FilterSupport.filterList evaluating "
                            + (count++) + " OMGraphic, " + omg);
                }

                boolean outsideFilter = true;

                // If not visible, automatically fails...
                if (!omg.isVisible()) {
                    if (DEBUG) {
                        Debug.output("   OMGraphic not visible, ignoring");
                    }
                    continue;
                }

                if (omg instanceof OMGraphicList) {
                    if (omg == omgl) {
                        Debug.output("   OMGraphic is parent list (points to itself), ignoring...");
                        continue;
                    }

                    if (DEBUG) {
                        Debug.output("  (filterList recursiving handing OMGraphicList)");
                    }

                    OMGraphicList subList = filterList((OMGraphicList) omg,
                            area,
                            getInsideArea);

                    if (!subList.isEmpty()) {
                        if (DEBUG) {
                            Debug.output("  +++ OMGraphicList's contents ("
                                    + subList.size()
                                    + ") pass filter, adding...");
                        }

                        if (((OMGraphicList)omg).isVague()) {
                            passedFilter(omg);
                            omg.setVisible(true);
                            ret.add(omg);
                        } else {
                            passedFilter(subList);
                            ret.add(subList);
                        }
                    } else {
                        if (DEBUG) {
                            Debug.output("  --- OMGraphicList's contents fail filter, ignoring...");
                        }

                        failedFilter(omg);
                    }
                    continue;
                } else {
                    Shape omgShape = omg.getShape();
                    if (omgShape != null) {
                        if (omgShape.getBounds2D().getWidth() == 0 && omgShape.getBounds2D().getHeight() == 0) {
                            if (area.contains(omgShape.getBounds2D().getX(), omgShape.getBounds2D().getY())) {
                                if (DEBUG_DETAIL) {
                                    Debug.output("   +++ omg contains position");
                                }

                                outsideFilter = false;
                            }
                        }
                        else if (area.intersects(omgShape.getBounds2D())) {
                            if (DEBUG_DETAIL) {
                                Debug.output("   +++ omg intersects bounds");
                            }

                            // The area.interects() method above is a
                            // general case. If you care about
                            // preciseness, set the precise flag.
                            // Depending on the performance cost, we might
                            // want to make it permanent.

                            if (precise) {
                                Area omgArea = new Area(omgShape);
                                if (!omgArea.isSingular()) {
                                    Area clone = (Area) area.clone();
                                    clone.intersect(omgArea);
                                    if (!clone.isEmpty()) {
                                        outsideFilter = false;
                                    }
                                } else {
                                    outsideFilter = false;
                                }
                            } else {
                                outsideFilter = false;
                            }
                        }
                    }

                    // decide what to do depending on filteredOut and
                    // getInsideArea
                    if ((outsideFilter && !getInsideArea)
                            || (!outsideFilter && getInsideArea)) {

                        if (DEBUG) {
                            Debug.output("   +++ OMGraphic passes filter, adding...");
                        }

                        passedFilter(omg);
                        ret.add(omg);
                    } else {
                        if (DEBUG) {
                            Debug.output("   --- OMGraphic fails filter, hiding...");
                        }

                        failedFilter(omg);
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Returns true if the OMGraphicHandler can handle SQL statements
     * for filtering.
     */
    public boolean supportsSQL() {
        return false;
    }

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
    public OMGraphicList filter(String SQLQuery) {
        return new OMGraphicList();
    }

    /**
     * Allows the OMGraphicHandler to receive graphics or take some
     * action on one.
     * 
     * @param graphic the OMGraphic to do the action on.
     * @param action the OMAction describing what to do to the
     *        graphic.
     * @return true if the action was able to be carried out.
     */
    public boolean doAction(OMGraphic graphic, OMAction action) {
        OMGraphicList list = getList();
        if (list != null) {
            list.doAction(graphic, action);
        }
        return true; // we can handle it.
    }

    /**
     * Return the graphic list currently being used by the
     * OMGraphicHandler. If filters have been applied, then the
     * OMGraphics that have made it through the filter are visible.
     * List may be null, if it hasn't been set.
     * 
     * @see OMGraphic#isVisible().
     */
    public synchronized OMGraphicList getList() {
        if (DEBUG) {
            Debug.output("FilterSupport.getList() with "
                    + (list != null ? list.size() + " graphics." : "null list."));
        }
        return list;
    }

    /**
     * Indicates if the OMGraphicHandler can have its OMGraphicList
     * set.
     */
    public boolean canSetList() {
        return true;
    }

    /**
     * Set the OMGraphicList within this OMGraphicHandler. Works if
     * canSetGraphicList == true.
     */
    public synchronized void setList(OMGraphicList omgl) {
        if (DEBUG) {
            Debug.output("FilterSupport.setList() with "
                    + (omgl != null ? omgl.size() + " graphics." : "null list."));
        }
        list = omgl;
    }

    /**
     * Remove all filters, and reset all graphics to be visible.
     */
    public void resetFiltering() {
        OMGraphicList list = getList();
        if (list != null)
            list.setVisible(true);
    }

    /**
     * Method called when FilterSupport finds an OMGraphic that fails
     * the filter test. The OMGraphic is not being added to a list
     * that is being returned for passing OMGraphics in another
     * method, this call-out is an opportunity to make settings on
     * OMGraphics that pass the filter. By default, the visibility of
     * the OMGraphic is set to false.
     */
    protected void failedFilter(OMGraphic omg) {
        omg.setVisible(false);
    }

    /**
     * Method called when FilterSupport finds an OMGraphic that passes
     * the filter test. The OMGraphic is already being added to a list
     * that is being returned in another method, this call-out is an
     * opportunity to make settings on OMGraphics that pass the
     * filter.
     */
    protected void passedFilter(OMGraphic omg) {
    // NO-OP, by default
    }
}