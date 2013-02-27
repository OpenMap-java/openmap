// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjMath.java,v $
// $RCSfile: ProjMath.java,v $
// $Revision: 1.9 $
// $Date: 2007/06/21 21:39:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Math functions used by projection code.
 */
public final class ProjMath {

    /**
     * North pole latitude in radians.
     */
    public final static transient float NORTH_POLE_F = MoreMath.HALF_PI;

    /**
     * South pole latitude in radians.
     */
    public final static transient float SOUTH_POLE_F = -NORTH_POLE_F;

    /**
     * North pole latitude in radians.
     */
    public final static transient double NORTH_POLE_D = MoreMath.HALF_PI_D;

    /**
     * North pole latitude in degrees.
     */
    public final static transient double NORTH_POLE_DEG_D = 90d;

    /**
     * South pole latitude in radians.
     */
    public final static transient double SOUTH_POLE_D = -NORTH_POLE_D;

    /**
     * South pole latitude in degrees.
     */
    public final static transient double SOUTH_POLE_DEG_D = -NORTH_POLE_DEG_D;

    /**
     * Dateline longitude in radians.
     */
    public final static transient float DATELINE_F = (float) Math.PI;

    /**
     * Dateline longitude in radians.
     */
    public final static transient double DATELINE_D = Math.PI;

    /**
     * Dateline longitude in degrees.
     */
    public final static transient double DATELINE_DEG_D = 180d;

    /**
     * Longitude range in radians.
     */
    public final static transient float LON_RANGE_F = (float) MoreMath.TWO_PI;

    /**
     * Longitude range in radians.
     */
    public final static transient double LON_RANGE_D = MoreMath.TWO_PI_D;

    /**
     * Longitude range in degrees.
     */
    public final static transient double LON_RANGE_DEG_D = 360d;

    // cannot construct
    private ProjMath() {
    }

    /**
     * rounds the quantity away from 0.
     * 
     * @param x in value
     * @return double
     * @see #qint(double)
     */
    public final static double roundAdjust(double x) {
        return qint_old(x);
    }

    /**
     * Rounds the quantity away from 0.
     * 
     * @param x value
     * @return double
     */
    public final static double qint(double x) {
        return qint_new(x);
    }

    final private static double qint_old(double x) {
        return (((int) x) < 0) ? (x - 0.5) : (x + 0.5);
    }

    final private static double qint_new(double x) {
        // -1 or +1 away from zero
        return (x <= 0.0) ? (x - 1.0) : (x + 1.0);
    }

    /**
     * Calculate the shortest arc distance between two lons.
     * 
     * @param lon1 radians
     * @param lon2 radians
     * @return float distance
     */
    public final static float lonDistance(float lon1, float lon2) {
        return (float) Math.min(Math.abs(lon1 - lon2), ((lon1 < 0) ? lon1 + Math.PI : Math.PI
                - lon1)
                + ((lon2 < 0) ? lon2 + Math.PI : Math.PI - lon2));
    }

    /**
     * Convert between decimal degrees and scoords.
     * 
     * @param deg degrees
     * @return long scoords
     * 
     */
    public final static long DEG_TO_SC(double deg) {
        return (long) (deg * 3600000);
    }

    /**
     * Convert between decimal degrees and scoords.
     * 
     * @param sc scoords
     * @return double decimal degrees
     */
    public final static double SC_TO_DEG(int sc) {
        return ((double) (sc) / (60.0 * 60.0 * 1000.0));
    }

    /**
     * Convert radians to degrees.
     * 
     * @param rad radians
     * @return double decimal degrees
     */
    public final static double radToDeg(double rad) {
        return Math.toDegrees(rad);
    }

    /**
     * Convert radians to degrees.
     * 
     * @param rad radians
     * @return float decimal degrees
     */
    public final static float radToDeg(float rad) {
        return (float) Math.toDegrees((double) rad);
    }

