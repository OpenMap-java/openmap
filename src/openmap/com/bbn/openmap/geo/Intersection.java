/**
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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains great circle intersection algorithms and helper methods.
 * Sources: http://williams.best.vwh.net/intersect.htm
 * http://mathforum.org/library/drmath/view/60711.html
 * <P>
 * The Intersection class has been updated to manage query
 * intersections of GeoExtents over other GeoExtents. MatchCollectors
 * and MatchFilters can be used to help optimize the search and manage
 * the results.
 * 
 * @author Sachin Date
 * @author Ken Anderson
 * @version $Revision: 1.15 $ on $Date: 2006/12/04 15:35:02 $
 */
public class Intersection {

    protected final MatchFilter filter;
    protected final MatchCollector collector;

    /**
     * Create an Intersection class that will use the provided
     * MatchFilter and MatchCollector.
     * 
     * @param filter
     * @param collector
     */
    public Intersection(MatchFilter filter, MatchCollector collector) {
        this.filter = filter;
        this.collector = collector;
    }

    /**
     * Create an Intersection class that will use the
     * MatchFilter.MatchParameters class with STRICT settings, and a
     * MatchCollector.SetMatchCollector.
     */
    public static Intersection intersector() {
        return new Intersection(new MatchFilter.MatchParametersMF(MatchParameters.STRICT), new MatchCollector.SetMatchCollector());
    }

    /**
     * Create an Intersection class that will use the
     * MatchFilter.MatchParameters class with provided settings, and a
     * MatchCollector.SetMatchCollector.
     */
    public static Intersection intersector(MatchParameters params) {
        return new Intersection(new MatchFilter.MatchParametersMF(params), new MatchCollector.SetMatchCollector());

    }

    /**
     * Create an Intersection class that will use the
     * MatchFilter.MatchParameters class with provided settings, and a
     * MatchCollector.CollectionMatchCollector with the provided
     * collector.
     */
    public static Intersection intersector(MatchParameters params,
                                           final Collection c) {
        return new Intersection(new MatchFilter.MatchParametersMF(params), new MatchCollector.CollectionMatchCollector(c));
    }

    /**
     * Create an Intersection class that will use the provided
     * MatchFilter class and the provided MatchCollector.
     */
    public static Intersection intersector(MatchFilter filter,
                                           MatchCollector collector) {
        return new Intersection(filter, collector);
    }

    /**
     * Asks the Intersection class to calcuate the relationships
     * between object a and b. Calls the other consider methods,
     * depending on what a and b are. Consult the MatchCollector for
     * the results.
     * 
     * @param a A GeoExtent object, generally.
     * @param b A ExtentImpl object or GeoExtent object, generally.
     */
    public void consider(Object a, Object b) {
        if (b instanceof Collection) {
            if (a instanceof GeoRegion) {
                considerRegionXRegions((GeoRegion) a, (Collection) b);
            } else if (a instanceof GeoPath) {
                considerPathXRegions((GeoPath) a, (Collection) b);
            } else if (a instanceof GeoPoint) {
                considerPointXRegions((GeoPoint) a, (Collection) b);
            }
        } else if (b instanceof GeoRegion) {
            if (a instanceof GeoRegion) {
                considerRegionXRegion((GeoRegion) a, (GeoRegion) b);
            } else if (a instanceof GeoPath) {
                considerPathXRegion((GeoPath) a, (GeoRegion) b);
            } else if (a instanceof GeoPoint) {
                considerPointXRegion((GeoPoint) a, (GeoRegion) b);
            }
        }
    }

    public void considerRegionXRegions(GeoRegion r, Collection regions) {

        /*
         * since the path is closed we'll check the region index for
         * the whole thing instead of segment-by-segment
         */
        Iterator possibles;
        if (regions instanceof ExtentIndex) {
            // if we've got an index, narrow the set down
            possibles = ((ExtentIndex) regions).iterator(r);
        } else {
            possibles = regions.iterator();
        }

        while (possibles.hasNext()) {
            GeoExtent extent = (GeoExtent) possibles.next();
            if (extent instanceof GeoRegion) {
                considerRegionXRegion(r, (GeoRegion) extent);
            } else if (extent instanceof GeoPath) {
              // This body used to be the following:
              //   considerPathXRegion((GeoPath) extent, r);
              // but this reverses the match order and leads to "r" getting collected
              // instead of extent.  I've inlined the essential body and left it here
              for (GeoPath.SegmentIterator pit = ((GeoPath)extent).segmentIterator(); pit.hasNext();) {
                GeoSegment seg = pit.nextSegment();
                if (filter.preConsider(seg, r)
                    && considerSegmentXRegion(seg, r)) {
                  collector.collect(seg, extent);
                }
              }
            } else {
            	// usually, getting here means poly region vs radial region
                BoundingCircle bc = extent.getBoundingCircle();
                BoundingCircle rbc = r.getBoundingCircle();
              	// first pass check - the bounding circles intersect
                if (rbc.intersects(bc.getCenter(), bc.getRadius()
                        + filter.getHRange())) {
                	Geo[] pts = r.toPointArray();
                	if (isPointInPolygon(bc.getCenter(), pts)) {
                		// the center of extent is inside r
                		collector.collect(r, extent);
                	} else if (isPointNearPoly(bc.getCenter(), pts, bc.getRadius()+filter.getHRange())) {
                		// Center+radius of extent is within range an edge of the r
                		collector.collect(r, extent);
                	} // else no intersection
                }
            }
        }
    }

