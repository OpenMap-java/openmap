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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Gnomonic.java,v $
// $RCSfile: Gnomonic.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************




package com.bbn.openmap.proj;

import java.awt.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;


/**
 * Implements the Gnomonic projection.
 */
public class Gnomonic extends Azimuth {

    /**
     * The Gnomonic name.
     */
    public final static transient String GnomonicName = "Gnomonic";

    /**
     * The Gnomonic type of projection.
     */
    public final static transient int GnomonicType = 12;

    protected int hy, wx;

    // almost constant projection parameters
    protected float cosCtrLat;
    protected float sinCtrLat;

    public final static transient float epsilon = 0.0001f;
    public final static transient float HEMISPHERE_EDGE=
	(float)((Math.PI/180d)*80d);//80degrees
    public final static transient float hPrime =
	1f/(float)Math.pow(Math.cos(HEMISPHERE_EDGE),2d);


    protected final static float NORTH_BOUNDARY = NORTH_POLE-epsilon;
    protected final static float SOUTH_BOUNDARY = -NORTH_BOUNDARY;


    /**
     * Construct a Mercator projection.
     * <p>
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param w width of screen
     * @param h height of screen
     *
     */
    public Gnomonic(
	LatLonPoint center, float scale, int width, int height)
    {
	super(center, scale, width, height, GnomonicType);
	setMinScale(1000.0f);
    }


