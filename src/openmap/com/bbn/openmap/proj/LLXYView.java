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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LLXYView.java,v $
// $RCSfile: LLXYView.java,v $
// $Revision: 1.6 $
// $Date: 2006/02/16 16:22:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the LLXY projection.
 */
public class LLXYView extends LLXY {

    /**
     * The LLXY name.
     */
    public final static transient String LLXYViewName = "EPSG-4326";

    /**
     * The LLXY type of projection.
     */
    public final static transient int LLXYViewType = 6303;

    /** The "Helper" class manages user-space */
    private LLXYViewHelper helper;

    /** User-Space Center in lat/lon */
    protected LatLonPoint uCtr;
    protected float uCtrLat;
    protected float uCtrLon;

    /** Screen Origin in pixels (center=0,0) */
    protected int sCtrX, sCtrY;

    /** User Origin in pixels 0,0 = lat/lon center */
    protected int uCtrX;
    protected int uCtrY;

    protected int dUSX;
    /** delta between U and S, X axis */
    protected int dUSY;

    /** delta between U and S, Y axis */

    /**
     * Construct a LLXY projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public LLXYView(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
        computeParameters();
    }

    /**
     * Return stringified description of this projection.
     * 
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return "LLXYView[" + super.toString();
    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change. For
     * instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.
     * <p>
     */
    protected void computeParameters() {
        Debug.message("LLXYView", "LLXYView.computeParameters()");
        super.computeParameters();

        // compute the offsets
        this.hy = height / 2;
        this.wx = width / 2;
        cLon = ProjMath.radToDeg(centerX);

        // We have no way of constructing the User Space at anything
        // other than 0,0 for now.
        if (uCtr == null) {
            uCtrLat = (float) 0.0;
            uCtrLon = (float) 0.0;
            uCtr = new LatLonPoint.Float(uCtrLat, uCtrLon);
        }

        if (helper == null) {
            helper = new LLXYViewHelper(uCtr, (float)scale, width, height);
        }

        synchronized (helper) {
            super.computeParameters();

            // Screen stuff
            Point temp = new Point();

            helper.setAllParams(pixelsPerMeter,
                    planetRadius,
                    planetPixelRadius,
                    planetPixelCircumference,
                    minscale,
                    maxscale,
                    scale,
                    scaled_radius,
                    width,
                    height,
                    uCtrLat,
                    uCtrLon);

            helper.forward(centerY, centerX, temp, true);
            sCtrX = temp.x;
            sCtrY = temp.y;

            helper.forward(uCtrLat, uCtrLon, temp);
            uCtrX = temp.x;
            uCtrY = temp.y;

            dUSX = sCtrX - uCtrX;
            dUSY = sCtrY - uCtrY;

        }

        Debug.message("LLXYView", "User Center LL: " + uCtrLon + "," + uCtrLat
                + " User Center xy: " + uCtrX + "," + uCtrY
                + " Screen Center LL: " + ProjMath.radToDeg(centerY) + ","
                + ProjMath.radToDeg(centerX) + " Screen Center xy: " + sCtrX
                + "," + sCtrY + " Screen wh: " + width + "x" + height
                + " Screen halfwh: " + this.wx + "x" + this.hy + " Delta xy: "
                + dUSX + "," + dUSY);
    }