    public void considerRegionXRegion(GeoRegion r, GeoRegion region) {
        /* these must be cheap! */
        Geo[] regionBoundary = r.toPointArray();
        /* get the first path point */
        Geo pathPoint = regionBoundary[0];
        Geo[] rboundary = region.toPointArray();
        // check for total containment
        if ((rboundary != null && (Intersection.isPointInPolygon(pathPoint,
                rboundary) || Intersection.isPointInPolygon(rboundary[0],
                regionBoundary)))
                || (rboundary == null && (region.isPointInside(pathPoint) ||
                /* first path point is inside the region? */
                Intersection.isPointInPolygon(region.getBoundingCircle()
                        .getCenter(), regionBoundary)))) {
            collector.collect(r, region);
        } else {
            // gotta try harder, so we fall back to segment-by-segment
            // intersections
            for (GeoPath.SegmentIterator pit = r.segmentIterator(); pit.hasNext();) {
                GeoSegment seg = pit.nextSegment();
                if (filter.preConsider(seg, region)
                        && considerSegmentXRegion(seg, region)) {
                    collector.collect(seg, region);
                    // For the default implementation, we just care
                    // about first hit.
                    return;
                }
            }
        }
    }

    public void considerPathXRegions(GeoPath path, Collection regions) {
        /*
         * Since the path is open, then our best bet is to check each
         * segment separately
         */
        for (GeoPath.SegmentIterator pit = path.segmentIterator(); pit.hasNext();) {
            GeoSegment seg = pit.nextSegment();
            Iterator rit;
            if (regions instanceof ExtentIndex) {
                rit = ((ExtentIndex) regions).iterator(seg);
            } else {
                rit = regions.iterator();
            }

            while (rit.hasNext()) {
                GeoExtent extent = (GeoExtent) rit.next();
                if (filter.preConsider(path, extent)) {
                    if (extent instanceof GeoRegion) {
                        GeoRegion region = (GeoRegion) extent;
                        if (considerSegmentXRegion(seg, region)) {
                            collector.collect(seg, region);
                        }
                    } else if (extent instanceof GeoPath) {
                        GeoPath p = (GeoPath) extent;
                        if (isSegmentNearPoly(seg,
                                p.toPointArray(),
                                filter.getHRange()) != null) {
                            collector.collect(seg, p);
                        }
                    } else {
                        BoundingCircle bc = extent.getBoundingCircle();
                        if (isSegmentNearRadialRegion(seg,
                                bc.getCenter(),
                                bc.getRadius(),
                                filter.getHRange())) {
                            collector.collect(seg, extent);
                        }
                    }
                }

            }
        }
    }

    public void considerPathXRegion(GeoPath path, GeoRegion region) {
        for (GeoPath.SegmentIterator pit = path.segmentIterator(); pit.hasNext();) {
            GeoSegment seg = pit.nextSegment();

            if (filter.preConsider(seg, region)
                    && considerSegmentXRegion(seg, region)) {
                collector.collect(seg, region);
                // For the default implementation, we just care about
                // the first contact.
                return;
            }
        }
    }

    public boolean considerSegmentXRegion(GeoSegment seg, GeoRegion region) {
        return region.isSegmentNear(seg, filter.getHRange());
    }

    public void considerPointXRegions(GeoPoint p, Collection regions) {
        Iterator rit;
        if (regions instanceof ExtentIndex) {
            rit = ((ExtentIndex) regions).iterator(p);
        } else {
            rit = regions.iterator();
        }

        while (rit.hasNext()) {
            GeoExtent extent = (GeoExtent) rit.next();
            if (filter.preConsider(p, extent)) {
                if (extent instanceof GeoRegion) {
                    GeoRegion region = (GeoRegion) extent;
                    if (considerPointXRegion(p, region)) {
                        collector.collect(p, region);
                    }
                } else if (extent instanceof GeoPath) {
                    GeoPath path = (GeoPath) extent;
                    if (isPointNearPoly(p.getPoint(),
                            path.toPointArray(),
                            filter.getHRange())) {
                        collector.collect(p, path);
                    }
                } else {
                    BoundingCircle bc = extent.getBoundingCircle();
                    if (p.getPoint().distance(bc.getCenter()) <= bc.getRadius()
                            + filter.getHRange()) {
                        collector.collect(p, extent);
                    }
                }
            }
        }
    }

    public boolean considerPointXRegion(GeoPoint p, GeoRegion region) {
        return isPointInPolygon(p.getPoint(), region.toPointArray());
    }

    //
    // Static versions of intersection methods
    //

    /**
     * Simplified version of #intersect(Path, Collection, Algorithm)
     * for old code, using the default match algorithm, and returning
     * the identifiers of the regions that intersect with the path.
     * 
     * @param path
     * @param regions
     * @return a list of the identifiers of the intersecting regions.
     */
    public static Iterator intersect(Object path, Object regions) {
        MatchCollector.SetMatchCollector c = new MatchCollector.SetMatchCollector();
        Intersection ix = new Intersection(new MatchFilter.MatchParametersMF(MatchParameters.STRICT), c);
        ix.consider(path, regions);
        return c.iterator();
    }

