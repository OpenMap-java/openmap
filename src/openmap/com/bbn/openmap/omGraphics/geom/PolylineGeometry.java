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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/geom/PolylineGeometry.java,v $
// $RCSfile: PolylineGeometry.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.geom;

import java.io.Serializable;

import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.proj.DrawUtil;

/**
 * Graphic object that represents a polygon.
 * <p>
 * All of the OMGraphics are moving to having their internal
 * representation as java.awt.Shape objects. Unfortunately, this has
 * the side effect of slowing OMPolys down, because the way that the
 * projection classes handle transformations cause more objects to be
 * allocated and more loops to be run through. So, by default, the
 * OMPoly does NOT use Shape objects internally, to keep layers that
 * throw down many, many polys running quickly. If you want to do some
 * spatial analysis on an OMPoly, call setDoShapes(true) on it, then
 * generate(Projection), and then call getShapes() to get the
 * java.awt.Shape objects for the poly. You can then run the different
 * Shape spatial analysis methods on the Shape objects.
 * 
 * <h3>NOTES:</h3>
 * <ul>
 * <li>See the <a
 * href="../../../../com.bbn.openmap.proj.Projection.html#poly_restrictions">
 * RESTRICTIONS </a> on Lat/Lon polygons/polylines. Not following the
 * guidelines listed may result in ambiguous/undefined shapes! Similar
 * assumptions apply to the other vector graphics that we define:
 * circles, ellipses, rects, lines.
 * <li>LatLon OMPolys store latlon coordinates internally in radian
 * format for efficiency in projecting. Subclasses should follow this
 * model.
 * <li>Holes in the poly are not supported. If you want holes, use
 * multiple PolyGeometrys in a OMGeometryList.
 * <p>
 * </ul>
 * <h3>TODO:</h3>
 * <ul>
 * <li>Polar filled-polygon correction for Cylindrical projections
 * (like OMCircle).
 * </ul>
 */
public abstract class PolylineGeometry extends PolygonGeometry implements
        Serializable, OMGeometry {

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * This works if generate() has been successful.
     * 
     * @param x horizontal pixel location.
     * @param y vertical pixel location.
     * @return the distance of the object to the location given.
     */
    public float distance(double x, double y) {
        if (getShape() != null) {
            return super.distance(x, y);
        }

        // If shape is null, then we have to do things the old way.

        float temp, distance = Float.POSITIVE_INFINITY;

        if (getNeedToRegenerate()) {
            return distance;
        }

        // safety: grab local reference of projected points
        float[][] xpts = xpoints;
        float[][] ypts = ypoints;
        int len = xpts.length;

        for (int i = 0; i < len; i++) {
            float[] _x = xpts[i];
            float[] _y = ypts[i];

            // get the closest point
            temp = DrawUtil.closestPolyDistance(_x, _y, x, y, false);
            if (temp < distance)
                distance = temp;
        }

        return distance;
    }

    public static class LL extends PolygonGeometry.LL {

        /**
         * Create an LL PolygonGeometry from a list of float lat/lon
         * pairs.
         * <p>
         * NOTES:
         * <ul>
         * <li>llPoints array is converted into radians IN PLACE for
         * more efficient handling internally if it's not already in
         * radians! For even better performance, you should send us an
         * array already in radians format!
         * <li>If you want the poly to be connected (as a polygon),
         * you need to ensure that the first and last coordinate pairs
         * are the same.
         * </ul>
         * 
         * @param llPoints array of lat/lon points, arranged lat, lon,
         *        lat, lon, etc.
         * @param units radians or decimal degrees. Use
         *        OMGraphic.RADIANS or OMGraphic.DECIMAL_DEGREES
         * @param lType line type, from a list defined in OMGraphic.
         */
        public LL(double[] llPoints, int units, int lType) {
            this(llPoints, units, lType, -1);
        }

        /**
         * Create an OMPoly from a list of float lat/lon pairs.
         * <p>
         * NOTES:
         * <ul>
         * <li>llPoints array is converted into radians IN PLACE for
         * more efficient handling internally if it's not already in
         * radians! For even better performance, you should send us an
         * array already in radians format!
         * <li>If you want the poly to be connected (as a polygon),
         * you need to ensure that the first and last coordinate pairs
         * are the same.
         * </ul>
         * 
         * @param llPoints array of lat/lon points, arranged lat, lon,
         *        lat, lon, etc.
         * @param units radians or decimal degrees. Use
         *        OMGraphic.RADIANS or OMGraphic.DECIMAL_DEGREES
         * @param lType line type, from a list defined in OMGraphic.
         * @param nsegs number of segment points (only for
         *        LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types,
         *        and if &lt; 1, this value is generated internally)
         */
        public LL(double[] llPoints, int units, int lType, int nsegs) {
            super(llPoints, units, lType, nsegs);
            setIsPolygon(false);
        }
    }

    public static class XY extends PolygonGeometry.XY {

        /**
         * Create an OMPoly from a list of xy pairs. If you want the
         * poly to be connected, you need to ensure that the first and
         * last coordinate pairs are the same.
         * 
         * @param xypoints array of x/y points, arranged x, y, x, y,
         *        etc.
         */
        public XY(float[] xypoints) {
            super(xypoints);
            setIsPolygon(false);
        }

        /**
         * Create an x/y OMPoly. If you want the poly to be connected,
         * you need to ensure that the first and last coordinate pairs
         * are the same.
         * 
         * @param xPoints float[] of x coordinates
         * @param yPoints float[] of y coordinates
         */
        public XY(float[] xPoints, float[] yPoints) {
            super(xPoints, yPoints);
            setIsPolygon(false);
        }
    }

    public static class Offset extends PolygonGeometry.Offset {

        /**
         * Create an x/y OMPoly at an offset from lat/lon. If you want
         * the poly to be connected, you need to ensure that the first
         * and last coordinate pairs are the same.
         * 
         * @param latPoint latitude in decimal degrees
         * @param lonPoint longitude in decimal degrees
         * @param xypoints float[] of x,y pairs
         * @param cMode offset coordinate mode
         */
        public Offset(double latPoint, double lonPoint, float[] xypoints, int cMode) {
            super(latPoint, lonPoint, xypoints, cMode);
            setIsPolygon(false);
        }

        /**
         * Create an x/y OMPoly at an offset from lat/lon. If you want
         * the poly to be connected, you need to ensure that the first
         * and last coordinate pairs are the same.
         * 
         * @param latPoint latitude in decimal degrees
         * @param lonPoint longitude in decimal degrees
         * @param xPoints float[] of x coordinates
         * @param yPoints float[] of y coordinates
         * @param cMode offset coordinate mode
         */
        public Offset(double latPoint, double lonPoint, float[] xPoints,
                float[] yPoints, int cMode) {

            super(latPoint, lonPoint, xPoints, yPoints, cMode);
            setIsPolygon(false);
        }
    }
}
