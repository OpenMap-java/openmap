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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/geo/Intersection.java,v $
// $RCSfile: Intersection.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.geo;

/**
 * Contains great circle intersection algorithms and helper methods.
 * Sources: 
 * <pre>
 * http://williams.best.vwh.net/intersect.htm
 * http://mathforum.org/library/drmath/view/60711.html
 * </pre>
 */
public class Intersection {

    /**
     * Returns the two antipodal points of interection of 
     * two great circles defined by the arcs 
     * (lat1, lon1) to (lat2, lon2) and 
     * (lat2, lon2) to (lat4, lon4).
     * All lat-lon values are in degrees.
     *
     * @return an array of two lat-lon points arranged as lat, lon,
     * lat, lon
     */
    public static float[] getIntersection(float lat1, float lon1,
					  float lat2, float lon2,
					  float lat3, float lon3,
					  float lat4, float lon4) {

	Geo geo1 = new Geo(lat1, lon1);
	Geo geo2 = new Geo(lat2, lon2);

	Geo geoCross1 = geo1.crossNormalize(geo2);

	Geo geo3 = new Geo(lat3, lon3);
	Geo geo4 = new Geo(lat4, lon4);

	Geo geoCross2 = geo3.crossNormalize(geo4);

	Geo geo = geoCross1.crossNormalize(geoCross2);

	float lat = (float)geo.getLatitude();
	float lon = (float)geo.getLongitude();

	float[] latlon = new float[4];

	latlon[0] = lat;
	latlon[1] = lon;
	latlon[2] = - lat;
	latlon[3] = lon + 180;
     
	return latlon;
    }

