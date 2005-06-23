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
 * A geographic Point in space.  Used in Path Iterators.
 * 
 * @author mthome@bbn.com
 */
public interface GPoint {
    /** return the current point as a Geo object */
    Geo getPoint();

    /**
     * return an opaque indicator for which point is being current.
     * Different implementations may document the type to be returned.
     */
    Object getPointId();
}
