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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/GreatCircle.java,v $
// $RCSfile: GreatCircle.java,v $
// $Revision: 1.8 $
// $Date: 2005/12/09 21:09:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Methods for calculating great circle and other distances on the sphere and
 * ellipsoid. Note that as of OpenMap 4.7, all the method calls with a '_' in
 * them have been deprecated, replaced by method names that conform to Java
 * conventions.
 * <p>
 * Spherical equations taken from John Synder's <i>Map Projections --A Working
 * Manual </i>, pp29-31. <br>
 * Latitude/longitude arguments must be in valid radians: -PI&lt;=lambda&lt;PI,
 * -PI/2&lt;=phi&lt;=PI/2
 */
public class GreatCircle {

    // cannot construct
    private GreatCircle() {}

    /**
     * Determine azimuth and distance on the ellipsoid.
     * 
     * @param a Semi-major axis of ellipsoid
     * @param finv flattening of the ellipsoid (WGS84 is 1/298.257)
     * @param glat1 Latitude of from station
     * @param glon1 Longitude of from station
     * @param glat2 Latitude of to station
     * @param glon2 Longitude of to station
     * @param ret_val AziDist struct
     * @return AziDist ret_val struct with azimuth and distance
     * @deprecated this has been yanked until we have a more stable and
     *             documented algorithm
     */
    public final static AziDist ellipsoidalAziDist(double a, double finv,
                                                   double glat1, double glon1,
                                                   double glat2, double glon2,
                                                   AziDist ret_val) {
        return null;
    }

    /**
     * Calculate spherical arc distance between two points.
     * <p>
     * Computes arc distance `c' on the sphere. equation (5-3a). (0 &lt;= c
     * &lt;= PI)
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float arc distance `c'
     * 
     */
    final public static float sphericalDistance(float phi1, float lambda0,
                                                float phi, float lambda) {
        return (float) sphericalDistance((double) phi1,
                (double) lambda0,
                (double) phi,
                (double) lambda);
    }

    /**
     * @deprecated use sphericalDistance instead.
     */
    final public static float spherical_distance(float phi1, float lambda0,
                                                 float phi, float lambda) {
        return sphericalDistance(phi1, lambda0, phi, lambda);
    }

    /**
     * Calculate spherical arc distance between two points with double
     * precision.
     * <p>
     * Computes arc distance `c' on the sphere. equation (5-3a). (0 &lt;= c
     * &lt;= PI)
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float arc distance `c'
     */
    final public static double sphericalDistance(double phi1, double lambda0,
                                                 double phi, double lambda) {
        double pdiff = Math.sin(((phi - phi1) / 2.0));
        double ldiff = Math.sin((lambda - lambda0) / 2.0);
        double rval = Math.sqrt((pdiff * pdiff) + Math.cos(phi1)
                * Math.cos(phi) * (ldiff * ldiff));

        return 2.0 * Math.asin(rval);
    }

    /**
     * @deprecated use sphericalDistance instead.
     */
    final public static double spherical_distance(double phi1, double lambda0,
                                                  double phi, double lambda) {
        return sphericalDistance(phi1, lambda0, phi, lambda);
    }

    /**
     * Calculate spherical azimuth between two points.
     * <p>
     * Computes the azimuth `Az' east of north from phi1, lambda0 bearing toward
     * phi and lambda. (5-4b). (-PI &lt;= Az &lt;= PI).
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float azimuth east of north `Az'
     * 
     */
    final public static float sphericalAzimuth(float phi1, float lambda0,
                                               float phi, float lambda) {
        return (float) sphericalAzimuth((double) phi1,
                (double) lambda0,
                (double) phi,
                (double) lambda);
    }

    /**
     * @deprecated use sphericalAzimuth instead.
     */
    final public static float spherical_azimuth(float phi1, float lambda0,
                                                float phi, float lambda) {
        return sphericalAzimuth(phi1, lambda0, phi, lambda);
    }

