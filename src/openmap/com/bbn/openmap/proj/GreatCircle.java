// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.3 $
// $Date: 2003/07/16 00:02:34 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;

/**
 * Methods for calculating great circle and other distances on the sphere and
 * ellipsoid.
 * <p>
 * Spherical equations taken from John Synder's <i>Map Projections --A Working Manual</i>,
 * pp29-31.<br>
 * Latitude/longitude arguments must be in valid radians:
 * -PI&lt;=lambda&lt;PI, -PI/2&lt;=phi&lt;=PI/2
 */
public class GreatCircle {

    // cannot construct
    private GreatCircle() {}

    /**
     * Determine azimuth and distance on the ellipsoid.
     * @param a Semi-major axis of ellipsoid
     * @param finv flattening of the ellipsoid (WGS84 is 1/298.257)
     * @param glat1 Latitude of from station
     * @param glon1 Longitude of from station
     * @param glat2 Latitude of to station
     * @param glon2 Longitude of to station
     * @param ret_val AziDist struct
     * @return AziDist ret_val struct with azimuth and distance
     * @deprecated this has been yanked until we have a more stable and
     * documented algorithm
     */
    public final static AziDist ellipsoidalAziDist(
	    double a,
	    double finv,
	    double glat1,
	    double glon1,
	    double glat2,
	    double glon2,
	    AziDist ret_val)
    {
	return null;
    }

    /**
     * Calculate spherical arc distance between two points.
     * <p>
     * Computes arc distance `c' on the sphere. equation
     * (5-3a). (0 &lt;= c &lt;= PI)
     * <p>
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float arc distance `c'
     *
     */
    final public static float spherical_distance(
	float phi1, float lambda0, float phi, float lambda)
    {
	float pdiff = (float)Math.sin(((phi-phi1)/2f));
	float ldiff = (float)Math.sin((lambda-lambda0)/2f);
	float rval = (float)Math.sqrt((pdiff*pdiff) +
		     (float)Math.cos(phi1)*(float)Math.cos(phi)*(ldiff*ldiff));
	
	return 2.0f * (float)Math.asin(rval);
    }

    /**
     * Calculate spherical azimuth between two points.
     * <p>
     * Computes the azimuth `Az' east of north from phi1, lambda0
     * bearing toward phi and lambda. (5-4b).  (-PI &lt;= Az &lt;= PI).
     * <p>
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return float azimuth east of north `Az'
     *
     */
    final public static float spherical_azimuth(
	float phi1, float lambda0, float phi, float lambda)
    {
	float ldiff = lambda - lambda0;
	float cosphi = (float)Math.cos(phi);

	return (float)Math.atan2(
	    cosphi*(float)Math.sin(ldiff),
	    ((float)Math.cos(phi1)*(float)Math.sin(phi) -
	     (float)Math.sin(phi1)*cosphi*
	     (float)Math.cos(ldiff)));
    }

