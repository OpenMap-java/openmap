/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2001, 2002 All Rights Reserved
 *
 */

package com.bbn.openmap.geo;

/**
 * The terms that describe how matches between Geo objects are to be
 * performed.
 */
public interface MatchParameters {

    /**
     * return the horizontal deviation (range) to consider matching.
     * The value is in radians. 0.0 implies strict intersection. Note
     * that if a RegionIndex is being used, then this value probably
     * must be no larger than the index's margin to avoid missing
     * regions that are near index boundaries.
     */
    double horizontalRange();

    public class Standard implements MatchParameters {
        double hr;

        public Standard(double hr) {
            this.hr = hr;
        }

        public double horizontalRange() {
            return hr;
        }

    }

    /**
     * A set of parameters that matches radius of 10 nmiles.
     */
    MatchParameters ROUTE_DEFAULT = new Standard(Geo.nmToAngle(10));
    /**
     * A set of parameters for strict intersections, 0 nmiles.
     */
    MatchParameters STRICT = new Standard(0.0);
}
