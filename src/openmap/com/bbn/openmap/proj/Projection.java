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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Projection.java,v $
// $RCSfile: Projection.java,v $
// $Revision: 1.9 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Projection interface to the OpenMap projection library.
 * <p>
 * This is a READONLY interface to the projection object. See the
 * <code>Proj</code> class for read/write access.
 * <p>
 * A projection is an object that is maintained by the map, and represents a
 * abstract "view" of the data. The projection has the properties of x-width, *
 * y-height, scale (in pixels/meters), and a x/y center point that is in
 * different units, depending on the projection implementation.
 * <p>
 * 
 * <h4>Projection Notes/Assumptions:</h4>
 * <ul>
 * <li>At the center point of the projection, North is to the top of the screen.
 * <br>
 * <li><a href="#poly_restrictions">LatLon Polygon Restrictions. </a> <br>
 * <li><a href="#line_restrictions">LatLon Line Restrictions. </a> <br>
 * </ul>
 * <p>
 * 
 * <h4>Implementation Notes:</h4>
 * <ul>
 * <li>This methods in this interface are safe to use among different threads,
 * BUT the underlying classes may not be. Use with care.
 * </ul>
 * <p>
 * 
 * <h4>Bibliography:</h4>
 * <br>
 * Many of the specific projection equations were taken from <i>Map Projections
 * --A Working Manual </i>, by John Synder.
 * <p>
 * 
 * @see Proj
 * @see Cylindrical
 * @see Mercator
 * @see CADRG
 * @see Azimuth
 * @see Orthographic
 * @see Cartesian
 * @see GeoProj
 * 
 */
