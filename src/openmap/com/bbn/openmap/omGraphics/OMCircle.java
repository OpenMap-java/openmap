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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMCircle.java,v $
// $RCSfile: OMCircle.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.io.Serializable;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.*;

/**
 * Graphic object that represents a circle or an ellipse.
 * <p>
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#poly_restrictions">
 * RESTRICTIONS</a> on Lat/Lon polygons/polylines which apply to
 * circles as well.  Not following the guidelines listed may result in
 * ambiguous/undefined shapes!  Similar assumptions apply to the other
 * vector graphics that we define: polys, rects, lines.
 * <p>
 * We currently do not allow LatLon ellipses, only XY.
 * <p>
 * These assumptions are virtually the same as those on the more
 * generic OMPoly graphic type.
 * <p>
 * @see OMPoly
 */
public class OMCircle extends OMGraphic implements Serializable {

    /** Horizontal pixel location of the center. */
    protected int x1 = 0;
    /** Vertical pixel location of the center. */
    protected int y1 = 0;

    /** Horizontal pixel offset. */
    protected int off_x = 0;
    /** Vertical pixel offset. */
    protected int off_y = 0;
    /**
     * Center point.
     */
    protected LatLonPoint center;
    /**
     * Radius of circle in radians. For LATLON circle.  Note that the
     * methods for this class use Decimal Degrees, or ask for a Length
     * object to use for units.  The radius is converted to radians
     * for internal use.
     */
    protected float radius = 0.0f;
    /**
     * The pixel horizontal diameter of the circle/ellipse.
     * For XY and OFFSET circle/ellipse.
     */
    protected int width = 0;
    /**
     * The pixel vertical diameter of the circle/ellipse.
     * For XY and OFFSET circle/ellipse.
     */
    protected int height = 0;
  
    /**
     * Used to render circle in Cylindrical projections when the
     * circle encompases a pole.
     */
    private GeneralPath polarShapeLine = null;
    /**
     * Indicates that the polarShapeLine should be used for rendering.
     */
    private boolean correctFill = false;
    /** Force the correct polar hack. */
    private boolean correctPolar = false;

    /**
     * Number of vertices to draw for lat/lon poly-circles.
     */
    protected int nverts;

    /** The angle by which the circle/ellipse is to be rotated, in radians */
    protected double rotationAngle = DEFAULT_ROTATIONANGLE;

    /**
     * The simplest constructor for an OMCircle, and it expects that
     * all fields will be filled in later.  Rendertype is
     * RENDERTYPE_UNKNOWN.
     */
    public OMCircle() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Create a OMCircle, positioned with a lat-lon center and x-y
     * axis.  Rendertype is RENDERTYPE_OFFSET.
     *
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     */
    public OMCircle(float latPoint, float lonPoint,
		    int w, int h) {
	this (latPoint, lonPoint, 0, 0, w, h);
    }

    /**
     * Create a OMCircle, positioned with a x-y center with x-y axis.
     * Rendertype is RENDERTYPE_XY.
     *
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     */
    public OMCircle(int x1, int y1, 
		    int w, int h) { 
        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

	this.x1 = x1;
	this.y1 = y1;
	width = w;
	height = h;
    }

    /**
     * Create a OMCircle, positioned at a Lat-lon location, x-y
     * offset, x-y axis.  Rendertype is RENDERTYPE_OFFSET.
     *
     * @param latPoint latitude of center of circle/ellipse.
     * @param lonPoint longitude of center of circle/ellipse.
     * @param offset_x1 # pixels to the right the center will be moved
     * from lonPoint.
     * @param offset_y1 # pixels down that the center will be moved
     * from latPoint.
     * @param w horizontal diameter of circle/ellipse, pixels.
     * @param h vertical diameter of circle/ellipse, pixels.
     */
    public OMCircle(float latPoint, float lonPoint,
		    int offset_x1, int offset_y1, 
		    int w, int h) { 
        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

	center = new LatLonPoint(latPoint, lonPoint);
	off_x = offset_x1;
	off_y = offset_y1;
	width = w;
	height = h;
    }