    //
    // Utility methods (The Mathematics)
    //

    /**
     * Returns the two antipodal points of interection of two great
     * circles defined by the arcs (lat1, lon1) to (lat2, lon2) and
     * (lat2, lon2) to (lat4, lon4). All lat-lon values are in
     * degrees.
     * 
     * @return an array of two lat-lon points arranged as lat, lon,
     *         lat, lon
     */
    public static float[] getIntersection(float lat1, float lon1, float lat2,
                                          float lon2, float lat3, float lon3,
                                          float lat4, float lon4) {

        Geo geoCross1 = (new Geo(lat1, lon1)).crossNormalize(new Geo(lat2, lon2));
        Geo geoCross2 = (new Geo(lat3, lon3)).crossNormalize(new Geo(lat4, lon4));

        Geo geo = geoCross1.crossNormalize(geoCross2);
        Geo anti = geo.antipode();

        return new float[] { ((float) geo.getLatitude()),
                ((float) geo.getLongitude()), ((float) anti.getLatitude()),
                ((float) anti.getLongitude()) };
    }

    /**
     * Returns a Geo representing the interection of two great circles
     * defined by the arcs (lat1, lon1) to (lat2, lon2) and (lat2,
     * lon2) to (lat4, lon4). All lat-lon values are in degrees.
     * 
     * @return Geo containing intersection, might have to check
     *         antipode of Geo for actual intersection.
     */
    public static Geo getIntersectionGeo(float lat1, float lon1, float lat2,
                                         float lon2, float lat3, float lon3,
                                         float lat4, float lon4) {

        Geo geoCross1 = (new Geo(lat1, lon1)).crossNormalize(new Geo(lat2, lon2));
        Geo geoCross2 = (new Geo(lat3, lon3)).crossNormalize(new Geo(lat4, lon4));

        return geoCross1.crossNormalize(geoCross2);
    }

    /**
     * Returns true if the two segs intersect in at least one point.
     * All lat-lon values are in degrees. lat1,lon1-lat2,lon2 make up
     * one segment, lat3,lon3-lat4,lon4 make up the other segment.
     */
    public static boolean intersects(float lat1, float lon1, float lat2,
                                     float lon2, float lat3, float lon3,
                                     float lat4, float lon4) {

        float[] llp = getSegIntersection(lat1,
                lon1,
                lat2,
                lon2,
                lat3,
                lon3,
                lat4,
                lon4);

        return (llp[0] != Float.MAX_VALUE && llp[1] != Float.MAX_VALUE)
                || (llp[2] != Float.MAX_VALUE && llp[3] != Float.MAX_VALUE);
    }

    /**
     * Checks if the two polygonal areas intersect. The two polygonal
     * regions are represented by two lat-lon arrays in the lat1,
     * lon1, lat2, lon2,... format. For closed polygons the last pair
     * of points in the array should be the same as the first pair.
     * All lat-lon values are in degrees.
     */
    public static boolean polyIntersect(float[] polyPoints1, float[] polyPoints2) {

        // go through each side of poly1 and test to see if it
        // intersects with any side of poly2

        for (int i = 0; i < polyPoints1.length / 2 - 1; i++) {

            for (int j = 0; j < polyPoints2.length / 2 - 1; j++) {

                if (intersects(polyPoints1[2 * i],
                        polyPoints1[2 * i + 1],
                        polyPoints1[2 * i + 2],
                        polyPoints1[2 * i + 3],
                        polyPoints2[2 * j],
                        polyPoints2[2 * j + 1],
                        polyPoints2[2 * j + 2],
                        polyPoints2[2 * j + 3]))
                    return true;
            }
        }

        return false;
    }

    /**
     * checks if the polygon or polyline represented by the polypoints
     * contains any lines that intersect each other. All lat-lon
     * values are in degrees.
     */
    public static boolean isSelfIntersectingPoly(float[] polyPoints) {

        for (int i = 0; i < polyPoints.length / 2 - 1; i++) {

            for (int j = i + 1; j < polyPoints.length / 2 - 1; j++) {

                float lat1 = polyPoints[2 * i];
                float lon1 = polyPoints[2 * i + 1];
                float lat2 = polyPoints[2 * i + 2];
                float lon2 = polyPoints[2 * i + 3];

                float lat3 = polyPoints[2 * j];
                float lon3 = polyPoints[2 * j + 1];
                float lat4 = polyPoints[2 * j + 2];
                float lon4 = polyPoints[2 * j + 3];

                // ignore adjacent segments
                if ((lat1 == lat4 && lon1 == lon4)
                        || (lat2 == lat3 && lon2 == lon3))
                    continue;

                if (intersects(lat1, lon1, lat2, lon2, lat3, lon3, lat4, lon4))
                    return true;

            }
        }

        return false;

    }

    /**
     * Calculates the great circle distance from the point (lat, lon)
     * to the great circle containing the points (lat1, lon1) and
     * (lat2, lon2).
     * 
     * @return nautical miles
     */
    public static float pointCircleDistanceNM(Geo p1, Geo p2, Geo center) {
        return (float) Geo.nm(pointCircleDistance(p1, p2, center));
    }