public interface Projection
        extends java.io.Serializable {

    /**
     * Get the scale.
     * 
     * @return float scale
     */
    public float getScale();

    /**
     * Get the maximum scale value.
     * 
     * @return float maxscale
     */
    public float getMaxScale();

    /**
     * Get the minimum scale value.
     * 
     * @return float minscale
     */
    public float getMinScale();

    /**
     * Get the center coordinate.
     * 
     * @return center point
     */
    public <T extends Point2D> T getCenter();

    /**
     * Get the center coordinates set in a Point2D object provided.
     * 
     * @param fillInThis the Point2D to fill in.
     * @return center point provided.
     */
    public <T extends Point2D> T getCenter(T fillInThis);

    /**
     * Get the width of the map in pixels.
     * 
     * @return int width.
     */
    public int getWidth();

    /**
     * Get the height of the map in pixels.
     * 
     * @return int height.
     */
    public int getHeight();

    /**
     * Get the projection ID string.
     * 
     * @return String projID
     */
    public String getProjectionID();

    /**
     * Get the upper left (northwest) point of the projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * 
     * @return Point2D
     */
    public <T extends Point2D> T getUpperLeft();

    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * 
     * @return Point2D
     */
    public <T extends Point2D> T getLowerRight();

    /**
     * Checks if a point is plot-able.
     * <p>
     * Call this to check and see if a point can be plotted. This is meant to be
     * used for checking before projecting and rendering Point2D objects
     * (bitmaps or text objects tacked at a specific location, for instance).
     * 
     * @param point Point2D
     * @return boolean
     */
    public boolean isPlotable(Point2D point);

    /**
     * Checks if a location is plot-able.
     * <p>
     * Call this to check and see if a location can be plotted. This is meant to
     * be used for checking before projecting and rendering Point2D objects
     * (bitmaps or text objects tacked at a location, for instance).
     * 
     * @param lat vertical location component (units depend on the projection
     *        implementation).
     * @param lon horizontal location component (units depend on the projection
     *        implementation).
     * @return boolean true if plotable.
     */
    public boolean isPlotable(float lat, float lon);

    /**
     * Checks if a location is plot-able.
     * <p>
     * Call this to check and see if a location can be plotted. This is meant to
     * be used for checking before projecting and rendering Point2D objects
     * (bitmaps or text objects tacked at a location, for instance).
     * 
     * @param lat vertical location component (units depend on the projection
     *        implementation).
     * @param lon horizontal location component (units depend on the projection
     *        implementation).
     * @return boolean true of plotable.
     */
    public boolean isPlotable(double lat, double lon);

    /**
     * Forward project a world coordinate into XY pixel space.
     * 
     * @param coord Point2D
     * @return Point2D (new)
     */
    public Point2D forward(Point2D coord);

    /**
     * Forward projects a world coordinate into XY space and return a Point2D.
     * 
     * @param llp Point2D containing coordinates to be projected
     * @param pt A Point2D object to load the result into, a new Point2D object
     *        will be created if this is null.
     * @return Point2D The Point2D object provided (for convenience) or created
     *         with the result.
     */
    public Point2D forward(Point2D llp, Point2D pt);

    /**
     * Forward project y, x world coordinates into xy space.
     * 
     * @param lat float vertical location component (units depend on projection
     *        implementation).
     * @param lon float horizontal location component (units depend on
     *        projection implementation).
     * @return Point2D (new)
     */
    public Point2D forward(float lat, float lon);

    /**
     * Forward project y, x world coordinates into xy space.
     * 
     * @param lat double vertical location component (units depend on projection
     *        implementation).
     * @param lon double horizontal location component (units depend on
     *        projection implementation).
     * @return Point2D (new)
     */
    public Point2D forward(double lat, double lon);

    /**
     * Forward projects y, x world coordinates into XY space and returns a
     * Point2D.
     * 
     * @param lat float vertical location component (units depend on projection
     *        implementation).
     * @param lon float horizontal location component (units depend on
     *        projection implementation).
     * @param pt A Point2D object to load the result into, a new Point2D object
     *        will be created if this is null.
     * @return Point2D The Point2D object provided (for convenience) or created
     *         with the result.
     */
    public Point2D forward(float lat, float lon, Point2D pt);

    /**
     * Forward projects y, x world coordinates into XY space and returns a
     * Point2D.
     * 
     * @param lat double vertical location component (units depend on projection
     *        implementation).
     * @param lon double horizontal location component (units depend on
     *        projection implementation).
     * @param pt A Point2D object to load the result into, a new Point2D object
     *        will be created if this is null.
     * @return Point2D The Point2D object provided (for convenience) or created
     *         with the result.
     */
    public Point2D forward(double lat, double lon, Point2D pt);

    /**
     * Forward project a shape defined with world coordinates into map x, y
     * space.
     * 
     * @param shape java.awt.Shape object to project.
     * @return java.awt.Shape object defined for projection.
     */
    public Shape forwardShape(Shape shape);

    /**
     * Inverse project a Point2D from map x/y space into world coordinates.
     * 
     * @param point XY Point2D
     * @return Point2D (new)
     */
    public <T extends Point2D> T inverse(Point2D point);

    /**
     * Inverse project a Point2D from map x/y space into world coordinates.
     * 
     * @param point2D XY Point2D
     * @param llpt resulting Point2D object to load the result into, a new
     *        Point2D object will be created if this is null.
     * @return Point2D Object containing result.
     */
    public <T extends Point2D> T inverse(Point2D point2D, T llpt);

    /**
     * Inverse project x,y coordinates into world coordinates.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @return Point2D (new)
     */
    public <T extends Point2D> T inverse(double x, double y);

    /**
     * Inverse project x,y coordinates into world coordinates.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llpt Point2D to be loaded with the result. A new Point2D object
     *        will be created if this is null.
     * @return Point2D llpt
     * @see Proj#inverse(Point2D)
     */
    public <T extends Point2D> T inverse(double x, double y, T llpt);

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(180, c)</code> pan south `c' amount.
     * <li><code>pan(-90, c)</code> pan west `c' amount.
     * <li><code>pan(0, c)</code> pan north `c' amount.
     * <li><code>pan(90, c)</code> pan east `c' amount.
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     * @param c distance in units determined by the projection implementation.
     */
    public void pan(float Az, float c);

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(180)</code> pan south
     * <li><code>pan(-90)</code> pan west
     * <li><code>pan(0)</code> pan north
     * <li><code>pan(90)</code> pan east
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az);

    /**
     * Forward project a line defined by two coordinate end points.
     * <p>
     * Returns a ArrayList<int[]> of (x[], y[]) coordinate pair(s) of the
     * projected line(s).
     */
    public ArrayList<float[]> forwardLine(Point2D ll1, Point2D ll2);

    /**
     * Forward project a rectangle defined by an upper left point and a lower
     * right point.
     * <p>
     * Returns a ArrayList<int[]> of (x[], y[]) coordinate pairs of the
     * projected points.
     * 
     * @param ll1 Point2D of northwest corner
     * @param ll2 Point2D of southeast corner
     * @return ArrayList
     */
    public ArrayList<float[]> forwardRect(Point2D ll1, Point2D ll2);

    /**
     * Forward project a polygon defined by the coordinates. The isFilled flag
     * is only occasionally important, for certain projections in certain
     * situations.
     * 
     * @param rawllpts a set of y, x coordinates.
     * @param isFilled true of is important to note the area of the poly,
     *        instead of just the edge.
     * @return ArrayList<float[]> contains sets of float[]x, float[] y arrays.
     */
    public ArrayList<float[]> forwardPoly(float[] rawllpts, boolean isFilled);

    /**
     * Forward project a polygon defined by the coordinates. The isFilled flag
     * is only occasionally important, for certain projections in certain
     * situations.
     * 
     * @param rawllpts a set of y, x coordinates.
     * @param isFilled true of is important to note the area of the poly,
     *        instead of just the edge.
     * @return ArrayList<float[]> contains sets of float[]x, float[] y arrays.
     */
    public ArrayList<float[]> forwardPoly(double[] rawllpts, boolean isFilled);

    /**
     * Forward project a raw array of world coordinates. This assumes nothing
     * about the array of coordinates. In no way does it assume the points are
     * connected or that the composite figure is to be filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * 
     * @param rawllpts array of y, x
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     *        least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     *         visible.
     */
    public boolean forwardRaw(float[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen);

    /**
     * Forward project a raw array of world coordinates. This assumes nothing
     * about the array of coordinates. In no way does it assume the points are
     * connected or that the composite figure is to be filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * 
     * @param rawllpts array of y, x
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     *        least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     *         visible.
     */
    public boolean forwardRaw(double[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen);

    /**
     * Clone the projection.
     * 
     * @return Projection clone of this one.
     */
    public Projection makeClone();

    /**
     * Get the String used as a name, usually as a type.
     */
    public String getName();

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.geom.Point2D reflecting a pixel spot on the
     *        projection that matches the ll1 coordinate, the upper left corner
     *        of the area of interest. Note that this is where ll1 is going to
     *        go in the new projection, not where it is now.
     * @param point2 a java.awt.geom.Point2D reflecting a pixel spot on the
     *        projection that matches the ll2 coordinate, usually the lower
     *        right corner of the area of interest. Note that this is where ll2
     *        is going to go in the new projection, not where it is now.
     */
    public <T extends Point2D> float getScale(T ll1, T ll2, Point2D point1, Point2D point2);

    /**
     * Get the unprojected coordinates units of measure.
     * 
     * @return Length. May be null if unknown.
     */
    public Length getUcuom();

}
