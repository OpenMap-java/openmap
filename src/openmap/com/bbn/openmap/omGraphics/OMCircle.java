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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMCircle.java,v $
// $RCSfile: OMCircle.java,v $
// $Revision: 1.10 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Graphic object that represents a circle or an ellipse.
 * <p>
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#poly_restrictions">
 * RESTRICTIONS </a> on Lat/Lon polygons/polylines which apply to circles as
 * well. Not following the guidelines listed may result in ambiguous/undefined
 * shapes! Similar assumptions apply to the other vector graphics that we
 * define: polys, rects, lines.
 * <p>
 * We currently do not allow LatLon ellipses, only XY.
 * <p>
 * These assumptions are virtually the same as those on the more generic OMPoly
 * graphic type.
 * <p>
 * 
 * @see OMPoly
 */
public class OMCircle extends OMArc implements Serializable {

    /**
     * The simplest constructor for an OMCircle, and it expects that all fields
     * will be filled in later. Rendertype is RENDERTYPE_UNKNOWN.
     */
    public OMCircle() {
        super();
    }

    /**
     * Create a OMCircle, positioned with a lat-lon center and x-y axis.
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     */
    public OMCircle(double latPoint, double lonPoint, int w, int h) {
        this(latPoint, lonPoint, 0, 0, w, h);
    }

    /**
     * Create a OMCircle, positioned with a x-y center with x-y axis. Rendertype
     * is RENDERTYPE_XY.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     */
    public OMCircle(int x1, int y1, int w, int h) {
        super(x1, y1, w, h, 0f, 360f);
    }

    /**
     * Create a OMCircle, positioned at a Lat-lon location, x-y offset, x-y
     * axis. Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center of circle/ellipse.
     * @param lonPoint longitude of center of circle/ellipse.
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of circle/ellipse, pixels.
     * @param h vertical diameter of circle/ellipse, pixels.
     */
    public OMCircle(double latPoint, double lonPoint, int offset_x1,
            int offset_y1, int w, int h) {
        super(latPoint, lonPoint, offset_x1, offset_y1, w, h, 0f, 360f);
    }

    /**
     * Creates an OMCircle with a Lat-lon center and a lat-lon axis. Rendertype
     * is RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees (converted to radians
     *        internally).
     */
    public OMCircle(double latPoint, double lonPoint, double radius) {
        this(latPoint, lonPoint, radius, Length.DECIMAL_DEGREE, -1);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object.
     */
    public OMCircle(double latPoint, double lonPoint, double radius,
            Length units) {
        this(latPoint, lonPoint, radius, units, -1);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units.
     * @param nverts number of vertices for the poly-circle (if &lt; 3, value is
     *        generated internally)
     */
    public OMCircle(double latPoint, double lonPoint, double radius,
            Length units, int nverts) {
        this(new LatLonPoint.Double(latPoint, lonPoint), radius, units, nverts);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param center LatLon center of circle
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units for
     *        distance.
     * @param nverts number of vertices for the poly-circle(if &lt; 3, value is
     *        generated internally)
     */
    public OMCircle(LatLonPoint center, double radius, Length units, int nverts) {
        super(center, radius, units, nverts, 0f, 360f);
    }

    /**
     * Helper function that helps the generate method figure out if the center
     * point should be in the generate shape - if it's not, the code knows that
     * there is a problem with the poles, and the polar correction code needs to
     * be run.
     */
    protected boolean shouldCenterBeInShape() {
        return true;
    }

    /**
     * An internal method designed to fetch the Shape to be used for an XY or
     * OFFSET OMCircles. This method is smart enough to take the calculated
     * position information and make a call to Ellipse2D.Float.
     */
    protected Shape createArcShape(float x, float y, float fwidth, float fheight) {
        return new Ellipse2D.Float(x, y, fwidth, fheight);
    }

    /**
     * An internal method designed to fetch the ArrayList for LATLON OMCircles.
     * This method is smart enough to take the calculated position information
     * and make a call to Projection.forwardCircle.
     */
    protected ArrayList<float[]> getCoordLists(GeoProj proj, LatLonPoint center,
                                             float radius, int nverts) {
        return proj.forwardCircle(center, /* radians */
        true, radius, nverts, !isClear(fillPaint));
    }

    public boolean hasLineTypeChoice() {
        return false;
    }
}