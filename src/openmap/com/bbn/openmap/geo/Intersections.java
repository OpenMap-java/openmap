/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2005 All Rights Reserved
 * 
 */

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Algorithm class for computing intersections between complex
 * objects.
 */

public class Intersections {

    public static interface Algorithm {
        /**
         * invoked when Intersect finds a match. The parameters passed
         * depend on the arguments to the Intersect.
         * 
         * @param query the object or portion of the object passed as
         *        the first argument to Intersect.
         * @param match the object or portion of the object passed as
         *        the second argument to Intersect.
         */
        void match(Object queryObject, Object matchedObject);

        /**
         * invoked to consider matching a possible match.
         * Intersections will invoke match IFF this method return
         * true.
         */
        boolean consider(GSegment segment, GeoSpace region);

        /**
         * invoked at the beginning of a search. May be used to
         * initialize the Algorithm instance
         */
        void startingSearch();
    }

    /**
     * called by the algorithm-parameterized Intersection methods to
     * control the behavior of the geometric matches and to call back
     * into external code, for instance, to collect results. A new
     * instance must be used each time.
     * <p>
     * This implementation requires that setMatchParameters be called
     * prior to starting the match.
     */
    public static abstract class BasicAlgorithm implements Algorithm {
        // state and default algorithm
        protected double hrange = 0.0;

        /**
         * used to make certain that an algorithm instance is not
         * reused *
         */
        protected boolean fresh = true;

        public void setMatchParameters(MatchParameters params) {
            // initialize search parameters from method calls
            hrange = params.horizontalRange();
        }

        public boolean consider(GSegment segment, GeoSpace region) {
            return Intersections.segmentNearGeoSpace(segment, hrange, region);
        }

        /** invoked before a search to initialize search state * */
        public synchronized final void startingSearch() {
            if (!fresh) {
                throw new IllegalArgumentException("Instersections.Algorithm instances may only be used once!");
            }
            fresh = false;
        }

    }

    /** Is a segment horizontally within range of a GeoSpace region? */
    public static final boolean segmentNearGeoSpace(GSegment segment,
                                                    double hrange,
                                                    GeoSpace region) {
        return region.isSegmentNear(segment, hrange);
    }

    /**
     * find intersections between a Path and a set of regions of
     * interest. Invokes algorithm.match(path.iterator().getSegId(),
     * region) on matches.
     * 
     * @param path a set of points to match against regions.
     * @param regions a Collection of possible GeoSpace regions to
     *        match. If it is a RegionIndex, then a more constrained
     *        lookup can be performed.
     * @param algorithm search parameters, control mechanisms, and
     *        data collection for the search.
     */
    public static void intersect(Path path, Collection regions,
                                 Algorithm algorithm) {
        algorithm.startingSearch();

        for (Path.SegmentIterator pit = path.segmentIterator(); pit.hasNext();) {
            GSegment seg = pit.nextSegment();
            Iterator rit;
            if (regions instanceof RegionIndex) {
                rit = ((RegionIndex) regions).iterator(seg);
            } else {
                rit = regions.iterator();
            }

            while (rit.hasNext()) {
                try {
                    GeoSpace region = (GeoSpace) rit.next();
                    if (algorithm.consider(seg, region)) {
                        // notify the controller
                        algorithm.match(pit, region);
                    }
                } catch (ClassCastException cce) {
                    // Whew, blow it off...
                }
            }
        }
    }

    /**
     * Simplified version of #intersect(Path, Collection, Algorithm)
     * for old code, using the default match algorithm, and returning
     * the identifiers of the regions that intersect with the path.
     * 
     * @param path a set of points to match against regions.
     * @param regions a Collection of possible GeoSpace regions to
     *        match. If it is a RegionIndex, then a more constrained
     *        lookup can be performed.
     * @return an interator over a list of the identifiers of the
     *         intersecting regions.
     */
    public static Iterator intersect(Path path, Collection regions) {
        final ArrayList l = new ArrayList(10);

        BasicAlgorithm alg = new BasicAlgorithm() {
            public void match(Object query, Object match) {
                l.add(match);
            }
        };
        alg.setMatchParameters(MatchParameters.ROUTE_DEFAULT);
        intersect(path, regions, alg);
        return l.iterator();
    }