    /**
     * Creates an OMCircle with a Lat-lon center and a lat-lon axis.
     * Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees (converted to radians
     * internally).  
     */
    public OMCircle(float latPoint, float lonPoint, float radius) {
	this(new LatLonPoint(latPoint, lonPoint), radius, 
	     Length.DECIMAL_DEGREE, -1);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object.
     */
    public OMCircle(float latPoint, float lonPoint, 
		    float radius, Length units) {
	this(new LatLonPoint(latPoint, lonPoint), radius, units, -1);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units.
     * @param nverts number of vertices for the poly-circle (if &lt; 3, value
     * is generated internally)
     */
    public OMCircle(float latPoint, float lonPoint, 
		    float radius, Length units, int nverts) {
	this(new LatLonPoint(latPoint, lonPoint), radius, units, nverts);
    }

    /**
     * Create an OMCircle with a lat/lon center and a physical
     * distance radius.  Rendertype is RENDERTYPE_LATLON.
     *
     * @param center LatLon center of circle
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying
     * units for distance.
     * @param nverts number of vertices for the poly-circle(if &lt; 3, value
     * is generated internally) 
     */
    public OMCircle(LatLonPoint center, float radius, 
		    Length units, int nverts) {

	super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
	this.radius = units.toRadians(radius);
	this.center = center;
	this.nverts = nverts;
    }

    /**
     * Get the x position of the center.  This is always meaningful
     * only if the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET,
     * and meaningful after generation if the RENDERTYPE_LATLON.
     *
     * @return x position of center.  
     */
    public int getX() {
        return x1;
    }

    /**
     * Get the y position of the center.  This is always meaningful
     * only if the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET,
     * and meaningful after generation if the RENDERTYPE_LATLON.
     *
     * @return y position of center. 
     */
    public int getY() {
        return y1;
    }

    /**
     * Get the x offset from the center.  This is meaningful only if
     * the render type is RENDERTYPE_OFFSET.
     *
     * @return x offset from center. 
     */
    public int getOffX() {
        return off_x;
    }

    /**
     * Get the y position of the center.  This is meaningful only if
     * the render type is RENDERTYPE_OFFSET.
     *
     * @return y offset from center. 
     */
    public int getOffY() {
        return off_y;
    }

    /**
     * Get the center LatLonPoint.
     * This is meaningful only if the rendertype is RENDERTYPE_LATLON
     * or RENDERTYPE_OFFSET.
     *
     * @return LatLonPoint position of center. 
     */
    public LatLonPoint getLatLon() {
        return center;
    }

    /**
     * Get the radius.
     * This is meaningful only if the render type is
     * RENDERTYPE_LATLON.
     *
     * @return float radius in decimal degrees
     */
    public float getRadius() {
        return Length.DECIMAL_DEGREE.fromRadians(radius);
    }

    /**
     * Get the horizontal pixel diameter of the circle.  This is
     * meaningful only if the render type is RENDERTYPE_XY or
     * RENDERTYPE_OFFSET.
     *
     * @return the horizontal pixel diameter of the circle. 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the vertical pixel diameter of the circle.  This is
     * meaningful only if the render type is RENDERTYPE_XY or
     * RENDERTYPE_OFFSET.
     *
     * @return the vertical pixel diameter of the circle. 
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the number of vertices of the lat/lon circle.  This will be
     * meaningful only if the render type is RENDERTYPE_XY or
     * RENDERTYPE_OFFSET and for LINETYPE_GREATCIRCLE or
     * LINETYPE_RHUMB line types.
     *
     * @return int number of segment points
     */
    public int getNumVerts() {
	return nverts;
    }

    /**
     * Set the x position of the center.  This will be meaningful only
     * if the render type is RENDERTYPE_XY.
     *
     * @param value the x position of center.  
     */
    public void setX(int value) {
        if (x1 == value) return;
	x1 = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the y position of the center.  This will be meaningful only
     * if the render type is RENDERTYPE_XY.
     *
     * @param value the y position of center.  
     */
    public void setY(int value) {
        if (y1 == value) return;
	y1 = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the x offset from the center.  This will be meaningful only
     * if the render type is RENDERTYPE_OFFSET.
     *
     * @param value the x position of center. 
     */
    public void setOffX(int value) {
        if (off_x == value) return;
	off_x = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the y offset from the center.  This will be meaningful only
     * if the render type is RENDERTYPE_OFFSET.
     *
     * @param value the y position of center. 
     */
    public void setOffY(int value) {
        if (off_y == value) return;
	off_y = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the latitude and longitude of the center point.
     * This is meaningful only if the rendertype is RENDERTYPE_LATLON
     * or RENDERTYPE_OFFSET.
     *
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public void setLatLon(float lat, float lon) {
	LatLonPoint p = new LatLonPoint(lat, lon);
	if (p.equals(center))
	    return;
	center = p;
	setNeedToRegenerate(true);
    }

    /**
     * Set the radius. This is meaningful only if the render type is
     * RENDERTYPE_LATLON.  Note that while the radius is specified as
     * decimal degrees, it only means the distance along the ground
     * that that number of degrees represents at the equator, *NOT* a
     * radius of a number of degrees around a certain location.  There
     * is a difference.
     *
     * @param radius float radius in decimal degrees 
     */
    public void setRadius(float radius) {
        this.radius = Length.DECIMAL_DEGREE.toRadians(radius);
	setNeedToRegenerate(true);
    }

    /**
     * Set the radius with units.
     * This is meaningful only if the render type is
     * RENDERTYPE_LATLON.
     *
     * @param radius float radius
     * @param units Length specifying unit type.
     */
    public void setRadius(float radius, Length units) {
        this.radius = units.toRadians(radius);
	setNeedToRegenerate(true);
    }

    /**
     * Set the horizontal pixel diameter of the circle.  This is
     * meaningful only if the render type is RENDERTYPE_XY or
     * RENDERTYPE_OFFSET.
     *
     * @param value the horizontial pixel diamter of the circle. 
     */
    public void setWidth(int value) {
        if (width == value) return;
	width = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the vertical pixel diameter of the circle.  This is
     * meaningful only if the render type is RENDERTYPE_XY or
     * RENDERTYPE_OFFSET.
     *
     * @param value the vertical pixel diameter of the circle. 
     */
    public void setHeight(int value) {
        if (height == value) return;
	height = value;
	setNeedToRegenerate(true);
    }

    /**
     * Set the number of vertices of the lat/lon circle.  This is
     * meaningful only if the render type is RENDERTYPE_LATLON and for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types.  If &lt;
     * 1, this value is generated internally.
     *
     * @param nverts number of segment points
     */
    public void setNumVerts(int nverts) {
	this.nverts = nverts;
    }

    /**
     * Set the angle by which the circle/ellipse is to rotated.
     *
     * @param angle the number of radians the circle/ellipse is to be
     * rotated.  Measured clockwise from horizontal.  Positive numbers
     * move the positive x axis toward the positive y axis.
     */
    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
	setNeedToRegenerate(true);
    }

    /**
     * Get the current rotation of the circle/ellipse.
     *
     * @return the circle/ellipse rotation.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setNeedToRegenerate(boolean value) { 
	super.setNeedToRegenerate(value);
	if (value) {
	    polarShapeLine = null;
	    correctFill = false;
	}
    }

    /**
     * Get the polar-fill-correction-flag.
     *
     * @return boolean
     * @see #setPolarCorrection
     */
    public boolean getPolarCorrection() {
	return correctPolar;
    }

    /**
     * Set the polar-fill-correction-flag.
     * We don't correctly render *filled* circles/polygons which
     * encompass a pole in Cylindrical projections.  This method will
     * toggle support for correcting this problem.  You should only
     * set this on circles that encompass a pole and are drawn with a
     * fill color.  You do not need to set this if you're only drawing
     * the circle outline.
     *
     * @param value boolean
     * @see OMGraphic#setLineColor
     * @see OMGraphic#setFillColor
     */
    public void setPolarCorrection(boolean value) {
	correctPolar = value;
	setNeedToRegenerate(true);
    }

    /**
     * Prepare the circle for rendering.
     *
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
	shape = null;
	polarShapeLine = null;
	correctFill = false;

	if (proj == null) {
	    Debug.message("omgraphic", "OMCircle: null projection in generate!");
	    return false;
	}

	GeneralPath gp, gp1, gp2;
	PathIterator pi;
	AffineTransform af = null;

	switch (renderType) {
	case RENDERTYPE_OFFSET:
	    if (!proj.isPlotable(center)) {
		setNeedToRegenerate(true);//HMMM not the best flag
		return false;
	    }
	    Point p1 = proj.forward(center.radlat_, 
				    center.radlon_, 
				    new Point(), true);
	    x1 = p1.x + off_x;
	    y1 = p1.y + off_y;

	case RENDERTYPE_XY:
	    float fwidth = (float)width;
	    float fheight = (float)height;
	    float transx = (float)x1;
	    float transy = (float)y1;
	    float x = transx - fwidth/2f;
	    float y = transy - fheight/2f;

	    Ellipse2D e2d = new Ellipse2D.Float(x, y, fwidth, fheight);

	    if (rotationAngle != DEFAULT_ROTATIONANGLE) {
		af = new AffineTransform();
		af.rotate(rotationAngle, transx, transy);
	    }
	    pi = e2d.getPathIterator(af);
	    gp = new GeneralPath();
	    gp.append(pi, false);
	    // In X/Y or Offset RenderType, there is only one shape.
	    shape = gp;

	    break;

	case RENDERTYPE_LATLON:
	    ArrayList circles = null;
	    circles = proj.forwardCircle(
		center, /*radians*/true, radius, nverts, !isClear(fillPaint));

	    Point p = proj.forward(center.radlat_, 
				   center.radlon_, 
				   new Point(), true);
	    x1 = p.x;
	    y1 = p.y;

	    int size = circles.size();
	    GeneralPath tempShape = null;

	    for (int i=0; i<size; i+=2) {
		int[] xpoints = (int[])circles.get(i);
		int[] ypoints = (int[])circles.get(i+1);

		gp = createShape(xpoints, ypoints, true);

		if (shape == null) {
		    shape = gp;
		} else {
		    ((GeneralPath)shape).append(gp, false);
		}

//  		if (i == 0) {
		    correctFill = (proj instanceof Cylindrical) && 
			(!shape.contains(x1, y1) || correctPolar);
//  		}

		if (correctFill) {

		    int[][] alts = doPolarFillCorrection(
			xpoints, ypoints,
			(center.radlat_ > 0f) ? -1 : proj.getWidth()+1);

		    int gp2length = alts[0].length - 2;

		    gp1 = createShape(alts[0], alts[1], true);
		    gp2 = createShape(alts[0], alts[1], 0,
				      gp2length, false);

		    if (tempShape == null || polarShapeLine == null) {
			tempShape = gp1;
			polarShapeLine = gp2;
		    } else {
			tempShape.append(gp1, false);
			polarShapeLine.append(gp2, false);
		    }
		}
	    }

	    if (tempShape != null) {
		shape = tempShape;
	    }

	    break;
	case RENDERTYPE_UNKNOWN:
	    System.err.println("OMCircle.generate(): invalid RenderType");
	    return false;
	}

	setNeedToRegenerate(false);
	return true;
    }

    /**
     * Return the java.awt.Shape (GeneralPath) that reflects a circle
     * that encompases a pole.  Used when the projection is
     * Cylindrical.
     * @return a GeneralPath object, or null if it's not needed (which
     * is probably most of the time, if the circle doesn't include a
     * pole or the projection isn't Cylindrical).
     */
    public GeneralPath getPolarShapeLine() {
	return polarShapeLine;
    }

    /** 
     * Create alternate x,y coordinate arrays for rendering graphics
     * the encompass a pole in the Cylindrical projection.  
     * @return a two dimensional array of points.  The [0] array is
     * the x points, the [1] is the y points.
     */
    private int[][] doPolarFillCorrection(int[] xpoints, 
					  int[] ypoints, 
					  int y1) {
	int[][] ret = new int[2][];

	int len = xpoints.length;
	int[] alt_xpts = new int[len + 2];
	int[] alt_ypts = new int[len + 2];
	System.arraycopy(xpoints, 0, alt_xpts, 0, len);
	System.arraycopy(ypoints, 0, alt_ypts, 0, len);
	alt_xpts[len] = alt_xpts[len-1];
	alt_xpts[len+1] = alt_xpts[0];
	alt_ypts[len] = y1;
	alt_ypts[len+1] = alt_ypts[len];

	ret[0] = alt_xpts;
	ret[1] = alt_ypts;
	return ret;
    }

    /**
     * Paint the circle.
     *
     * @param g Graphics context to render into
     */
    public void render(Graphics g) {
	if (!isRenderable()) return;

	if (!correctFill) {
	    // super will catch a null shape...
	    super.render(g);
	} else {
	    // The polarShapeLine will be there only if a shape was generated.
	    // This is getting kicked off because the circle is
	    // encompassing a pole, so we need to handle it a little
	    // differently.
	    if (shouldRenderFill()) {
		setGraphicsForFill(g);
		fill(g);
	    }

	    // BUG There is still a bug apparent when, in a
	    // cylindrical projection, and drawing a circle around the
	    // south pole.  If the center of the circle is below any
	    // part of the edge of the circle, with the left lower dip
	    // of the circle on the screen, you get a line drawn from
	    // the right dip to the left dip.  Not sure why.  It's an
	    // unusual case, however.
	    if (shouldRenderEdge()) {
		setGraphicsForEdge(g);
		((Graphics2D)g).draw(polarShapeLine);
	    }
	}
    }
}