    /**
     * Calculate spherical azimuth between two points with double precision.
     * <p>
     * Computes the azimuth `Az' east of north from phi1, lambda0 bearing toward
     * phi and lambda. (5-4b). (-PI &lt;= Az &lt;= PI).
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float azimuth east of north `Az'
     * 
     */
    final public static double sphericalAzimuth(double phi1, double lambda0,
                                                double phi, double lambda) {
        double ldiff = lambda - lambda0;
        double cosphi = Math.cos(phi);

        return Math.atan2(cosphi * Math.sin(ldiff), (Math.cos(phi1)
                * Math.sin(phi) - Math.sin(phi1) * cosphi * Math.cos(ldiff)));
    }

    /**
     * @deprecated use sphericalAzimuth instead.
     */
    final public static double spherical_azimuth(double phi1, double lambda0,
                                                 double phi, double lambda) {
        return sphericalAzimuth(phi1, lambda0, phi, lambda);
    }

    /**
     * Calculate point at azimuth and distance from another point.
     * <p>
     * Returns a LatLonPoint.Float at arc distance `c' in direction `Az' from
     * start point.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @return LatLonPoint
     * 
     */
    final public static LatLonPoint sphericalBetween(float phi1, float lambda0,
                                                     float c, float Az) {
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        return new LatLonPoint.Float((float) Math.toDegrees(Math.asin(sinphi1
                * cosc + cosphi1 * sinc * cosAz)), (float) Math.toDegrees(Math.atan2(sinc
                * sinAz,
                cosphi1 * cosc - sinphi1 * sinc * cosAz)
                + lambda0));
    }

    /**
     * @deprecated use shoerucalBetween instead.
     */
    final public static LatLonPoint spherical_between(float phi1,
                                                      float lambda0, float c,
                                                      float Az) {
        return sphericalBetween(phi1, lambda0, c, Az);
    }

    /**
     * Calculate point at azimuth and distance from another point, with double
     * precision.
     * <p>
     * Returns a LatLonPoint.Double at arc distance `c' in direction `Az' from
     * start point.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @return LatLonPoint
     * 
     */
    final public static LatLonPoint sphericalBetween(double phi1,
                                                     double lambda0, double c,
                                                     double Az) {
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        return new LatLonPoint.Double(ProjMath.radToDeg(Math.asin(sinphi1
                * cosc + cosphi1 * sinc * cosAz)), ProjMath.radToDeg(Math.atan2(sinc
                * sinAz,
                cosphi1 * cosc - sinphi1 * sinc * cosAz)
                + lambda0));
    }

    /**
     * @deprecated use sphericalBetween instead.
     */
    final public static LatLonPoint spherical_between(double phi1,
                                                      double lambda0, double c,
                                                      double Az) {
        return sphericalBetween(phi1, lambda0, c, Az);
    }

    /**
     * Calculate point between two points.
     * <p>
     * Same as spherical_between() above except it calculates n equal segments
     * along the length of c.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @param n number of points along great circle edge to calculate
     * @return float[n+1] radian lat,lon pairs
     * 
     */
    final public static float[] sphericalBetween(float phi1, float lambda0,
                                                 float c, float Az, int n) {
        // full constants for the computation
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);
        int end = n << 1;

        // new radian points
        float[] points = new float[end + 2];
        points[0] = phi1;
        points[1] = lambda0;

