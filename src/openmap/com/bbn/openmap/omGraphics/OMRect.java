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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMRect.java,v $
// $RCSfile: OMRect.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Graphic type that lets you draw four-sided polygons that have corners that
 * share coordinates or window points.
 * <p>
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#poly_restrictions">
 * RESTRICTIONS </a> on Lat/Lon polygons/polylines which apply to rectangles as
 * well. Not following the guidelines listed may result in ambiguous/undefined
 * shapes! Similar assumptions apply to the other vector graphics that we
 * define: circles, ellipses, polys, lines.
 * <p>
 * These assumptions are virtually the same as those on the more generic OMPoly
 * graphic type.
 * <p>
 * 
 * @see OMPoly
 * 
 */
public class OMRect extends OMGraphicAdapter implements OMGraphic {

    /**
     * Horizontal window position of first corner, in pixels from left side of
     * window.
     */
    protected int x1 = 0;
    /**
     * Vertical window position of first corner, in pixels from the top of the
     * window.
     */
    protected int y1 = 0;
    /** Latitude of first corner, decimal degrees. */
    protected double lat1 = 0.0f;
    /** Longitude of first corner, decimal degrees. */
    protected double lon1 = 0.0f;
    /**
     * Horizontal window position of second corner, in pixels from left side of
     * window.
     */
    protected int x2 = 0;
    /**
     * Vertical window position of second corner, in pixels from the top of the
     * window.
     */
    protected int y2 = 0;
    /** Latitude of second corner, decimal degrees. */
    protected double lat2 = 0.0f;
    /** Longitude of second corner, decimal degrees. */
    protected double lon2 = 0.0f;

    /**
     * Number of segments to draw (used only for LINETYPE_GREATCIRCLE or
     * LINETYPE_RHUMB lines).
     */
    protected int nsegs = -1;

    /** Default constructor, waiting to be filled. */
    public OMRect() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Create a lat/lon rectangle.
     * 
     * @param lt1 latitude of north edge, decimal degrees.
     * @param ln1 longitude of west edge, decimal degrees.
     * @param lt2 latitude of south edge, decimal degrees.
     * @param ln2 longitude of east edge, decimal degrees.
     * @param lType line type - see OMGraphic.lineType.
     */
    public OMRect(double lt1, double ln1, double lt2, double ln2, int lType) {
        this(lt1, ln1, lt2, ln2, lType, -1);
    }

    /**
     * Create a lat/lon rectangle.
     * 
     * @param lt1 latitude of north edge, decimal degrees.
     * @param ln1 longitude of west edge, decimal degrees.
     * @param lt2 latitude of south edge, decimal degrees.
     * @param ln2 longitude of east edge, decimal degrees.
     * @param lType line type - see OMGraphic.lineType.
     * @param nsegs number of segment points (only for LINETYPE_GREATCIRCLE or
     *        LINETYPE_RHUMB line types, and if &lt; 1, this value is generated
     *        internally)
     */
    public OMRect(double lt1, double ln1, double lt2, double ln2, int lType, int nsegs) {
        super(RENDERTYPE_LATLON, lType, DECLUTTERTYPE_NONE);
        lat1 = lt1;
        lon1 = ln1;
        lat2 = lt2;
        lon2 = ln2;
        this.nsegs = nsegs;
    }

    /**
     * Construct an XY rectangle. It doesn't matter which corners of the
     * rectangle are used, as long as they are opposite from each other.
     * 
     * @param px1 x pixel position of the first corner relative to the window
     *        origin
     * @param py1 y pixel position of the first corner relative to the window
     *        origin
     * @param px2 x pixel position of the second corner relative to the window
     *        origin
     * @param py2 y pixel position of the second corner relative to the window
     *        origin
     */
    public OMRect(int px1, int py1, int px2, int py2) {
        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;
    }

    /**
     * Construct an XY rectangle relative to a lat/lon point
     * (RENDERTYPE_OFFSET). It doesn't matter which corners of the rectangle are
     * used, as long as they are opposite from each other.
     * 
     * @param lt1 latitude of the reference point, decimal degrees.
     * @param ln1 longitude of the reference point, decimal degrees.
     * @param px1 x pixel position of the first corner relative to the reference
     *        point
     * @param py1 y pixel position of the first corner relative to the reference
     *        point
     * @param px2 x pixel position of the second corner relative to the
     *        reference point
     * @param py2 y pixel position of the second corner relative to the
     *        reference point
     */
    public OMRect(double lt1, double ln1, int px1, int py1, int px2, int py2) {
        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        lat1 = lt1;
        lon1 = ln1;
        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;
    }

    /**
     * Set a lat/lon rectangle.
     * 
     * @param lt1 latitude of north edge, decimal degrees.
     * @param ln1 longitude of west edge, decimal degrees.
     * @param lt2 latitude of south edge, decimal degrees.
     * @param ln2 longitude of east edge, decimal degrees.
     * @param lType line type - see OMGraphic.lineType.
     */
    public void setLocation(double lt1, double ln1, double lt2, double ln2, int lType) {
        setRenderType(RENDERTYPE_LATLON);
        setLineType(lType);
        lat1 = lt1;
        lon1 = ln1;
        lat2 = lt2;
        lon2 = ln2;
        setNeedToRegenerate(true);
    }

