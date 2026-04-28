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
 * A geographic Point in space. Used in Path Iterators.
 * 
 * @author mthome@bbn.com
 */
public interface GeoPoint extends GeoExtent {
    /** return the current point as a Geo object */
    Geo getPoint();

    /**
     * A standard implementation of GeoPoint.
     * 
     * @author dietrick
     */
    public static class Impl implements GeoPoint {
        protected Geo point;
        protected Object id = GeoPoint.Impl.this;

        public Impl(Geo p) {
            point = p;
        }

        /**
         * Create a Impl from decimal degrees lat/lon coordinates.
         * 
         * @param lat
         * @param lon
         */
        public Impl(double lat, double lon) {
            point = new Geo(lat, lon);
        }

        /**
         * Create a Impl from decimal degrees lat/lon coordinates.
         * 
         * @param lat
         * @param lon
         * @param isDegrees flag to specify decimal degrees (true) or radians.
         */
        public Impl(double lat, double lon, boolean isDegrees) {
            point = new Geo(lat, lon, isDegrees);
        }
        
        public Geo getPoint() {
            return point;
        }

        /**
         * @deprecated use setID() instead.
         */
        public void setPointId(Object pid) {
            id = pid;
        }

        /**
         * @deprecated use getID() instead.
         */
        public Object getPointId() {
            return id;
        }

        public void setID(Object pid) {
            id = pid;
        }

        public Object getID() {
            return id;
        }

        public BoundingCircle getBoundingCircle() {
            return new BoundingCircle.Impl(point, 0.0);
        }

    }
}
