/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2005 All Rights Reserved
 * 
 */

package com.bbn.openmap.geo;

/**
 * A geographic (great circle) line segment. Used in Path Iterators.
 * 
 * @author mthome@bbn.com
 */

public interface GeoSegment extends GeoExtent {
    /**
     * @return the current segment as a two-element array of Geo The
     *         first point is the "current point" and the second is
     *         the next. If there isn't another point available, will
     *         throw an indexOutOfBounds exception.
     */
    Geo[] getSeg();

    /**
     * @return the current segment as a float[]. The first point is
     *         the "current point" and the second is the next. If
     *         there isn't another point available, will throw an
     *         indexOutOfBounds exception.
     */
    float[] getSegArray();

    /**
     * @return an opaque indicator for which segment is being current.
     *         Different implementations may document the type to be
     *         returned.
     */
    Object getSegId();
}
