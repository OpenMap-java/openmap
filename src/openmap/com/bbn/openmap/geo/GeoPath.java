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
 * An abstraction of an arbitrary geographic path. A path is assumed
 * to mean a chain of points that although it may share a common starting and end
 * point, it will not not represent an area in that case.
 * 
 * @author mthome@bbn.com
 */

public interface GeoPath extends GeoExtent {
    /** @return an iterator over the segments of the path * */
    GeoPath.SegmentIterator segmentIterator();

    /** @return an iterator over the points of the path * */
    GeoPath.PointIterator pointIterator();

    /**
     * Return the points that make up the path as an array of Geo
     * object. Closed paths are not specially marked. Specifically,
     * closed paths do not have equal first and last Geo points in the
     * returned array.
     * 
     * @return the Geo points of the Path
     */
    Geo[] toPointArray();

    /**
     * @return the number of points in the path.
     */
    int length();

    interface SegmentIterator extends java.util.Iterator {
        /** is there another segment? * */
        boolean hasNext();

        /**
         * standard implementation of Iterator.next() returns the same
         * value as nextSegment(), albeit needing casting to GSegment.
         */
        Object next();

        /**
         * Advance to the next pegment. Some implementations will also
         * implement GSegment, so that #next() returns the iterator
         * instance itself, but this should not be depended on.
         * 
         * @return the next GSegment
         */
        GeoSegment nextSegment();
    }

    interface PointIterator extends java.util.Iterator {
        /** is there another point? * */
        boolean hasNext();

        /**
         * standard implementation of Iterator.next() returns the same
         * value as nextPoint(), albeit needing casting to GPoint.
         */
        Object next();

        /**
         * Advance to the next point. Some implementations will also
         * implement GPoint, so that #next() returns the iterator
         * instance itself, but this should not be depended on.
         * 
         * @return the next GPoint
         */
        GeoPoint nextPoint();
    }

}
