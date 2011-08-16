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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Proj.java,v $
// $RCSfile: Proj.java,v $
// $Revision: 1.14 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.bbn.openmap.util.Debug;

/**
 * Proj is the base class of all Projections.
 * <p>
 * You probably don't want to use this class unless you are hacking your own
 * projections, or need extended functionality. To be safe you will want to use
 * the Projection interface.
 * 
 * <h3>Notes:</h3>
 * 
 * <ul>
 * 
 * <li>There are no assumption made as to the units of the values provided to
 * the projection, each projection type has its own interpretation of what the
 * values are.
 * 
 * <li>The order of coordinate parameters changes, depending on the convention
 * of the superclass object of the one being referred to. Coordinate tuples
 * referring to projected coordinates are generally x, y, those referring to
 * unprojected coordinates are y, x (lat/lon).
 * 
 * <li>This class is not thread safe. If two or more threads are using the same
 * Proj, then they could disrupt each other. Occasionally you may need to call a
 * <code>set</code> method of this class. This might interfere with another
 * thread that's using the same projection for <code>forwardPoly</code> or
 * another Projection interface method. In general, you should not need to call
 * any of the <code>set</code> methods directly, but let the MapBean do it for
 * you.
 * 
 * <li>All the various <code>forwardOBJ()</code> methods for ArrayList graphics
 * ultimately go through <code>forwardPoly()</code>.
 * 
 * </ul>
 * 
 * @see Cartesian
 * @see GeoProj
 * 
 */