    /**
     * Convert degrees to radians.
     * 
     * @param deg degrees
     * @return double radians
     */
    public final static double degToRad(double deg) {
        return Math.toRadians(deg);
    }

    /**
     * Convert degrees to radians.
     * 
     * @param deg degrees
     * @return float radians
     */
    public final static float degToRad(float deg) {
        return (float) Math.toRadians((double) deg);
    }

    /**
     * Generate a hashCode value for a lat/lon pair.
     * 
     * @param lat latitude
     * @param lon longitude
     * @return int hashcode
     * 
     */
    public final static int hashLatLon(float lat, float lon) {
        if (lat == -0f)
            lat = 0f;// handle negative zero (anything else?)
        if (lon == -0f)
            lon = 0f;
        int tmp = Float.floatToIntBits(lat);
        int hash = (tmp << 5) | (tmp >> 27);// rotate the lat bits
        return hash ^ Float.floatToIntBits(lon);// XOR with lon
    }

    /**
     * Converts an array of decimal degrees float lat/lons to float radians in
     * place.
     * 
     * @param degs float[] lat/lons in decimal degrees
     * @return float[] lat/lons in radians
     */
    public final static float[] arrayDegToRad(float[] degs) {
        for (int i = 0; i < degs.length; i++) {
            degs[i] = degToRad(degs[i]);
        }
        return degs;
    }

    /**
     * Converts an array of radian float lat/lons to decimal degrees in place.
     * 
     * @param rads float[] lat/lons in radians
     * @return float[] lat/lons in decimal degrees
     */
    public final static float[] arrayRadToDeg(float[] rads) {
        for (int i = 0; i < rads.length; i++) {
            rads[i] = radToDeg(rads[i]);
        }
        return rads;
    }

    /**
     * Converts an array of decimal degrees double lat/lons to double radians in
     * place.
     * 
     * @param degs double[] lat/lons in decimal degrees
     * @return double[] lat/lons in radians
     */
    public final static double[] arrayDegToRad(double[] degs) {
        for (int i = 0; i < degs.length; i++) {
            degs[i] = degToRad(degs[i]);
        }
        return degs;
    }

    /**
     * Converts an array of radian double lat/lons to decimal degrees in place.
     * 
     * @param rads double[] lat/lons in radians
     * @return double[] lat/lons in decimal degrees
     */
    public final static double[] arrayRadToDeg(double[] rads) {
        for (int i = 0; i < rads.length; i++) {
            rads[i] = radToDeg(rads[i]);
        }
        return rads;
    }

    /**
     * @deprecated use normalizeLatitude instead.
     */
    public final static float normalize_latitude(float lat, float epsilon) {
        return normalizeLatitude(lat, epsilon);
    }

    /**
     * Normalizes radian latitude. Normalizes latitude if at or exceeds epsilon
     * distance from a pole.
     * 
     * @param lat float latitude in radians
     * @param epsilon epsilon (&gt;= 0) radians distance from pole
     * @return float latitude (-PI/2 &lt;= phi &lt;= PI/2)
     * @see com.bbn.openmap.proj.coords.LatLonPoint#normalizeLatitude(double)
     */
    public final static float normalizeLatitude(float lat, float epsilon) {
        if (lat > NORTH_POLE_F - epsilon) {
            return NORTH_POLE_F - epsilon;
        } else if (lat < SOUTH_POLE_F + epsilon) {
            return SOUTH_POLE_F + epsilon;
        }
        return lat;
    }

    /**
     * @deprecated use normalizeLatitude instead.
     */
    public final static double normalize_latitude(double lat, double epsilon) {
        return normalizeLatitude(lat, epsilon);
    }

