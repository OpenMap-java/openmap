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
public interface GeoPoint extends GeoExtent {
    /** return the current point as a Geo object */
    Geo getPoint();

    /**
     * return an opaque indicator for which point is being current.
     * Different implementations may document the type to be returned.
     */
    Object getPointId();
    
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
         * @param lat
         * @param lon
         */
        public Impl(float lat, float lon) {
            point = new Geo(lat, lon);
        }
        
        public Geo getPoint() {
            return point;
        }
        
        public void setPointId(Object pid) {
            id = pid;
        }
        
        public Object getPointId() {
            return id;
        }
        
        public BoundingCircle getBoundingCircle() {
            return new BoundingCircle.Impl(point, 0.0);
        }
        
    }
}
