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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LLXY.java,v $
// $RCSfile: LLXY.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import java.util.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;


/**
 * Implements the LLXY projection, which is basically something where
 * the lat/lon and pixel ratios are the same.
 */
public class LLXY extends Cylindrical {

    /**
     * The LLXY name.
     */
    public final static transient String LLXYName = "LLXY";

    /**
     * The LLXY type of projection.
     */
    public final static transient int LLXYType = 6304;

    // world<->screen coordinate offsets
    protected int hy, wx;
    protected float cLon;
    protected float cLat;
    /** Pixel per degree */
    protected float ppd;
    
    /**
     * Construct a LLXY projection.
     *
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public LLXY (LatLonPoint center, float scale, 
		 int width, int height) {
	super(center, scale, width, height, LLXYType);
    }

    public LLXY (LatLonPoint center, float scale, 
		 int width, int height, int type) {
	super(center, scale, width, height, type);
    }

//      protected void finalize() {
//  	Debug.message("gc", "LLXY finalized");
//      }

    /**
     * Return stringified description of this projection.
     * 
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
	return "LLXY[" + super.toString() + "]";
    }

    /**
     * Called when some fundamental parameters change.
     * 
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.
     */
    protected void computeParameters() {
	Debug.message("proj", "LLXY.computeParameters()");
	super.computeParameters();

	// compute the offsets
	hy = height/2;
	wx = width/2;
	// Degrees longitude of the center of the projection.
	cLon = ProjMath.radToDeg(ctrLon);
	cLat = ProjMath.radToDeg(ctrLat);
	ppd = world.x/360f;

	float latLimit = 90f - ((float)hy / ppd);

	if (cLat > latLimit) {
	    cLat = latLimit;
	    ctrLon = ProjMath.degToRad(cLat);
	} else if (cLat < -latLimit) {
	    cLat = -latLimit;
	    ctrLon = ProjMath.degToRad(cLat);
	}

	if (Debug.debugging("llxy")) {
	    Debug.output("LLXY.computeParameters: with center lat:" + cLat +
			 ", lon:" + cLon + " | width:" + width +
			 ", height:" + height + " | scale:" + scale);
	}
    }

    /**
     * Sets radian latitude to something sane.  This is an abstract
     * function since some projections don't deal well with extreme
     * latitudes.
     *
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     */
    public float normalize_latitude(float lat) {
	if (lat > NORTH_POLE) {
	    return NORTH_POLE;
	} else if (lat < SOUTH_POLE) {
	    return SOUTH_POLE;
	}
	return lat;
    }

    /**
     * Checks if a LatLonPoint is plot-able.
     * 
     * A point is always plot-able in the LLXY projection.
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
	return true;
    }

    /**
     * Projects a point from Lat/Lon space to X/Y space.
     * 
     * @param pt LatLonPoint
     * @param p Point retval
     * @return Point p
     */
    public Point forward(LatLonPoint pt, Point p) {
	return forward(pt.getLatitude(), pt.getLongitude(), p, false);
    }

    /**
     * Forward projects a lat,lon coordinates.
     * 
     * @param lat raw latitude in decimal degrees
     * @param lon raw longitude in decimal degrees
     * @param p Resulting XY Point
     * @return Point p
     */
    public Point forward(float lat, float lon, Point p) {
	return forward(lat, lon, p, false);
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point.
     * 
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param p Resulting XY Point
     * @param isRadian bogus argument indicating that lat,lon
     * arguments are in radians
     * @return Point p
     */
    public Point forward(float lat, float lon, 
			 Point p, boolean isRadian) {
	if (isRadian) {
	    lat = ProjMath.radToDeg(normalize_latitude(lat));
	    lon = ProjMath.radToDeg(lon);
	} else {
	lat = Length.DECIMAL_DEGREE.fromRadians(normalize_latitude(Length.DECIMAL_DEGREE.toRadians(lat)));
	}

	float newLon = Length.DECIMAL_DEGREE.fromRadians(wrap_longitude(Length.DECIMAL_DEGREE.toRadians(lon - cLon)));

	p.x = wx + Math.round(newLon * ppd);
	p.y = hy - Math.round((lat - cLat) * ppd);

  	if (Debug.debugging("llxydetail")) {
	    Debug.output("LLXY.forward(lon:" + ProjMath.radToDeg(lon) +
			 ", lat:" + ProjMath.radToDeg(lat) + 
			 " isRadian:" + isRadian + ")");
	    Debug.output("LLXY.forward   x:" + p.x + ", y:" + p.y + 
			 " scale: " + (float)scale);
	}
	return p;
    }

    /**
     * Inverse project a Point.
     * 
     * @param point x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     */
    public LatLonPoint inverse(Point pt, LatLonPoint llp) {
	return inverse(pt.x, pt.y, llp);
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     */
    public LatLonPoint inverse(int x, int y, LatLonPoint llp) {

	// convert from screen to world coordinates, and then
	// basically undo the math from the forward method.
	llp.setLongitude(((x - wx)/ppd) + cLon);
	llp.setLatitude(((hy - y)/ppd) + cLat);

	return llp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
	return LLXYName;
    }
}
