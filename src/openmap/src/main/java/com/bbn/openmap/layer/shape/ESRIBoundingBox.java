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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIBoundingBox.java,v $
// $RCSfile: ESRIBoundingBox.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import com.bbn.openmap.util.HashCodeUtil;

/**
 * A bounding box is a rectangle that fully encloses some number of shapes. The
 * rectangle is represented as four doubles, xmin ymin, xmax, and ymax.
 * 
 * 
 * <H2>To Do</H2>
 * <UL>
 * <LI>Make addPoint take two doubles to avoid unnecessarily consing ESRIPoints
 * to add to the box.</LI>
 * </UL>
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.3 $ $Date: 2004/10/14 18:06:04 $
 */
public class ESRIBoundingBox {

    /** The minimum point. */
    public ESRIPoint min;

    /** The maximum point. */
    public ESRIPoint max;

    /**
     * Initialize a null bounding box. All coordinates are set to zero.
     */
    public ESRIBoundingBox() {
    }

    /**
     * Initialize a bounding box from a point. The bounding box is initialized
     * to encompass the given point.
     * 
     * @param point the point to enclose
     */
    public ESRIBoundingBox(ESRIPoint point) {
        this(point.x, point.y);
    }

    /**
     * Initialize a bounding box from two doubles representing a point. The
     * bounding box is initialized to encompass the given location.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public ESRIBoundingBox(double x, double y) {
        addPoint(x, y);
    }

    /**
     * Initialize a bounding box to encompass a minimum and maximum point. The
     * bounding box is initialized to fully encompass both points.
     * 
     * @param _min a point to enclose
     * @param _max another point to enclose
     */
    public ESRIBoundingBox(ESRIPoint _min, ESRIPoint _max) {
        addPoint(_min);
        addPoint(_max);
    }

    /**
     * Increase the extents of this bounding box to enclose the given bounding
     * box.
     * 
     * @param bb a bounding box to be enclosed
     */
    public void addBounds(ESRIBoundingBox bb) {
        addPoint(bb.min);
        addPoint(bb.max);
    }

    /**
     * Increase the extents of this bounding box to enclose all of the given
     * points.
     * 
     * @param points a set of points to enclose
     */
    public void addPoints(ESRIPoint[] points) {
        for (int j = 0; j < points.length; j++) {
            addPoint(points[j]);
        }
    }

    /**
     * Increase the extents of this bounding box to enclose the given point.
     * 
     * @param point a point to enclose
     */
    public void addPoint(ESRIPoint point) {
        addPoint(point.x, point.y);
    }

    /**
     *  
     */
    public void addPoint(double x, double y) {
        if (min == null) {
            min = new ESRIPoint(x, y);
            max = new ESRIPoint(x, y);
        } else {
            if (min.x > x)
                min.x = x;
            if (min.y > y)
                min.y = y;
            if (max.x < x)
                max.x = x;
            if (max.y < y)
                max.y = y;
        }
    }

    /**
     * Determines equality with another bounding box
     * 
     * @param obj a candidate object
     * @return <code>true</code> if <code>obj</code> is of type
     *         <code>ESRIBoundingBox</code> <b><i>and </i> </b> the extents of
     *         that bounding box match this box's extents. <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ESRIBoundingBox bb = (ESRIBoundingBox) obj;
        return (min.equals(bb.min) && max.equals(bb.max));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        // collect the contributions of various fields
        result = HashCodeUtil.hash(result, min.x);
        result = HashCodeUtil.hash(result, min.y);
        result = HashCodeUtil.hash(result, max.x);
        result = HashCodeUtil.hash(result, max.y);
        return result;
    }
}