    /**
     * Set an XY rectangle. It doesn't matter which corners of the rectangle are
     * used, as long as they are opposite from each other.
     * 
     * @param px1 x pixel position of the first corner relative to the window
     *        origin
     * @param py1 y pixel position of the first corner relative to the window
     *        origin
     * @param px2 x pixel position of the second corner relative to the window
     *        origin
     * @param py2 y pixel position of the second corner relative to the window
     *        origin
     */
    public void setLocation(int px1, int py1, int px2, int py2) {
        setRenderType(RENDERTYPE_XY);
        setLineType(LINETYPE_UNKNOWN);
        x1 = Math.min(px1, px2);
        y1 = Math.min(py1, py2);
        x2 = Math.max(px1, px2);
        y2 = Math.max(py1, py2);
        setNeedToRegenerate(true);
    }

    /**
     * Set an XY rectangle relative to a lat/lon point (RENDERTYPE_OFFSET). It
     * doesn't matter which corners of the rectangle are used, as long as they
     * are opposite from each other.
     * 
     * @param lt1 latitude of the reference point, decimal degrees.
     * @param ln1 longitude of the reference point, decimal degrees.
     * @param px1 x pixel position of the first corner relative to the reference
     *        point
     * @param py1 y pixel position of the first corner relative to the reference
     *        point
     * @param px2 x pixel position of the second corner relative to the
     *        reference point
     * @param py2 y pixel position of the second corner relative to the
     *        reference point
     */
    public void setLocation(double lt1, double ln1, int px1, int py1, int px2, int py2) {
        setRenderType(RENDERTYPE_OFFSET);
        setLineType(LINETYPE_UNKNOWN);
        lat1 = lt1;
        lon1 = ln1;
        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;
        setNeedToRegenerate(true);
    }

    /**
     * Get the latitude of the north edge in a LatLon rectangle. It also happens
     * to be the latitude of the offset point.
     * 
     * @return float latitude
     */
    public double getNorthLat() {
        return lat1;
    }

    /**
     * Get the longitude of the west edge in a LatLon rectangle. It also happens
     * to be the longitude of the offset point.
     * 
     * @return float longitude
     */
    public double getWestLon() {
        return lon1;
    }

    /**
     * Get the latitude of the south edge in a LatLon rectangle.
     * 
     * @return float latitude
     */
    public double getSouthLat() {
        return lat2;
    }

    /**
     * Get the longitude of the east edge in a LatLon rectangle.
     * 
     * @return float longitude
     */
    public double getEastLon() {
        return lon2;
    }

    /**
     * Get the top of XY rectangle.
     * 
     * @return int
     */
    public int getTop() {
        return y1;
    }

    /**
     * Get the left of XY rectangle.
     * 
     * @return int
     */
    public int getLeft() {
        return x1;
    }

    /**
     * Get the bottom of XY rectangle.
     * 
     * @return int
     */
    public int getBottom() {
        return y2;
    }

    /**
     * Get the right of XY rectangle.
     * 
     * @return int
     */
    public int getRight() {
        return x2;
    }

    /**
     * Set the number of segments of the lat/lon lines. (This is only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types, and if &lt; 1, this
     * value is generated internally).
     * 
     * @param nsegs number of segment points
     */
    public void setNumSegs(int nsegs) {
        this.nsegs = nsegs;
    }

    /**
     * Get the number of segments of the lat/lon lines. (This is only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types).
     * 
     * @return int number of segment points
     */
    public int getNumSegs() {
        return nsegs;
    }

    /**
     * Prepare the rectangle for rendering.
     * 
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {

        setNeedToRegenerate(true);
        
        if (proj == null) {
            Debug.message("omgraphic", "OMRect: null projection in generate!");
            return false;
        }

        // reset the internals

        switch (renderType) {
        case RENDERTYPE_XY:
            setShape(createBoxShape((int) Math.min(x2, x1), (int) Math.min(y2, y1), (int) Math.abs(x2
                    - x1), (int) Math.abs(y2 - y1)));
            break;
        case RENDERTYPE_OFFSET:
            if (!proj.isPlotable(lat1, lon1)) {
                setNeedToRegenerate(true);// HMMM not the best flag
                return false;
            }
            Point p1 = (Point) proj.forward(lat1, lon1, new Point());

            setShape(createBoxShape((int) Math.min(p1.x + x1, p1.x + x2), (int) Math.min(p1.y + y1, p1.y
                    + y2), (int) Math.abs(x2 - x1), (int) Math.abs(y2 - y1)));
            break;
        case RENDERTYPE_LATLON:
            ArrayList<float[]> rects;

            if (proj instanceof GeoProj) {
                rects = ((GeoProj) proj).forwardRect(new LatLonPoint.Double(lat1, lon1), // NW
                        new LatLonPoint.Double(lat2, lon2), // SE
                        lineType, nsegs, !isClear(fillPaint));
            } else {
                rects = proj.forwardRect(new Point2D.Double(lon1, lat1), new Point2D.Double(lon2, lat2));
            }
            int size = rects.size();
            GeneralPath projectedShape = null;
            for (int i = 0; i < size; i += 2) {
                GeneralPath gp = createShape(rects.get(i), rects.get(i + 1), true);

                projectedShape = appendShapeEdge(projectedShape, gp, false);
            }
            
            setShape(projectedShape);
            break;
        case RENDERTYPE_UNKNOWN:
            System.err.println("OMRect.generate(): invalid RenderType");
            return false;
        }
        setNeedToRegenerate(false);
        return true;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMRect) {
            OMRect rect = (OMRect) source;
            this.x1 = rect.x1;
            this.y1 = rect.y1;
            this.lat1 = rect.lat1;
            this.lon1 = rect.lon1;
            this.x2 = rect.x2;
            this.y2 = rect.y2;
            this.lat2 = rect.lat2;
            this.lon2 = rect.lon2;
            this.nsegs = rect.nsegs;
        }
    }
}