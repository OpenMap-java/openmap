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
 * An arbitrary space described in terms of Geo objects. GeoRegions
 * are assumed to be closed paths representing areas.
 */
public interface GeoRegion extends GeoExtent {

    /**
     * return an array of Geo objects that contain the space.
     */
    Geo[] getBoundary();

    /** Does the segment s come within epsilon (in radians) of us? */
    boolean isSegmentNear(GeoSegment s, double epsilon);

    Object getRegionId();

    public static class Impl implements GeoRegion {
        protected Geo[] boundary;
        protected Object id = GeoRegion.Impl.this;

        public Impl(Geo[] coords) {
            boundary = coords;
        }

        public Geo[] getBoundary() {
            return boundary;
        }

        public void setRegionId(Object rid) {
            id = rid;
        }

        public Object getRegionId() {
            return id;
        }

        public boolean isSegmentNear(GeoSegment s, double epsilon) {
            return Intersection.isSegmentNearPolyRegion(s,
                    getBoundary(),
                    epsilon);
        }

        public BoundingCircle getBoundingCircle() {
            return new BoundingCircle.Impl(this);
        }
    }

}