    /**
     * Returns the point of intersection of two great circle segments
     * defined by the arcs (lat1, lon1) to (lat2, lon2) and (lat2,
     * lon2) to (lat4, lon4).  All lat-lon values are in degrees.
     *
     * @return a float array of length 4 containing upto 2 valid 
     * lat-lon points of intersection that lie on both segments.
     * Positions in the array not containing a valid lat/lon value are 
     * initialized to Float.MAX_VALUE.
     */
    public static float[] getSegIntersection(float lat1, float lon1,
					     float lat2, float lon2,
					     float lat3, float lon3,
					     float lat4, float lon4) {
     
	float[] ll = getIntersection(lat1, lon1, lat2, lon2, 
				     lat3, lon3, lat4, lon4);

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

	float[] llp = new float[] {Float.MAX_VALUE, Float.MAX_VALUE,
				   Float.MAX_VALUE, Float.MAX_VALUE};

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

    /**
     * Returns true if the two segs intersect in at least one point.
     * All lat-lon values are in degrees.
     */
    public static boolean intersects(float lat1, float lon1,
				     float lat2, float lon2,
				     float lat3, float lon3,
				     float lat4, float lon4) {

	float[] llp = getSegIntersection(lat1, lon1, lat2, lon2,
					 lat3, lon3, lat4, lon4);

	return (llp[0] != Float.MAX_VALUE && llp[1] != Float.MAX_VALUE) ||
	    (llp[2] != Float.MAX_VALUE && llp[3] != Float.MAX_VALUE);
    }

    /**
     * Checks if the two polygonal areas intersect. The two polygonal
     * regions are represented by two lat-lon arrays in the lat1,
     * lon1, lat2, lon2,... format. For closed polygons the last pair
     * of points in the array should be the same as the first pair.
     * All lat-lon values are in degrees.
     */
    public static boolean polyIntersect(float[] polyPoints1, 
					float[] polyPoints2) {

	// go through each side of poly1 and test to see if it intersects
	// with any side of poly2

	for (int i = 0; i < polyPoints1.length/2 - 1; i++) {

	    for (int j = 0; j < polyPoints2.length/2 - 1; j++) {

		if (intersects(polyPoints1[2*i],   polyPoints1[2*i+1],
			       polyPoints1[2*i+2], polyPoints1[2*i+3],
			       polyPoints2[2*j],   polyPoints2[2*j+1],
			       polyPoints2[2*j+2], polyPoints2[2*j+3]))
		    return true;
	    }
	}

	return false;
    }

    /**
     * Checks if the polygon or polyline represented by the polypoints
     * contains any lines that intersect each other.  All lat-lon
     * values are in degrees.
     */
    public static boolean isSelfIntersectingPoly(float[] polyPoints) {

	for (int i = 0; i < polyPoints.length/2 - 1; i++) {

	    for (int j = i+1; j < polyPoints.length/2 - 1; j++) {

		float lat1 = polyPoints[2*i];
		float lon1 = polyPoints[2*i+1];
		float lat2 = polyPoints[2*i+2];
		float lon2 = polyPoints[2*i+3];

		float lat3 = polyPoints[2*j];
		float lon3 = polyPoints[2*j+1];
		float lat4 = polyPoints[2*j+2];
		float lon4 = polyPoints[2*j+3];

		// ignore adjacent segments
		if ((lat1 == lat4 && lon1 == lon4) ||
		    (lat2 == lat3 && lon2 == lon3))
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
     * (lat2, lon2). <br> All lat-lon values are in degrees.
     * @returns distance in radians.
     */
    public static float pointCircleDistance(float lat1, float lon1, 
					    float lat2, float lon2,
					    float lat, float lon) {

	Geo n = Geo.crossNormalize(new Geo(lat1, lon1), new Geo(lat2, lon2));
	Geo c = new Geo(lat, lon);
	c = c.normalize();
	double cosTheta = Geo.dot(n, c);
	double theta = Math.acos(cosTheta);
    
	return (float) Math.abs(Math.PI/2-theta);
    }

    /**
     * Returns true or false depending on whether the great circle seg
     * from point (lat1, lon1) to (lat2, lon2) intersects the circle
     * of radius centered at point (lat, lon). All lat-lon values are
     * in degrees, radius is in radians.
     */
    public static boolean intersectsCircle(float lat1, float lon1, 
					   float lat2, float lon2,
					   float lat, float lon,
					   float radius) {

	// check if either of the end points of the seg are inside the circle
	double d1 = Geo.distance(lat1, lon1, lat, lon);
	if (d1 < radius)
	    return true;

	double d2 = Geo.distance(lat2, lon2, lat, lon);
	if (d2 < radius)
	    return true;

	float dist = pointCircleDistance(lat1, lon1, lat2, lon2,
					 lat, lon);

	if (dist > radius)
	    return false;

	// calculate point of intersection of great circle containing
	// (lat, lon) and perpendicular to great circle containing
	// (lat1, lon1) and (lat2, lon2)
	Geo a = new Geo(lat1, lon1);
	Geo b = new Geo(lat2, lon2);
	Geo c = new Geo(lat, lon);

	Geo g = a.cross(b);
	Geo f = c.cross(g);
	Geo i = f.crossNormalize(g);
	Geo i2 = i.antipode();

	// check if point of intersection lies on the segment
	// length of seg
	double d = Geo.distance(lat1, lon1, lat2, lon2);

	// between seg endpoints and first point of intersection
	double d11 = Geo.distance(lat1, lon1, i.getLatitude(), i.getLongitude());
	double d12 = Geo.distance(lat2, lon2, i.getLatitude(), i.getLongitude());

	// between seg1 endpoints and second point of intersection
	double d21 = Geo.distance(lat1, lon1, i2.getLatitude(), i2.getLongitude());
	double d22 = Geo.distance(lat2, lon2, i2.getLatitude(), i2.getLongitude());

	return ((d11 <= d && d12 <= d) || (d21 <= d && d22 <= d));
    
    }

    /**
     * Returns true if the specified poly path intersects the circle
     * centered at (lat, lon).  All lat-lon values are in degrees,
     * radius is in radians.
     */
    public static boolean intersectsCircle(float[] polyPoints, 
					   float lat, float lon,
					   float radius) {

	for (int i = 0; i < polyPoints.length/2 - 1; i++) {

	    float lat1 = polyPoints[2*i];
	    float lon1 = polyPoints[2*i+1];
	    float lat2 = polyPoints[2*i+2];
	    float lon2 = polyPoints[2*i+3];

	    if (intersectsCircle(lat1, lon1, lat2, lon2,
				 lat, lon, radius))
		return true;
	}

	return false;
    }

    /**
     * Calculates the great circle distance between the two lat, lon
     * points.  All lat-lon values are in degrees.
     */
    public static float greatCircleDistance(float lat1, float lon1, 
					    float lat2, float lon2) {

	return (float) (Math.acos(Math.sin(lat1) * Math.sin(lat2) +
				  Math.cos(lat1) * Math.cos(lat2) *
				  Math.cos(lon1 - lon2)));
    }


    public static void main (String[] args) {

	float lat1 = 60;
	float lon1 = -130;
	float lat2 = 30;
	float lon2 = -70;

	float lat3 = 60;
	float lon3 = -70;
	float lat4 = 30;
	float lon4 = -130;

	float[] ll = getSegIntersection(lat1, -lon1, lat2, -lon2, 
					lat3, -lon3, lat4, -lon4);

	System.out.println("(1)="+ll[0] + ", " + (-ll[1]));
	System.out.println("(2)="+ll[2] + ", " + (-ll[3]));

	boolean b1 = intersects(lat1, -lon1, lat2, -lon2, 
				lat3, -lon3, lat4, -lon4);

	System.out.println("intersects="+b1);


	float[] polypoints1 = new float[] {60, -130, 60, -70,
					   30, -70, 30, -130,
					   60, -130};

	float[] polypoints2 = new float[] {50, -60, 50, -40,
					   20, -40, 50, -60};

	boolean b2 = polyIntersect(polypoints1, polypoints2);

	System.out.println("polyIntersect="+b2);

	float dist = pointCircleDistance(60, -130, 10, 10, 60, -70); 
	System.out.println("dist="+dist);


	boolean b3 = intersectsCircle(60, -130, 70, -100, 60, -70, 
				      (float)Geo.nmToAngle(1247)); 
	System.out.println("b3="+b3);
    }

};
