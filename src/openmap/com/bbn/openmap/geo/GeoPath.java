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
 * to mean a chain of points that although it may share a common
 * starting and end point, it will not not represent an area in that
 * case.
 * 
 * @author mthome@bbn.com
 */

public interface GeoPath extends GeoExtent {
    /** @return an iterator over the segments of the path * */
    GeoPath.SegmentIterator segmentIterator();

    /** @return an iterator over the points of the path * */
    GeoPath.PointIterator pointIterator();

    /** Does the segment s come within epsilon (in radians) of us? */
    boolean isSegmentNear(GeoSegment s, double epsilon);

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
        /** Asking if there is another segment. * */
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
        /** Asking if is there another point. * */
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

    /**
     * An implementation of Path that takes an alternating lat/lon
     * array and (optionally) an array of altitudes.
     */
    public static class Impl implements GeoPath {
        protected Geo[] pts;
        protected int length;

        protected Impl() {}

        /**
         * Create a path of LatLon pairs.
         * 
         * @param lls alternating lat/lon in decimal degrees.
         */
        public Impl(float[] lls) {
            this(lls, true);
        }

        /**
         * Create a path of LatLon pairs.
         * 
         * @param lls alternating lat/lon values.
         * @param isDegrees true if lat/lon are in degrees, false if
         *        in radians.
         */
        public Impl(float[] lls, boolean isDegrees) {
            int al = lls.length;
            int length = al / 2;
            Geo[] pts = new Geo[length];
            for (int i = 0, p = 0; i < al; i = i + 2) {
                pts[p] = new Geo(lls[i], lls[i + 1], isDegrees);
                p++;
            }
            setPoints(pts);
        }

        /**
         * Create a path from Geos.
         * 
         * @param geos
         */
        public Impl(Geo[] geos) {
            setPoints(geos);
        }
        
        /**
         * Method for subclasses to set pts and length of Geos.
         * @param points
         */
        protected void setPoints(Geo[] points) {
            pts = points;
            if (pts != null) {
                length = pts.length;
            } else {
                length = 0;
            }
        }

        public Geo[] toPointArray() {
            return pts;
        }

        public boolean isSegmentNear(GeoSegment s, double epsilon) {
            return Intersection.isSegmentNearPoly(s,
                    toPointArray(),
                    epsilon) != null;
        }

        protected transient BoundingCircle bc = null;

        public synchronized BoundingCircle getBoundingCircle() {
            if (bc == null)
                bc = new BoundingCircle.Impl(this);

            return bc;
        }

        public int length() {
            return length;
        }

        public GeoPath.SegmentIterator segmentIterator() {
            return new SegIt();
        }

        public GeoPath.PointIterator pointIterator() {
            return new PointIt();
        }

        /**
         * Callback for the SegIt to find out how the LatLonPath wants
         * the segment IDed.
         * 
         * @param i The index of the segment in question.
         * @return Object that IDs the segment, could be this path,
         *         too. Depends on what the Intersection Algorithm
         *         wants to do in consider().
         */
        protected Object getSegID(int i) {
            return new Integer(i);
        }

        /**
         * Callback for the PointIt to find out how the LatLonPath
         * wants the points IDed.
         * 
         * @param i The index of the point in question.
         * @return Object that IDs the point, could be this path, too.
         *         Depends on what the Intersection Algorithm wants to
         *         do in consider().
         */
        protected Object getPointID(int i) {
            return new Integer(i);
        }

        protected class SegIt implements GeoPath.SegmentIterator, GeoSegment {
            int i = -1;
            Geo[] seg = new Geo[2];

            public SegIt() {
                seg[1] = pts[0];
            }

            /** Constructs a new bounding circle instance each call. * */
            public BoundingCircle getBoundingCircle() {
                Geo c = Intersection.center(seg);
                return new BoundingCircle.Impl(c, c.distance(seg[0]));
            }

            public boolean hasNext() {
                return i < (length - 2);
            }

            public Object next() {
                return nextSegment();
            }

            public GeoSegment nextSegment() {
                i++;
                seg[0] = seg[1];
                seg[1] = pts[i + 1];
                return this;
            }

            public void remove() {
                throw new UnsupportedOperationException("Path.SegmentIterator doesn't support remove");
            }

            /**
             * GSegment method.
             * 
             * @return the current segment as a two-element array of
             *         Geo The first point is the "current point" and
             *         the second is the next. TODO If there isn't
             *         another point available, will throw an
             *         indexOutOfBounds exception.
             */
            public Geo[] getSeg() {
                return seg;
            }

            /**
             * @return the current segment as a float[]. The first
             *         point is the "current point" and the second is
             *         the next. TODO If there isn't another point
             *         available, will throw an indexOutOfBounds
             *         exception.
             */
            public float[] getSegArray() {
                return new float[] { (float) seg[0].getLatitude(),
                        (float) seg[0].getLongitude(),
                        (float) seg[1].getLatitude(),
                        (float) seg[1].getLongitude() };
            }

            /**
             * Return Object ID for current segment.
             */
            public Object getSegId() {
                return GeoPath.Impl.this.getSegID(i);
            }
        }

        protected class PointIt implements GeoPath.PointIterator, GeoPoint {
            int i = -1;

            public PointIt() {}

            public boolean hasNext() {
                return i < length;
            }

            public Object next() {
                return nextPoint();
            }

            public GeoPoint nextPoint() {
                i++;
                return this;
            }

            public void remove() {
                throw new UnsupportedOperationException("Path.Iterator doesn't support remove");
            }

            public Geo getPoint() {
                return pts[i];
            }

            public Object getPointId() {
                return GeoPath.Impl.this.getPointID(i);
            }

            public BoundingCircle getBoundingCircle() {
                return new BoundingCircle.Impl(pts[i], 0.0);
            }
        }
    }

}
