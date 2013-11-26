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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/geom/PolygonGeometry.java,v $
// $RCSfile: PolygonGeometry.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.geom;

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import java.util.ArrayList;

import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

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
public abstract class PolygonGeometry extends BasicGeometry implements
        Serializable, OMGeometry {

    /** Internal array of projected x coordinate arrays. */
    protected float[][] xpoints = new float[0][0];

    /** Internal array of projected y coordinate arrays. */
    protected float[][] ypoints = new float[0][0];

    /**
     * Whether it is a polygon, as opposed to a polyline. Should be a
     * polygon, since that is what is being created. The
     * PolylineGeometry subclasses set this to false.
     */
    protected boolean isPolygon = true;

    /**
     * Flag for telling the PolygonGeometry to use the Shape objects
     * to represent itself internally. See intro for more info.
     */
    protected boolean doShapes = true;

    protected PolygonGeometry() {}

    public void setDoShapes(boolean set) {
        doShapes = set;
    }

    public boolean getDoShapes() {
        return doShapes;
    }

    protected void setIsPolygon(boolean set) {
        isPolygon = set;
    }

    public boolean getIsPolygon() {
        return isPolygon;
    }

    /**
     * Since OMPoly has the option to not create a Shape, this method
     * is here to create it if it is asked for. The OMPoly needs to be
     * generated.
     */
    protected abstract GeneralPath createShape();

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
        float[] _x, _y;
        int len = xpts.length;

        for (int i = 0; i < len; i++) {
            _x = xpts[i];
            _y = ypts[i];

            // check if point inside polygon
            if (DrawUtil.inside_polygon(_x, _y, x, y))
                return 0f; // close as can be

            // get the closest point
            temp = DrawUtil.closestPolyDistance(_x, _y, x, y, false);
            if (temp < distance)
                distance = temp;
        }

        return distance;
    }

    /**
     * Get the array of java.awt.Shape objects that represent the
     * projected graphic. The array will contain more than one Shape
     * object of the object wraps around the earth and needs to show
     * up in more than one place on the map.
     * <p>
     * 
     * The java.awt.Shape object gives you the ability to do a little
     * spatial analysis on the graphics.
     * 
     * @return java.awt.Shape[], or null if the graphic needs to be
     *         generated with the current map projection, or null if
     *         the OMGeometry hasn't been updated to use Shape objects
     *         for its internal representation.
     */
    public synchronized GeneralPath getShape() {
        GeneralPath shape = super.getShape();
        if (shape == null && !getNeedToRegenerate() && !doShapes) {
            // Since polygons have the option of not creating shape
            // objects, should create one if asked.
            shape = createShape();
            setShape(shape);
        }
        return shape;
    }

    public static class LL extends PolygonGeometry {

        /** raw float lats and lons stored internally in radians. */
        protected double[] rawllpts = null;

        /**
         * Number of segments to draw (used only for
         * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB lines).
         */
        protected int nsegs = -1;

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
            setLineType(lType);
            setLocation(llPoints, units);
            setNumSegs(nsegs);
        }

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
         * Set an OMPoly from a list of float lat/lon pairs.
         * <p>
         * NOTES:
         * <ul>
         * <li>llPoints array is converted into radians IN PLACE for
         * more efficient handling internally if it's not already in
         * radians! If you don't want the array to be changed, send in
         * a copy.
         * <li>If you want the poly to be connected (as a polygon),
         * you need to ensure that the first and last coordinate pairs
         * are the same.
         * </ul>
         * This is for RENDERTYPE_LATLON polys.
         * 
         * @param llPoints array of lat/lon points, arranged lat, lon,
         *        lat, lon, etc.
         * @param units radians or decimal degrees. Use
         *        OMGraphic.RADIANS or OMGraphic.DECIMAL_DEGREES
         */
        public void setLocation(double[] llPoints, int units) {
            if (units == OMGraphic.DECIMAL_DEGREES) {
                ProjMath.arrayDegToRad(llPoints);
            }
            rawllpts = llPoints;
            setNeedToRegenerate(true);
        }

        /**
         * Return the rawllpts array. NOTE: this is an unsafe method
         * to access the rawllpts array. Use with caution. These are
         * RADIANS!
         * 
         * @return float[] rawllpts of lat, lon, lat, lon
         */
        public double[] getLatLonArray() {
            return rawllpts;
        }

        /**
         * Set the number of subsegments for each segment in the poly.
         * (This is only for LINETYPE_GREATCIRCLE or LINETYPE_RHUMB
         * line types, and if &lt; 1, this value is generated
         * internally).
         * 
         * @param nsegs number of segment points
         */
        public void setNumSegs(int nsegs) {
            this.nsegs = nsegs;
        }

        /**
         * Get the number of subsegments for each segment in the poly.
         * (This is only for LINETYPE_GREATCIRCLE or LINETYPE_RHUMB
         * line types).
         * 
         * @return int number of segment points
         */
        public int getNumSegs() {
            return nsegs;
        }

        public boolean generate(Projection proj) {

            setNeedToRegenerate(true);

            if (proj == null) {
                Debug.message("omgraphic",
                        "OMPoly: null projection in generate!");
                return false;
            }

            // polygon/polyline project the polygon/polyline.
            // Vertices should already be in radians.
            ArrayList vector;
            if (proj instanceof GeoProj) {
                vector = ((GeoProj) proj).forwardPoly(rawllpts,
                        lineType,
                        nsegs,
                        isPolygon);
            } else {
                vector = proj.forwardPoly(rawllpts, isPolygon);
            }
            int size = vector.size();

            if (!doShapes) {
                xpoints = new float[(int) (size / 2)][0];
                ypoints = new float[xpoints.length][0];
            }

            // We could call create shape, but this is more efficient.
            GeneralPath projectedShape = null;
            for (int i = 0, j = 0; i < size; i += 2, j++) {
                if (doShapes) {
                    GeneralPath gp = BasicGeometry.createShape((float[]) vector.get(i),
                            (float[]) vector.get(i + 1),
                            isPolygon);
                    
                    projectedShape = appendShapeEdge(projectedShape, gp, false);
                    
                } else {
                    xpoints[j] = (float[]) vector.get(i);
                    ypoints[j] = (float[]) vector.get(i + 1);
                }
            }
            
            setShape(projectedShape);

            setNeedToRegenerate(false);
            return true;
        }

        protected GeneralPath createShape() {

            if (getNeedToRegenerate()) {
                return null;
            }

            int size = xpoints.length;
            GeneralPath projectedShape = null;
            for (int i = 0; i < size; i++) {
                GeneralPath gp = BasicGeometry.createShape(xpoints[i],
                        ypoints[i],
                        isPolygon);

                projectedShape = appendShapeEdge(projectedShape, gp, false);
            }
            
            return projectedShape;
        }

        public int getRenderType() {
            return RENDERTYPE_LATLON;
        }
    }

    public static class XY extends PolygonGeometry {

        /** The array of x pixel coordinates. */
        protected float[] xs = null;

        /** The array of y pixel coordinates. */
        protected float[] ys = null;

        /**
         * To satify the Offset constructor complaint.
         */
        protected XY() {}

        /**
         * Create an OMPoly from a list of xy pairs. If you want the
         * poly to be connected, you need to ensure that the first and
         * last coordinate pairs are the same.
         * 
         * @param xypoints array of x/y points, arranged x, y, x, y,
         *        etc.
         */
        public XY(float[] xypoints) {
            setLocation(xypoints);
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
            setLocation(xPoints, yPoints);
        }

        /**
         * Set an OMPoly from a list of xy pixel pairs. If you want
         * the poly to be connected, you need to ensure that the first
         * and last coordinate pairs are the same. This is for
         * RENDERTYPE_XY polys.
         * 
         * @param xypoints array of x/y points, arranged x, y, x, y,
         *        etc.
         */
        public void setLocation(float[] xypoints) {
            int end = xypoints.length >> 1;
            xs = new float[end];
            ys = new float[end];
            for (int i = 0, j = 0; i < end; i++, j += 2) {
                xs[i] = xypoints[j];
                ys[i] = xypoints[j + 1];
            }
            setNeedToRegenerate(true);
        }

        /**
         * Set an OMPoly from a x/y coordinates. If you want the poly
         * to be connected, you need to ensure that the first and last
         * coordinate pairs are the same. This is for RENDERTYPE_XY
         * polys.
         * 
         * @param xPoints float[] of x coordinates
         * @param yPoints float[] of y coordinates
         */
        public void setLocation(float[] xPoints, float[] yPoints) {
            xs = xPoints;
            ys = yPoints;
            setNeedToRegenerate(true);
        }

        /**
         * Set the array of x points.
         */
        public void setXs(float[] x) {
            xs = x;
            setNeedToRegenerate(true);
        }

        /**
         * Get the array of x points.
         */
        public float[] getXs() {
            return xs;
        }

        /**
         * Set the array of y points.
         */
        public void setYs(float[] y) {
            ys = y;
            setNeedToRegenerate(true);
        }

        /**
         * Get the array of y points.
         */
        public float[] getYs() {
            return ys;
        }

        public boolean generate(Projection proj) {

            if (proj == null) {
                Debug.message("omgraphic",
                        "OMPoly: null projection in generate!");
                setNeedToRegenerate(true);
                return false;
            }

            if (xs == null) {
                Debug.message("omgraphic",
                        "OMPoly x/y rendertype null coordinates");
                return false;
            }

            // Need to keep these around for the LabeledOMPoly
            xpoints = new float[1][0];
            xpoints[0] = xs;
            ypoints = new float[1][0];
            ypoints[0] = ys;

            if (doShapes) {
                setShape(createShape());
            } else {
                setShape(null);
            }
            setNeedToRegenerate(false);
            return true;
        }

        protected GeneralPath createShape() {

            if (getNeedToRegenerate()) {
                return null;
            }

            return BasicGeometry.createShape(xpoints[0], ypoints[0], isPolygon);
        }

        public int getRenderType() {
            return RENDERTYPE_XY;
        }
    }

    public static class Offset extends XY {

        /**
         * Translation offsets. The xy points are relative to the
         * position of fixed latlon point.
         */
        public final static int COORDMODE_ORIGIN = 0;

        /**
         * Delta offsets. Each xy point in the array is relative to
         * the previous point, and the first point is relative to the
         * fixed latlon point.
         */
        public final static int COORDMODE_PREVIOUS = 1;

        /**
         * The latitude of the starting point of the poly. Stored as
         * radians!
         */
        protected double lat = 0.0f;

        /**
         * The longitude of the starting point of the poly. Stored as
         * radians!
         */
        protected double lon = 0.0f;

        /**
         * Type of offset.
         * 
         * @see #COORDMODE_ORIGIN
         * @see #COORDMODE_PREVIOUS
         */
        protected int coordMode = COORDMODE_ORIGIN;

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
            setLocation(latPoint, lonPoint, OMGraphic.DECIMAL_DEGREES, xypoints);
            setCoordMode(cMode);
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

            setLocation(latPoint,
                    lonPoint,
                    OMGraphic.DECIMAL_DEGREES,
                    xPoints,
                    yPoints);
            setCoordMode(cMode);
        }

        /**
         * Set the location based on a latitude, longitude, and some
         * xy points. The coordinate mode and the polygon setting are
         * the same as in the constructor used.
         * 
         * @param latPoint latitude in decimal degrees.
         * @param lonPoint longitude in decimal degrees.
         * @param units radians or decimal degrees. Use
         *        OMGraphic.RADIANS or OMGraphic.DECIMAL_DEGREES
         * @param xypoints array of x/y points, arranged x, y, x, y,
         *        etc.
         */
        public void setLocation(double latPoint, double lonPoint, int units,
                                float[] xypoints) {
            if (units == OMGraphic.DECIMAL_DEGREES) {
                lat = ProjMath.degToRad(latPoint);
                lon = ProjMath.degToRad(lonPoint);
            } else {
                lat = latPoint;
                lon = lonPoint;
            }
            int end = xypoints.length >> 1;
            xs = new float[end];
            ys = new float[end];
            for (int i = 0, j = 0; i < end; i++, j += 2) {
                xs[i] = xypoints[j];
                ys[i] = xypoints[j + 1];
            }
            setNeedToRegenerate(true);
        }

        /**
         * Set the location based on a latitude, longitude, and some
         * xy points. The coordinate mode and the polygon setting are
         * the same as in the constructor used.
         * 
         * @param latPoint latitude in decimal degrees
         * @param lonPoint longitude in decimal degrees
         * @param units radians or decimal degrees. Use
         *        OMGraphic.RADIANS or OMGraphic.DECIMAL_DEGREES
         * @param xPoints float[] of x coordinates
         * @param yPoints float[] of y coordinates
         */
        public void setLocation(double latPoint, double lonPoint, int units,
                                float[] xPoints, float[] yPoints) {
            if (units == OMGraphic.DECIMAL_DEGREES) {
                lat = ProjMath.degToRad(latPoint);
                lon = ProjMath.degToRad(lonPoint);
            } else {
                lat = latPoint;
                lon = lonPoint;
            }
            xs = xPoints;
            ys = yPoints;
            setNeedToRegenerate(true);
        }

        /**
         * Type of offset.
         * 
         * @see #COORDMODE_ORIGIN
         * @see #COORDMODE_PREVIOUS
         */
        public void setCoordMode(int coordMode) {
            this.coordMode = coordMode;
        }

        /**
         * Type of offset.
         * 
         * @see #COORDMODE_ORIGIN
         * @see #COORDMODE_PREVIOUS
         */
        public int getCoordMode() {
            return coordMode;
        }

        /**
         * Set the latitude of the offset point, in decimal degrees.
         */
        public void setLat(double lat) {
            this.lat = ProjMath.degToRad(lat);
            setNeedToRegenerate(true);
        }

        /**
         * Get the latitude of the offset point, in decimal degrees.
         */
        public double getLat() {
            return ProjMath.radToDeg(lat);
        }

        /**
         * Set the longitude of the offset point, in decimal degrees.
         */
        public void setLon(double lon) {
            this.lon = ProjMath.degToRad(lon);
            setNeedToRegenerate(true);
        }

        /**
         * Get the longitude of the offset point, in decimal degrees.
         */
        public double getLon() {
            return ProjMath.radToDeg(lon);
        }

        public boolean generate(Projection proj) {

            if (proj == null) {
                Debug.message("omgraphic",
                        "OMPoly: null projection in generate!");
                setNeedToRegenerate(true);
                return false;
            }

            if (xs == null) {
                Debug.message("omgraphic",
                        "OMPoly offset rendertype null coordinates");
                return false;
            }

            int npts = xs.length;
            float[] _x = new float[npts];
            float[] _y = new float[npts];

            // forward project the radian point
            Point origin = new Point();
            if (proj instanceof GeoProj) {
                ((GeoProj)proj).forward(lat, lon, origin, true);//radians
            } else {
                proj.forward(Math.toDegrees(lat), Math.toDegrees(lon), origin);
            }

            if (coordMode == COORDMODE_ORIGIN) {
                for (int i = 0; i < npts; i++) {
                    _x[i] = xs[i] + origin.x;
                    _y[i] = ys[i] + origin.y;
                }
            } else { // CModePrevious offset deltas
                _x[0] = xs[0] + origin.x;
                _y[0] = ys[0] + origin.y;

                for (int i = 1; i < npts; i++) {
                    _x[i] = xs[i] + _x[i - 1];
                    _y[i] = ys[i] + _y[i - 1];
                }
            }
            // Need to keep these around for the LabeledOMPoly
            xpoints = new float[1][0];
            xpoints[0] = _x;
            ypoints = new float[1][0];
            ypoints[0] = _y;

            setShape(doShapes? this.createShape() : null);

            setNeedToRegenerate(false);
            return true;
        }

        public int getRenderType() {
            return RENDERTYPE_OFFSET;
        }
    }
}
