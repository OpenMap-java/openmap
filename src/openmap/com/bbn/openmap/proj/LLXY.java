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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LLXY.java,v $
// $RCSfile: LLXY.java,v $
// $Revision: 1.10 $
// $Date: 2006/04/07 15:21:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the LLXY projection, which is basically something where
 * the lat/lon and pixel ratios are the same.
 */
public class LLXY extends Cylindrical implements EqualArc {

    /**
     * The LLXY name.
     */
    public final static transient String LLXYName = "LLXY";

    // world<->screen coordinate offsets
    protected int hy, wx;
    protected double cLon;
    protected double cLat;
    /** Pixel per degree */
    protected double ppd;

    /**
     * Construct a LLXY projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public LLXY(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
    }

    // protected void finalize() {
    // Debug.message("gc", "LLXY finalized");
    // }

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
     * Each projection will decide how to respond to this change. For
     * instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.
     */
    protected void computeParameters() {
        Debug.message("proj", "LLXY.computeParameters()");
        super.computeParameters();

        // compute the offsets
        hy = height / 2;
        wx = width / 2;
        // Degrees longitude of the center of the projection.
        cLon = ProjMath.radToDeg(centerX);
        cLat = ProjMath.radToDeg(centerY);
        ppd = world.x / 360f;

        double latLimit = 90 - (hy / ppd);

        // Add check for zoom allowing more than 90 degrees viewable
        if (latLimit < 0.0f)
            latLimit = 0.0f;

        if (cLat > latLimit) {
            cLat = latLimit;
            centerY = ProjMath.degToRad(cLat);
        } else if (cLat < -latLimit) {
            cLat = -latLimit;
            centerY = ProjMath.degToRad(cLat);
        }

        if (Debug.debugging("llxy")) {
            Debug.output("LLXY.computeParameters: with center lat:" + cLat
                    + ", lon:" + cLon + " | width:" + width + ", height:"
                    + height + " | scale:" + scale);
        }
    }

    /**
     * Sets radian latitude to something sane. This is an abstract
     * function since some projections don't deal well with extreme
     * latitudes.
     * 
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     */
    public double normalizeLatitude(double lat) {
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
    public boolean isPlotable(double lat, double lon) {
        return true;
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point2D.
     * 
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param p Resulting XY Point2D
     * @param isRadian bogus argument indicating that lat,lon
     *        arguments are in radians
     * @return Point2D p
     */
    public Point2D forward(double lat, double lon, Point2D p, boolean isRadian) {
        if (isRadian) {
            lat = Math.toDegrees(normalizeLatitude(lat));
            lon = Math.toDegrees(lon);
        } else {
            lat = Math.toDegrees(normalizeLatitude(Math.toRadians(lat)));
        }

        double newLon = Math.toDegrees(wrapLongitude(Math.toRadians(lon - cLon)));

        double x = wx + Math.round(newLon * ppd);
        double y = hy - Math.round((lat - cLat) * ppd);

        if (Debug.debugging("llxydetail")) {
            Debug.output("LLXY.forward(lon:" + ProjMath.radToDeg(lon)
                    + ", lat:" + ProjMath.radToDeg(lat) + " isRadian:"
                    + isRadian + ")");
            Debug.output("LLXY.forward   x:" + x + ", y:" + y + " scale: "
                    + (float) scale);
        }
        
        p.setLocation(x, y);
        return p;
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point2D)
     */
    public Point2D inverse(double x, double y, Point2D llp) {

        // convert from screen to world coordinates, and then
        // basically undo the math from the forward method.

        llp.setLocation(((x - wx) / ppd) + cLon, ((hy - y) / ppd) + cLat);

        return llp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return LLXYName;
    }

    /**
     * Returns the x pixel constant of the projection. This was
     * calcuated when the projection was created. Represents the
     * number of pixels around the earth (360 degrees).
     */
    public double getXPixConstant() {
        return ppd * 360.0;
    }

    /**
     * Returns the y pixel constant of the projection. This was
     * calcuated when the projection was created. Represents the
     * number of pixels from 0 to 90 degrees.
     */
    public double getYPixConstant() {
        return ppd * 90.0;
    }

}