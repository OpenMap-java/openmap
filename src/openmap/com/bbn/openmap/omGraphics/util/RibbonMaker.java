package com.bbn.openmap.omGraphics.util;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.GeoArray;
import com.bbn.openmap.geo.Intersection;
import com.bbn.openmap.geo.Ribbon;
import com.bbn.openmap.geo.RibbonIterator;
import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Length;

/**
 * The RibbonMaker class takes polygon coordinates and creates another polygon
 * from it, a buffer region based on ground distance, around the original poly.
 * It assumes that the polygon coordinates are going in a clockwise direction.
 * The returned polygon is actually an OMAreaList made up of OMPolys, OMArcs and
 * OMLines, depending on the shape of the original polygon.
 * <p>
 * 
 * The RibbonMaker is created from one of the factory methods that designates
 * whether the coordinates are radians or decimal degrees. FYI - OMPoly
 * coordinates retrieved from the getLatLonArray method are in radians. After
 * the RibbonMaker is created, call the method that creates the appropriate
 * shape. For now, getOuterRing is your only option. To use:
 * 
 * <pre>
 * 
 * llPoints = new double[] {
 *     40.0f,
 *     -92.0f,
 *     42.0f,
 *     -87.0f,
 *     38.57,
 *     -90.825,
 *     37.0f,
 *     -89.0f,
 *     35.0f,
 *     -94.0f,
 *     40.0f,
 *     -92.0f
 * };
 * 
 * OMGraphic omg = RibbonMaker.createFromDecimalDegrees(llPoints).getOuterRing(Length.MILE.toRadians(100));
 * 
 * </pre>
 * 
 * 
 * @author ddietrick
 */
public class RibbonMaker {

    protected GeoArray geoCoords;
    private static final Logger logger = Logger.getLogger("com.bbn.openmap.omGraphics.util.RibbonMaker");
    protected final static int STRAIGHT = 0;
    protected final static int BENDS_LEFT = -1;
    protected final static int BENDS_RIGHT = 1;
    protected double bufferLimit = 4.778825E-10;

    protected RibbonMaker(GeoArray gCoords) {
        geoCoords = gCoords;
    }

    /**
     * Create a RibbonMaker from decimal degree coordinates.
     * 
     * @param coords lat,lon,lat,lon in decimal degrees.
     * @return RibbonMaker
     */
    public static RibbonMaker createFromDecimalDegrees(double[] coords) {
        return new RibbonMaker(GeoArray.Double.createFromLatLonDegrees(coords));
    }

    /**
     * Create a RibbonMaker from radian coordinates.
     * 
     * @param coords lat,lon,lat,lon in radians.
     * @return RibbonMaker
     */
    public static RibbonMaker createFromRadians(double[] coords) {
        return new RibbonMaker(GeoArray.Double.createFromLatLonRadians(coords));
    }

    /**
     * Assumes coords represent a polygon, returns an OMAreaList representing
     * buffer zone around the outside of a polygon.
     * 
     * @param dist distance of buffer area, in radians. Use Length to convert.
     * @return OMAreaList of a polygon that is a distance away from the
     *         coordinate polygon's edges.
     */
    public OMAreaList getOuterRing(double dist) {
        OMAreaList ret = new OMAreaList();

        if (dist <= bufferLimit) {
            return ret;
        }

        int numCoords = geoCoords.getSize();
        if (numCoords >= 3) {

            Geo g1 = geoCoords.get(0);
            Geo g2 = geoCoords.get(1);
            Geo g3 = geoCoords.get(2);

            handlePointsForOuterRing(g1, g2, g3, dist, ret);

            for (int i = 3; i < numCoords; i++) {
                g1 = g2;
                g2 = g3;
                g3 = geoCoords.get(i);

                handlePointsForOuterRing(g1, g2, g3, dist, ret);
            }

            // test, and close it off if needed
            if (!geoCoords.get(0).equals(geoCoords.get(numCoords - 1))) {
                g1 = g2;
                g2 = g3;
                g3 = geoCoords.get(0);

                handlePointsForOuterRing(g1, g2, g3, dist, ret);
            }

            // Now round out the first and last segment, centering on the first
            // coordinate
            g1 = g2;
            g2 = g3;
            g3 = geoCoords.get(1);

            handlePointsForOuterRing(g1, g2, g3, dist, ret);

        }
        return ret;
    }