    /**
     * Return stringified description of this projection.
     * <p>
     * @return String
     * @see Projection#getProjectionID
     *
     */
    public String toString() {
	return "Gnomonic[" + super.toString();
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.<p>
     *
     */
    protected void computeParameters() {
	Debug.message("proj", "Gnomonic.computeParameters()");
	super.computeParameters();

	// minscale is the minimum scale allowable (before integer wrapping
	// can occur)
	minscale = (float)Math.ceil((2*hPrime*planetPixelRadius)/(int)Integer.MAX_VALUE);
	if (minscale < 1)
	    minscale = 1;
	if (scale < minscale)
	    scale = minscale;

	// maxscale = scale at which a world hemisphere fits in the window
	maxscale = (width < height)
	    ? (float)(planetPixelRadius*2*hPrime)/(float)width
	    : (float)(planetPixelRadius*2*hPrime)/(float)height;
	if (maxscale < minscale) {
	    maxscale = minscale;
	}
	if (scale > maxscale) {
	    scale = maxscale;
	}
	scaled_radius = planetPixelRadius/scale;

	// width of the world in pixels at current scale.  We see only
	// one hemisphere.
	world.x = (int)((planetPixelRadius*2*hPrime)/scale);

	// calculate cutoff scale for XWindows workaround
	XSCALE_THRESHOLD = (int)((planetPixelRadius*2*hPrime)/64000);//fudge it a little bit

	// do some precomputation of stuff
	cosCtrLat = (float)Math.cos(ctrLat);
	sinCtrLat = (float)Math.sin(ctrLat);
	
	// compute the offsets
	hy = height/2;
	wx = width/2;
    }


    /**
     * Draw the background for the projection.
     * @param g Graphics
     */
    public void drawBackground(Graphics g) {
	drawBackground((Graphics2D)g, backgroundColor);
    }

    /**
     * Draw the background for the projection.
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
     */
    public void drawBackground(Graphics2D g, java.awt.Paint paint) {
	g.setPaint(paint);
	g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Sets radian latitude to something sane.  This is an abstract
     * function since some projections don't deal well with extreme
     * latitudes.<p>
     *
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     *
     */
    public float normalize_latitude(float lat) {
	if (lat > NORTH_BOUNDARY) {
	    return NORTH_BOUNDARY;
	} else if (lat < SOUTH_BOUNDARY) {
	    return SOUTH_BOUNDARY;
	}
	return lat;
    }


    /**
     * Get the distance c of the point from the center of the hemisphere.
     * <p>
     * @param phi1 latitude
     * @param lambda0 longitude
     * @param phi latitude
     * @param lambda longitude
     * @return float c angular distance in radians
     *
     */
    final public static float hemisphere_distance(
	float phi1, float lambda0, float phi, float lambda)
    {
	return GreatCircle.spherical_distance(
	    phi1, lambda0, phi, lambda)/*-epsilon*/;
    }


    /**
     * Check if a given lat/lon is within the visible hemisphere.
     * <p>
     * @param phi1 latitude
     * @param lambda0 longitude
     * @param phi latitude
     * @param lambda longitude
     * @return boolean true if within the visible hemisphere, false if not
     *
     */
    final public static boolean hemisphere_clip(
	float phi1, float lambda0, float phi, float lambda)
    {
	return (GreatCircle.spherical_distance(
	    phi1, lambda0, phi, lambda)/*-epsilon*/ <= HEMISPHERE_EDGE);
    }



    /**
     * Calculate point along edge of hemisphere (using center point and
     * current azimuth).
     * <p>
     * This is invoked for points that aren't visible in the current
     * hemisphere.
     * <p>
     * @param p Point
     * @return Point p
     *
     */
    private Point edge_point(Point p, float current_azimuth) {
	float c = HEMISPHERE_EDGE;
	LatLonPoint tmpll = GreatCircle.spherical_between(
		ctrLat, ctrLon, c/*-epsilon*/, current_azimuth);
	float phi = tmpll.radlat_;
	float lambda = tmpll.radlon_;

	float kPrime = 1f/(float)Math.cos(c);
	float cosPhi = (float)Math.cos(phi);
	float sinPhi = (float)Math.sin(phi);
	float lambdaMinusCtrLon = (float)(lambda-ctrLon);
	float cosLambdaMinusCtrLon = (float)Math.cos(lambdaMinusCtrLon);
	float sinLambdaMinusCtrLon = (float)Math.sin(lambdaMinusCtrLon);

	p.x = (int)(scaled_radius*kPrime*cosPhi*sinLambdaMinusCtrLon) + wx;
	p.y = hy - (int)(scaled_radius*kPrime*
		(cosCtrLat*sinPhi - sinCtrLat*cosPhi*cosLambdaMinusCtrLon));

	return p;
    }


    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is plot-able if it is within the visible hemisphere.
     * <p>
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
	lat = normalize_latitude(ProjMath.degToRad(lat));
	lon = wrap_longitude(ProjMath.degToRad(lon));
	return hemisphere_clip(ctrLat, ctrLon, lat, lon);
    }


    /**
     * Forward project a point.
     * If the point is not within the viewable hemisphere, return flags in
     * AzimuthVar variable if specified.
     * @param phi float latitude in radians
     * @param lambda float longitude in radians
     * @param pt Point
     * @param azVar AzimuthVar or null
     * @return Point pt
     */
    protected Point _forward (
	    float phi, float lambda, Point p, AzimuthVar azVar)
    {
	float c = hemisphere_distance(ctrLat, ctrLon, phi, lambda);
	// normalize invalid point to the edge of the sphere
	if (c > HEMISPHERE_EDGE) {
	    float az =
		GreatCircle.spherical_azimuth(ctrLat, ctrLon, phi, lambda);
	    if (azVar != null) {
		azVar.invalid_forward = true;	// set the invalid flag
		azVar.current_azimuth = az;	// record azimuth of this point
	    }
	    return edge_point(p, az);
	}

	float kPrime = 1f/(float)Math.cos(c);
	float cosPhi = (float)Math.cos(phi);
	float sinPhi = (float)Math.sin(phi);
	float lambdaMinusCtrLon = (float)(lambda-ctrLon);
	float cosLambdaMinusCtrLon = (float)Math.cos(lambdaMinusCtrLon);
	float sinLambdaMinusCtrLon = (float)Math.sin(lambdaMinusCtrLon);

	p.x = (int)(scaled_radius*kPrime*cosPhi*sinLambdaMinusCtrLon) + wx;
	p.y = hy - (int)(scaled_radius*kPrime*
		(cosCtrLat*sinPhi - sinCtrLat*cosPhi*cosLambdaMinusCtrLon));

	return p;
    }


    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     *
     */
    public LatLonPoint inverse(int x, int y, LatLonPoint llp) {
	// convert from screen to world coordinates
	x = x - wx;
	y = hy - y;


// 	Debug.output("Gnomonic.inverse: x,y=" + x + "," + y);

	float rho = (float)Math.sqrt(x*x + y*y);
	if (rho == 0f) {
	    Debug.message("proj", "Gnomonic.inverse: center!");
	    llp.setLatLon(
		    ProjMath.radToDeg(ctrLat),
		    ProjMath.radToDeg(ctrLon));
	    return llp;
	}

	float c = (float)Math.atan2(rho, scaled_radius);
	float cosC = (float)Math.cos(c);
	float sinC = (float)Math.sin(c);

	// calculate latitude 
	float lat = (float)Math.asin(
	    cosC * sinCtrLat + (y * sinC * (cosCtrLat/rho)));


	// calculate longitude
	float lon = ctrLon + (float)Math.atan2(
		(x*sinC),
		(rho*cosCtrLat*cosC - y*sinCtrLat*sinC));
// 	Debug.output("Gnomonic.inverse: lat,lon=" +
// 			   ProjMath.radToDeg(lat) + "," +
// 			   ProjMath.radToDeg(lon));

	// check if point in outer space
//	if (MoreMath.approximately_equal(lat, ctrLat) &&
//	       MoreMath.approximately_equal(lon, ctrLon) &&
//	       (Math.abs(x-(width/2))<2) &&
//	       (Math.abs(y-(height/2))<2))
	if (Float.isNaN(lat) || Float.isNaN(lon))
	{
//	    Debug.message("proj", "Gnomonic.inverse(): outer space!");
	    lat = ctrLat;
	    lon = ctrLon;
	}
	llp.setLatLon(
		ProjMath.radToDeg(lat),
		ProjMath.radToDeg(lon));
	return llp;
    }


    /**
     * Inverse project a Point.
     * <p>
     * @param point x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     *
     */
    public LatLonPoint inverse(Point pt, LatLonPoint llp) {
	return inverse(pt.x, pt.y, llp);
    }


    /**
     * Check if equator is visible on screen.
     * @return boolean
     */
    public boolean overEquator () {
	LatLonPoint llN = inverse(width/2, 0, new LatLonPoint());
	LatLonPoint llS = inverse(width/2, height, new LatLonPoint());
	return MoreMath.sign(llN.radlat_) != MoreMath.sign(llS.radlat_);
    }


    /**
     * Get the upper left (northernmost and westernmost) point of the
     * projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft() {
	LatLonPoint tmp = new LatLonPoint();
	float lat, lon;

	// over north pole
	if (overNorthPole()) {
	    lat = NORTH_POLE;
	    lon = -DATELINE;
	}

	// over south pole
	else if (overSouthPole()) {
	    lon = -DATELINE;
	    if (overEquator()) {
		// get top center for latitude
		tmp = inverse(width/2, 0, tmp);
		lat = tmp.radlat_;
	    } else {
		// get left top corner for latitude
		tmp = inverse(0, 0, tmp);
		lat = tmp.radlat_;
	    }
	}

	// view in northern hemisphere
	else if (ctrLat >= 0f) {
	    // get left top corner for longitude
	    tmp = inverse(0,0,tmp);
	    lon = tmp.radlon_;
	    // get top center for latitude
	    tmp = inverse(width/2, 0, tmp);
	    lat = tmp.radlat_;
	}

	// view in southern hemisphere
	else {
	    // get left bottom corner for longitude
	    tmp = inverse(0, height, tmp);
	    lon = tmp.radlon_;

	    if (overEquator()) {
		// get top center (for latitude)
		tmp = inverse(width/2, 0, tmp);
		lat = tmp.radlat_;
	    } else {
		// get left top corner (for latitude)
		tmp = inverse(0, 0, tmp);
		lat = tmp.radlat_;
	    }
	}
	tmp.setLatLon(lat, lon, true);
//	Debug.output("ul="+tmp);
	return tmp;
    }


    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * <p>
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight() {
	LatLonPoint tmp = new LatLonPoint();
	float lat, lon;

	// over north pole
	if (overNorthPole()) {
	    lon = DATELINE;
	    if (overEquator()) {
		// get bottom center for latitude
		tmp = inverse(width/2, height, tmp);
		lat = tmp.radlat_;
	    } else {
		// get bottom right corner for latitude
		tmp = inverse(width, height, tmp);
		lat = tmp.radlat_;
	    }
	}

	// over south pole
	else if (overSouthPole()) {
	    lat = SOUTH_POLE;
	    lon = DATELINE;
	}

	// view in northern hemisphere
	else if (ctrLat >= 0f) {
	    // get the right top corner for longitude
	    tmp = inverse(width, 0, tmp);
	    lon = tmp.radlon_;

	    if (overEquator()) {
		// get the bottom center (for latitude)
		tmp = inverse(width/2, height, tmp);
		lat = tmp.radlat_;
	    } else {
		// get the right bottom corner (for latitude)
		tmp = inverse(width, height, tmp);
		lat = tmp.radlat_;
	    }
	}

	// view in southern hemisphere
	else {
	    // get the right bottom corner for longitude
	    tmp = inverse(width,height,tmp);
	    lon = tmp.radlon_;
	    // get bottom center for latitude
	    tmp = inverse(width/2, height, tmp);
	    lat = tmp.radlat_;
	}
	tmp.setLatLon(lat, lon, true);
//	Debug.output("lr="+tmp);
	return tmp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
	return GnomonicName;
    }
}
