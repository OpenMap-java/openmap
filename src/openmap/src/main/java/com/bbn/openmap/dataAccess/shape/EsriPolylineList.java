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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolylineList.java,v $
// $RCSfile: EsriPolylineList.java,v $
// $Revision: 1.10 $
// $Date: 2007/01/30 18:39:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.util.Iterator;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.util.Debug;

/**
 * An EsriGraphicList ensures that only EsriPolygons are added to its list.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public class EsriPolylineList extends EsriGraphicList {

    /**
     * Over-ride the add( ) method to trap for inconsistent shape geometry. If
     * you are adding a OMGraphic that is not a list, make sure this list is a
     * sub-list containing multiple geometry parts. Only add another list to a
     * top level EsriGraphicList.
     * 
     * @param shape the non-null OMGraphic to add
     */
    public boolean add(OMGraphic shape) {
        boolean ret = false;
        try {
            if (typeMatches(shape)) {
                ret = graphics.add(shape);
                addExtents(((EsriGraphic) shape).getExtents());
            } else if (shape instanceof OMPoly) {
                EsriPolyline eg = convert((OMPoly) shape);
                if (typeMatches(eg)) {
                    ret = graphics.add(eg);
                    addExtents(eg.getExtents());
                }
            } else if (shape instanceof OMLine) {
                OMPoly omp = EsriPolylineList.convert((OMLine) shape);
                if (omp != null) {
                    EsriPolyline eg = convert(omp);
                    if (typeMatches(eg)) {
                        ret = graphics.add(eg);
                        addExtents(eg.getExtents());
                    }
                }
            } else if (shape instanceof OMGraphicList
                    && !((OMGraphicList) shape).isVague()) {
                for (Iterator<OMGraphic> it = ((OMGraphicList) shape).iterator(); it.hasNext();) {
                    add((OMGraphic) it.next());
                }
                ret = true;
            } else {
                Debug.message("esri",
                        "EsriPolygonList.add()- graphic isn't a EsriPoly or OMPoly, can't add.");
            }
        } catch (ClassCastException cce) {
        }

        return ret;
    }

    public EsriPolyline convert(OMPoly ompoly) {
        return EsriPolyline.convert(ompoly);
    }

    public boolean typeMatches(OMGraphic omg) {
        return (omg instanceof EsriGraphic && ((EsriGraphic) omg).getType() == getType());
    }

    /**
     * Get the list type in ESRI type number form - 3.
     */
    public int getType() {
        return SHAPE_TYPE_POLYLINE;
    }

    /**
     * Construct an EsriPolylineList.
     */
    public EsriPolylineList() {
        super();
    }

    /**
     * Construct an EsriPolylineList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public EsriPolylineList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an EsriPolylineList with an initial capacity and a standard
     * increment value.
     * 
     * @param initialCapacity the initial capacity of the list
     * @param capacityIncrement the capacityIncrement for resizing
     * @deprecated - capacityIncrement doesn't do anything.
     */
    public EsriPolylineList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity);
    }

    public static OMPoly convert(OMLine omLine) {
        if (omLine.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            OMPoly poly = new OMPoly(omLine.getLL(), OMGraphic.DECIMAL_DEGREES, omLine.getLineType());
            poly.setAttributes(omLine.getAttributes());
            DrawingAttributes da = new DrawingAttributes();
            da.setFrom(omLine);
            da.setTo(poly);
            return poly;
        } else
            return null;
    }

    public EsriGraphic shallowCopy() {
        EsriPolylineList ret = new EsriPolylineList(size());
        ret.setAttributes(getAttributes());
        for (Iterator<OMGraphic> iter = iterator(); iter.hasNext();) {
            EsriGraphic g = (EsriGraphic) iter.next();
            ret.add((OMGraphic) g.shallowCopy());
        }
        return ret;
    }
}