public abstract class Proj
        implements Projection, Cloneable, Serializable {

    /**
     * Minimum width of projection.
     */
    public final static transient int MIN_WIDTH = 10; // pixels

    /**
     * Minimum height of projection.
     */
    public final static transient int MIN_HEIGHT = 10; // pixels

    protected int width = 640, height = 480;
    protected double minscale = 1.0; // 1:minscale
    protected double maxscale = Double.MAX_VALUE;
    protected double scale = maxscale;
    protected double centerX;
    protected double centerY;
    protected String projID = null; // identifies this projection (if needed)

    /**
     * The unprojected coordinates units of measure.
     */
    protected Length ucuom = null;

    /**
     * Construct a projection.
     * 
     * @param center LatLonPoint center of projection
     * @param s float scale of projection
     * @param w width of screen
     * @param h height of screen
     * @see ProjectionFactory
     */
    public Proj(Point2D center, float s, int w, int h) {
        if (Debug.debugging("proj")) {
            Debug.output("Proj()");
        }

        setParms(center, s, w, h);
        projID = null;

    }

    /**
     * Set the scale of the projection.
     * <p>
     * Sets the projection to the scale 1:s iff minscale &lt; s &lt; maxscale. <br>
     * If s &lt; minscale, sets the projection to minscale. <br>
     * If s &gt; maxscale, sets the projection to maxscale. <br>
     * 
     * @param s float scale
     */
    public void setScale(float s) {
        scale = s;
        if (scale < minscale) {
            scale = minscale;
        } else if (scale > maxscale) {
            scale = maxscale;
        }
        computeParameters();
        projID = null;
    }

    /**
     * Set the minscale of the projection.
     * <p>
     * Usually you will not need to do this.
     * 
     * @param s float minscale
     */
    public void setMinScale(float s) {
        if (s > maxscale)
            return;

        minscale = s;
        if (scale < minscale) {
            scale = minscale;
        }
        computeParameters();
        projID = null;
    }

    /**
     * Set the maximum scale of the projection.
     * <p>
     * Usually you will not need to do this.
     * 
     * @param s float minscale
     */
    public void setMaxScale(float s) {
        if (s < minscale)
            return;

        maxscale = s;
        if (scale > maxscale) {
            scale = maxscale;
        }
        computeParameters();
        projID = null;
    }

    /**
     * Get the scale of the projection.
     * 
     * @return double scale value
     */
    public float getScale() {
        return (float) scale;
    }

    /**
     * Get the maximum scale of the projection.
     * 
     * @return double max scale value
     */
    public float getMaxScale() {
        return (float) maxscale;
    }

    /**
     * Get minimum scale of the projection.
     * 
     * @return double min scale value
     */
    public float getMinScale() {
        return (float) minscale;
    }

    /**
     * Set center point of projection.
     * 
     * @param pt Point2D for center. Point2D values will be copied.
     */
    public void setCenter(Point2D pt) {
        setCenter(pt.getY(), pt.getX());
    }

    /**
     * Set center point of projection.
     * 
     * @param y vertical value of center.
     * @param x horizontal value of center.
     */
    public void setCenter(double y, double x) {
        // Since we need to re-run computeParameters and the projID,
        // we better make a clone of the center, so whoever sets the
        // center can't change it by simply changing the center's
        // parameters without recalculating the parameters.
        centerX = x;
        centerY = y;
        computeParameters();
        projID = null;
    }

    /**
     * Get center point of projection.
     * 
     * @return Point2D center of projection, created just for you.
     */
    public Point2D getCenter() {
        return getCenter(new Point2D.Double());
    }

    /**
     * Returns a center Point2D that was provided, with the location filled into
     * the Point2D object. Calls Point2D.setLocation(x, y).
     */
    public <T extends Point2D> T getCenter(T center) {
        center.setLocation(centerX, centerY);
        return center;
    }

    /**
     * Set projection width.
     * 
     * @param width width of projection screen
     */
    public void setWidth(int width) {
        this.width = width;

        if (this.width < MIN_WIDTH) {
            Debug.message("proj", "Proj.setWidth: width too small!");
            this.width = MIN_WIDTH;
        }
        computeParameters();
        projID = null;
    }

    /**
     * Set projection height.
     * 
     * @param height height of projection screen
     */
    public void setHeight(int height) {
        this.height = height;
        if (this.height < MIN_HEIGHT) {
            Debug.message("proj", "Proj.setHeight: height too small!");
            this.height = MIN_HEIGHT;
        }
        computeParameters();
        projID = null;
    }

    /**
     * Get projection width.
     * 
     * @return width of projection screen
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get projection height.
     * 
     * @return height of projection screen
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets all the projection variables at once before calling
     * computeParameters().
     * 
     * @param center LatLonPoint center
     * @param scale float scale
     * @param width width of screen
     * @param height height of screen
     */
    protected void setParms(Point2D center, float scale, int width, int height) {
        centerX = center.getX();
        centerY = center.getY();
        this.scale = scale;

        this.width = width;
        if (this.width < MIN_WIDTH) {
            Debug.message("proj", "Proj.setParms: width too small!");
            this.width = MIN_WIDTH;
        }
        this.height = height;
        if (this.height < MIN_HEIGHT) {
            Debug.message("proj", "Proj.setParms: height too small!");
            this.height = MIN_HEIGHT;
        }

        init();

        if (this.scale < minscale) {
            this.scale = minscale;
        } else if (this.scale > maxscale) {
            this.scale = maxscale;
        }

        computeParameters();
        projID = null;
    }

    /**
     * Called after the center and scale is set in setParams, but before the
     * scale is checked for legitimacy. This is an opportunity to set constants
     * in subclasses before anything else gets called or checked for validity.
     * This is different than computeParameters() which is called after some
     * checks. This is a good time to pre-calculate constants and set maxscale
     * and minscale.
     * <P>
     * Make sure you call super.init() if you override this method.
     */
    protected void init() {
    }

    /**
     * Sets the projection ID used for determining equality. The projection ID
     * String is interned for efficient comparison.
     */
    protected void setProjectionID() {
        projID = (getClass().getName() + ":" + scale + ":" + centerX + ":" + centerY + ":" + width + ":" + height + ":");
    }

    /**
     * Gets the projection ID used for determining equality.
     * 
     * @return the projection ID, as an intern()ed String
     */
    public String getProjectionID() {
        if (projID == null)
            setProjectionID();
        return projID;
    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change. For instance,
     * they may need to recalculate "constant" parameters used in the forward()
     * and inverse() calls.
     */
    protected abstract void computeParameters();

    /**
     * Stringify the projection.
     * 
     * @return stringified projection
     * @see #getProjectionID
     */
    public String toString() {
        return (" center(" + centerX + ":" + centerY + ") scale=" + scale + " maxscale=" + maxscale + " minscale=" + minscale
                + " width=" + width + " height=" + height + "]");
    }

    /**
     * Test for equality.
     * 
     * @param o Object to compare.
     * @return boolean comparison
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass()) {
            return false;
        }
        return getProjectionID().equals(((Projection) o).getProjectionID());
    }

    /**
     * Return hashcode value of projection.
     * 
     * @return int hashcode
     */
    public int hashCode() {
        return getProjectionID().hashCode();
    }

    /**
     * Clone the projection.
     * 
     * @return Projection clone of this one.
     */
    public Projection makeClone() {
        return (Projection) this.clone();
    }

    /**
     * Copies this projection.
     * 
     * @return a copy of this projection.
     */
    public Object clone() {
        try {
            return (Proj) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Forward project a LatLonPoint.
     * <p>
     * Forward projects a LatLon point into XY space. Returns a Point2D.
     * 
     * @param llp LatLonPoint to be projected
     * @return Point2D (new)
     */
    public Point2D forward(Point2D llp) {
        return forward(llp.getY(), llp.getX(), new Point2D.Float());
    }

    /**
     * Forward projects a LatLonPoint into XY space and return a
     * java.awt.geom.Point2D.
     * 
     * @param llp LatLonPoint to be projected
     * @param pt Resulting XY Point2D
     * @return Point2D pt
     */
    public Point2D forward(Point2D llp, Point2D pt) {
        return forward(llp.getY(), llp.getX(), pt);
    }

    /**
     * Forward project lat,lon coordinates.
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return Point2D (new)
     */
    public Point2D forward(float lat, float lon) {
        return forward((double) lat, (double) lon, new Point2D.Float());
    }

    public Point2D forward(float lat, float lon, Point2D pt) {
        return forward((double) lat, (double) lon, pt);
    }

    public Point2D forward(double lat, double lon) {
        return forward(lat, lon, new Point2D.Double());
    }

    public abstract Point2D forward(double lat, double lon, Point2D pt);

    public <T extends Point2D> T inverse(Point2D point, T llpt) {
        return inverse(point.getX(), point.getY(), llpt);
    }

    /**
     * Inverse project a Point2D from x,y space to coordinate space.
     * 
     * @param point x,y Point2D
     * @return LatLonPoint (new)
     */
    public Point2D inverse(Point2D point) {
        return inverse(point.getX(), point.getY(), new Point2D.Double());
    }

    /**
     * Inverse project x,y coordinates.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @return LatLonPoint (new)
     * @see #inverse(Point2D)
     */
    public Point2D inverse(double x, double y) {
        return inverse(x, y, new Point2D.Double());
    }

    public abstract <T extends Point2D> T inverse(double x, double y, T llpt);

    /**
     * Simple shape projection, doesn't take into account what kind of lines
     * should be drawn between shape points, assumes they should be 2D lines as
     * rendered in 2D space, not interpolated for accuracy as Great Circle/Rhumb
     * lines on a globe..
     */
    public Shape forwardShape(Shape shape) {
        PathIterator pi = shape.getPathIterator(null);
        double[] coords = new double[6];
        Point2D world = new Point2D.Double();
        Point2D world2 = new Point2D.Double();
        Point2D world3 = new Point2D.Double();
        Point screen = new Point();
        Point screen2 = new Point();
        Point screen3 = new Point();

        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);

            world.setLocation(coords[0], coords[1]);
            forward(world, screen);

            if (type == PathIterator.SEG_MOVETO) {
                path.moveTo(screen.x, screen.y);
            } else if (type == PathIterator.SEG_LINETO) {
                path.lineTo(screen.x, screen.y);
            } else if (type == PathIterator.SEG_CLOSE) {
                path.closePath();
            } else {
                world2.setLocation(coords[2], coords[3]);
                forward(world2, screen2);
                if (type == PathIterator.SEG_QUADTO) {
                    path.quadTo(screen.x, screen.y, screen2.x, screen2.y);
                } else if (type == PathIterator.SEG_CUBICTO) {
                    world3.setLocation(coords[4], coords[5]);
                    forward(world3, screen3);
                    path.curveTo(screen.x, screen.y, screen2.x, screen2.y, screen3.x, screen3.y);
                }
            }

            pi.next();
        }

        return path;
    }

    /**
     * Forward project a raw array of points. This assumes nothing about the
     * array of coordinates. In no way does it assume the points are connected
     * or that the composite figure is to be filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * <p>
     * 
     * @param rawllpts array of y, x world coordinates.
     * @param rawoff offset into rawllpts.
     * @param xcoords x projected horizontal map coordinates.
     * @param ycoords y projected vertical map coordinates.
     * @param visible coordinates visible?
     * @param copyoff offset into x,y visible arrays.
     * @param copylen number of coordinates (coordinate arrays should be at
     *        least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     *         visible.
     */
    public boolean forwardRaw(float[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        Point temp = new Point();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            forward(rawllpts[j], rawllpts[j + 1], temp);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;
            visible[i] = true;
        }
        return true;
    }

    /**
     * Forward project a raw array of points. This assumes nothing about the
     * array of coordinates. In no way does it assume the points are connected
     * or that the composite figure is to be filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * <p>
     * 
     * @param rawllpts array of y, x world coordinates.
     * @param rawoff offset into rawllpts.
     * @param xcoords x projected horizontal map coordinates.
     * @param ycoords y projected vertical map coordinates.
     * @param visible coordinates visible?
     * @param copyoff offset into x,y visible arrays.
     * @param copylen number of coordinates (coordinate arrays should be at
     *        least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     *         visible.
     */
    public boolean forwardRaw(double[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        Point temp = new Point();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            forward(rawllpts[j], rawllpts[j + 1], temp);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;
            visible[i] = true;
        }
        return true;
    }

    public ArrayList<float[]> forwardLine(Point2D ll1, Point2D ll2) {
        double[] rawllpts = {
            ll1.getY(),
            ll1.getX(),
            ll2.getY(),
            ll2.getX()
        };
        return forwardPoly(rawllpts, false);
    }

    /**
     * Forward project a rectangle.
     * 
     * @param ll1 LatLonPoint
     * @param ll2 LatLonPoint
     * @return ArrayList<float[]>
     */
    public ArrayList<float[]> forwardRect(Point2D ll1, Point2D ll2) {
        double[] rawllpts = {
            ll1.getY(),
            ll1.getX(),
            ll1.getY(),
            ll2.getX(),
            ll2.getY(),
            ll2.getX(),
            ll2.getY(),
            ll1.getX(),
            // connect:
            ll1.getY(),
            ll1.getX()
        };
        return forwardPoly(rawllpts, true);
    }

    public ArrayList<float[]> forwardPoly(float[] rawllpts, boolean isFilled) {
        // For regular OMGraphics, some of the rawllpts are in radians and must
        // be translated into decimal degrees before they really are able to be
        // displayed here, i.e.:
        // wx = Math.toDegrees(wx);
        // wy = Math.toDegrees(wy);

        int n, k;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<float[]>(0);

        // determine when to stop
        Point temp = new Point(0, 0);
        float[] xs = new float[len];
        float[] ys = new float[len];

        // forward project the first point
        forward(rawllpts[0], rawllpts[1], temp);
        xs[0] = temp.x;
        ys[0] = temp.y;
        // forward project the other points
        for (n = 1, k = 2; n < len; n++, k += 2) {
            forward(rawllpts[k], rawllpts[k + 1], temp);
            xs[n] = temp.x;
            ys[n] = temp.y;
        }

        // now create the return list
        ArrayList<float[]> ret_val = null;
        ret_val = new ArrayList<float[]>(2);
        ret_val.add(xs);
        ret_val.add(ys);

        return ret_val;
    }

    public ArrayList<float[]> forwardPoly(double[] rawllpts, boolean isFilled) {
        int n, k;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<float[]>(0);

        // determine when to stop
        Point temp = new Point(0, 0);
        float[] xs = new float[len];
        float[] ys = new float[len];

        // forward project the first point
        forward(rawllpts[0], rawllpts[1], temp);
        xs[0] = temp.x;
        ys[0] = temp.y;
        // forward project the other points
        for (n = 1, k = 2; n < len; n++, k += 2) {
            forward(rawllpts[k], rawllpts[k + 1], temp);
            xs[n] = temp.x;
            ys[n] = temp.y;
        }

        // now create the return list
        ArrayList<float[]> ret_val = null;
        ret_val = new ArrayList<float[]>(2);
        ret_val.add(xs);
        ret_val.add(ys);
        return ret_val;
    }

    /**
     * Pan the map/projection.
     * <p>
     * Example pans:
     * <ul>
     * <li><code>pan(180, c)</code> pan south `c' degrees
     * <li><code>pan(-90, c)</code> pan west `c' degrees
     * <li><code>pan(0, c)</code> pan north `c' degrees
     * <li><code>pan(90, c)</code> pan east `c' degrees
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees
     */
    abstract public void pan(float Az, float c);

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(180, c)</code> pan south
     * <li><code>pan(-90, c)</code> pan west
     * <li><code>pan(0, c)</code> pan north
     * <li><code>pan(90, c)</code> pan east
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az) {
        pan(Az, 45f);
    }

    /**
     * pan the map northwest.
     */
    final public void panNW() {
        pan(-45f);
    }

    final public void panNW(float c) {
        pan(-45f);
    }

    /**
     * pan the map north.
     */
    final public void panN() {
        pan(0f);
    }

    final public void panN(float c) {
        pan(0f);
    }

    /**
     * pan the map northeast.
     */
    final public void panNE() {
        pan(45f);
    }

    final public void panNE(float c) {
        pan(45f);
    }

    /**
     * pan the map east.
     */
    final public void panE() {
        pan(90f);
    }

    final public void panE(float c) {
        pan(90f);
    }

    /**
     * pan the map southeast.
     */
    final public void panSE() {
        pan(135f);
    }

    final public void panSE(float c) {
        pan(135f);
    }

    /**
     * pan the map south.
     */
    final public void panS() {
        pan(180f);
    }

    final public void panS(float c) {
        pan(180f);
    }

    /**
     * pan the map southwest.
     */
    final public void panSW() {
        pan(-135f);
    }

    final public void panSW(float c) {
        pan(-135f);
    }

    /**
     * pan the map west.
     */
    final public void panW() {
        pan(-90f);
    }

    final public void panW(float c) {
        pan(-90f);
    }

    /**
     */
    public boolean isPlotable(float lat, float lon) {
        return isPlotable((double) lat, (double) lon);
    }

    public boolean isPlotable(Point2D point) {
        return isPlotable(point.getY(), point.getX());
    }

    /**
     * Draw the background for the projection.
     * 
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
     */
    public void drawBackground(Graphics2D g, java.awt.Paint paint) {
        g.setPaint(paint);
        drawBackground(g);
    }

    /**
     * Draw the background for the projection. Assume that the Graphics has been
     * set with the Paint/Color needed, just render the shape of the background.
     * 
     * @param g Graphics
     */
    public void drawBackground(Graphics g) {
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return "Proj";
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.geom.Point2D reflecting a pixel spot on the
     *        projection that matches the ll1 coordinate, the upper left corner
     *        of the area of interest. Note that this is the location where you
     *        want ll1 to go in the new projection scale, not where it is now.
     * @param point2 a java.awt.geom.Point2D reflecting a pixel spot on the
     *        projection that matches the ll2 coordinate, usually the lower
     *        right corner of the area of interest. Note that this is the
     *        location where you want ll2 to go in the new projection, not where
     *        it is now.
     */
    public abstract float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2);

    /**
     * Overridden to ensure that setParameters() are called with the read
     * values.
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        computeParameters();
        projID = null;
    }

    /**
     * Get the unprojected coordinates units of measure.
     * 
     * @return Length. May be null if unknown.
     * @see Length
     */
    public Length getUcuom() {
        return ucuom;
    }

    /**
     * Set the unprojected coordinates units of measure.
     * 
     * @param ucuom
     */
    public void setUcuom(Length ucuom) {
        this.ucuom = ucuom;
    }

    /**
     * Get the world coordinate of the upper left corner of the map.
     */
    public Point2D getUpperLeft() {
        return inverse(0, 0, new Point2D.Double());
    }

    /**
     * Get the world coordinate of the lower right corner of the map.
     */
    public Point2D getLowerRight() {
        return inverse(width, height, new Point2D.Double());
    }
}
