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
 * An arbitrary space described in terms of Geo objects. GeoRegions are assumed
 * to be closed paths representing areas.
 */
public interface GeoRegion extends GeoPath {

    /**
     * Is the Geo inside the region?
     * 
     * @param point
     * @return true if point is inside region.
     */
    boolean isPointInside(Geo point);

    // ------------------------------
    // Basic Implementation
    // ------------------------------

    public static class Impl extends GeoPath.Impl implements GeoRegion {

        public Impl(Geo[] coords) {
            super(coords);
        }

        /**
         * Create a region of LatLon pairs.
         * 
         * @param lls alternating lat/lon in decimal degrees.
         */
        public Impl(double[] lls) {
            this(lls, true);
        }

        /**
         * Create a region of LatLon pairs.
         * 
         * @param lls alternating lat/lon values.
         * @param isDegrees true if lat/lon are in degrees, false if in radians.
         */
        public Impl(double[] lls, boolean isDegrees) {
            super(lls, isDegrees);
        }
        
        /**
         * Create a region from a GeoArray.
         * @param points
         */
        public Impl(GeoArray points) {
            super(points);
        }

        /**
         * @deprecated use getID() instead.
         */
        public void setRegionId(Object rid) {
            id = rid;
        }

        /**
         * @deprecated use getID() instead.
         */
        public Object getRegionId() {
            return id;
        }

        public boolean isSegmentNear(GeoSegment s, double epsilon) {
            return Intersection.isSegmentNearPolyRegion(s,
                    getPoints(),
//                    toPointArray(),
                    epsilon);
        }

        public boolean isPointInside(Geo p) {
            return Intersection.isPointInPolygon(p, getPoints()/*toPointArray()*/);
        }

        public BoundingCircle getBoundingCircle() {
            if (bc == null) {
                return new BoundingCircle.Impl(this);
            }
            return bc;
        }
    }

}