    /**
     * Projects a point from Lat/Lon space to X/Y space.
     * 
     * @param pt LatLonPoint
     * @param p Point retval
     * @return Point p
     */
    public Point forward(LatLonPoint pt, Point p) {

        // First translate to user-space
        helper.forward(pt, p);

        // Now translate to screen-space
        p.x = p.x + this.wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        if (Debug.debugging("LLXYViewf")) {
            Debug.output("LLXYView.forward(pt.lon_:" + pt.getLongitude()
                    + ", pt.lat_:" + pt.getLatitude() + ")\n"
                    + "LLXYView.forward   x:" + p.x + ", y:" + p.y);
        }
        return p;
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

        // First translate to user-space
        helper.forward(lat, lon, p);

        // Now translate to screen-space
        p.x = p.x + this.wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        if (Debug.debugging("LLXYViewf")) {
            Debug.output("LLXYView.forward(lon:" + lon + ", lat:" + lat + ")\n"
                    + "LLXYView.forward   x:" + p.x + ", y:" + p.y + " ctrLon:"
                    + cLon + " wx:" + this.wx + " hy:" + this.hy);
        }
        return p;
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point.
     * 
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param p Resulting XY Point
     * @param isRadian bogus argument indicating that lat,lon
     *        arguments are in radians
     * @return Point p
     */
    public Point forward(float lat, float lon, Point p, boolean isRadian) {
        // First translate to user-space
        helper.forward(lat, lon, p, isRadian);

        // Now translate to screen-space
        p.x = p.x + this.wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        if (Debug.debugging("LLXYViewf")) {
            Debug.output("LLXYView.forward(lon:" + ProjMath.radToDeg(lon)
                    + ", lat:" + ProjMath.radToDeg(lat) + " isRadian:"
                    + isRadian + ")\n" + "LLXYView.forward   x:" + p.x + ", y:"
                    + p.y + " scale: " + (float) scale);
        }
        return p;
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
    public Point2D inverse(int x, int y, Point2D llp) {
        int tx = x - this.wx + dUSX;
        int ty = this.hy - y + dUSY;

        if (Debug.debugging("LLXYViewi")) {
            // This is only to aid printing....
            helper.inverse(tx, ty, llp);

            Debug.output("xy: " + x + "," + y + " txty: " + tx + "," + ty
                    + " llp: " + llp.getY() + "," + llp.getX());
        }

        return (helper.inverse(tx, ty, llp));
    }

    /**
     * Helper class manages "user space"
     */
    private class LLXYViewHelper extends LLXY {

        public LLXYViewHelper(LatLonPoint center, float scale, int width,
                int height) {
            super(center, scale, width, height);
        }

        public void setAllParams(int hPixelsPerMeter, float hPlanetRadius,
                                 float hPlanetPixelRadius,
                                 float hPlanetPixelCircumference,
                                 float hMinscale, float hMaxscale,
                                 float hScale, float hScaled_radius,
                                 int hWidth, int hHeight, float hCtrLat,
                                 float hCtrLon) {

            this.pixelsPerMeter = hPixelsPerMeter;
            this.planetRadius = hPlanetRadius;
            this.planetPixelRadius = hPlanetPixelRadius;
            this.planetPixelCircumference = hPlanetPixelCircumference;
            this.minscale = hMinscale;
            this.maxscale = hMaxscale;
            this.scale = hScale;
            this.scaled_radius = hScaled_radius;
            this.width = hWidth;
            this.height = hHeight;
            this.centerY = hCtrLat;
            this.centerX = hCtrLon;
            this.computeParameters();
        }

        public void setAllParams(double hPixelsPerMeter, double hPlanetRadius,
                                 double hPlanetPixelRadius,
                                 double hPlanetPixelCircumference,
                                 double hMinscale, double hMaxscale,
                                 double hScale, double hScaled_radius,
                                 int hWidth, int hHeight, float hCtrLat,
                                 float hCtrLon) {

            this.pixelsPerMeter = hPixelsPerMeter;
            this.planetRadius = hPlanetRadius;
            this.planetPixelRadius = hPlanetPixelRadius;
            this.planetPixelCircumference = hPlanetPixelCircumference;
            this.minscale = hMinscale;
            this.maxscale = hMaxscale;
            this.scale = hScale;
            this.scaled_radius = hScaled_radius;
            this.width = hWidth;
            this.height = hHeight;
            this.centerY = hCtrLat;
            this.centerX = hCtrLon;
            this.computeParameters();
        }
        
        public String toString() {
            return "LLXYViewHelper[" + super.toString();
        }

        /**
         * Forward projects a LatLonPoint into USER space.
         */
        public Point forward(LatLonPoint pt, Point p) {
            super.forward(pt, p);

            if (Debug.debugging("LLXYViewHelperf")) {
                Debug.output("forward l,l,p: " + pt.getLongitude() + ","
                        + pt.getLatitude() + " help xy: " + p.x + "," + p.y);
            }

            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point forward(float lat, float lon, Point p) {
            super.forward(lat, lon, p);

            if (Debug.debugging("LLXYViewHelperf")) {
                Debug.output("forward l,l,p: " + lon + "," + lat + " help xy: "
                        + p.x + "," + p.y);
            }
            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point forward(float lat, float lon, Point p, boolean isRadian) {
            super.forward(lat, lon, p, isRadian);
            if (Debug.debugging("LLXYViewHelperf")) {
                Debug.output("forward l,l,p: " + ProjMath.radToDeg(lon) + ","
                        + ProjMath.radToDeg(lat) + " help xy: " + p.x + ","
                        + p.y);
            }
            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point2D inverse(Point pt, Point2D llp) {
            return this.inverse(pt.x, pt.y, llp);
        }

        public Point2D inverse(int x, int y, Point2D llp) {
            x = x + this.wx;
            y = this.hy - y;

            if (Debug.debugging("LLXYViewHelperi")) {
                Debug.output("inverse helper x,y: " + x + "," + y);
            }

            // inverse project using LLXY.inverse
            return (super.inverse(x, y, llp));
        }
    }
}