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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/geo/Geo.java,v $
// $RCSfile: Geo.java,v $
// $Revision: 1.3 $
// $Date: 2003/12/23 22:55:23 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.geo;

import java.util.Enumeration;

/** 
 * This is a Ken Anderson class for dealing with geographical coordinates.
 * <p>
 * A class that represents a point on the Earth as a three
 * dimensional unit length vector, rather than latitude and longitude.
 * For the theory and an efficient implementation using partial
 * evaluation see: <br>
 * http://openmap.bbn.com/~kanderso/lisp/performing-lisp/essence.ps<p>
 *
 * This implementation matches the theory carefully, but does not use
 * partial evaluation.<p>
 *
 * For the area calculation see: http://math.rice.edu/~pcmi/sphere/<p>
 *
 * @author Ken Anderson with additions by Ben Lubin
 */
public class Geo {

    /** Constants for the shape of the earth.
	see http://www.gfy.ku.dk/%7Eiag/HB2000/part4/groten.htm **/
    public static final double radiusKM = 6378.13662; // in KM.
    public static final double radiusNM = 3443.9182; // in NM.
    public static final double flattening = 1.0/298.25642;
    public static final double f = (1.0 - flattening)*(1.0 - flattening);

    /**
     * Convert from geographic to geocentric latitude (radians).
     */
    public static double geocentricLatitude(double geographicLatitude) {
	return Math.atan((Math.tan(geographicLatitude)*f)); 
    }

    /**
     * Convert from geocentric to geographic latitude (radians) 
     */
    public static double geographicLatitude(double geocentricLatitude) {
	return Math.atan(Math.tan(geocentricLatitude)/f); 
    }
  
    /**
     * Convert from degrees to radians. 
     */
    public static double radians(double degrees) {
	return degrees*Math.PI/180.0; 
    }

    /**
     * Convert from radians to degrees. 
     */
    public static double degrees(double radians) {
	return  radians*180.0/Math.PI; 
    }

    /**
     * Convert radians to kilometers. 
     */
    public static double km(double radians) {
	return radians*radiusKM;
    }

    /**
     * Convert kilometers to radians. 
     */
    public static double kmToAngle(double km) {
	return km/radiusKM;
    }

    /** 
     * Convert radians to nauticalMiles. 
     */
    public static double nm(double radians) {
	return radians*radiusNM;
    }

    /** 
     * Convert nautical miles to radians. 
     */
    public static double nmToAngle(double nm) {
	return nm/radiusNM;
    }

    private double x;
    private double y;
    private double z;

    /**
     * Construct a Geo from its latitude and longitude. 
     */
    public Geo(double lat, double lon) {
	double theta = radians(lon);
	double rlat = geocentricLatitude(radians(lat));
	double c = Math.cos(rlat);
	x = c*Math.cos(theta);
	y = c*Math.sin(theta);
	z = Math.sin(rlat);
    }


    /**
     * Construct a Geo from its latitude and longitude in radians. 
     */
    public static Geo createGeo(double rlatR, double rlon) {
	double theta = rlon;
	double rlat = geocentricLatitude(rlatR);
	double c = Math.cos(rlat);
	double x = c*Math.cos(theta);
	double y = c*Math.sin(theta);
	double z = Math.sin(rlat);
	return new Geo(x, y, z);
    }