    /**
     * Normalizes radian latitude. Normalizes latitude if at or exceeds epsilon
     * distance from a pole.
     * 
     * @param lat double latitude in radians
     * @param epsilon epsilon (&gt;= 0) radians distance from pole
     * @return double latitude (-PI/2 &lt;= phi &lt;= PI/2)
     * @see com.bbn.openmap.proj.coords.LatLonPoint#normalizeLatitude(double)
     */
    public final static double normalizeLatitude(double lat, double epsilon) {
        if (lat > NORTH_POLE_D - epsilon) {
            return NORTH_POLE_D - epsilon;
        } else if (lat < SOUTH_POLE_D + epsilon) {
            return SOUTH_POLE_D + epsilon;
        }
        return lat;
    }

    /**
     * @deprecated use wrapLongitde instead.
     */
    public final static float wrap_longitude(float lon) {
        return wrapLongitude(lon);
    }

    /**
     * Sets radian longitude to something sane.
     * 
     * @param lon float longitude in radians
     * @return float longitude (-PI &lt;= lambda &lt; PI)
     */
    public final static float wrapLongitude(float lon) {
        if ((lon < -DATELINE_F) || (lon > DATELINE_F)) {
            lon += DATELINE_F;
            lon %= LON_RANGE_F;
            lon += (lon < 0) ? DATELINE_F : -DATELINE_F;
        }
        return lon;
    }

    /**
     * @deprecated use wrapLongitude instead.
     */
    public final static double wrap_longitude(double lon) {
        return wrapLongitude(lon);
    }

    /**
     * Sets radian longitude to something sane.
     * 
     * @param lon double longitude in radians
     * @return double longitude (-PI &lt;= lambda &lt; PI)
     */
    public final static double wrapLongitude(double lon) {
        if ((lon < -DATELINE_D) || (lon > DATELINE_D)) {
            lon += DATELINE_D;
            lon %= LON_RANGE_D;
            lon += (lon < 0) ? DATELINE_D : -DATELINE_D;
        }
        return lon;
    }

    /**
     * Sets degree longitude to something sane.
     * 
     * @param lon double longitude in degrees
     * @return double longitude (-180 &lt;= lambda &lt; 180)
     */
    public final static double wrapLongitudeDeg(double lon) {
        if ((lon < -DATELINE_DEG_D) || (lon > DATELINE_DEG_D)) {
            lon += DATELINE_DEG_D;
            lon %= LON_RANGE_DEG_D;
            lon += (lon < 0) ? DATELINE_DEG_D : -DATELINE_DEG_D;
        }
        return lon;
    }

    /**
     * Converts units (km, nm, miles, etc) to decimal degrees for a spherical
     * planet. This does not check for arc distances &gt; 1/2 planet
     * circumference, which are better represented as (2pi - calculated arc).
     * 
     * @param u units float value
     * @param uCircumference units circumference of planet
     * @return float decimal degrees
     */
    public final static float sphericalUnitsToDeg(float u, float uCircumference) {
        return 360f * (u / uCircumference);
    }

    /**
     * Converts units (km, nm, miles, etc) to arc radians for a spherical
     * planet. This does not check for arc distances &gt; 1/2 planet
     * circumference, which are better represented as (2pi - calculated arc).
     * 
     * @param u units float value
     * @param uCircumference units circumference of planet
     * @return float arc radians
     */
    public final static float sphericalUnitsToRad(float u, float uCircumference) {
        return MoreMath.TWO_PI * (u / uCircumference);
    }

    /**
     * @deprecated use geocentricLatitude instead.
     */
    public final static float geocentric_latitude(float lat, float lon) {
        return geocentricLatitude(lat, lon);
    }