    /**
     * Takes a corner represented by the three geos, and adds OMGraphics to the
     * OMAreaList depending on which way the corner bends - for right turns,
     * it'll add an OMLine, OMArc and OMLine. The OMLines will go from half the
     * distance of the legs to the rounded corner. The left turn will have a
     * polygon added.
     * 
     * @param g1 point 1
     * @param g2 point 2
     * @param g3 point 3
     * @param dist buffer distance in radians
     * @param ret OMAreaList to add OMGraphics to.
     */
    protected void handlePointsForOuterRing(Geo g1, Geo g2, Geo g3, double dist, OMAreaList ret) {
        int bend = bends(g1, g2, g3);

        Geo gret = g3;
        RibbonIterator leg1 = new RibbonIterator(g1, g2, dist);
        OMPoly poly1 = getHalfPoly(leg1, Ribbon.LEFT, false);

        RibbonIterator leg2 = new RibbonIterator(g2, g3, dist);
        OMPoly poly2 = getHalfPoly(leg2, Ribbon.LEFT, true);

        if (bend == STRAIGHT || g2.equals(g3)) {
            ret.add(poly1);
            ret.add(poly2);
        } else {
            if (bend == BENDS_LEFT) {
                // short, need to find intersection of two legs and remove
                // points
                // from polys to only go to intersection

                double dg12 = g1.distance(g2);
                double dg23 = g2.distance(g3);
                double legTestDist = dist * 2;

                if (dg12 < legTestDist || dg23 < legTestDist) {
                    addShortLegPolyForIntersection(g1, g2, g3, Ribbon.LEFT, dist, ret);
                } else {
                    addPolyForIntersection(poly1, poly2, dist, ret);
                }

            } else {
                OMGraphic omp = getPushbackPoly(poly1, dist);
                if (omp != null) {
                    ret.add(omp);
                }
                // Add OMArc in the middle, rounding around a corner
                OMGraphic oma = getArc(g2, poly1, poly2);
                if (oma != null) {
                    ret.add(oma);
                }
                omp = getPushbackPoly(poly2, dist);
                if (omp != null) {
                    ret.add(omp);
                }
            }
        }

    }

    /**
     * Method that determines which way the angle between the three points
     * bends.
     * 
     * @param g1
     * @param g2
     * @param g3
     * @return STRAIGHT if no bend, BENDS_LEFT if bends less than PI,
     *         BENDS_RIGHT if bends more than PI.
     */
    protected int bends(Geo g1, Geo g2, Geo g3) {
        double bend = g1.crossNormalize(g2).distance(g3) - (Math.PI / 2.0);

        if (Math.abs(bend) < .0001) {
            return STRAIGHT; // essentially straight
        } else {
            if (bend < 0) {
                return BENDS_LEFT;
            }
        }

        return BENDS_RIGHT;
    }

    /**
     * Checks to see if a point is too close to any side of the original
     * polygon.
     * 
     * @param pnt
     * @param distance
     * @return true if is too close and should not be added to the buffer
     *         polygon.
     */
    protected boolean tooClose(Geo pnt, double distance) {
        return Intersection.isPointNearPoly(pnt, geoCoords, distance - bufferLimit);
    }

    /**
     * Takes a poly that's going to be added to the buffer and removes any
     * points that may be too close to the original poly.
     * 
     * @param omp the buffer poly to be added later
     * @param dist the distance all points should be from the original
     * @return the OMGraphic with good points.
     */
    protected OMGraphic getPushbackPoly(OMPoly omp, double dist) {
        double[] coords = omp.getLatLonArray();
        List<Geo> results = new LinkedList<Geo>();
        for (int i = 0; i < coords.length - 2; i += 2) {
            Geo g = new Geo(coords[i], coords[i + 1], false);
            if (!tooClose(g, dist)) {
                results.add(g);
            }
        }

        if (results.size() == 1) {
            results.add(new Geo(results.get(0)));
        }

        if (results.size() > 1) {
            return getOMPolyFromGeos(results);
        }

        return null;

    }

