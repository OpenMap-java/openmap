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

/**
 * Contains great circle intersection algorithms and helper methods.
 * Sources: http://williams.best.vwh.net/intersect.htm
 * http://mathforum.org/library/drmath/view/60711.html
 * 
 * @author Sachin Date
 * @author Ken Anderson
 * @version $Revision: 1.6 $ on $Date: 2005/06/23 22:57:40 $
 */
public class Intersection {

    /**
     * returns the two antipodal points of interection of two great
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

    public static float[] intersect2(float lat1, float lon1, float lat2,
                                     float lon2, float lat3, float lon3,
                                     float lat4, float lon4) {
        return getIntersection(lat1, lon1, lat2, lon2, lat3, lon3, lat4, lon4);
    }

    /**
     * returns the point of interection of two great circle segments
     * defined by the segments. (lat1, lon1) to (lat2, lon2) and
     * (lat2, lon2) to (lat4, lon4). All lat-lon values are in
     * degrees.
     * 
     * @return a float array of length 4 containing upto 2 valid
     *         lat-lon points of intersection that lie on both
     *         segments. Positions in the array not containing a valid
     *         lat/lon value are initialized to Float.MAX_VALUE.
     */
    public static float[] getSegIntersectionOrig(float lat1, float lon1,
                                                 float lat2, float lon2,
                                                 float lat3, float lon3,
                                                 float lat4, float lon4) {
        // KRA 03SEP03: We can do better than this.

        float[] ll = getIntersection(lat1,
                lon1,
                lat2,
                lon2,
                lat3,
                lon3,
                lat4,
                lon4);

        // check if the point of intersection lies on both segs

        // length of seg1
        double d1 = Geo.distance(lat1, lon1, lat2, lon2);
        // length of seg2
        double d2 = Geo.distance(lat3, lon3, lat4, lon4);

        // between seg1 endpoints and first point of intersection
        double d111 = Geo.distance(lat1, lon1, ll[0], ll[1]);
        double d121 = Geo.distance(lat2, lon2, ll[0], ll[1]);

        // between seg1 endpoints and second point of intersection
        double d112 = Geo.distance(lat1, lon1, ll[2], ll[3]);
        double d122 = Geo.distance(lat2, lon2, ll[2], ll[3]);

        // between seg2 endpoints and first point of intersection
        double d211 = Geo.distance(lat3, lon3, ll[0], ll[1]);
        double d221 = Geo.distance(lat4, lon4, ll[0], ll[1]);

        // between seg2 endpoints and second point of intersection
        double d212 = Geo.distance(lat3, lon3, ll[2], ll[3]);
        double d222 = Geo.distance(lat4, lon4, ll[2], ll[3]);

        float[] llp = new float[] { Float.MAX_VALUE, Float.MAX_VALUE,
                Float.MAX_VALUE, Float.MAX_VALUE };

        // check if first point of intersection lies on both segments
        if (d1 >= d111 && d1 >= d121 && d2 >= d211 && d2 >= d221) {
            llp[0] = ll[0];
            llp[1] = ll[1];
        }

        // check if second point of intersection lies on both segments
        if (d1 >= d112 && d1 >= d122 && d2 >= d212 && d2 >= d222) {
            llp[2] = ll[2];
            llp[3] = ll[3];
        }

        return llp;
    }

    public static Geo getIntersectionGeo(float lat1, float lon1, float lat2,
                                         float lon2, float lat3, float lon3,
                                         float lat4, float lon4) {

        Geo geoCross1 = (new Geo(lat1, lon1)).crossNormalize(new Geo(lat2, lon2));
        Geo geoCross2 = (new Geo(lat3, lon3)).crossNormalize(new Geo(lat4, lon4));

        return geoCross1.crossNormalize(geoCross2);
    }

    public static boolean intersects2(float lat1, float lon1, float lat2,
                                      float lon2, float lat3, float lon3,
                                      float lat4, float lon4) {
        return intersects(lat1, lon2, lat2, lon2, lat3, lon3, lat4, lon4);
    }

