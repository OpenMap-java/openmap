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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/OMGraphicHandlerPlugIn.java,v $
// $RCSfile: OMGraphicHandlerPlugIn.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A PlugIn that implements the OMGraphicHandler interface.
 */
public class OMGraphicHandlerPlugIn extends BeanContextAbstractPlugIn
    implements OMGraphicHandler {

    protected OMGraphicList list = null;

    protected FilterSupport filter = new FilterSupport();
    
    public OMGraphicHandlerPlugIn() {
        super();
    }

    public OMGraphicHandlerPlugIn(Component comp) {
        super(comp);
    }

    /**
     * Don't set to null.  This is here to let subclasses put a
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
     * coords, height, width.
     */
    public OMGraphicList getRectangle(Projection p) {

        OMGraphicList list = (OMGraphicList)getList();
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
    public OMGraphicList filter(Shape shapeBoundary, 
                                 boolean getInsideBoundary) {
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
     * Indicates if the OMGraphicHandler can have its OMGraphicList set.
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