    /**
     * Called when it's known that one of the legs between the corner is smaller
     * than the buffer depth. Does some extra work to figure out what points
     * should be added to the buffer.
     * 
     * @param g1 point 1
     * @param g2 point 2, the corner
     * @param g3 point 3
     * @param ribbonSide which side of the ribbon should be calculated.
     * @param dist the distance the buffer should be from the legs
     * @param ret the OMGraphicList to add the resulting poly to.
     */
    protected void addShortLegPolyForIntersection(Geo g1, Geo g2, Geo g3, int ribbonSide, double dist, OMAreaList ret) {

        /**
         * We need to do some extra work here. Since one of the legs is shorter
         * than 2*dist of the buffer, half of the buffer polygon won't reach the
         * intersection point between the two legs. So, we need to recalculate
         * the polys so the represent the entire legs, in order to find the
         * intersection point.
         * 
         * Once we have that, we can go back to the half polygons, and test for
         * that intersection point against each little segment. If any of those
         * points are further away than buffer distance to the opposite poly,
         * they should be included on the polygon added to the list. If a point
         * is inside that distance, it should be disregarded. If there is only
         * one point (i.e. the original intersection point), then a duplicate
         * intersection point should be added, so the OMAreaList will handle it
         * properly.
         */
        List<Geo> results = new LinkedList<Geo>();
        RibbonIterator leg1 = new RibbonIterator(g1, g2, dist);
        OMPoly fullPoly1 = getPoly(leg1, ribbonSide);

        RibbonIterator leg2 = new RibbonIterator(g2, g3, dist);
        OMPoly fullPoly2 = getPoly(leg2, ribbonSide);

        // Intersection is the point on both polys that is buffer distance away
        // from corner
        Geo intersection = getPolyIntersection(fullPoly1, fullPoly2);

        if (intersection == null) {
            // GAAH! This shouldn't happen
            return;
        }

        leg1 = new RibbonIterator(g1, g2, dist);
        OMPoly halfPoly1 = getHalfPoly(leg1, ribbonSide, false);
        GeoArray geoPoly2 = GeoArray.Double.createFromLatLonRadians(fullPoly2.getLatLonArray());

        double[] leg1Coords = halfPoly1.getLatLonArray();
        for (int i = 0; i < leg1Coords.length - 1; i += 2) {
            Geo pnt = new Geo(leg1Coords[i], leg1Coords[i + 1], false);

            if (!tooClose(pnt, dist)) {
                results.add(pnt);
            }
        }

        if (!tooClose(intersection, dist)) {
            results.add(intersection);
        }

        leg2 = new RibbonIterator(g2, g3, dist);
        OMPoly halfPoly2 = getHalfPoly(leg2, ribbonSide, true);
        GeoArray geoPoly1 = GeoArray.Double.createFromLatLonRadians(fullPoly1.getLatLonArray());

        double[] leg2Coords = halfPoly2.getLatLonArray();
        for (int i = 0; i < leg2Coords.length - 1; i += 2) {
            Geo pnt = new Geo(leg2Coords[i], leg2Coords[i + 1], false);

            if (!tooClose(pnt, dist)) {
                results.add(pnt);
            }
        }

        if (results.size() == 1 && !tooClose(intersection, dist)) {
            results.add(intersection);
        }

        if (results.size() > 1) {
            ret.add(getOMPolyFromGeos(results));
        }
    }

    /**
     * Just return the point where the two polygons cross.
     * 
     * @param poly1
     * @param poly2
     * @return null if no point found.
     */
    protected Geo getPolyIntersection(OMPoly poly1, OMPoly poly2) {
        double[] p1Coords = poly1.getLatLonArray();
        double[] p2Coords = poly2.getLatLonArray();

        Geo a1, a2, b1, b2;
        Geo intersect = null;
        int index1 = 0, index2 = 0;
        for (; index2 + 3 < p2Coords.length; index2 += 2) {
            b1 = new Geo(p2Coords[index2], p2Coords[index2 + 1], false);
            b2 = new Geo(p2Coords[index2 + 2], p2Coords[index2 + 3], false);

            if (intersect == null) {
                for (; index1 + 3 < p1Coords.length; index1 += 2) {

                    a1 = new Geo(p1Coords[index1], p1Coords[index1 + 1], false);
                    a2 = new Geo(p1Coords[index1 + 2], p1Coords[index1 + 3], false);

                    intersect = Intersection.segmentsIntersect(a1, a2, b1, b2);

                    if (intersect != null) {
                        return intersect;
                    }
                }
            }
        }

        return intersect;
    }

    /**
     * Converts Vector of Geos to an OMPoly with linetype great_circle. Assumes
     * that the List has valid coordinates on it. Does not do a closeness check
     * to the original poly, expected that's been done.
     * 
     * @param geos a set of coordinates
     * @return OMPoly
     */
    protected OMPoly getOMPolyFromGeos(List<Geo> geos) {
        double[] tmpCoords = new double[geos.size() * 2];
        int index = 0;
        for (Geo geo : geos) {
            tmpCoords[index++] = geo.getLatitudeRadians();
            tmpCoords[index++] = geo.getLongitudeRadians();
        }

        return new OMPoly(tmpCoords, OMPoly.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);
    }

