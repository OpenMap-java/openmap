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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/DataBounds.java,v $
// $RCSfile: DataBounds.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * A simple utility class that holds a min, max for a group of points.
 */
public class DataBounds {

    protected Point2D min;
    protected Point2D max;

    public DataBounds() {}

    public DataBounds(double minx, double miny, double maxx, double maxy) {
        add(minx, miny);
        add(maxx, maxy);
    }

    public DataBounds(Point minP, Point maxP) {
        add(minP);
        add(maxP);
    }

    /**
     * Returns a point set to the average of the min and max values.
     * May return null if no points have been added.
     */
    public Point2D getCenter() {
        if (min != null) {
            double minx = min.getX();
            double miny = min.getY();
            double maxx = max.getX();
            double maxy = max.getY();
            return new Point2D.Double((minx + maxx)/2, (miny + maxy)/2);
        } else return null;
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

            min.setLocation(minx, miny);
            max.setLocation(maxx, maxy);
        }
    }

    public void add(Point2D point) {
        add((double)point.getX(), (double)point.getY());
    }
}