    /**
     * Calculate point at azimuth and distance from another point.
     * <p>
     * Returns a LatLonPoint at arc distance `c' in direction `Az'
     * from start point.
     * <p>
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @return LatLonPoint
     *
     */
    final public static LatLonPoint spherical_between(
	float phi1, float lambda0, float c, float Az)
    {
	float cosphi1 = (float)Math.cos(phi1);
	float sinphi1 = (float)Math.sin(phi1);
	float cosAz = (float)Math.cos(Az);
	float sinAz = (float)Math.sin(Az);
	float sinc = (float)Math.sin(c);
	float cosc = (float)Math.cos(c);

	return new LatLonPoint(ProjMath.radToDeg(
	    (float)Math.asin(sinphi1*cosc + cosphi1*sinc*cosAz)),
	    ProjMath.radToDeg((float)Math.atan2(
		sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0));
    }

    /**
     * Calculate point between two points.
     * <p>
     * Same as spherical_between() above except it calculates n equal
     * segments along the length of c.
     * <p>
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @param n number of points along great circle edge to calculate
     * @return float[n+1] radian lat,lon pairs
     *
     */
    final public static float[] spherical_between(
	float phi1, float lambda0, float c, float Az, int n)
    {
	// full constants for the computation
	float cosphi1 = (float)Math.cos(phi1);
	float sinphi1 = (float)Math.sin(phi1);
	float cosAz = (float)Math.cos(Az);
	float sinAz = (float)Math.sin(Az);
	int end = n<<1;

	// new radian points
	float[] points = new float[end+2];
	points[0] = phi1;
	points[1] = lambda0;

	float inc = c/n; c = inc;
	for (int i=2; i<=end; i+=2, c+=inc) {

	    // partial constants
	    float sinc = (float)Math.sin(c);
	    float cosc = (float)Math.cos(c);

	    // generate new point
	    points[i] =
		(float)Math.asin(sinphi1*cosc + cosphi1*sinc*cosAz);

	    points[i+1] =
	        (float)Math.atan2(
		    sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0;
	}
	return points;
    }

    /**
     * Calculate great circle between two points on the sphere.
     * <p>
     * Folds all computation (distance, azimuth, points between) into
     * one function for optimization. returns n or n+1 pairs of lat,lon
     * on great circle between lat-lon pairs.
     * <p>
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @param n number of segments
     * @param include_last return n or n+1 segments
     * @return float[n] or float[n+1] radian lat,lon pairs
     *
     */
    final public static float[] great_circle(
	float phi1, float lambda0, float phi, float lambda,
	int n, boolean include_last)
    {
	// number of points to generate
	int end = include_last ? n+1 : n;
	end<<=1;//*2 for pairs

	// calculate a bunch of stuff for later use
	float cosphi = (float)Math.cos(phi);
	float cosphi1 = (float)Math.cos(phi1);
	float sinphi1 = (float)Math.sin(phi1);
	float ldiff = lambda - lambda0;
	float p2diff = (float)Math.sin(((phi-phi1)/2));
	float l2diff = (float)Math.sin((ldiff)/2);


	// calculate spherical distance
	float c = 2.0f * (float)Math.asin(
	    (float)Math.sqrt(p2diff*p2diff + cosphi1*cosphi*l2diff*l2diff));

	
	// calculate spherical azimuth
	float Az = (float)Math.atan2(
	    cosphi*(float)Math.sin(ldiff),
	    (cosphi1*(float)Math.sin(phi) -
	     sinphi1*cosphi*
	     (float)Math.cos(ldiff)));
	float cosAz = (float)Math.cos(Az);
	float sinAz = (float)Math.sin(Az);


	// generate the great circle line
	float[] points = new float[end];
	points[0] = phi1;
	points[1] = lambda0;

	float inc = c/n; c = inc;
	for (int i=2; i<end; i+=2, c+=inc) {

	    // partial constants
	    float sinc = (float)Math.sin(c);
	    float cosc = (float)Math.cos(c);

	    // generate new point
	    points[i] =
		(float)Math.asin(sinphi1*cosc + cosphi1*sinc*cosAz);

	    points[i+1] =
	        (float)Math.atan2(
		    sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0;
	}
// 	Debug.output("Calculating GreatCircle: ");
// 	for (int i = 0; i< points.length; i++) {
// 	    Debug.output("(" + ProjMath.radToDeg(points[i].lat) + "," +
// 			       ProjMath.radToDeg(points[i].lon) + ") ");
// 	}
	return points;
    }//great_circle()

    /**
     * Calculate partial earth circle on the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param s starting angle in radians.  North up is zero.
     * @param e angular extent in radians, clockwise right from
     * starting angle.
     * @param n number of points along circle edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     *
     */
    final public static float[] earth_circle(
        float phi1, float lambda0, float c, float s, float e, int n)
    {
        return earth_circle(phi1, lambda0, c, s, e, n, new float[n<<1]);
    }

    /**
     * Calculate earth circle on the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     *
     */
    final public static float[] earth_circle(
	float phi1, float lambda0, float c, int n)
    {
	return earth_circle(phi1, lambda0, c, 0.0f, MoreMath.TWO_PI,
                            n, new float[n<<1]);
    }

    /**
     * Calculate earth circle in the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * @param phi1 latitude in radians of center point
     * @param lambda0 longitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @param ret_val float[] ret_val array of n*2 number of points along circle edge to calculate
     * @return float[n] radian lat,lon pairs along earth circle
     *
     */
    final public static float[] earth_circle(
	float phi1, float lambda0, float c, int n, float[] ret_val)
    {
      return earth_circle(phi1, lambda0, c, 0.0f, MoreMath.TWO_PI,
                          n, ret_val);
  }


    /**
     * Calculate earth circle in the sphere.
     * <p>
     * Returns n float lat,lon pairs at arc distance c from point at
     * phi1,lambda0.
     * <p>
     * @param phi1 latitude in radians of center point.
     * @param lambda0 longitude in radians of center point.
     * @param c arc radius in radians (0 &lt; c &lt; PI).
     * @param s starting angle in radians.  North up is zero.
     * @param e angular extent in radians, clockwise right from
     * starting angle.
     * @param n number of points along circle edge to calculate.
     * @param ret_val float[] ret_val array of n*2 number of points
     * along circle edge to calculate.
     * @return float[n] radian lat,lon pairs along earth circle.
     *
     */
    final public static float[] earth_circle(
	float phi1, float lambda0, float c, float s, float e,
        int n, float[] ret_val)
    {
	float Az, cosAz, sinAz;
	float cosphi1 = (float)Math.cos(phi1);
	float sinphi1 = (float)Math.sin(phi1);
	float sinc = (float)Math.sin(c);
	float cosc = (float)Math.cos(c);
	int end = n<<1;//*2
//	float[] ret_val = new float[end];

	float inc = e/n;
	Az = s;

	// generate the points in clockwise order (conforming to
	// internal standard!)
	for (int i = 0; i < end; i+=2, Az+=inc) {
	    cosAz = (float)Math.cos(Az);
	    sinAz = (float)Math.sin(Az);

	    ret_val[i] =
		(float)Math.asin(sinphi1*cosc + cosphi1*sinc*cosAz);
	    ret_val[i+1] = 
		(float)Math.atan2(
		    sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0;
	}

	return ret_val;
    }

    /* testing
    public final static void main (String[] args) {
	double phi1 = 34.3;
	double lambda0 = 130.299;
	double phi = -24;
	double lambda = 33.23;

	float dist_sphere = spherical_distance (
		ProjMath.degToRad((float)phi1),
		ProjMath.degToRad((float)lambda0),
		ProjMath.degToRad((float)phi),
		ProjMath.degToRad((float)lambda)
		);
	// meters
	dist_sphere = Planet.wgs84_earthEquatorialCircumferenceMeters*(dist_sphere/MoreMath.TWO_PI);
	Debug.output("sphere distance="+dist_sphere/1000f+" km");

	AziDist invVar = ellipsoidalAziDist (
		Planet.wgs84_earthEquatorialRadiusMeters,//major in meters
		Planet.wgs84_earthFlat,
//		Planet.international1974_earthEquatorialRadiusMeters,//major in meters
//		Planet.international1974_earthFlat,
		ProjMath.degToRad(phi1),
		ProjMath.degToRad(lambda0),
		ProjMath.degToRad(phi),
		ProjMath.degToRad(lambda),
		new AziDist()
		);
	Debug.output("ellipsoid distance="+invVar.distance/1000d+" km");
    }
    */
}