    /**
     * Called to handle BENDS_LEFT, concave corners. RuntimeException is thrown
     * when one of the legs is much shorter than the other and the mid-point is
     * still in the buffer of one of the legs. In that special case, the
     * intersection point is not found.
     * 
     * @param poly1
     * @param poly2
     * @param ret
     */
    protected void addPolyForIntersection(OMPoly poly1, OMPoly poly2, double dist, OMAreaList ret) {
        double[] p1Coords = poly1.getLatLonArray();
        double[] p2Coords = poly2.getLatLonArray();

        List<Geo> results = new LinkedList<Geo>();

        Geo a1, a2, b1, b2;
        Geo intersect = null;
        int index1 = 0, index2 = 0;
        for (; index2 + 3 < p2Coords.length; index2 += 2) {
            b1 = new Geo(p2Coords[index2], p2Coords[index2 + 1], false);
            b2 = new Geo(p2Coords[index2 + 2], p2Coords[index2 + 3], false);

            if (intersect == null) {
                for (; index1 + 3 < p1Coords.length; index1 += 2) {

                    a1 = new Geo(p1Coords[index1], p1Coords[index1 + 1], false);
                    a2 = new Geo(p1Coords[index1 + 2], p1Coords[index1 + 3], false);

                    intersect = Intersection.segmentsIntersect(a1, a2, b1, b2);

                    if (!tooClose(a1, dist)) {
                        results.add(a1);
                    }

                    if (intersect != null) {
                        if (!tooClose(intersect, dist)) {
                            results.add(intersect);
                        }
                        break;
                    }
                }
            }

            if (intersect != null && !tooClose(b2, dist)) {
                results.add(b2);
            }
        }

        if (results.size() > 1) {
            ret.add(getOMPolyFromGeos(results));
        }
    }

    /**
     * Given a RibbonIterator created from two Geos, create a poly from half of
     * that buffer path. Points are not checked for closeness to original poly.
     * 
     * @param rIterator RibbonIterator for one of the legs of corner
     * @param side which RibbonIterator side
     * @param first which half you want, true for first part
     * @return OMPoly that represents half of the buffered path.
     */
    protected OMPoly getHalfPoly(RibbonIterator rIterator, int side, boolean first) {
        List<Geo> results = new LinkedList<Geo>();
        for (Ribbon rib : rIterator) {
            Geo g = rib.get(side);
            results.add(g);
        }

        int numCoords = results.size();

        if (numCoords > 0) {

            int startingIndex = 0;
            int copyLength = numCoords / 2;

            if (numCoords % 2 == 0) {
                if (!first) {
                    startingIndex = copyLength; // middle
                }
            } else {
                if (!first) {
                    startingIndex = copyLength;
                }
                copyLength++;
            }

            List<Geo> newGeoCoords = new LinkedList<Geo>();
            for (int index = 0; index < copyLength; index++) {
                Geo g = results.get(startingIndex + index);
                newGeoCoords.add(g);
            }

            return getOMPolyFromGeos(newGeoCoords);
        }
        return null;
    }

    /**
     * Given a RibbonIterator created from two Geos, create a poly from that
     * buffer path.
     * 
     * @param rIterator RibbonIterator for one of the legs of corner
     * @param side which RibbonIterator side
     * @return OMPoly that represents buffered path between geos.
     */
    protected OMPoly getPoly(RibbonIterator rIterator, int side) {
        List<Geo> bufferCoords = new LinkedList<Geo>();
        for (Ribbon rib : rIterator) {
            bufferCoords.add(rib.get(side));
        }

        if (bufferCoords.size() > 1) {
            return getOMPolyFromGeos(bufferCoords);
        }
        return null;
    }

    /**
     * Given two polylines, with the end point of poly1 being the same distance
     * from a point as the starting point of poly2, create an arc that connects
     * them.
     * 
     * @param gc point
     * @param poly1 polyline where the last end point is used
     * @param poly2 polyline where the first end point is used.
     * @return OMArc
     */
    public OMGraphic getArc(Geo gc, OMPoly poly1, OMPoly poly2) {

        double[] poly1Coords = poly1.getLatLonArray();
        Geo pt1 = new Geo(poly1Coords[poly1Coords.length - 2], poly1Coords[poly1Coords.length - 1], false);
        double radAngle1 = gc.azimuth(pt1);

        double[] poly2Coords = poly2.getLatLonArray();
        Geo pt2 = new Geo(poly2Coords[0], poly2Coords[1], false);
        double radAngle2 = gc.azimuth(pt2);

        double dist = gc.distance(pt1);

        if (radAngle2 < radAngle1) {
            radAngle2 += MoreMath.TWO_PI_D;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(new StringBuilder("Making arg starting at ").append(Length.DECIMAL_DEGREE.fromRadians(radAngle1))
                                                                    .append(", ")
                                                                    .append(Length.DECIMAL_DEGREE.fromRadians(radAngle2 - radAngle1))
                                                                    .toString());
        }

        List<Geo> points = new LinkedList<Geo>();
        double inc = Length.DECIMAL_DEGREE.toRadians(2.0);
        double angle = radAngle1 + inc;

        while (angle < radAngle2 - inc) {
            Geo g = gc.offset(dist, angle);
            if (!tooClose(g, dist)) {
                points.add(g);
            }
            angle += inc;
        }

        return getOMPolyFromGeos(points);
        // return new OMArc(gc.getLatitude(), gc.getLongitude(), dist,
        // Length.RADIAN, 100, Length.DECIMAL_DEGREE.fromRadians(radAngle1),
        // Length.DECIMAL_DEGREE.fromRadians(radAngle2 - radAngle1));

    }
}
