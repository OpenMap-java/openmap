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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/MercatorView.java,v $
// $RCSfile: MercatorView.java,v $
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
 * Implements the MercatorView projection.
 */
public class MercatorView extends Mercator {

    /**
     * The MercatorView name.
     */
    public final static transient String MercatorViewName = "MercatorView";

    /**
     * The MercatorView type of projection.
     */
    public final static transient int MercatorViewType = 22;

    private MercatorViewHelper helper;

    // User = the space into which the projection is made
    // The relationship between this and the real screen is held
    // by some offsets etc.
    // Screen = the space that we're looking at.

    protected LatLonPoint uCtr;
    protected float uCtrLat; // User Center in lat/lon
    protected float uCtrLon;

    protected int sCtrX; // Screen Origin in pixels (center=0,0)
    protected int sCtrY; // 

    protected int uCtrX; // User Origin in pixels
    protected int uCtrY; // 0,0 = lat/lon center

    protected int dUSX; // delta between U and S, X axis
    protected int dUSY; // delta between U and S, Y axis

    /**
     * Construct a MercatorView projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public MercatorView(LatLonPoint center, float scale, int width, int height) {
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
        return "MercatorView[" + super.toString();
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
        Debug.message("mercatorview", "MercatorView.computeParameters()");

        // We have no way of constructing the User Space at anything
        // other than 0,0 for now.
        if (uCtr == null) {
            uCtrLat = (float) 0.0;
            uCtrLon = (float) 0.0;
            uCtr = new LatLonPoint.Float(uCtrLat, uCtrLon);
        }

        if (helper == null) {
            helper = new MercatorViewHelper(uCtr, (float) scale, width, height);
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

        Debug.message("mercatorview", "User Center LL: " + uCtrLon + ","
                + uCtrLat + " User Center xy: " + uCtrX + "," + uCtrY
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
        helper.forward(pt, p);

        Debug.message("mercatorview-f", "forward llp,p: " + pt.getLongitude()
                + "," + pt.getLatitude() + " merc xy: " + p.x + "," + p.y);

        p.x = p.x + wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        Debug.message("mercatorview-f", "forward llp,p: " + pt.getLongitude()
                + "," + pt.getLatitude() + " view xy: " + p.x + "," + p.y);
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
        helper.forward(lat, lon, p);

        Debug.message("mercatorview-f", "forward l,l,p: " + lon + "," + lat
                + " merc xy: " + p.x + "," + p.y);

        p.x = p.x + wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        Debug.message("mercatorview-f", "forward l,l,p: " + lon + "," + lat
                + " view xy: " + p.x + "," + p.y);
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
        helper.forward(lat, lon, p, isRadian);

        Debug.message("mercatorview-f", "forward l,l,p,i: "
                + ProjMath.radToDeg(lon) + "," + ProjMath.radToDeg(lat)
                + " merc xy: " + p.x + "," + p.y);

        p.x = p.x + wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        Debug.message("mercatorview-f", "forward l,l,p,i: "
                + ProjMath.radToDeg(lon) + "," + ProjMath.radToDeg(lat)
                + " view xy: " + p.x + "," + p.y);
        return p;
    }

    /**
     * Inverse project a Point.
     * 
     * @param pt x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     */
    public Point2D inverse(Point pt, Point2D llp) {
        // convert from screen to user coordinates
        int x = pt.x - wx + dUSX;
        int y = this.hy - pt.y + dUSY;

        Debug.message("mercatorview-i", "pt: " + pt.x + "," + pt.y + "xy: " + x
                + "," + y);

        return (helper.inverse(x, y, llp));
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
        // convert from screen to world coordinates
        int tx = x - wx + dUSX;
        int ty = this.hy - y + dUSY;

        // This is only to aid printing....
        Point2D tllp = helper.inverse(tx, ty, llp);

        Debug.message("mercatorview-i", "xy: " + x + "," + y + " txty: " + tx
                + "," + ty + " llp: " + tllp.getY() + ","
                + tllp.getX());

        return (helper.inverse(tx, ty, llp));
    }

    /**
     * MercatorViewHelper
     * 
     * This will hold the "USER" space version of the Mercator The
     * forward and inverse methods are extended here to undo the
     * screen coordinate conversions (i.e this helper deals in user
     * space while its "super" deals in what it thinks is screen
     * space.
     * 
     */

    private class MercatorViewHelper extends Mercator {

        /**
         * MercatorViewHelper Constructor
         * 
         * @param center the center of the projection into "user
         *        space"
         * @param scale the scale of the projection
         * @param width the width of the projection into user space
         * @param height the height of the projection into user space
         */

        public MercatorViewHelper(LatLonPoint center, float scale, int width,
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
            return "MercatorViewHelper[" + super.toString();
        }

        /** 
         */
        public Point forward(LatLonPoint pt, Point p) {
            super.forward(pt, p);

            Debug.message("mercatorview-f", "forward l,l,p: "
                    + pt.getLongitude() + "," + pt.getLatitude() + " help xy: "
                    + p.x + "," + p.y);

            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point forward(float lat, float lon, Point p) {
            super.forward(lat, lon, p);

            Debug.message("mercatorview-f", "forward l,l,p: " + lon + "," + lat
                    + " help xy: " + p.x + "," + p.y);
            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point forward(float lat, float lon, Point p, boolean isRadian) {
            super.forward(lat, lon, p, isRadian);
            Debug.message("mercatorview-f", "forward l,l,p: "
                    + ProjMath.radToDeg(lon) + "," + ProjMath.radToDeg(lat)
                    + " help xy: " + p.x + "," + p.y);
            p.x = p.x - this.wx;
            p.y = this.hy - p.y;
            return p;
        }

        public Point2D inverse(int x, int y, Point2D llp) {
            x = x + this.wx;
            y = this.hy - y;
            return (super.inverse(x, y, llp));
        }
    }
}