    /**
     * returns true if the two segs intersect in at least one point.
     * All lat-lon values are in degrees.
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
     * checks if the two polygonal areas intersect. The two polygonal
     * regions are represented by two lat-lon arrays in the lat1,
     * lon1, lat2, lon2,... format. For closed polygons the last pair
     * of points in the array should be the same as the first pair.
     * All lat-lon values are in degrees.
     */
    public static boolean polyIntersect(float[] polyPoints1, float[] polyPoints2) {

        // go through each side of poly1 and test to see if it
        // intersects
        // with any side of poly2

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
     * (lat2, lon2) All lat-lon values are in degrees.
     */
    public static float pointCircleDistanceNM(Geo p1, Geo p2, Geo center) {

        Geo n = Geo.crossNormalize(p1, p2);
        Geo c = center;
        c = c.normalize();
        double cosTheta = Geo.dot(n, c);
        double theta = Math.acos(cosTheta);

        double distNM = Geo.nm(Math.abs(Math.PI / 2 - theta));

        return (float) distNM;
    }

    /**
     * Point i is on the great circle defined by the points a and b.
     * Returns true if i is between a and b, false otherwise.
     */
    public static boolean isOnSegment(Geo a, Geo b, Geo i) {

        //assert (< (Math.abs (.dot (.crossNormalize a b) i))
        // 1.e-15))

        return ((a.distance(i) < a.distance(b)) && (b.distance(i) < b.distance(a)));
    }

    /**
     * Returns the Geo point i, which is on the great circle segment
     * between Geo points a and b and which is closest to Geo point c
     * or returns null if there is no such point.
     */
    public static Geo segIntersection(Geo a, Geo b, Geo c) {

        Geo g = a.crossNormalize(b); // Normal to great circle between
        // a and b
        Geo f = c.crossNormalize(g); // Normal to the great circle
        // between c and g
        Geo i = f.crossNormalize(g); // The intersection is normal to
        // both
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

        Geo a = new Geo(lat1, lon1);
        Geo b = new Geo(lat2, lon2);
        Geo c = new Geo(lat, lon);

        Geo i = segIntersection(a, b, c);

        if (i == null)
            return -1f;
        else
            return (float) c.distanceNM(i);

    }

    /**
     * Returns true or false depending on whether the great circle seg
     * from point (lat1, lon1) to (lat2, lon2) intersects the circle
     * of radius radiusNM (nautical miles) centered at point (lat,
     * lon). All lat-lon values are in degrees.
     */
    public static boolean intersectsCircle(Geo p1, Geo p2, Geo center,
                                           float radiusNM) {

        // check if either of the end points of the seg are inside the
        // circle
        double d1 = Geo.distanceNM(p1, center);
        if (d1 < radiusNM)
            return true;

        double d2 = Geo.distanceNM(p2, center);
        if (d2 < radiusNM)
            return true;

        float distNM = pointCircleDistanceNM(p1, p2, center);

        if (distNM > radiusNM)
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
        if (c.distanceNM(i) < radiusNM) {
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
        else if (c.distanceNM(i2) < radiusNM) {
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
     */
    public static boolean intersectsCircle(float[] polyPoints, float lat,
                                           float lon, float radiusNM) {

        for (int i = 0; i < polyPoints.length / 2 - 1; i++) {

            float lat1 = polyPoints[2 * i];
            float lon1 = polyPoints[2 * i + 1];
            float lat2 = polyPoints[2 * i + 2];
            float lon2 = polyPoints[2 * i + 3];

            if (intersectsCircle(new Geo(lat1, lon1),
                    new Geo(lat2, lon2),
                    new Geo(lat, lon),
                    radiusNM))
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
     *   
     *    
     *     poly[0] = latitude 1
     *     poly[1] = longitude 1
     *     poly[2] = latitude 2
     *     poly[3] = longitude 2
     *     .
     *     .
     *     .
     *     poly[n-1] = latitude 1
     *     poly[n] = longitude 1
     *     
     *    
     *   
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
        return in;
    }

    /**
     * Ask if a Geo point is in a polygon, with the poly coordinates specified in radians.
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...] are in radians
     * @return true for Geo in poly
     */
    public static boolean isPointInPolygonRadians(Geo x, float[] poly) {
        return isPointInPolygon(x, poly, false);
    }

    /**
     * Ask if a Geo point is in a polygon, with the poly coordinates specified in decimal degrees.
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...] are in decimal degrees
     * @return true for Geo in poly
     */
    public static boolean isPointInPolygon(Geo x, float[] poly) {
        return isPointInPolygon(x, poly, true);
    }

    /**
     * Ask if a Geo point is in a polygon.
     * @param x
     * @param poly float array where [lat, lon, lat, lon,...] 
     * @param polyInDegrees true of poly floats represent decimal degrees.
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
}