    /**
     * Calculates the great circle distance from the point (lat, lon)
     * to the great circle containing the points (lat1, lon1) and
     * (lat2, lon2).
     * 
     * @return radians
     */
    public static double pointCircleDistance(Geo p1, Geo p2, Geo center) {

        Geo n = Geo.crossNormalize(p1, p2);
        Geo c = center;
        c = c.normalize();
        double cosTheta = Geo.dot(n, c);
        double theta = Math.acos(cosTheta);

        return Math.abs(Math.PI / 2 - theta);
    }

    /**
     * Point i is on the great circle defined by the points a and b.
     * Returns true if i is between a and b, false otherwise.
     */
    public static boolean isOnSegment(Geo a, Geo b, Geo i) {

        // assert (< (Math.abs (.dot (.crossNormalize a b) i))
        // 1.e-15))

        return ((a.distance(i) < a.distance(b)) && (b.distance(i) < b.distance(a)));
    }

    /**
     * @return the Geo point i, which is on the great circle segment
     *         between Geo points a and b and which is closest to Geo
     *         point c. Returns null if there is no such point.
     */
    public static Geo segIntersection(Geo a, Geo b, Geo c) {
        // Normal to great circle between a and b
        Geo g = a.crossNormalize(b);
        // Normal to the great circle between c and g
        Geo f = c.crossNormalize(g);
        // The intersection is normal to both
        Geo i = f.crossNormalize(g);
        if (isOnSegment(a, b, i)) {
            return i;
        } else {
            Geo ai = i.antipode();
            if (isOnSegment(a, b, ai)) {
                return i.antipode();
            } else {
                return null;
            }
        }
    }

    /**
     * returns the distance in NM between the point (lat, lon) and the
     * point of intersection of the great circle passing through (lat,
     * lon) and perpendicular to great circle segment (lat1, lon1,
     * lat2, lon2). returns -1 if point of intersection of the two
     * great circle segs is not on the great circle segment (lat1,
     * lon1, lat2, lon2).
     */
    public static float pointSegDistanceNM(float lat1, float lon1, float lat2,
                                           float lon2, float lat, float lon) {
        double ret = pointSegDistance(new Geo(lat1, lon1),
                new Geo(lat2, lon2),
                new Geo(lat, lon));

        return (float) (ret == -1 ? ret : Geo.nm(ret));
    }

    /**
     * Returns the distance in radians between the point c and the
     * point of intersection of the great circle passing through c and
     * perpendicular to great circle segment between a and b. Returns
     * -1 if point of intersection of the two great circle segs is not
     * on the great circle segment a-b.
     */
    public static double pointSegDistance(Geo a, Geo b, Geo c) {
        Geo i = segIntersection(a, b, c);
        return (i == null) ? -1 : c.distance(i);
    }

    /**
     * Returns true or false depending on whether the great circle seg
     * from point p1 to point p2 intersects the circle of radius
     * (radians) around center.
     */
    public static boolean intersectsCircle(Geo p1, Geo p2, Geo center,
                                           double radius) {

        // check if either of the end points of the seg are inside the
        // circle
        double d1 = Geo.distance(p1, center);
        if (d1 < radius)
            return true;

        double d2 = Geo.distance(p2, center);
        if (d2 < radius)
            return true;

        double dist = pointCircleDistance(p1, p2, center);

        if (dist > radius)
            return false;

        // calculate point of intersection of great circle containing
        // (lat, lon) and perpendicular to great circle containing
        // (lat1, lon1) and (lat2, lon2)
        Geo a = p1;
        Geo b = p2;
        Geo c = center;

        Geo g = a.cross(b);
        Geo f = c.cross(g);
        Geo i = f.crossNormalize(g);
        Geo i2 = i.antipode();

        // check if point of intersection lies on the segment
        // length of seg
        double d = Geo.distance(p1, p2);

        // Make sure the intersection point is inside the exclusion
        // zone
        if (c.distance(i) < radius) {
            // between seg endpoints and first point of intersection
            double d11 = Geo.distance(p1, i);
            double d12 = Geo.distance(p2, i);
            // Check the distance of the intersection point and either
            // endpoint to see if it falls between them. Add a second
            // test to make sure that we are on the shorter of the two
            // segments between the endpoints.
            return (d11 <= d && d12 <= d && Math.abs(d11 + d12 - d) < 0.01f);
        }
        // Make sure the intersection point is inside the exclusion
        // zone
        else if (c.distance(i2) < radius) {
            // between seg1 endpoints and second point of intersection
            double d21 = Geo.distance(p1, i2);
            double d22 = Geo.distance(p2, i2);
            // Check the distance of the intersection point and either
            // endpoint to see if it falls between them. Add a second
            // test to make sure that we are on the shorter of the two
            // segments between the endpoints.
            return (d21 <= d && d22 <= d && Math.abs(d21 + d22 - d) < 0.01f);
        } else {
            return false;
        }
    }

    /**
     * returns true if the specified poly path intersects the circle
     * centered at (lat, lon). All lat-lon values are in degrees.
     * radius is in radians.
     */
    public static boolean intersectsCircle(float[] polyPoints, float lat,
                                           float lon, double radius) {

        Geo a = null;
        Geo b = null;
        Geo c = new Geo(lat, lon);

        for (int i = 0; i < polyPoints.length / 2 - 1; i++) {

            float lat1 = polyPoints[2 * i];
            float lon1 = polyPoints[2 * i + 1];
            float lat2 = polyPoints[2 * i + 2];
            float lon2 = polyPoints[2 * i + 3];

            if (a == null || b == null) {
                a = new Geo(lat1, lon1);
                b = new Geo(lat2, lon2);
            } else {
                a.initialize(lat1, lon1);
                b.initialize(lat2, lon2);
            }

            if (intersectsCircle(a, b, c, radius))
                return true;
        }

        return false;
    }

