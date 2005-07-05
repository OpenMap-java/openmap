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
 * An arbitrary space described in terms of Geo objects.
 */
public interface Region {

    /**
     * @return RoundingCircle encompassing all points in the boundary.
     */
    BoundingCircle getBoundingCircle();
    
    /**
     * return an array of Geo objects that contain the space. This is
     * a hack for now, as we really want a better mechanism to
     * describe these guys
     */
    Geo[] getBoundary();

    /** Does the segment s come within epsilon (in radians) of us? */
    boolean isSegmentNear(GeoSegment s, double epsilon);
}
