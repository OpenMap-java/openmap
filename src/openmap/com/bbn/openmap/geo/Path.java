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
 * An abstraction of an arbitrary geographic path.
 * 
 * @author mthome@bbn.com
 */

public interface Path extends GExtent {
    Path.SegmentIterator segmentIterator();

    Path.PointIterator pointIterator();

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
        GSegment nextSegment();
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
        GPoint nextPoint();
    }

}