        float inc = c / n;
        c = inc;
        for (int i = 2; i <= end; i += 2, c += inc) {

            // partial constants
            double sinc = Math.sin(c);
            double cosc = Math.cos(c);

            // generate new point
            points[i] = (float) Math.asin(sinphi1 * cosc + cosphi1 * sinc
                    * cosAz);

            points[i + 1] = (float) Math.atan2(sinc * sinAz, cosphi1 * cosc
                    - sinphi1 * sinc * cosAz)
                    + lambda0;
        }
        return points;
    }

    /**
     * @deprecated use sphericalBetween instead.
     */
    final public static float[] spherical_between(float phi1, float lambda0,
                                                  float c, float Az, int n) {
        return sphericalBetween(phi1, lambda0, c, Az, n);
    }

    /**
     * Calculate point between two points with double precision.
     * <p>
     * Same as spherical_between() above except it calculates n equal segments
     * along the length of c.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @param n number of points along great circle edge to calculate
     * @return double[n+1] radian lat,lon pairs
     * 
     */
    final public static double[] sphericalBetween(double phi1, double lambda0,
                                                  double c, double Az, int n) {
        // full constants for the computation
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);
        int end = n << 1;

        // new radian points
        double[] points = new double[end + 2];
        points[0] = phi1;
        points[1] = lambda0;

        double inc = c / n;
        c = inc;
        for (int i = 2; i <= end; i += 2, c += inc) {

            // partial constants
            double sinc = Math.sin(c);
            double cosc = Math.cos(c);

            // generate new point
            points[i] = Math.asin(sinphi1 * cosc + cosphi1 * sinc * cosAz);

            points[i + 1] = Math.atan2(sinc * sinAz, cosphi1 * cosc - sinphi1
                    * sinc * cosAz)
                    + lambda0;
        }
        return points;
    }

    /**
     * @deprecated use shpericalBetween instead.
     */
    final public static double[] spherical_between(double phi1, double lambda0,
                                                   double c, double Az, int n) {
        return sphericalBetween(phi1, lambda0, c, Az, n);
    }

    /**
     * Calculate great circle between two points on the sphere.
     * <p>
     * Folds all computation (distance, azimuth, points between) into one
     * function for optimization. returns n or n+1 pairs of lat,lon on great
     * circle between lat-lon pairs.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @param n number of segments
     * @param include_last return n or n+1 segments
     * @return float[n] or float[n+1] radian lat,lon pairs
     * 
     */
    final public static float[] greatCircle(float phi1, float lambda0,
                                            float phi, float lambda, int n,
                                            boolean include_last) {
        // number of points to generate
        int end = include_last ? n + 1 : n;
        end <<= 1;// *2 for pairs

        // calculate a bunch of stuff for later use
        double cosphi = Math.cos(phi);
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double ldiff = lambda - lambda0;
        double p2diff = Math.sin(((phi - phi1) / 2));
        double l2diff = Math.sin((ldiff) / 2);

        // calculate spherical distance
        double c = 2.0f * Math.asin(Math.sqrt(p2diff * p2diff + cosphi1
                * cosphi * l2diff * l2diff));

        // calculate spherical azimuth
        double Az = Math.atan2(cosphi * Math.sin(ldiff), (cosphi1
                * Math.sin(phi) - sinphi1 * cosphi * Math.cos(ldiff)));
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);

        // generate the great circle line
        float[] points = new float[end];
        points[0] = phi1;
        points[1] = lambda0;

        double inc = c / n;
        c = inc;
        for (int i = 2; i < end; i += 2, c += inc) {

            // partial constants
            double sinc = Math.sin(c);
            double cosc = Math.cos(c);

            // generate new point
            points[i] = (float) Math.asin(sinphi1 * cosc + cosphi1 * sinc
                    * cosAz);

            points[i + 1] = (float) Math.atan2(sinc * sinAz, cosphi1 * cosc
                    - sinphi1 * sinc * cosAz)
                    + lambda0;
        }

        return points;
    }

    /**
     * @deprecated use greatCircle instead.
     */
    final public static float[] great_circle(float phi1, float lambda0,
                                             float phi, float lambda, int n,
                                             boolean include_last) {
        return greatCircle(phi1, lambda0, phi, lambda, n, include_last);
    }

    /**
     * Calculate great circle between two points on the sphere with double
     * precision.
     * <p>
     * Folds all computation (distance, azimuth, points between) into one
     * function for optimization. returns n or n+1 pairs of lat,lon on great
     * circle between lat-lon pairs.
     * <p>
     * 
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @param n number of segments
     * @param include_last return n or n+1 segments
     * @return double[n] or double[n+1] radian lat,lon pairs
     * 
     */
    final public static double[] greatCircle(double phi1, double lambda0,
                                             double phi, double lambda, int n,
                                             boolean include_last) {
        // number of points to generate
        int end = include_last ? n + 1 : n;
        end <<= 1;// *2 for pairs

        // calculate a bunch of stuff for later use
        double cosphi = Math.cos(phi);
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double ldiff = lambda - lambda0;
        double p2diff = Math.sin(((phi - phi1) / 2));
        double l2diff = Math.sin((ldiff) / 2);

        // calculate spherical distance
        double c = 2.0f * Math.asin(Math.sqrt(p2diff * p2diff + cosphi1
                * cosphi * l2diff * l2diff));

        // calculate spherical azimuth
        double Az = Math.atan2(cosphi * Math.sin(ldiff), (cosphi1
                * Math.sin(phi) - sinphi1 * cosphi * Math.cos(ldiff)));
        double cosAz = Math.cos(Az);
        double sinAz = Math.sin(Az);

        // generate the great circle line
        double[] points = new double[end];
        points[0] = phi1;
        points[1] = lambda0;

        double inc = c / n;
        c = inc;
        for (int i = 2; i < end; i += 2, c += inc) {

            // partial constants
            double sinc = Math.sin(c);
            double cosc = Math.cos(c);

            // generate new point
            points[i] = Math.asin(sinphi1 * cosc + cosphi1 * sinc * cosAz);

            points[i + 1] = Math.atan2(sinc * sinAz, cosphi1 * cosc - sinphi1
                    * sinc * cosAz)
                    + lambda0;
        }

        return points;
    }

    /**
     * Return a point that is approximately a certain distance along the great
     * circle line between two points. Returns the nearest coordinate along a
     * set of calculated segments (as dictated by n) that fits the desired
     * distance.
     * 
     * @param phi1 latitude of point 1 in radians.
     * @param lambda0 longitude of point 1 in radians.
     * @param phi latitude of point 2 in radians.
     * @param lambda longitude of point 2 in radians.
     * @param distance in radians.
     * @param n number of segments to divide path into. The more segments, the
     *        more accurate. If n <= 0, the OpenMap default of 512 is used.
     * @return LatLonPoint if distance is less than distance between points,
     *         null if it is greater.
     */
    public static LatLonPoint pointAtDistanceBetweenPoints(double phi1,
                                                           double lambda0,
                                                           double phi,
                                                           double lambda,
                                                           double distance,
                                                           int n) {
        LatLonPoint ret = null;

        double pntDist = sphericalDistance(phi1, lambda0, phi, lambda);

        if (pntDist > distance) {
            if (n <= 0) {
                n = GeoProj.NUM_DEFAULT_GREAT_SEGS;
            }

            double[] gcpoints = greatCircle(phi1, lambda0, phi, lambda, n, true);

            // Ratio of desired distance to total distance between points - how
            // far down the line we need to go.
            double distRatio = distance / pntDist;
            // all lat, lon points, get number of vertices, find index of the
            // one that fits the ratio of the desired distance to the overall
            // distance between points, and then multiply by 2 to get the actual
            // index of the matching latitude.
            int index = (int) ((int) (gcpoints.length / 2) * distRatio) * 2;
            ret = new LatLonPoint.Double(gcpoints[index], gcpoints[index + 1], true);
        }

        return ret;
    }

    /**
     * @deprecated use greatCircle instead.
     */
    final public static double[] great_circle(double phi1, double lambda0,
                                              double phi, double lambda, int n,
                                              boolean include_last) {
        return greatCircle(phi1, lambda0, phi, lambda, n, include_last);
    }

    /**
     * Calculate partial earth circle on the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param s starting angle in radians. North up is zero.
     * @param e angular extent in radians, clockwise right from starting angle.
     * @param n number of points along circle edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static float[] earthCircle(float phi1, float lambda0, float c,
                                            float s, float e, int n) {
        return earthCircle(phi1, lambda0, c, s, e, n, new float[n << 1]);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static float[] earth_circle(float phi1, float lambda0,
                                             float c, float s, float e, int n) {
        return earthCircle(phi1, lambda0, c, s, e, n);
    }

    /**
     * Calculate earth circle on the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static float[] earthCircle(float phi1, float lambda0, float c,
                                            int n) {
        return earthCircle(phi1,
                lambda0,
                c,
                0.0f,
                MoreMath.TWO_PI,
                n,
                new float[n << 1]);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static float[] earth_circle(float phi1, float lambda0,
                                             float c, int n) {
        return earthCircle(phi1, lambda0, c, n);
    }

    /**
     * Calculate earth circle in the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @param ret_val float[] ret_val array of n*2 number of points along circle
     *        edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static float[] earthCircle(float phi1, float lambda0, float c,
                                            int n, float[] ret_val) {
        return earthCircle(phi1, lambda0, c, 0.0f, MoreMath.TWO_PI, n, ret_val);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static float[] earth_circle(float phi1, float lambda0,
                                             float c, int n, float[] ret_val) {
        return earthCircle(phi1, lambda0, c, n, ret_val);
    }

    /**
     * Calculate earth circle in the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point.
     * @param lambda0 longitude in radians of center point.
     * @param c arc radius in radians (0 &lt; c &lt; PI).
     * @param s starting angle in radians. North up is zero.
     * @param e angular extent in radians, clockwise right from starting angle.
     * @param n number of points along circle edge to calculate.
     * @param ret_val float[] ret_val array of n*2 number of points along circle
     *        edge to calculate.
     * @return float[n] radian lat,lon pairs along earth circle.
     * 
     */
    final public static float[] earthCircle(float phi1, float lambda0, float c,
                                            float s, float e, int n,
                                            float[] ret_val) {
        double Az, cosAz, sinAz;
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);
        if (n < 2)
            n = 2; // Safety to avoid / by zero later.
        int end = n << 1;// *2

        // Only want to create a new return float array if there was a
        // null one passed in, or if the number of desired coordinates
        // is bigger than what ret_val is currently allocated for.
        if (ret_val == null || end > ret_val.length) {
            ret_val = new float[end];
        }

        double inc = e / (n - 1);
        Az = s;

        // generate the points in clockwise order (conforming to
        // internal standard!)
        for (int i = 0; i < end; i += 2, Az += inc) {
            cosAz = Math.cos(Az);
            sinAz = Math.sin(Az);

            ret_val[i] = (float) Math.asin(sinphi1 * cosc + cosphi1 * sinc
                    * cosAz);
            ret_val[i + 1] = (float) Math.atan2(sinc * sinAz, cosphi1 * cosc
                    - sinphi1 * sinc * cosAz)
                    + lambda0;
        }

        return ret_val;
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static float[] earth_circle(float phi1, float lambda0,
                                             float c, float s, float e, int n,
                                             float[] ret_val) {
        return earthCircle(phi1, lambda0, c, s, e, n, ret_val);
    }

    /**
     * Calculate partial earth circle on the sphere with double precision.
     * <p>
     * Returns n double lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param s starting angle in radians. North up is zero.
     * @param e angular extent in radians, clockwise right from starting angle.
     * @param n number of points along circle edge to calculate
     * @return double[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static double[] earthCircle(double phi1, double lambda0,
                                             double c, double s, double e, int n) {
        return earthCircle(phi1, lambda0, c, s, e, n, new double[n << 1]);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static double[] earth_circle(double phi1, double lambda0,
                                              double c, double s, double e,
                                              int n) {
        return earthCircle(phi1, lambda0, c, s, e, n);
    }

    /**
     * Calculate earth circle on the sphere with double precision.
     * <p>
     * Returns n double lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @return double[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static double[] earthCircle(double phi1, double lambda0,
                                             double c, int n) {
        return earthCircle(phi1,
                lambda0,
                c,
                0.0f,
                MoreMath.TWO_PI_D,
                n,
                new double[n << 1]);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static double[] earth_circle(double phi1, double lambda0,
                                              double c, int n) {
        return earthCircle(phi1, lambda0, c, n);
    }

    /**
     * Calculate earth circle in the sphere with double precision.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @param ret_val double[] ret_val array of n*2 number of points along
     *        circle edge to calculate
     * @return double[n] radian lat,lon pairs along earth circle
     * 
     */
    final public static double[] earthCircle(double phi1, double lambda0,
                                             double c, int n, double[] ret_val) {
        return earthCircle(phi1,
                lambda0,
                c,
                0.0f,
                MoreMath.TWO_PI_D,
                n,
                ret_val);
    }

    /**
     * @deprecated use earthCircle instead.
     */
    final public static double[] earth_circle(double phi1, double lambda0,
                                              double c, int n, double[] ret_val) {
        return earthCircle(phi1, lambda0, c, n, ret_val);
    }

    /**
     * Calculate earth circle in the sphere in double precision.
     * <p>
     * Returns n double lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * 
     * @param phi1 latitude in radians of center point.
     * @param lambda0 longitude in radians of center point.
     * @param c arc radius in radians (0 &lt; c &lt; PI).
     * @param s starting angle in radians. North up is zero.
     * @param e angular extent in radians, clockwise right from starting angle.
     * @param n number of points along circle edge to calculate.
     * @param ret_val double[] ret_val array of n*2 number of points along
     *        circle edge to calculate.
     * @return double[n] radian lat,lon pairs along earth circle.
     * 
     */
    final public static double[] earthCircle(double phi1, double lambda0,
                                             double c, double s, double e,
                                             int n, double[] ret_val) {
        double Az, cosAz, sinAz;
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);
        if (n < 2)
            n = 2; // Safety to avoid / by zero later.
        int end = n << 1;// *2

        // Only want to create a new return float array if there was a
        // null one passed in, or if the number of desired coordinates
        // is bigger than what ret_val is currently allocated for.
        if (ret_val == null || end > ret_val.length) {
            ret_val = new double[end];
        }

        double inc = e / (n - 1);
        Az = s;

        // generate the points in clockwise order (conforming to
        // internal standard!)
        for (int i = 0; i < end; i += 2, Az += inc) {
            cosAz = Math.cos(Az);
            sinAz = Math.sin(Az);

            ret_val[i] = Math.asin(sinphi1 * cosc + cosphi1 * sinc * cosAz);
            ret_val[i + 1] = Math.atan2(sinc * sinAz, cosphi1 * cosc - sinphi1
                    * sinc * cosAz)
                    + lambda0;
        }

        return ret_val;
    }

    /**
     * @deprecated use earthCorcle instead.
     */
    final public static double[] earth_circle(double phi1, double lambda0,
                                              double c, double s, double e,
                                              int n, double[] ret_val) {
        return earthCircle(phi1, lambda0, c, s, e, n, ret_val);
    }

    /*
     * testing public final static void main (String[] args) { double phi1 =
     * 34.3; double lambda0 = 130.299; double phi = -24; double lambda = 33.23;
     * 
     * float dist_sphere = spherical_distance ( ProjMath.degToRad((float)phi1),
     * ProjMath.degToRad((float)lambda0), ProjMath.degToRad((float)phi),
     * ProjMath.degToRad((float)lambda) ); // meters dist_sphere =
     * Planet.wgs84_earthEquatorialCircumferenceMeters
     * *(dist_sphere/MoreMath.TWO_PI);
     * Debug.output("sphere distance="+dist_sphere/1000f+" km");
     * 
     * AziDist invVar = ellipsoidalAziDist (
     * Planet.wgs84_earthEquatorialRadiusMeters,//major in meters
     * Planet.wgs84_earthFlat, //
     * Planet.international1974_earthEquatorialRadiusMeters,//major in meters //
     * Planet.international1974_earthFlat, ProjMath.degToRad(phi1),
     * ProjMath.degToRad(lambda0), ProjMath.degToRad(phi),
     * ProjMath.degToRad(lambda), new AziDist() ); Debug.output("ellipsoid
     * distance="+invVar.distance/1000d+" km"); }
     */
}