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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/OMGraphicHandlerPlugIn.java,v $
// $RCSfile: OMGraphicHandlerPlugIn.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:19 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin;

import java.awt.Component;
import java.awt.Shape;

import com.bbn.openmap.omGraphics.FilterSupport;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * A PlugIn that implements the OMGraphicHandler interface.
 */
public class OMGraphicHandlerPlugIn extends BeanContextAbstractPlugIn implements
        OMGraphicHandler {

    protected FilterSupport filter = new FilterSupport();

    public OMGraphicHandlerPlugIn() {
        super();
    }

    public OMGraphicHandlerPlugIn(Component comp) {
        super(comp);
    }

    /**
     * Don't set to null. This is here to let subclasses put a
     * more/less capable FilterSupport in place.
     */
    public void setFilter(FilterSupport fs) {
        filter = fs;
    }

    /**
     * Get the FilterSupport object that is handling the
     * OMGraphicHandler methods.
     */
    public FilterSupport getFilter() {
        return filter;
    }

    /**
     * The getRectangle call is the main call into the PlugIn module.
     * The module is expected to fill the graphics list with objects
     * that are within the screen parameters passed.
     * 
     * @param p projection of the screen, holding scale, center
     *        coords, height, width.
     */
    public OMGraphicList getRectangle(Projection p) {

        OMGraphicList list = (OMGraphicList) getList();
        list.generate(p);
        return list;

    } //end getRectangle

    // OMGraphicHandler methods, deferred to FilterSupport...

    public OMGraphicList filter(Shape withinThisShape) {
        return filter.filter(withinThisShape);
    }

    /**
     * @see OMGraphicHandler#filter(Shape, boolean).
     */
    public OMGraphicList filter(Shape shapeBoundary, boolean getInsideBoundary) {
        return filter.filter(shapeBoundary, getInsideBoundary);
    }

    /**
     * @see OMGraphicHandler#supportsSQL().
     */
    public boolean supportsSQL() {
        return filter.supportsSQL();
    }

    /**
     */
    public OMGraphicList filter(String SQLQuery) {
        return filter.filter(SQLQuery);
    }

    /**
     */
    public boolean doAction(OMGraphic graphic, OMAction action) {
        return filter.doAction(graphic, action);
    }

    /**
     */
    public OMGraphicList getList() {
        OMGraphicList list = filter.getList();
        if (list == null) {
            list = new OMGraphicList();
            filter.setList(list);
        }
        return list;
    }

    /**
     * Indicates if the OMGraphicHandler can have its OMGraphicList
     * set.
     */
    public boolean canSetList() {
        return filter.canSetList();
    }

    /**
     * Set the OMGraphicList within this OMGraphicHandler. Works if
     * canSetGraphicList == true.
     */
    public void setList(OMGraphicList omgl) {
        filter.setList(omgl);
    }

    /**
     * Remove all filters, and reset all graphics to be visible.
     */
    public void resetFiltering() {
        filter.resetFiltering();
    }

} //end OMGraphicHandlerPlugIn