    /**
     * Returns the center of the polygon poly.
     */
    public static Geo center(Geo[] poly) {
        Geo c = new Geo(poly[0]);
        Geo p = new Geo(poly[0]);
        for (int i = 1; i < poly.length; i++) {
            p.initialize(poly[i]);
            c = c.add(p);
        }
        Geo res = c.normalize();
        return res;
    }

    /**
     * Determines whether <code>x</code> is inside <code>poly</code>.
     * 
     * <p>
     * <em>N.B.</em><br>
     * <ul>
     * <li><code>poly</code> must be a closed polygon. In other
     * words, the first and last point must be the same.
     * <li>It is recommended that a bounds check is run before this
     * method. This method will return true if either <code>x</code>
     * or the antipode (the point on the opposite side of the planet)
     * of <code>x</code> are inside <code>poly</code>.
     * </ul>
     * 
     * <p>
     * <code>poly<code> is an array of latitude/longitude points where:
     * <br>
     * <pre>
     *                                  
     *                                                                         
     *                                                                          poly[0] = latitude 1
     *                                                                          poly[1] = longitude 1
     *                                                                          poly[2] = latitude 2
     *                                                                          poly[3] = longitude 2
     *                                                                          .
     *                                                                          .
     *                                                                          .
     *                                                                          poly[n-1] = latitude 1
     *                                                                          poly[n] = longitude 1
     *                                                                          
     * </pre>
     *
     * @param x a geographic coordinate
     * @param poly an array of lat/lons describing a closed polygon
     * @return true iff <code>x</code> or <code>antipode(x)</code> is
     * inside <code>poly</code>
     */
    public static boolean isPointInPolygon(Geo x, Geo[] poly) {
        Geo c = center(poly);

        // bail out if the point is more than 90 degrees off the
        // centroid
        double d = x.distance(c);
        if (d >= (Math.PI / 2)) {
            return false;
        }
        // ray is normal to the great circle from c to x.
        Geo ray = c.crossNormalize(x);
        /*
         * side is a point on the great circle between c and x. It is
         * used to choose a direction.
         */
        Geo side = Geo.crossNormalize(x, ray);
        boolean in = false;
        Geo p1 = new Geo(poly[0]);
        Geo p2 = new Geo(poly[0]);
        for (int i = 1; i < poly.length; i++) {
            p2.initialize(poly[i]);
            /*
             * p1 and p2 are on different sides of the ray, and the
             * great acircle between p1 and p2 is on the side that
             * counts;
             */
            if ((p1.dot(ray) < 0.0) != (p2.dot(ray) < 0.0)
                    && p1.intersect(p2, ray).dot(side) > 0.0)
                in = !in;
            Geo temp = p1;
            p1 = p2;
            p2 = temp;
        }

        // Check for unclosed polygons, if the polygon isn't closed,
        // do the calculation for the last point to the starting
        // point.
        if (!p1.equals(poly[0])) {
            p2.initialize(poly[0]);
            if ((p1.dot(ray) < 0.0) != (p2.dot(ray) < 0.0)
                    && p1.intersect(p2, ray).dot(side) > 0.0) {
                in = !in;
            }
        }

        return in;
    }

    /**
     * Ask if a Geo point is in a polygon, with the poly coordinates
     * specified in radians.
     * 
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...] are in
     *        radians
     * @return true for Geo in poly
     */
    public static boolean isPointInPolygonRadians(Geo x, float[] poly) {
        return isPointInPolygon(x, poly, false);
    }

    /**
     * Ask if a Geo point is in a polygon, with the poly coordinates
     * specified in decimal degrees.
     * 
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...] are in
     *        decimal degrees
     * @return true for Geo in poly
     */
    public static boolean isPointInPolygon(Geo x, float[] poly) {
        return isPointInPolygon(x, poly, true);
    }

    /**
     * Ask if a Geo point is in a polygon.
     * 
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...]
     * @param polyInDegrees true of poly floats represent decimal
     *        degrees.
     * @return true for Geo in poly
     */
    public static boolean isPointInPolygon(Geo x, float[] poly,
                                           boolean polyInDegrees) {
        Geo[] rg = new Geo[poly.length / 2];
        for (int j = 0; j < poly.length / 2; j++) {
            rg[j] = new Geo(poly[j * 2], poly[j * 2 + 1], polyInDegrees);
        }
        return isPointInPolygon(x, rg);
    }

