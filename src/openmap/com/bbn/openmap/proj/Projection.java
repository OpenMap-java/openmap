// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.3 $
// $Date: 2003/12/23 20:43:57 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.Image;
import java.util.ArrayList;

import com.bbn.openmap.LatLonPoint;


/**
 * Projection interface to the OpenMap projection library.
 * <p>
 * This is a READONLY interface to the projection object.  See the
 * <code>Proj</code> class for read/write access.
 * <p>
 * A projection is an object that is maintained by the map, and represents a
 * abstract "view" of the data.  The projection has the properties of x-width, * y-height, scale (in pixels/meters), and latitude/longitude center point.
 * <p>
 *
 * <h4>Projection Notes/Assumptions:</h4>
 * <ul>
 * <li>At the center point of the projection, North is to the top of the
 * screen.<br>
 * <li><a href="#poly_restrictions">LatLon Polygon Restrictions.</a><br>
 * <li><a href="#line_restrictions">LatLon Line Restrictions.</a><br>
 * </ul>
 * <p>
 *
 * <h4>Implementation Notes:</h4>
 * <ul>
 * <li>This methods in this interface are safe to use among different threads,
 * BUT the underlying classes may not be.  Use with care.
 * </ul>
 * <p>
 *
 * <h4>Bibliography:</h4><br>
 * Many of the specific projection equations were taken from <i>Map
 * Projections --A Working Manual</i>, by John Synder.<p>
 * 
 * @see Proj
 * @see Cylindrical
 * @see Mercator
 * @see CADRG
 * @see Azimuth
 * @see Orthographic
 *
 */
public interface Projection extends java.io.Serializable {

    /**
     * Get the scale.
     * @return float scale
     */
    public float getScale();

    /**
     * Get the maximum scale.
     * @return float maxscale
     */
    public float getMaxScale();

    /**
     * Get the minimum scale.
     * @return float minscale
     */
    public float getMinScale();

    /**
     * Get the center LatLonPoint.
     * @return center point
     */
    public LatLonPoint getCenter();

    /**
     * Get the width of the map.
     * @return int width.
     */
    public int getWidth();

    /**
     * Get the height of the map.
     * @return int height.
     */
    public int getHeight();

    /**
     * Get the type of projection.
     * @return int type
     */
    public int getProjectionType();

    /**
     * Get the projection ID string.
     * @return String projID
     */
    public String getProjectionID();

