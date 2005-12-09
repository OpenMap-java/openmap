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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/OrthographicView.java,v $
// $RCSfile: OrthographicView.java,v $
// $Revision: 1.7 $
// $Date: 2005/12/09 21:09:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the OrthographicView projection.
 */
public class OrthographicView extends Orthographic {

    /**
     * The OrthographicView name.
     */
    public final static transient String OrthographicViewName = "OrthographicView";

    /**
     * The OrthographicView type of projection.
     */
    public final static transient int OrthographicViewType = 77;

    private OrthographicViewHelper helper;

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
     * Construct an OrthographicView projection.
     * <p>
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * 
     */
    public OrthographicView(LatLonPoint center, float scale, int width,
            int height) {
        super(center, scale, width, height);
        setMinScale(1000.0f);
        computeParameters();
    }

    /**
     * Return stringified description of this projection.
     * <p>
     * 
     * @return String
     * @see Projection#getProjectionID
     * 
     */
    public String toString() {
        return "OrthographicView[" + super.toString();
    }

    /**
     * Set center point of projection.
     * <p>
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * 
     */
    public void setCenter(float lat, float lon) {
        centerY = normalize_latitude(ProjMath.degToRad(lat));
        centerX = wrap_longitude(ProjMath.degToRad(lon));
        computeParameters();
        projID = null;
    }

    /**
     * Set center point of projection.
     * <p>
     * 
     * @param pt LatLonPoint
     */
    public void setCenter(LatLonPoint pt) {
        setCenter(pt.getLatitude(), pt.getLongitude());
    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change. For
     * instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.
     * <p>
     * 
     */
    protected void computeParameters() {
        Debug.message("orthographicview",
                "OrthographicView.computeParameters()");
        // We have no way of constructing the User Space at anything
        // other than 0,0 for now.
        if (uCtr == null) {
            uCtrLat = (float) 0.0;
            uCtrLon = (float) 0.0;
            uCtr = new LatLonPoint.Float(uCtrLat, uCtrLon);
        }

        if (helper == null) {
            helper = new OrthographicViewHelper(uCtr, (float) scale, width, height);
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

            // compute the offsets
            this.hy = height / 2;
            this.wx = width / 2;
            helper.forward(centerY, centerX, temp, true);
            sCtrX = temp.x;
            sCtrY = temp.y;

            helper.forward(uCtrLat, uCtrLon, temp);
            uCtrX = temp.x;
            uCtrY = temp.y;

            dUSX = sCtrX - uCtrX;
            dUSY = sCtrY - uCtrY;

        }
        Debug.message("orthographicview", "User Center LL: " + uCtrLon + ","
                + uCtrLat + " User Center xy: " + uCtrX + "," + uCtrY
                + " Screen Center LL: " + ProjMath.radToDeg(centerY) + ","
                + ProjMath.radToDeg(centerX) + " Screen Center xy: " + sCtrX
                + "," + sCtrY + " Screen wh: " + width + "x" + height
                + " Screen halfwh: " + this.wx + "x" + this.hy + " Delta xy: "
                + dUSX + "," + dUSY);
    }

    /**
     * Forward project a point. If the point is not within the
     * viewable hemisphere, return flags in AzimuthVar variable if
     * specified.
     * 
     * @param phi float latitude in radians
     * @param lambda float longitude in radians
     * @param p Point
     * @param azVar AzimuthVar or null
     * @return Point pt
     */
    protected Point _forward(float phi, float lambda, Point p, AzimuthVar azVar) {
        helper._forward(phi, lambda, p, azVar);

        Debug.message("orthographicview-f", "forward p,p,p,a: "
                + ProjMath.radToDeg(lambda) + "," + ProjMath.radToDeg(phi)
                + " orth xy: " + p.x + "," + p.y);

        p.x = p.x + this.wx - dUSX;
        p.y = this.hy - p.y + dUSY;

        Debug.message("orthographicview-f", "forward p,p,p,a: "
                + ProjMath.radToDeg(lambda) + "," + ProjMath.radToDeg(phi)
                + " view xy: " + p.x + "," + p.y);
        return p;
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     * 
     */
    public Point2D inverse(int x, int y, Point2D llp) {
        // convert from screen to world coordinates
        int tx = x - this.wx + dUSX;
        int ty = this.hy - y + dUSY;

        // This is only to aid printing....
        helper.inverse(tx, ty, llp);

        Debug.message("mercatorview-i", "xy: " + x + "," + y + " txty: " + tx
                + "," + ty + " llp: " + llp.getY() + "," + llp.getX());

        return (helper.inverse(tx, ty, llp));
    }

    public Point2D getUpperLeft() {
        return (helper.getUpperLeft());
    }

    public Point2D getLowerRight() {
        return (helper.getLowerRight());
    }

    // ////////////////////////////////////////////////////////////////////
    //
    //
    //
    // HELPER!
    //
    // This will hold the "USER" space version of the Mercator
    // The forward and inverse methods are extended here to undo
    // the screen coordinate conversions (i.e this helper deals
    // in user space while its "super" deals in what it thinks is
    // screen
    // space.
    //
    // ////////////////////////////////////////////////////////////////////

    private class OrthographicViewHelper extends Orthographic {

        public OrthographicViewHelper(LatLonPoint center, float scale,
                int width, int height) {
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

        public void setAllParams(int hPixelsPerMeter, double hPlanetRadius,
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
            return "OrthographicViewHelper[" + super.toString();
        }

        public Point _forward(float phi, float lambda, Point p, AzimuthVar azVar) {
            super._forward(phi, lambda, p, azVar);
            Debug.message("ortrhographicview-f", "forward p,l,l,a: "
                    + ProjMath.radToDeg(lambda) + "," + ProjMath.radToDeg(phi)
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