    /**
     * return true IFF some point of the first argument is inside the
     * region specified by the closed polygon specified by the second
     * argument
     */
    public static boolean isPolylineInsidePolygon(float[] poly, float[] region) {
        int l = poly.length / 2;
        Geo[] rg = new Geo[region.length / 2];
        {
            for (int j = 0; j < region.length / 2; j++) {
                rg[j] = new Geo(region[j * 2], region[j * 2 + 1]);
            }
        }

        for (int i = 0; i < l; i++) {
            if (isPointInPolygon(new Geo(poly[i * 2], poly[i * 2 + 1]), rg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the point of intersection of two great circle segments
     * defined by the segments. (lat1, lon1) to (lat2, lon2) and
     * (lat2, lon2) to (lat4, lon4). All lat-lon values are in
     * degrees.
     * 
     * @return a float array of length 4 containing upto 2 valid
     *         lat-lon points of intersection that lie on both
     *         segments. Positions in the array not containing a valid
     *         lat/lon value are initialized to Float.MAX_VALUE.
     */
    public static float[] getSegIntersection(float lat1, float lon1,
                                             float lat2, float lon2,
                                             float lat3, float lon3,
                                             float lat4, float lon4) {
        // KRA 03SEP03: The original version of this consed 26+ Geo's.
        // This one conses 8+.

        Geo p1 = new Geo(lat1, lon1);
        Geo p2 = new Geo(lat2, lon2);
        Geo p3 = new Geo(lat3, lon3);
        Geo p4 = new Geo(lat4, lon4);

        Geo geoCross1 = p1.crossNormalize(p2);
        Geo geoCross2 = p3.crossNormalize(p4);

        Geo i1 = geoCross1.crossNormalize(geoCross2);
        Geo i2 = i1.antipode();

        // check if the point of intersection lies on both segs
        // length of seg1
        // double d1 = Geo.distance(lat1, lon1, lat2, lon2);
        double d1 = p1.distance(p2);
        // length of seg2
        // double d2 = Geo.distance(lat3, lon3, lat4, lon4);
        double d2 = p3.distance(p4);

        // between seg1 endpoints and first point of intersection
        // double d111 = Geo.distance(lat1, lon1, ll[0], ll[1]);
        double d111 = p1.distance(i1);
        // double d121 = Geo.distance(lat2, lon2, ll[0], ll[1]);
        double d121 = p2.distance(i1);

        // between seg1 endpoints and second point of intersection
        // double d112 = Geo.distance(lat1, lon1, ll[2], ll[3]);
        double d112 = p1.distance(i2);
        // double d122 = Geo.distance(lat2, lon2, ll[2], ll[3]);
        double d122 = p2.distance(i2);

        // between seg2 endpoints and first point of intersection
        // double d211 = Geo.distance(lat3, lon3, ll[0], ll[1]);
        double d211 = p3.distance(i1);
        // double d221 = Geo.distance(lat4, lon4, ll[0], ll[1]);
        double d221 = p4.distance(i1);

        // between seg2 endpoints and second point of intersection
        // double d212 = Geo.distance(lat3, lon3, ll[2], ll[3]);
        double d212 = p3.distance(i2);
        // double d222 = Geo.distance(lat4, lon4, ll[2], ll[3]);
        double d222 = p4.distance(i2);

        float[] llp = new float[] { Float.MAX_VALUE, Float.MAX_VALUE,
                Float.MAX_VALUE, Float.MAX_VALUE };

        // check if first point of intersection lies on both segments
        if (d1 >= d111 && d1 >= d121 && d2 >= d211 && d2 >= d221) {
            llp[0] = ((float) i1.getLatitude());
            llp[1] = ((float) i1.getLongitude());
        }
        // check if second point of intersection lies on both segments
        if (d1 >= d112 && d1 >= d122 && d2 >= d212 && d2 >= d222) {
            llp[2] = ((float) i2.getLatitude());
            llp[3] = ((float) i2.getLongitude());
        }
        return llp;
    }

    // /**
    // * returns the point of interection of two great circle segments
    // * defined by the segments. (lat1, lon1) to (lat2, lon2) and
    // * (lat2, lon2) to (lat4, lon4). All lat-lon values are in
    // * degrees.
    // *
    // * @return a float array of length 4 containing upto 2 valid
    // * lat-lon points of intersection that lie on both
    // * segments. Positions in the array not containing a valid
    // * lat/lon value are initialized to Float.MAX_VALUE.
    // */
    // public static float[] getSegIntersectionOrig(float lat1, float
    // lon1,
    // float lat2, float lon2,
    // float lat3, float lon3,
    // float lat4, float lon4) {
    // // KRA 03SEP03: We can do better than this.
    //
    // float[] ll = getIntersection(lat1,
    // lon1,
    // lat2,
    // lon2,
    // lat3,
    // lon3,
    // lat4,
    // lon4);
    //
    // // check if the point of intersection lies on both segs
    //
    // // length of seg1
    // double d1 = Geo.distance(lat1, lon1, lat2, lon2);
    // // length of seg2
    // double d2 = Geo.distance(lat3, lon3, lat4, lon4);
    //
    // // between seg1 endpoints and first point of intersection
    // double d111 = Geo.distance(lat1, lon1, ll[0], ll[1]);
    // double d121 = Geo.distance(lat2, lon2, ll[0], ll[1]);
    //
    // // between seg1 endpoints and second point of intersection
    // double d112 = Geo.distance(lat1, lon1, ll[2], ll[3]);
    // double d122 = Geo.distance(lat2, lon2, ll[2], ll[3]);
    //
    // // between seg2 endpoints and first point of intersection
    // double d211 = Geo.distance(lat3, lon3, ll[0], ll[1]);
    // double d221 = Geo.distance(lat4, lon4, ll[0], ll[1]);
    //
    // // between seg2 endpoints and second point of intersection
    // double d212 = Geo.distance(lat3, lon3, ll[2], ll[3]);
    // double d222 = Geo.distance(lat4, lon4, ll[2], ll[3]);
    //
    // float[] llp = new float[] { Float.MAX_VALUE, Float.MAX_VALUE,
    // Float.MAX_VALUE, Float.MAX_VALUE };
    //
    // // check if first point of intersection lies on both segments
    // if (d1 >= d111 && d1 >= d121 && d2 >= d211 && d2 >= d221) {
    // llp[0] = ll[0];
    // llp[1] = ll[1];
    // }
    //
    // // check if second point of intersection lies on both segments
    // if (d1 >= d112 && d1 >= d122 && d2 >= d212 && d2 >= d222) {
    // llp[2] = ll[2];
    // llp[3] = ll[3];
    // }
    //
    // return llp;
    // }

    /**
     * Does the segment come within near radians of the region defined
     * by rCenter at rRadius?
     */
    public static final boolean isSegmentNearRadialRegion(GeoSegment segment,
                                                          Geo rCenter,
                                                          double rRadius,
                                                          double near) {
        Geo[] s = segment.getSeg();
        if (s != null && s.length == 2) {
            return isSegmentNearRadialRegion(s[0], s[1], rCenter, rRadius, near);
        }
        return false;
    }

    /**
     * Does the segment come within near radians of the region defined
     * by rCenter at rRadius?
     */
    public static final boolean isSegmentNearRadialRegion(Geo s1, Geo s2,
                                                          Geo rCenter,
                                                          double rRadius,
                                                          double near) {
        return s1.isInside(s2, near + rRadius, rCenter);
    }

    /** Is a segment horizontally within range of a Region region? */
    public static final boolean isSegmentNearRegion(GeoSegment segment,
                                                    double hrange,
                                                    GeoRegion region) {
        // Need to be careful here - calling
        // region.isSegmentNear(segment, hrange) can result in
        // circular code if the region just calls this method, which
        // may seem reasonable, if you look at the API.
        return isSegmentNearPolyRegion(segment, region.toPointArray(), hrange);
    }

    /**
     * Does the segment come within near radians of the region defined
     * by the polygon in r[*]? Catches segments within poly region and
     * returns after first hit, which is why it returns boolean.
     */
    public static final boolean isSegmentNearPolyRegion(GeoSegment segment,
                                                        Geo[] r, double near) {
        Geo[] s = segment.getSeg();
        if (s != null && s.length == 2) {
            return isSegmentNearPolyRegion(s[0], s[1], r, near);
        }
        return false;
    }

    /**
     * Does the segment s1-s2 come within near radians of the region
     * defined by the polygon in r[*]? Catches segments within poly
     * region and returns after first hit, which is why it returns
     * boolean.
     */
    public static final boolean isSegmentNearPolyRegion(Geo s1, Geo s2,
                                                        Geo[] r, double near) {

        return isSegmentNearPoly(s1, s2, r, near) != null
                || isPointInPolygon(s1, r);
    }

    /**
     * Where does the segment come within near radians of the region
     * defined by the polygon in r[*]?
     * 
     * @return a List of Geos where the intersections occur. If the
     *         segment is contained within the region, an empty list
     *         is returned. If there are no intersections and the
     *         segment is not contained in the region, the method
     *         returns null.
     */
    public static final List segmentNearPolyRegion(GeoSegment segment, Geo[] r,
                                                   double near) {
        Geo[] s = segment.getSeg();
        List list = null;
        if (s != null && s.length == 2) {
            list = segmentNearPolyRegion(s[0], s[1], r, near);
        }
        return list;
    }

    /**
     * Where does the segment s1-s2 come within near radians of the
     * region defined by the polygon in r[*]?
     * 
     * @return a List of Geos where the intersections occur. If the
     *         segment is contained within the region, an empty list
     *         is returned. If there are no intersections and the
     *         segment is not contained in the region, the method
     *         returns null.
     */
    public static final List segmentNearPolyRegion(Geo s1, Geo s2, Geo[] r,
                                                   double near) {
        List list = segmentNearPoly(s1, s2, r, near);
        // second arg is geo[]!
        if (list == null && Intersection.isPointInPolygon(s1, r)) {
            list = new LinkedList();
        }

        return list;
    }

    /**
     * Where is a segment within range of a region?
     */
    public static final Geo isSegmentNearPoly(GeoSegment segment, Geo[] r,
                                              double near) {
        Geo[] s = segment.getSeg();
        if (s != null && s.length == 2) {
            return isSegmentNearPoly(s[0], s[1], r, near);
        }
        return null;
    }

    /**
     * Is a segment, represented by endpoints 's1' and 's2', withing a
     * range 'near' of region 'r'?
     * 
     * @param s1 Endpoint of segment
     * @param s2 Endpoint of segment
     * @param r Region of interest
     * @param near acceptable range between the segment and region, in
     *        radians.
     * @return Geo location where the condition was initially met
     *         (yes), null if conditions weren't met (no).
     */
    public static final Geo isSegmentNearPoly(Geo s1, Geo s2, Geo[] r,
                                              double near) {

        int rlen = r.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            Geo ret = segmentsIntersectOrNear(s1, s2, pl0, pl1, near);

            if (ret != null) {
                return ret;
            }

            pl0 = pl1;
        }
        return null;
    }

    /**
     * Where is a segment within range of a region?
     */
    public static final List segmentNearPoly(GeoSegment segment, Geo[] r,
                                             double near) {
        Geo[] s = segment.getSeg();
        List list = null;
        if (s != null && s.length == 2) {
            list = segmentNearPoly(s[0], s[1], r, near);
        }
        return list;
    }

    /**
     * Where is a segment, represented by endpoints 's1' and 's2',
     * withing a range 'near' of region 'r'?
     * 
     * @param s1 Endpoint of segment
     * @param s2 Endpoint of segment
     * @param r Region of interest
     * @param near acceptable range between the segment and region, in
     *        radians.
     * @return Geo location where the condition was met (yes), null if
     *         conditions weren't met (no).
     */
    public static final List segmentNearPoly(Geo s1, Geo s2, Geo[] r,
                                             double near) {
        int rlen = r.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        List list = null;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            Geo ret = segmentsIntersectOrNear(s1, s2, pl0, pl1, near);

            if (ret != null) {
                if (list == null) {
                    list = new LinkedList();
                }

                list.add(ret);
            }

            pl0 = pl1;
        }
        return list;
    }

    /**
     * Does the point s come within 'near' radians of the boarder of
     * the region defined by the polygon in r[*]?
     */
    public static final boolean isPointNearPoly(Geo s, Geo[] r, double near) {
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
     * Is one region's boundary within 'near' range of a region? Note:
     * good practice is s describes a smaller area than r.
     * 
     * @return the Geo location where the condition was first met,
     *         null if the condition wasn't met.
     */
    public static final Geo isPolyNearPoly(Geo[] s, Geo[] r, double near) {
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
                Geo ret = segmentsIntersectOrNear(sl0, sl1, pl0, pl1, near);

                if (ret != null) {
                    return ret;
                }
                sl0 = sl1;
            }
            pl0 = pl1;
        }
        return null;
    }

    /**
     * Is one region's boundary within 'near' range of a region? Note:
     * good practice is s describes a smaller area than r.
     * 
     * @return a List where the polys intersect within the range, null
     *         if the condition wasn't met.
     */
    public static final List polyNearPoly(Geo[] s, Geo[] r, double near) {
        int rlen = r.length;
        int slen = s.length;
        Geo pl0 = r[rlen - 1];
        Geo pl1;
        Geo sl0 = s[slen - 1];
        Geo sl1;
        List list = null;
        for (int j = 0; j < rlen; j++) {
            pl1 = r[j];
            for (int i = 0; i < slen; i++) {
                sl1 = s[i];
                Geo ret = segmentsIntersectOrNear(sl0, sl1, pl0, pl1, near);

                if (ret != null) {
                    if (list == null) {
                        list = new LinkedList();
                    }
                    list.add(ret);
                }
                sl0 = sl1;
            }
            pl0 = pl1;
        }

        return list;
    }

    /**
     * @return a Geo location iff the great circle segments defined by
     *         a1-a2 and b1-b2 intersect. the angles between the
     *         segments must be < PI or the results are
     *         ambiguous.Returns null if the segments don't interset
     *         within the range.
     */
    public static Geo segmentsIntersect(Geo a1, Geo a2, Geo b1, Geo b2) {
        return segmentsIntersectOrNear(a1, a2, b1, b2, 0);
    }

    /**
     * @return a Geo location iff the great circle segments defined by
     *         a1-a2 and b1-b2 come within the range (r, radians) of
     *         each other. The angles between the segments must be <
     *         PI or the results are ambiguous. Returns null if the
     *         segments don't interset within the range.
     */
    public static Geo segmentsIntersectOrNear(Geo a1, Geo a2, Geo b1, Geo b2,
                                              double r) {

        if (a1 == null || a2 == null || b1 == null || b2 == null) {
            return null;
        }

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
                return null;
            }
        }
        // i is intersection or anti-intersection point now.