    /**
     * Construct a Geo from its parts. 
     */
    public Geo(double x, double y, double z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public Geo midPoint(Geo g2) {
	return add(g2).normalize();
    }

    public Geo interpolate(Geo g2, double x) {
	return scale(x).add(g2.scale(1-x)).normalize();
    }
  
    public String toString() {
	return "(Geo. " + getLatitude() + " " + getLongitude() + ")";
    }
  	
    public double getLatitude() {
	return degrees(geographicLatitude 
		       (Math.atan2(z, Math.sqrt(x*x + y*y))));
    }

    public double getLongitude() {
	return degrees(Math.atan2(y, x));
    }

    // Readers
    public double x() { return this.x; }
    public double y() { return this.y; }
    public double z() { return this.z; }

    /** North pole. */
    public static Geo north = new Geo(0.0, 0.0, 1.0);

    /**
     * Dot product. 
     */
    public double dot(Geo b) {
	return (this.x()*b.x() + this.y()*b.y() + this.z()*b.z());
    }

    /**
     * Dot product. 
     */
    public static double dot(Geo a, Geo b) {
	return (a.x()*b.x() + a.y()*b.y() + a.z()*b.z()); 
    }

    /**
     * Euclidian length. 
     */
    public double length() { 
	return Math.sqrt(this.dot(this)); 
    }

    /** 
     * Multiply this by s. 
     */
    public Geo scale(double s) { 
	return new Geo(this.x()*s, this.y()*s, this.z()*s); 
    }
    
    /**
     * Returns a unit length vector parallel to this. 
     */
    public Geo normalize() { 
	return this.scale(1.0/this.length()); 
    }
	
    /**
     * Vector cross product. 
     */
    public Geo cross(Geo b) {
	return new Geo(this.y()*b.z() - this.z()*b.y(),
		       this.z()*b.x() - this.x()*b.z(),
		       this.x()*b.y() - this.y()*b.x()); 
    }
  
    /**
     * Equivalent to this.cross(b).length(). 
     */
    public double crossLength(Geo b) {
	double x = this.y()*b.z() - this.z()*b.y();
	double y = this.z()*b.x() - this.x()*b.z();
	double z = this.x()*b.y() - this.y()*b.x();
	return Math.sqrt(x*x + y*y + z*z);
    }
  
    /** 
     * Equivalent to this.cross(b).normalize(). 
     */
    public Geo crossNormalize(Geo b) {
	double x = this.y()*b.z() - this.z()*b.y();
	double y = this.z()*b.x() - this.x()*b.z();
	double z = this.x()*b.y() - this.y()*b.x();
	double L = Math.sqrt(x*x + y*y + z*z);
	return new Geo(x/L, y/L, z/L);
    }
  
    /**
     * Eqvivalent to this.cross(b).normalize(). 
     */
    public static Geo crossNormalize(Geo a, Geo b) {
	return a.crossNormalize(b);
    }

    /**
     * Returns this + b. 
     */
    public Geo add(Geo b) {
	return new Geo(this.x() + b.x(),
		       this.y() + b.y(),
		       this.z() + b.z()); 
    }
	
    /**
     * Returns this - b. 
     */
    public Geo subtract(Geo b) {
	return new Geo(this.x() - b.x(),
		       this.y() - b.y(),
		       this.z() - b.z()); 
    }
	
    /**
     * Angular distance, in radians between this and v2. 
     */
    public double distance(Geo v2) {
	return Math.atan2(v2.crossLength(this), v2.dot(this)); 
    }
	
    /**
     * Angular distance, in radians between v1 and v2. 
     */
    public static double distance(Geo v1, Geo v2) {
	return v1.distance(v2); 
    }

    /**
     * Angular distance, in radians between the two lat lon points. 
     */
    public static double distance(double lat1, double lon1, 
				  double lat2, double lon2) {
	return Geo.distance(new Geo(lat1, lon1), new Geo(lat2, lon2)); 
    }

    /**
     * Distance in kilometers. 
     */
    public double distanceKM(Geo v2) { 
	return km(distance(v2)); 
    }

    /** 
     * Distance in kilometers. 
     */
    public static double distanceKM(Geo v1, Geo v2) {
	return v1.distanceKM(v2); 
    }

    /** 
     * Distance in kilometers. 
     */
    public static double distanceKM(double lat1, double lon1, 
				    double lat2, double lon2) {
	return Geo.distanceKM(new Geo(lat1, lon1), new Geo(lat2, lon2)); 
    }

    /** 
     * Distance in nautical miles. 
     */
    public double distanceNM(Geo v2) { 
	return nm(distance(v2)); 
    }

    /**
     * Distance in nautical miles. 
     */
    public static double distanceNM(Geo v1, Geo v2) {
	return v1.distanceNM(v2); 
    }

    /**
     * Distance in nautical miles. 
     */
    public static double distanceNM(double lat1, double lon1, 
				    double lat2, double lon2) {
	return Geo.distanceNM(new Geo(lat1, lon1), new Geo(lat2, lon2)); 
    }

    /**
     * Azimuth in radians from this to v2. 
     */
    public double azimuth(Geo v2) {
	/* n1 is the great circle representing the meridian of v1.  n2 is
	   the great circle between v1 and v2.  The azimuth is the angle
	   between them but we specialized the cross product. */
	Geo n1 = north.cross(this);
	double s1 = n1.length();
	Geo n2 = v2.cross(this);
	double az = Math.atan2(-north.dot(n2), n1.dot(n2));
	return (az > 0.0) ? az : 2.0*Math.PI + az;
    }
		
    /** 
     * Given 3 points on a sphere, p0, p1, p2, return the angle
     * between them in radians. 
     */
    public static double angle(Geo p0, Geo p1, Geo p2) {
	return Math.PI - p0.cross(p1).distance(p1.cross(p2)); }
	
    /**
     * Computes the area of a polygon on the surface of a unit sphere
     * given an enumeration of its point.  For a non unit
     * sphere, multiply this by the radius of sphere squared.
     * @param vs an Enumeration of Geos marking the polygon.
     */
    public static double area(Enumeration vs) {
	int count = 0;
	double area = 0;
	Geo v0 = (Geo) vs.nextElement();
	Geo v1 = (Geo) vs.nextElement();
	Geo p0 = v0;
	Geo p1 = v1;
	Geo p2 = null;
	while (vs.hasMoreElements()) {
	    count = count + 1;
	    p2 = (Geo) vs.nextElement();
	    area = area + angle(p0, p1, p2);
	    p0 = p1;
	    p1 = p2; }
		
	count = count + 1;
	p2 = v0;
	area = area + angle (p0, p1, p2);
	p0 = p1;
	p1 = p2;
    
	count = count + 1;
	p2 = v1;
	area = area + angle(p0, p1, p2);
		
	return area - (count - 2)*Math.PI;
    }

    /**
     * Is the point, p, within radius radians of the great circle
     * segment between this and v2?
     * @param v2 Geo marking the center of the area to test.
     * @param radius great circle length of segment to test for p's
     * distance away from v2.  In radians.
     * @param p Geo marking the test point.
     */
    public boolean isInside(Geo v2, double radius, Geo p) {
	Geo gc = this.crossNormalize(v2);
	if (!(Math.abs(gc.dot(p)) <= Math.cos((Math.PI/2.0) - radius)))
	    return false;
	if (this.distance(p) <= radius || v2.distance(p) <= radius)
	    return true;
	Geo d = v2.subtract(this);
	double L = d.length();
	Geo n = d.normalize();
	Geo dp = p.subtract(this);
	double size = n.dot(dp);
	return (0 <= size && size <= L);
    }


    /**
     * Static versions using conventional coordinates. 
     */
    public static boolean isInside(double lat1, double lon1,
				   double lat2, double lon2,
				   double radius,
				   double lat3, double lon3) {
	return (new Geo(lat1, lon1)).isInside(new Geo(lat2, lon2), radius,
					      new Geo(lat3, lon3));
    }

    /**
     * Is Geo p inside the time bubble along the great circle segment
     * from this to v2 looking forward forwardRadius and backward
     * backwardRadius.
     */
    public boolean inBubble(Geo v2,
			    double forwardRadius,
			    double backRadius,
			    Geo p) {
	return
	    distance(p) <=
	    ((v2.subtract(this).normalize().dot(p.subtract(this)) > 0.0)
	     ? forwardRadius
	     :  backRadius);
    }

    /**
     * Returns the point opposite this point on the earth. 
     */
    public Geo antipode() { return this.scale(-1.0); }

    /**
     * Find the intersection of the great circle between this and q
     * and the great circle normal to r.  <p>
     *
     * That is, find the point y lying between this and q such that
     *	<pre>
     *	y = x*this + (1-x)*q
     *	y.dot(r) = 0
     *	[x*this + (1-x)*q].dot(r) = 0
     *	x*this.dot(r) + (1-x)*q.dot(r) = 0
     *	x*a + (1-x)*b = 0
     *	x = -b/(a - b)
     *	</pre>
     */
    public Geo intersect(Geo q, Geo r) {
	double a = this.dot(r);
	double b = q.dot(r);
	double x = -b/(a - b);
	return this.scale(x).add(q.scale(1.0 - x)).normalize();
    }
}