    /**
     * Calculate the geocentric latitude given a geographic latitude. According
     * to John Synder: <br>
     * "The geographic or geodetic latitude is the angle which a line
     * perpendicular to the surface of the ellipsoid at the given point makes
     * with the plane of the equator. ...The geocentric latitude is the angle
     * made by a line to the center of the ellipsoid with the equatorial plane".
     * ( <i>Map Projections --A Working Manual </i>, p 13)
     * <p>
     * Translated from Ken Anderson's lisp code <i>Freeing the Essence of
     * Computation </i>
     * 
     * @param lat float geographic latitude in radians
     * @param flat float flatening factor
     * @return float geocentric latitude in radians
     * @see #geographic_latitude
     */
    public final static float geocentricLatitude(float lat, float flat) {
        float f = 1.0f - flat;
        return (float) Math.atan((f * f) * (float) Math.tan(lat));
    }

    /**
     * @deprecated use geographicLoatitude instead.
     */
    public final static float geographic_latitude(float lat, float lon) {
        return geographicLatitude(lat, lon);
    }

    /**
     * Calculate the geographic latitude given a geocentric latitude. Translated
     * from Ken Anderson's lisp code <i>Freeing the Essence of Computation </i>
     * 
     * @param lat float geocentric latitude in radians
     * @param flat float flatening factor
     * @return float geographic latitude in radians
     * @see #geocentric_latitude
     */
    public final static float geographicLatitude(float lat, float flat) {
        float f = 1.0f - flat;
        return (float) Math.atan((float) Math.tan(lat) / (f * f));
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param projection the projection to use for other projection parameters,
     *        like map width and map height.
     */
    public static float getScale(Point2D ll1, Point2D ll2, Projection projection) {
        if (projection == null) {
            return Float.MAX_VALUE;
        }

        Point2D point1 = projection.forward(ll1);
        Point2D point2 = projection.forward(ll2);

        return getScale(ll1, ll2, point1, point2, projection);
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection. This method is generally called from objects dealing with
     * MouseEvents.
     * 
     * @param point1 a java.awt.Point reflecting a pixel spot on the projection,
     *        usually the upper left corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the projection,
     *        usually the lower right corner of the area of interest.
     * @param projection the projection to use for other projection parameters,
     *        like map width and map height.
     */
    public static float getScale(Point point1, Point point2, Projection projection) {

        return getScaleFromProjected(point1, point2, projection);
    }

    /**
     * Given a couple of points representing a bounding box of projected
     * coordinates, find out what the scale should be in order to make those
     * points appear at the corners of the projection, with the intention that
     * the bounding box will then fill the projected space.
     * 
     * @param point1 a java.awt.Point reflecting a pixel spot on the projection,
     *        usually the upper left corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the projection,
     *        usually the lower right corner of the area of interest.
     * @param projection the projection to use for other projection parameters,
     *        like map width and map height.
     */
    public static float getScaleFromProjected(Point2D point1, Point2D point2, Projection projection) {
        if (projection == null) {
            return Float.MAX_VALUE;
        }

        /**
         * Just to make sure of the order for upper left and lower right, which
         * seems to make a difference for the projection calculation.
         */
        Point2D upperLeft = new Point2D.Double(Math.min(point1.getX(), point2.getX()), Math.min(point1.getY(), point2.getY()));
        Point2D lowerRight = new Point2D.Double(Math.max(point1.getX(), point2.getX()), Math.max(point1.getY(), point2.getY()));

        Point2D ll1 = projection.inverse(upperLeft);
        Point2D ll2 = projection.inverse(lowerRight);

        return getScale(ll1, ll2, upperLeft, lowerRight, projection);
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the projection
     *        that matches the ll1 coordinate in the new space, the upper left
     *        corner of the area of interest. Where the ll1 is going to go in
     *        the new projection.
     * @param point2 a java.awt.Point reflecting a pixel spot on the projection
     *        that matches the ll2 coordinate in the new space, usually the
     *        lower right corner of the area of interest. Where the ll2 is going
     *        to go in the new projection.
     * @param projection the projection to use to query to get the scale for,
     *        for projection type and height and width.
     */
    protected static float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2,
                                    Projection projection) {

        return projection.getScale(ll1, ll2, point1, point2);
    }

    /*
     * public static void main(String[] args) { float degs =
     * sphericalUnitsToRad( Planet.earthEquatorialRadius/2,
     * Planet.earthEquatorialRadius); Debug.output("degs = " + degs); float
     * LAT_DEC_RANGE = 90.0f; float LON_DEC_RANGE = 360.0f; float lat, lon; for
     * (int i = 0; i < 100; i++) { lat =
     * com.bbn.openmap.LatLonPoint.normalize_latitude(
     * (float)Math.random()*LAT_DEC_RANGE); lon =
     * com.bbn.openmap.LatLonPoint.wrap_longitude(
     * (float)Math.random()*LON_DEC_RANGE); Debug.output( "(" + lat + "," + lon
     * + ") : (" + degToRad(lat) + "," + degToRad(lon) + ") : (" +
     * radToDeg(degToRad(lat)) + "," + radToDeg(degToRad(lon)) + ")"); } }
     */

    /**
     * Generic test for seeing if an left longitude value and a right longitude
     * value seem to constitute crossing the dateline.
     * 
     * @param leftLon the leftmost longitude, in decimal degrees. Expected to
     *        represent the location of the left side of a map window.
     * @param rightLon the rightmost longitude, in decimal degrees. Expected to
     *        represent the location of the right side of a map window.
     * @param projScale the projection scale, considered if the two values are
     *        very close to each other and leftLon less than rightLon.
     * @return true if it seems like these two longitude values represent a
     *         dateline crossing.
     */
    public static boolean isCrossingDateline(double leftLon, double rightLon, float projScale) {
        // if the left longitude is greater than the right, we're obviously
        // crossing the dateline. If they are approximately equal, we could be
        // showing the whole earth, but only if the scale is significantly
        // large. If the scale is small, we could be really zoomed in.
        return ((leftLon > rightLon) || (MoreMath.approximately_equal(leftLon, rightLon, .001f) && projScale > 1000000f));
    }

    /**
     * Given a projection and the starting point of a box (pt1), look at pt2 to
     * see if it represents the ratio of the projection map size. If it doesn't,
     * provide a point that does.
     * 
     * @param proj the projection to use for the calculations.
     * @param pt1 upper left point in pixel space
     * @param pt2 second point in pixel space.
     * @return new pt that matches projection h/w ratio.
     */
    public static Point getRatioPoint(Projection proj, Point pt1, Point pt2) {
        float mapRatio = (float) proj.getHeight() / (float) proj.getWidth();

        float boxHeight = (float) (pt1.y - pt2.y);
        float boxWidth = (float) (pt1.x - pt2.x);
        float boxRatio = Math.abs(boxHeight / boxWidth);
        int isNegative = -1;
        if (boxRatio > mapRatio) {
            // box is too tall, adjust boxHeight
            if (boxHeight < 0) {
                isNegative = 1;
            }
            boxHeight = Math.abs(mapRatio * boxWidth);
            pt2.y = pt1.y + (isNegative * (int) boxHeight);

        } else if (boxRatio < mapRatio) {
            // box is too wide, adjust boxWidth
            if (boxWidth < 0) {
                isNegative = 1;
            }
            boxWidth = Math.abs(boxHeight / mapRatio);
            pt2.x = pt1.x + (isNegative * (int) boxWidth);
        }
        return pt2;
    }

    /**
     * Given a projection to match against and the starting point of a box
     * (pt1), look at pt2 to see if it represents the ratio of the projection
     * map size. Return a rectangle for the resulting zoom area.
     * 
     * @param proj the current projection.
     * @param pt1 first point, where mouse down happened, for instance.
     * @param pt2 latest point, where mouse released happened, for instance.
     * @param zoomOnCenter whether the first point represents the center of the
     *        zoom box, or one of the corners.
     * @return Rectangle2D representing the new zoom box.
     */
    public static Rectangle2D getRatioBox(Projection proj, Point2D pt1, Point2D pt2,
                                          boolean zoomOnCenter) {
        double mapRatio = (double) proj.getHeight() / (double) proj.getWidth();

        double boxHeight = Math.abs(pt1.getY() - pt2.getY());
        double boxWidth = Math.abs(pt1.getX() - pt2.getX());
        double boxRatio = Math.abs(boxHeight / boxWidth);

        if (boxRatio > mapRatio) {
            boxHeight = Math.abs(mapRatio * boxWidth);
        } else if (boxRatio < mapRatio) {
            boxWidth = Math.abs(boxHeight / mapRatio);
        }

        if (zoomOnCenter) {
            double anchorx = pt1.getX() - boxWidth;
            double anchory = pt1.getY() - boxHeight;

            return new Rectangle2D.Double(anchorx, anchory, 2 * boxWidth, 2 * boxHeight);
        } else {

            double anchorx = pt1.getX();
            if (pt2.getX() < anchorx) {
                anchorx -= boxWidth;
            }
            double anchory = pt1.getY();
            if (pt2.getY() < anchory) {
                anchory -= boxHeight;
            }

            return new Rectangle2D.Double(anchorx, anchory, boxWidth, boxHeight);
        }
    }

    /**
     * Walks around the perimeter of the sourceMapProjection and returns the
     * lat/lon coords of the outline.
     * 
     * @param sourceMapProjection the source map's projection.
     * @return double[] in y, x order, in whatever units the source map
     *         projection's inverse function returns.
     */
    public static double[] getProjectionScreenOutlineCoords(Projection sourceMapProjection) {

        // Sourge projection not yet set
        if (sourceMapProjection == null) {
            return null;
        }

        // Would have used ArrayList<LatLonPoint> here but didn't for
        // backward compatibility.
        ArrayList<Point2D> l = new ArrayList<Point2D>();

        // Get the parameters needed for building the coverage polygon
        int width = sourceMapProjection.getWidth();
        int height = sourceMapProjection.getHeight();
        double xinc = ((double) width) / 10.0;
        double yinc = ((double) height) / 10.0;

        Point2D center = sourceMapProjection.getCenter(new Point2D.Double());
        Point2D tmpllp;

        // Walk the top edge of the source projection's screen bounds
        for (int i = 0; i <= 10; i++) {
            tmpllp = sourceMapProjection.inverse(xinc * i, 0, new Point2D.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the right edge of the source projection's screen bounds
        for (int i = 0; i <= 10; i++) {
            tmpllp = sourceMapProjection.inverse(width, yinc * i, new Point2D.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the south edge of the source projection's screen bounds
        for (int i = 10; i >= 0; i--) {
            tmpllp = sourceMapProjection.inverse(xinc * i, height, new Point2D.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the left edge of the source projection's screen bounds
        for (int i = 10; i >= 0; i--) {
            tmpllp = sourceMapProjection.inverse(0, yinc * i, new Point2D.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // populate the coordinate array for the polygon
        double[] llarr = new double[l.size() * 2];
        int i = 0;
        for (Point2D pnt : l) {
            llarr[i] = pnt.getY();
            llarr[i + 1] = pnt.getX();
            i += 2;
        }

        return llarr;
    }

    /**
     * Returns true if the Point is visible on the provided projection.
     * 
     * @param sourceMapProjection
     * @param llp
     * @return true if the Point is visible on the provided projection.
     */
    public static boolean isVisible(Projection sourceMapProjection, Point2D llp) {
        boolean ret = false;
        if (sourceMapProjection != null) {
            if (sourceMapProjection.isPlotable(llp)) {
                Point2D p = sourceMapProjection.forward(llp);
                double x = p.getX();
                double y = p.getY();
                if (x >= 0 && x <= sourceMapProjection.getWidth() && y >= 0
                        && y <= sourceMapProjection.getWidth()) {
                    ret = true;
                }
            }
        }
        return ret;
    }

}