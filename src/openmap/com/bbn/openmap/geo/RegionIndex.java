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

import java.util.Iterator;

/**
 * A Collection of Regions that supports indexed subsets. That is, in
 * addition to acting like a normal collection, it also allows getting
 * an iterator that will return a superset of all intersecting regions
 * that is a subset of the whole collection.
 * 
 * @author mthome@bbn.com
 */
public interface RegionIndex extends java.util.Collection {
    /**
     * report on the maximum horizontalRange supported by this index.
     */
    double indexHorizontalRange();
    
    /**
     * Add a region to the index.
     * @param region
     * @returns true if Region successfully added, false if not.
     */
    boolean addRegion(Region region);
    
    /**
     * Remove a region from the index.
     * @param region
     * @return true if the region was found and removed.
     */
    boolean removeRegion(Region region);
    
    /**
     * Resets the index to an empty state. 
     */
    void clearRegions();

    /**
     * return an iterator listing a subset of the whole collection
     * that is a superset of the actual matches. A valid (but
     * inefficient) implementation would return an iterator over the
     * whole collection.
     * 
     * Implementation should match anything that is likely to match -
     * this will generally include, for instance, additional space
     * around the actual segment to accommodate buffer zones around
     * the segment.
     */
    Iterator iterator(GeoExtent extent);
    
    /**
     * return an iterator listing a subset of the whole collection
     * that is a superset of the actual matches. A valid (but
     * inefficient) implementation would return an iterator over the
     * whole collection.
     * 
     * Implementation should match anything that is likely to match -
     * this will generally include, for instance, additional space
     * around the actual point to accommodate buffer zones around
     * the segment.  The BoundingCircle allows you to set the buffer.
     */
    Iterator iterator(BoundingCircle bc);
}
