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
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:23 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import com.bbn.openmap.MoreMath;

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
     * South pole latitude in radians.
     */
    public final static transient double SOUTH_POLE_D = -NORTH_POLE_D;

    /**
     * Dateline longitude in radians.
     */
    public final static transient float DATELINE_F = (float) Math.PI;

    /**
     * Dateline longitude in radians.
     */
    public final static transient double DATELINE_D = Math.PI;

    /**
     * Longitude range in radians.
     */
    public final static transient float LON_RANGE_F = (float) MoreMath.TWO_PI;

    /**
     * Longitude range in radians.
     */
    public final static transient double LON_RANGE_D = MoreMath.TWO_PI_D;

    // cannot construct
    private ProjMath() {}

    /**
     * rounds the quantity away from 0.
     * 
     * @param x in value
     * @return double
     * @see #qint(double)
     */
    final public static double roundAdjust(double x) {
        return qint_old(x);
    }

    /**
     * Rounds the quantity away from 0.
     * 
     * @param x value
     * @return double
     */
    final public static double qint(double x) {
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
    final public static float lonDistance(float lon1, float lon2) {
        return (float) Math.min(Math.abs(lon1 - lon2), ((lon1 < 0) ? lon1
                + Math.PI : Math.PI - lon1)
                + ((lon2 < 0) ? lon2 + Math.PI : Math.PI - lon2));
    }

    /**
     * Convert between decimal degrees and scoords.
     * 
     * @param deg degrees
     * @return long scoords
     *  
     */
    final public static long DEG_TO_SC(double deg) {
        return (long) (deg * 3600000);
    }

    /**
     * Convert between decimal degrees and scoords.
     * 
     * @param sc scoords
     * @return double decimal degrees
     */
    final public static double SC_TO_DEG(int sc) {
        return ((double) (sc) / (60.0 * 60.0 * 1000.0));
    }

    /**
     * Convert radians to degrees.
     * 
     * @param rad radians
     * @return double decimal degrees
     */
    final public static double radToDeg(double rad) {
        return (rad * (180.0d / Math.PI));
    }

    /**
     * Convert radians to degrees.
     * 
     * @param rad radians
     * @return float decimal degrees
     */
    final public static float radToDeg(float rad) {
        return (rad * (180.0f / (float) Math.PI));
    }

    /**
     * Convert degrees to radians.
     * 
     * @param deg degrees
     * @return double radians
     */
    final public static double degToRad(double deg) {
        return (deg * (Math.PI / 180.0d));
    }

    /**
     * Convert degrees to radians.
     * 
     * @param deg degrees
     * @return float radians
     */
    final public static float degToRad(float deg) {
        return (deg * ((float) Math.PI / 180.0f));
    }

    /**
     * Generate a hashCode value for a lat/lon pair.
     * 
     * @param lat latitude
     * @param lon longitude
     * @return int hashcode
     *  
     */
    final public static int hashLatLon(float lat, float lon) {
        if (lat == -0f)
            lat = 0f;//handle negative zero (anything else?)
        if (lon == -0f)
            lon = 0f;
        int tmp = Float.floatToIntBits(lat);
        int hash = (tmp << 5) | (tmp >> 27);//rotate the lat bits
        return hash ^ Float.floatToIntBits(lon);//XOR with lon
    }

    /**
     * Converts an array of decimal degrees float lat/lons to float
     * radians in place.
     * 
     * @param degs float[] lat/lons in decimal degrees
     * @return float[] lat/lons in radians
     */
    final public static float[] arrayDegToRad(float[] degs) {
        for (int i = 0; i < degs.length; i++) {
            degs[i] = degToRad(degs[i]);
        }
        return degs;
    }

    /**
     * Converts an array of radian float lat/lons to decimal degrees
     * in place.
     * 
     * @param rads float[] lat/lons in radians
     * @return float[] lat/lons in decimal degrees
     */
    final public static float[] arrayRadToDeg(float[] rads) {
        for (int i = 0; i < rads.length; i++) {
            rads[i] = radToDeg(rads[i]);
        }
        return rads;
    }

    /**
     * Converts an array of decimal degrees double lat/lons to double
     * radians in place.
     * 
     * @param degs double[] lat/lons in decimal degrees
     * @return double[] lat/lons in radians
     */
    final public static double[] arrayDegToRad(double[] degs) {
        for (int i = 0; i < degs.length; i++) {
            degs[i] = degToRad(degs[i]);
        }
        return degs;
    }

    /**
     * Converts an array of radian double lat/lons to decimal degrees
     * in place.
     * 
     * @param rads double[] lat/lons in radians
     * @return double[] lat/lons in decimal degrees
     */
    final public static double[] arrayRadToDeg(double[] rads) {
        for (int i = 0; i < rads.length; i++) {
            rads[i] = radToDeg(rads[i]);
        }
        return rads;
    }

    /**
     * Normalizes radian latitude. Normalizes latitude if at or
     * exceeds epsilon distance from a pole.
     * 
     * @param lat float latitude in radians
     * @param epsilon epsilon (&gt;= 0) radians distance from pole
     * @return float latitude (-PI/2 &lt;= phi &lt;= PI/2)
     * @see Proj#normalize_latitude(float)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     */
    public final static float normalize_latitude(float lat, float epsilon) {
        if (lat > NORTH_POLE_F - epsilon) {
            return NORTH_POLE_F - epsilon;
        } else if (lat < SOUTH_POLE_F + epsilon) {
            return SOUTH_POLE_F + epsilon;
        }
        return lat;
    }

    /**
     * Normalizes radian latitude. Normalizes latitude if at or
     * exceeds epsilon distance from a pole.
     * 
     * @param lat double latitude in radians
     * @param epsilon epsilon (&gt;= 0) radians distance from pole
     * @return double latitude (-PI/2 &lt;= phi &lt;= PI/2)
     * @see Proj#normalize_latitude(float)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     */
    public final static double normalize_latitude(double lat, double epsilon) {
        if (lat > NORTH_POLE_D - epsilon) {
            return NORTH_POLE_D - epsilon;
        } else if (lat < SOUTH_POLE_D + epsilon) {
            return SOUTH_POLE_D + epsilon;
        }
        return lat;
    }

    /**
     * Sets radian longitude to something sane.
     * 
     * @param lon float longitude in radians
     * @return float longitude (-PI &lt;= lambda &lt; PI)
     * @see com.bbn.openmap.LatLonPoint#wrap_longitude(float)
     */
    public final static float wrap_longitude(float lon) {
        if ((lon < -DATELINE_F) || (lon > DATELINE_F)) {
            lon += DATELINE_F;
            lon = (lon % LON_RANGE_F);
            lon += (lon < 0) ? DATELINE_F : -DATELINE_F;
        }
        return lon;
    }

    /**
     * Sets radian longitude to something sane.
     * 
     * @param lon double longitude in radians
     * @return double longitude (-PI &lt;= lambda &lt; PI)
     * @see #wrap_longitude(float)
     */
    public final static double wrap_longitude(double lon) {
        if ((lon < -DATELINE_D) || (lon > DATELINE_D)) {
            lon += DATELINE_D;
            lon = (lon % LON_RANGE_D);
            lon += (lon < 0) ? DATELINE_D : -DATELINE_D;
        }
        return lon;
    }

    /**
     * Converts units (km, nm, miles, etc) to decimal degrees for a
     * spherical planet. This does not check for arc distances &gt;
     * 1/2 planet circumference, which are better represented as (2pi -
     * calculated arc).
     * 
     * @param u units float value
     * @param uCircumference units circumference of planet
     * @return float decimal degrees
     */
    final public static float sphericalUnitsToDeg(float u, float uCircumference) {
        return 360f * (u / uCircumference);
    }

    /**
     * Converts units (km, nm, miles, etc) to arc radians for a
     * spherical planet. This does not check for arc distances &gt;
     * 1/2 planet circumference, which are better represented as (2pi -
     * calculated arc).
     * 
     * @param u units float value
     * @param uCircumference units circumference of planet
     * @return float arc radians
     */
    final public static float sphericalUnitsToRad(float u, float uCircumference) {
        return MoreMath.TWO_PI * (u / uCircumference);
    }

    /**
     * Calculate the geocentric latitude given a geographic latitude.
     * According to John Synder: <br>
     * "The geographic or geodetic latitude is the angle which a line
     * perpendicular to the surface of the ellipsoid at the given
     * point makes with the plane of the equator. ...The geocentric
     * latitude is the angle made by a line to the center of the
     * ellipsoid with the equatorial plane". ( <i>Map Projections --A
     * Working Manual </i>, p 13)
     * <p>
     * Translated from Ken Anderson's lisp code <i>Freeing the Essence
     * of Computation </i>
     * 
     * @param lat float geographic latitude in radians
     * @param flat float flatening factor
     * @return float geocentric latitude in radians
     * @see #geographic_latitude
     */
    public final static float geocentric_latitude(float lat, float flat) {
        float f = 1.0f - flat;
        return (float) Math.atan((f * f) * (float) Math.tan(lat));
    }

    /**
     * Calculate the geographic latitude given a geocentric latitude.
     * Translated from Ken Anderson's lisp code <i>Freeing the Essence
     * of Computation </i>
     * 
     * @param lat float geocentric latitude in radians
     * @param flat float flatening factor
     * @return float geographic latitude in radians
     * @see #geocentric_latitude
     */
    public final static float geographic_latitude(float lat, float flat) {
        float f = 1.0f - flat;
        return (float) Math.atan((float) Math.tan(lat) / (f * f));
    }

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param projection the projection to use for other projection
     *        parameters, like map width and map height.
     */
    public static float getScale(com.bbn.openmap.LatLonPoint ll1,
                                 com.bbn.openmap.LatLonPoint ll2,
                                 Projection projection) {
        if (projection == null) {
            return Float.MAX_VALUE;
        }

        java.awt.Point point1 = projection.forward(ll1);
        java.awt.Point point2 = projection.forward(ll2);

        return getScale(ll1, ll2, point1, point2, projection);
    }

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     * 
     * @param point1 a java.awt.Point reflecting a pixel spot on the
     *        projection, usually the upper left corner of the area of
     *        interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the
     *        projection, usually the lower right corner of the area
     *        of interest.
     * @param projection the projection to use for other projection
     *        parameters, like map width and map height.
     */
    public static float getScale(java.awt.Point point1, java.awt.Point point2,
                                 Projection projection) {

        if (projection == null) {
            return Float.MAX_VALUE;
        }

        com.bbn.openmap.LatLonPoint ll1 = projection.inverse(point1);
        com.bbn.openmap.LatLonPoint ll2 = projection.inverse(point2);

        return getScale(ll1, ll2, point1, point2, projection);
    }

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the
     *        projection that matches the ll1 coordinate, the upper
     *        left corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the
     *        projection that matches the ll2 coordinate, usually the
     *        lower right corner of the area of interest.
     * @param projection the projection to use to query to get the
     *        scale for, for projection type and height and width.
     */
    protected static float getScale(com.bbn.openmap.LatLonPoint ll1,
                                    com.bbn.openmap.LatLonPoint ll2,
                                    java.awt.Point point1,
                                    java.awt.Point point2, Projection projection) {

        return projection.getScale(ll1, ll2, point1, point2);
    }

    /*
     * public static void main(String[] args) { float degs =
     * sphericalUnitsToRad( Planet.earthEquatorialRadius/2,
     * Planet.earthEquatorialRadius); Debug.output("degs = " + degs);
     * float LAT_DEC_RANGE = 90.0f; float LON_DEC_RANGE = 360.0f;
     * float lat, lon; for (int i = 0; i < 100; i++) { lat =
     * com.bbn.openmap.LatLonPoint.normalize_latitude(
     * (float)Math.random()*LAT_DEC_RANGE); lon =
     * com.bbn.openmap.LatLonPoint.wrap_longitude(
     * (float)Math.random()*LON_DEC_RANGE); Debug.output( "(" + lat +
     * "," + lon + ") : (" + degToRad(lat) + "," + degToRad(lon) + ") : (" +
     * radToDeg(degToRad(lat)) + "," + radToDeg(degToRad(lon)) + ")"); } }
     */
}