    /**
     * Does the segment come within near radians of the region defined
     * by rCenter at rRadius?
     */
    public static final boolean isSegmentNearRadialRegion(GSegment segment,
                                                          Geo rCenter,
                                                          double rRadius,
                                                          double near) {
        Geo[] s = segment.getSeg();
        return s[0].isInside(s[1], near + rRadius, rCenter);
    }

    /**
     * Does the segment come within near radians of the region
     * defined by the polygon in r[*]?
     */
    public static final boolean isSegmentNearPolyRegion(GSegment segment,
                                                        Geo[] r, double near) {
        Geo[] s = segment.getSeg();
        int rlen = r.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            /*
             * if (Geo.isInside(s[0],s[1],near,pl0,pl1)) { return
             * true; // near enough to a region edge }
             */
            if (segmentsIntersectOrNear(s[0], s[1], pl0, pl1, near)) {
                return true;
            }
            pl0 = pl1;
        }

        // second arg is geo[]!
        if (Intersection.isPointInPolygon(s[0], r)) {
            return true;
        }

        return false;
    }

    /**
     * Does the point s come within near radians of the boarder of the
     * region defined by the polygon in r[*]?
     */
    public static final boolean isPointNearPolyBorder(Geo s, Geo[] r,
                                                      double near) {
        int rlen = r.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            if (pl0.isInside(pl1, near, s)) {
                return true; // near enough to a region edge
            }
            pl0 = pl1;
        }
        return false;
    }

    /**
     * Is a segment within range of a region?
     */
    public static final boolean isSegmentNearPolyBorder(GSegment segment,
                                                        Geo[] r, double near) {
        Geo[] s = segment.getSeg();
        int rlen = r.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            if (segmentsIntersectOrNear(s[0], s[1], pl0, pl1, near)) {
                return true;
            }
            pl0 = pl1;
        }
        return false;
    }

    /**
     * is one region's boundary within range of a region? Note: good
     * practice is s describes a smaller area than r
     */
    public static final boolean isPolyBorderNearPolyBorder(Geo[] s, Geo[] r,
                                                           double near) {
        int rlen = r.length;
        int slen = s.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        Geo sl0 = s[slen - 1];
        Geo sl1;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            for (int i = 0; i < slen; i++) {
                sl1 = s[i];
                if (segmentsIntersectOrNear(sl0, sl1, pl0, pl1, near)) {
                    return true;
                }
                sl0 = sl1;
            }
            pl0 = pl1;
        }
        return false;
    }

    /**
     * returns true iff the great circle segments defined by a1-a2 and
     * b1-b2 intersect. the angles between the segments must be < PI
     * or the results are ambiguous.
     */
    public static boolean segmentsIntersect(Geo a1, Geo a2, Geo b1, Geo b2) {
        return segmentsIntersectOrNear(a1, a2, b1, b2, 0);
    }

    /**
     * returns true iff the great circle segments defined by a1-a2 and
     * b1-b2 come within the range (r, radians) of each other. The
     * angles between the segments must be < PI or the results are
     * ambiguous.
     */
    public static boolean segmentsIntersectOrNear(Geo a1, Geo a2, Geo b1,
                                                  Geo b2, double r) {
        // ac and bc are the unit vectors normal to the two great
        // circles defined by the segments
        Geo ac = a1.crossNormalize(a2);
        Geo bc = b1.crossNormalize(b2);

        // aL and bL are the lengths (in radians) of the segments
        double aL = a1.distance(a2) + r;
        double bL = b1.distance(b2) + r;

        // i is one of the two points where the two great circles
        // intersect.
        Geo i = ac.crossNormalize(bc);

        // if i is not on A
        if (!(i.distance(a1) <= aL && i.distance(a2) <= aL)) {
            i = i.antipode(); // switch to the antipode instead
            if (!(i.distance(a1) <= aL && i.distance(a2) <= aL)) { // check
                // again
                // nope - neither i nor i' is on A, so we'll bail out
                return false;
            }
        }
        // i is intersection or anti-intersection point now.

        if (i.distance(b1) <= bL && i.distance(b2) <= bL) { // see if
            // it
            // intersects
            // with b
            return true;
        } else {
            return false;
        }
    }
}
