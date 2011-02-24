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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTreeRect.java,v
// $
// $RCSfile: QuadTreeRect.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.quadtree;

import java.io.Serializable;

public class QuadTreeRect
        implements Serializable {

    static final long serialVersionUID = -5585535433679092922L;

    public float north;
    public float south;
    public float west;
    public float east;

    public QuadTreeRect(float n, float w, float s, float e) {
        north = n;
        west = w;
        south = s;
        east = e;
    }

    public boolean within(QuadTreeRect rect) {
        return within(rect.north, rect.west, rect.south, rect.east);
    }

    public boolean within(float n, float w, float s, float e) {

        // We check for equality for the northern and western border
        // because the rectangles, out of convention for this package,
        // will contain points that exactly match those borders.

        // Thanks to Paul Tomblin for pointing out that the old code
        // wasn't entirely correct, and supplied the better algorithm.

        if (s >= north)
            return false;
        if (n < south)
            return false;
        if (w > east)
            return false;
        if (e <= west)
            return false;
        return true;
    }

    public boolean pointWithinBounds(float lat, float lon) {
        if (lon >= west && lon < east && lat <= north && lat > south) {
            return true;
        } else
            return false;
    }

    /**
     * A utility method to figure out the closest distance of a border to a
     * point. If the point is inside the rectangle, return 0.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return closest distance to the point.
     */
    public double borderDistance(float lat, float lon) {

        double nsdistance;
        double ewdistance;

        if (south <= lat && lat <= north) {
            nsdistance = 0;
        } else {
            nsdistance = Math.min((Math.abs(lat - north)), (Math.abs(lat - south)));
        }

        if (west <= lon && lon <= east) {
            ewdistance = 0;
        } else {
            ewdistance = Math.min((Math.abs(lon - east)), (Math.abs(lon - west)));
        }

        double distance = Math.sqrt(Math.pow(nsdistance, 2.0) + Math.pow(ewdistance, 2.0));
        return distance;
    }

    /**
     * Notice the change from borderDistance() to borderDistanceSqr() since
     * distance squared must be used throughout, which is now given by:
     * 
     * @param lat
     * @param lon
     * @return border distance squared
     */
    public double borderDistanceSqr(double lat, double lon) {
        double nsdistance;
        double ewdistance;

        if (south <= lat && lat <= north) {
            nsdistance = 0.0;
        } else {
            nsdistance = Math.min((Math.abs(lat - north)), (Math.abs(lat - south)));
        }

        if (west <= lon && lon <= east) {
            ewdistance = 0.0;
        } else {
            ewdistance = Math.min((Math.abs(lon - east)), (Math.abs(lon - west)));
        }

        if (nsdistance == 0.0 && ewdistance == 0.0) // save computing 0 distance
            return 0.0;

        double dx = ewdistance * ewdistance;
        double dy = nsdistance * nsdistance;
        double distanceSqr = dx * dx + dy * dy;
        return distanceSqr;
    }
}