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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/DataBounds.java,v $
// $RCSfile: DataBounds.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.awt.geom.Point2D;

/**
 * A simple utility class that holds a min, max for a group of points.
 */
public class DataBounds {

    protected Point2D min;
    protected Point2D max;

    protected DataBounds hardLimits;

    /**
     * True if the direction of the y coordinates increase in the up direction.
     * Should be set to false if larger y values are actually lower pixel values
     * on the map.
     */
    boolean yDirUp = true;

    public DataBounds() {
    }

    public DataBounds(double minx, double miny, double maxx, double maxy) {
        add(minx, miny);
        add(maxx, maxy);
    }

    public DataBounds(Point2D minP, Point2D maxP) {
        add(minP);
        add(maxP);
    }

    /**
     * Returns a point set to the average of the min and max values. May return
     * null if no points have been added.
     */
    public Point2D getCenter() {
        if (min != null) {
            double minx = min.getX();
            double miny = min.getY();
            double maxx = max.getX();
            double maxy = max.getY();
            return new Point2D.Double((minx + maxx) / 2, (miny + maxy) / 2);
        } else
            return null;
    }

    public String toString() {
        return "DataBounds| min:" + min + " max:" + max;
    }

    /**
     * Upper right point.
     */
    public Point2D getMax() {
        return max;
    }

    /**
     * Lower left point.
     */
    public Point2D getMin() {
        return min;
    }

    public void add(double x, double y) {
        if (min == null) {
            min = new Point2D.Double(x, y);
            max = new Point2D.Double(x, y);
        } else {
            double minx = min.getX();
            double miny = min.getY();
            double maxx = max.getX();
            double maxy = max.getY();

            if (minx > x)
                minx = x;
            if (miny > y)
                miny = y;
            if (maxx < x)
                maxx = x;
            if (maxy < y)
                maxy = y;

            if (hardLimits != null) {
                double hlminx = hardLimits.min.getX();
                double hlminy = hardLimits.min.getY();
                double hlmaxx = hardLimits.max.getX();
                double hlmaxy = hardLimits.max.getY();

                minx = setInRange(hlmaxx, hlminx, minx);
                maxx = setInRange(hlmaxx, hlminx, maxx);
                miny = setInRange(hlmaxy, hlminy, miny);
                maxy = setInRange(hlmaxy, hlminy, maxy);

            }

            min.setLocation(minx, miny);
            max.setLocation(maxx, maxy);
        }
    }

    /**
     * Make sure the value is within the range.
     * 
     * @param hi high range value
     * @param lo low range value
     * @param val testing value
     * @return the value, adjusted if necessary.
     */
    protected double setInRange(double hi, double lo, double val) {
        if (val > hi) {
            val = hi;
        }

        if (val < lo) {
            val = lo;
        }

        return val;
    }

    public void add(Point2D point) {
        add((double) point.getX(), (double) point.getY());
    }

    public boolean contains(Point2D query) {
        double x = query.getX();
        double y = query.getY();
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY();
    }

    public double getWidth() {
        return max.getX() - min.getX();
    }

    public double getHeight() {
        return max.getY() - min.getY();
    }

    public DataBounds getHardLimits() {
        return hardLimits;
    }

    public void setHardLimits(DataBounds hardLimits) {
        this.hardLimits = hardLimits;
    }

    public boolean isyDirUp() {
        return yDirUp;
    }

    public void setyDirUp(boolean yDirUp) {
        this.yDirUp = yDirUp;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DataBounds) {
            DataBounds dobj = (DataBounds) obj;
            boolean match = (min == null && dobj.getMin() == null && max == null && dobj.getMax() == null);
            boolean match2 = false;
            try {
                match2 = getMin().equals(dobj.getMin()) && getMax().equals(dobj.getMax());
            } catch (NullPointerException npe) {

            }

            return this.yDirUp == dobj.yDirUp && (match || match2);
        }

        return false;
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        // collect the contributions of various fields
        if (max != null) {
            result = HashCodeUtil.hash(result, max.getY());
            result = HashCodeUtil.hash(result, max.getX());
        }
        if (min != null) {
            result = HashCodeUtil.hash(result, min.getY());
            result = HashCodeUtil.hash(result, min.getX());
        }
        result = HashCodeUtil.hash(result, yDirUp);
        return result;
    }

    public boolean intersects(DataBounds db2) {
        if (db2 == null) {
            return false;
        }
        Point2D min2 = db2.getMin();
        Point2D max2 = db2.getMax();
        return !(min2 == null || (min2.getY() > max.getY() || max2.getY() < min.getY()) || (min2.getX() > max.getX() || max2.getX() < min.getX()));
    }
}