    /**
     * Get the upper left (northwest) point of the projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft();

    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight();

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * Call this to check and see if a LatLonPoint can be plotted.  This is
     * meant to be used for checking before projecting and rendering Point
     * objects (bitmaps or text objects tacked at a LatLonPoint for instance).
     * @param llpoint LatLonPoint
     * @return boolean
     */
    public boolean isPlotable(LatLonPoint llpoint);

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * Call this to check and see if a LatLonPoint can be plotted.  This is
     * meant to be used for checking before projecting and rendering Point
     * objects (bitmaps or text objects tacked at a LatLonPoint for instance).
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon);

    /**
     * Forward project a LatLonPoint into XY space.
     * @param llpoint LatLonPoint
     * @return Point (new)
     */
    public Point forward(LatLonPoint llpoint);

    /**
     * Forward projects a LatLonPoint into XY space and return a Point.
     * @param llp LatLonPoint to be projected
     * @param pt Resulting XY Point
     * @return Point pt
     */
    public Point forward(LatLonPoint llp, Point pt);

    /**
     * Forward project lat,lon coordinates into xy space.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees decimal degrees
     * @return Point (new)
     */
    public Point forward(float lat, float lon);

    /**
     * Forward projects lat,lon coordinates into XY space and returns
     * a Point.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @param pt Resulting XY Point
     * @return Point pt
     */
    public Point forward(float lat, float lon, Point pt);

    /**
     * Forward projects lat,lon coordinates into XY space and returns
     * a Point.
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param pt Resulting XY Point
     * @param isRadian placeholder argument indicating that lat,lon
     * arguments are in radians (can be true or false)
     * @see #forward(float,float,Point)
     * @return Point pt
     */
    public Point forward(float lat, float lon, Point pt, boolean isRadian);

    /**
     * Inverse project a Point.
     * @param point XY Point
     * @return LatLonPoint (new)
     */
    public LatLonPoint inverse(Point point);

    /**
     * Inverse project a point with llpt.
     * @param point x,y Point
     * @param llpt resulting LatLonPoint
     * @return LatLonPoint llpt
     */
    public LatLonPoint inverse(Point point, LatLonPoint llpt);

    /**
     * Inverse project x,y coordinates.
     * @param x @param y
     * @return LatLonPoint (new)
     */
    public LatLonPoint inverse(int x, int y);

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llpt LatLonPoint
     * @return LatLonPoint llpt
     * @see Proj#inverse(Point)
     */
    public LatLonPoint inverse(int x, int y, LatLonPoint llpt);

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(±180, c)</code> pan south `c' degrees
     * <li><code>pan(-90, c)</code> pan west `c' degrees
     * <li><code>pan(0, c)</code> pan north `c' degrees
     * <li><code>pan(90, c)</code> pan east `c' degrees
     * </ul>
     * @param Az azimuth "east of north" in decimal degrees:
     * <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees
     */
    public void pan(float Az, float c);

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(±180)</code> pan south
     * <li><code>pan(-90)</code> pan west
     * <li><code>pan(0)</code> pan north
     * <li><code>pan(90)</code> pan east
     * </ul>
     * @param Az azimuth "east of north" in decimal degrees:
     * <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az);

    /**
     * Forward project a LatLon Poly.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pair(s) of the
     * projected poly.
     * <a name="poly_restrictions">
     * <h4>RESTRICTIONS:</h4><br>
     * </a>
     * All the following restrictions apply to LatLon polygons (either filled
     * or non-filled).  Many of these restrictions apply to other poly-like
     * ArrayList graphics (Lines, Rectangles, Circles, Ellipses, ...).  See also
     * <a href="#line_restrictions">restrictions on LatLon lines.</a>
     * <p>
     * <a name="antarctica_anomaly"></a> For the cylindrical projections,
     * (e.g. Mercator), your polygons should not include or touch the poles.
     * This is because a polygon or polyline that includes a pole becomes a
     * non-continuous straight line on the map.  "So what about Antarctica",
     * you say, "after all it's a polygon that is draped over the South Pole".
     * Well, if you want to see it in a cylindrical projection, you will need
     * to "augment" the vertices to turn it into a valid x-y polygon.  You
     * could do this by removing the segment which crosses the dateline, and
     * instead add two extra edges down along both sides of the dateline to
     * very near the south pole and then connect these ends back the other way
     * around the world (not across the dateline) with a few extra line
     * segments (remember the <a href="#line_restrictions">line length
     * restrictions</a>).  This way you've removed the polar anomaly from the
     * data set.  On the screen, all you see is a sliver artifact down along
     * the dateline.  This is the very method that our DCW data server shows
     * Antarctica.
     * <p>
     * There is a fundamental ambiguity with filled polygons on a sphere:
     * which side do you draw the fill-color?  The Cylindrical family will
     * draw the polygon as if it were in x-y space.  For the Azimuthal
     * projections, (e.g. Orthographic), you can have polygons that cover the
     * pole, but it's important to specify the vertices in a clockwise order
     * so that we can do the correct clipping along the hemisphere edge.  We
     * traverse the vertices assuming that the fill will be to the right hand
     * side if the polygon straddles the edge of the projection.  (This
     * default can be changed).
     * <p>
     * <h3>To Be (Mostly) Safe:</h3>
     * <ul>
     * <li>Polygons should not touch or encompass the poles unless you
     * will be viewing them with azimuthal projections, such as Orthographic.<br>
     * <li>Polygons should not encompass more area than one hemisphere.<br>
     * <li>Polygon vertices should be specified in "clockwise", fill-on-right
     * order to ensure proper filling.<br>
     * <li>Polygon edges are also restricted by the
     * <a href="#line_restrictions">restrictions on LatLon lines</a>.
     * </ul>
     * <p>
     * <h3>Optimization Notes:</h3>
     * The projection library deals internally in radians, and so
     * you're required to pass in an array of radian points.  See
     * <a href="com.bbn.openmap.proj.ProjMath.html#arrayDegToRad">
     * ProjMath.arrayDegToRad(float[])</a> for an efficient in-place
     * conversion.
     * <p>
     * For no-frills, no-assumptions, fast and efficient projecting, see
     * <a href="#forwardRaw">forwardRaw()</a>.
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @param isFilled poly is filled? or not
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see #forwardRaw
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     */
    public ArrayList forwardPoly(float[] rawllpts, int ltype, int nsegs, boolean isFilled);

    /**
     * Forward project a LatLon Line.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pair(s) of the
     * projected line(s).
     *
     * <a name="line_restrictions">
     * <h4>RESTRICTIONS:</h4>
     * </a>
     * A line segment must be less than 180 degrees of arc (half the
     * circumference of the world).  If you need to draw a longer
     * line, then draw several several individual segments of less
     * than 180 degrees, or draw a single polyline of those segments.
     * <p>
     * We make this restriction because from any point on a sphere, you can
     * reach any other point with a maximum traversal of 180degrees of arc.
     * <p>
     * Furthermore, for the Cylindrical family of projections, a line must be
     * &lt; 180 degrees of arc in longitudinal extent.  In other words, the
     * difference of longitudes between both vertices must be &lt; 180
     * degrees.  Same as above: if you need a long line, you must break it
     * into several segments.
     *
     * @param ll1 LatLonPoint
     * @param ll2 LatLonPoint
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @return ArrayList
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     *
     */
    public ArrayList forwardLine(LatLonPoint ll1, LatLonPoint ll2, int ltype, int nsegs);

    /**
     * Forward project a LatLon Rectangle.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pairs of the
     * projected points.
     * <p>
     * Rects have the same restrictions as <a href="#poly_restrictions">
     * polys<a> and <a href="#line_restrictions">lines</a>.
     *
     * @param ll1 LatLonPoint of northwest corner
     * @param ll2 LatLonPoint of southeast corner
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @see #forwardPoly
     * @return ArrayList
     */
    public ArrayList forwardRect(LatLonPoint ll1, LatLonPoint ll2, 
				 int ltype, int nsegs, boolean isFilled);

    /**
     * Forward project a LatLon Arc.
     * <p>
     * Arcss have the same restrictions as <a href="#poly_restrictions">
     * polys</a>.
     *
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @param start the starting angle of the arc in decimal degrees.
     * @param extent the angular extent of the arc in decimal degrees.
     * @param arcType type of arc to create - see java.awt.geom.Arc2D
     * for (OPEN, CHORD, PIE).  Arc2D.OPEN means that the just the
     * points for the curved edge will be provided.  Arc2D.PIE means
     * that addition lines from the edge of the curve to the center
     * point will be added.  Arc2D.CHORD means a single line from each
     * end of the curve will be drawn.
     * @see #forwardPoly
     */
    public ArrayList forwardArc(LatLonPoint c, boolean radians,
				float radius, int nverts,
				float start, float extent,
				int arcType);

    /**
     * Forward project a LatLon Circle.
     * <p>
     * Circles have the same restrictions as <a href="#poly_restrictions">
     * polys</a>.
     *
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @see #forwardPoly
     */
    public ArrayList forwardCircle(LatLonPoint c, boolean radians, 
				   float radius, int nverts, boolean isFilled);

    /**
     * Forward projects a raster.
     * <p>
     * This is currently unimplemented in the projection implementations.
     * @param llNW LatLonPoint of NorthWest corner of Image
     * @param llSE LatLonPoint of SouthEast corner of Image
     * @param image raster image
     */
    public ArrayList forwardRaster(LatLonPoint llNW, LatLonPoint llSE, Image image);

    /**
     * Forward project a raw array of radian points.
     * This assumes nothing about the array of coordinates.  In no way does it
     * assume the points are connected or that the composite figure is to be
     * filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * @param rawllpts array of lat,lon,... in RADIANS!
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     * least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     * visible.
     */
    public boolean forwardRaw(float[] rawllpts, int rawoff,
			      int[] xcoords, int[] ycoords, boolean[] visible,
			      int copyoff, int copylen);

    /**
     * Clone the projection.
     * @return Projection clone of this one.
     */
    public Projection makeClone();

    /**
     * Get the String used as a name, usually as a type.
     */
    public String getName();

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     *
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll1 coordinate, the upper left
     * corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll2 coordinate, usually the lower
     * right corner of the area of interest.
     */
    public float getScale(LatLonPoint ll1, LatLonPoint ll2, Point point1, Point point2);

}