        // Now see if it intersects with b
        if (i.distance(b1) <= bL && i.distance(b2) <= bL) {
            return i;
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        /**
         * Produces output: (1)=53.130104, -100.0 (2)=3.4028235E38,
         * -3.4028235E38 intersects=true polyIntersect=true
         * dist=655.4312 b3=true
         */

        float lat1 = 60;
        float lon1 = -130;
        float lat2 = 30;
        float lon2 = -70;

        float lat3 = 60;
        float lon3 = -70;
        float lat4 = 30;
        float lon4 = -130;

        float[] ll = getSegIntersection(lat1,
                -lon1,
                lat2,
                -lon2,
                lat3,
                -lon3,
                lat4,
                -lon4);

        System.out.println("(1)=" + ll[0] + ", " + (-ll[1]));
        System.out.println("(2)=" + ll[2] + ", " + (-ll[3]));

        boolean b1 = intersects(lat1,
                -lon1,
                lat2,
                -lon2,
                lat3,
                -lon3,
                lat4,
                -lon4);

        System.out.println("intersects=" + b1);

        float[] polypoints1 = new float[] { 38, -27, -46, 165 };

        float[] polypoints2 = new float[] { 51, -42, 55, -17, 11, -23, 51, -42 };

        boolean b2 = polyIntersect(polypoints1, polypoints2);

        System.out.println("polyIntersect=" + b2);
    }
}
