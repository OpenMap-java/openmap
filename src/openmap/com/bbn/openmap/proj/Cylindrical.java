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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Cylindrical.java,v $
// $RCSfile: Cylindrical.java,v $
// $Revision: 1.3 $
// $Date: 2003/11/14 20:56:43 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.*;
import java.util.ArrayList;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;

/**
 * Base of all cylindrical projections.
 * <p>
 * @see Projection
 * @see Proj
 * @see Mercator
 * @see CADRG
 *
 */
public abstract class Cylindrical extends Proj {


    // used for calculating wrapping of ArrayList graphics
    protected Point world;			// world width in pixels.
    protected int half_world;			// world.x / 2


    /**
     * Construct a cylindrical projection.
     * <p>
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * @param type projection type
     *
     */
    public Cylindrical(LatLonPoint center,
	    float scale, int width, int height, int type)
    {
	super(center, scale, width, height, type);
    }


    /**
     * Return stringified description of this projection.
     * <p>
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
	return " world(" + world.x + "," + world.y + ")" + super.toString();
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" parameters
     * used in the forward() and inverse() calls.<p>
     */
    protected void computeParameters() {
	planetPixelRadius = planetRadius * pixelsPerMeter;
	planetPixelCircumference = MoreMath.TWO_PI*planetPixelRadius;

	// minscale is the minimum scale allowable (before integer wrapping
	// can occur)
	minscale = (float)Math.ceil(planetPixelCircumference/(int)Integer.MAX_VALUE);
	if (minscale < 1)
	    minscale = 1;
	if (scale < minscale)
	    scale = minscale;

	// maxscale = scale at which world circumference fits in window
	maxscale = (float) planetPixelCircumference/(float)width;
	if (maxscale < minscale) {
	    maxscale = minscale;
	}
	if (scale > maxscale) {
	    scale = maxscale;
	}
	scaled_radius = planetPixelRadius/scale;

	if (world == null)
	    world = new Point(0,0);

	// width of the world in pixels at current scale
	world.x = (int)(planetPixelCircumference/scale);
	half_world = world.x/2;

	// calculate cutoff scale for XWindows workaround
	XSCALE_THRESHOLD = (int)(planetPixelCircumference/64000);//fudge it a little bit

	if (Debug.debugging("proj")) {
	    Debug.output("Cylindrical.computeParameters(): " +
		    "world.x = " + world.x + " half_world = " +
		    half_world + " XSCALE_THRESHOLD = " + XSCALE_THRESHOLD);
	}
    }

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(±180, c)</code> pan south
     * <li><code>pan(-90, c)</code> pan west
     * <li><code>pan(0, c)</code> pan north
     * <li><code>pan(90, c)</code> pan east
     * </ul>
     * @param Az azimuth "east of north" in decimal degrees:
     * <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az) {
	if (MoreMath.approximately_equal(Math.abs(Az), 180f, 0.01f)) {
	    setCenter(inverse(width/2,height));//south
	} else if (MoreMath.approximately_equal(Az, -135f, 0.01f)) {
	    setCenter(inverse(0,height));//southwest
	} else if (MoreMath.approximately_equal(Az, -90f, 0.01f)) {
	    setCenter(inverse(0,height/2));//west
	} else if (MoreMath.approximately_equal(Az, -45f, 0.01f)) {
	    setCenter(inverse(0,0));//northwest
	} else if (MoreMath.approximately_equal(Az, 0f, 0.01f)) {
	    setCenter(inverse(width/2,0));//north
	} else if (MoreMath.approximately_equal(Az, 45f, 0.01f)) {
	    setCenter(inverse(width,0));//northeast
	} else if (MoreMath.approximately_equal(Az, 90f, 0.01f)) {
	    setCenter(inverse(width,height/2));//east
	} else if (MoreMath.approximately_equal(Az, 135f, 0.01f)) {
	    setCenter(inverse(width,height));//southeast
	} else {
	    super.pan(Az);
	}
    }

    /**
     * Get the upper left (northwest) point of the projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * @return LatLonPoint
     *
     */
    public LatLonPoint getUpperLeft() {
	return inverse(0,0);
    }


    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * @return LatLonPoint
     *
     */
    public LatLonPoint getLowerRight() {
	return inverse(width, height);
    }


    /**
     * Forward project a raw array of radian points.
     * This assumes nothing about the array of coordinates.  In no way does it
     * assume the points are connected or that the composite figure is to be
     * filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * <p>
     * @param rawllpts array of lat,lon,... in radians
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
    public boolean forwardRaw(
	    float[] rawllpts, int rawoff,
	    int[] xcoords, int[] ycoords, boolean[] visible,
	    int copyoff, int copylen)
    {
	Point temp = new Point();
	int end = copylen+copyoff;
	for (int i=copyoff, j=rawoff; i<end; i++, j+=2) {
	    forward(rawllpts[j], rawllpts[j+1], temp, true);
	    xcoords[i] = temp.x;
	    ycoords[i] = temp.y;
	    visible[i] = true;//should always be visible in cylindrical family
	}
	// everything is visible
	return true;
    }


    /**
     * Forward project a raw float[] Poly.
     * <p>
     * <strong>Implementation:</strong><br>
     * For the cylindrical "boxy" family of projections, we project all the
     * points, and check the horizontal (longitudinal) spacing between
     * vertices as we go.  If the spacing is greater than half the world width
     * (circumference) in pixels, we assume that the segment has wrapped off
     * one edge of the screen and back onto the other side.  (NOTE that our
     * restrictions on line segments mentioned in the Projection interface do
     * not allow for lines &gt;= 180 degrees of arc or for the difference in
     * longitude between two points to be &gt;= 180 degrees of arc).
     * <p>
     * For the case where a segment wraps offscreen, we keep track of the
     * wrapping adjustment factor, and shift the points as we go.  After
     * projecting and shifting all the points, we have a single continuous x-y
     * polygon.  We then need to make shifted copies of this polygon for the
     * maxima and minima wrap values calculated during the projection process.
     * This allows us to see the discontinuous (wrapped) sections on the
     * screen when they are drawn.
     * <p>
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segments between vertices (or if &lt; 0,
     * generate this value internally)
     * @param isFilled filled poly?  this is currently ignored
     * for cylindrical projections.
     * @return ArrayList of x[], y[], x[], y[], ...  the projected poly
     */
    protected ArrayList _forwardPoly(
	float[] rawllpts, int ltype, int nsegs, boolean isFilled)
    {
	int p, n, k, flag=0, min=0, max=0, xp, xadj=0;

	// determine length of pairs list
	int len = rawllpts.length>>1;	// len/2, chop off extra
	if (len < 2)
	    return new ArrayList(0);

	// handle complicated line in specific routines
	if (isComplicatedLineType(ltype)) {
	    return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);
	}

	// determine when to stop
	Point temp = new Point(0,0);
	int[] xs = new int[len];
	int[] ys = new int[len];

	// forward project the first point
	forward(rawllpts[0], rawllpts[1], temp, true);
	xp = temp.x;
	xs[0] = temp.x;
	ys[0] = temp.y;
	// forward project the other points
	for (n=1, k=2; n<len; n++, k+=2) {
	    forward(rawllpts[k], rawllpts[k+1], temp, true);
	    xs[n] = temp.x;
	    ys[n] = temp.y;
	    // segment crosses longitude along screen edge
	    if (Math.abs(xp - xs[n]) >= half_world) {
		flag += (xp < xs[n]) ? -1 : 1;//inc/dec the wrap count
		min = (flag < min) ? flag : min;//left wrap count
		max = (flag > max) ? flag : max;//right wrap count
		xadj = flag * world.x;//adjustment to x coordinates
//		Debug.output("flag=" + flag + " xadj=" + xadj);
	    }
	    xp = temp.x;//save previous unshifted x coordinate
	    if (flag != 0) {
		xs[n] += xadj;//adjust x coordinates
	    }
	}
	min *= -1;//positive magnitude

	// now create the return list
	ArrayList ret_val = null;
	ret_val = new ArrayList(2 + 2*(max+min));
	ret_val.add(xs);
	ret_val.add(ys);
	int[] altx=null;

	/*
	if (Debug.debugging("proj")) {
	    dumpPoly(rawllpts, xs, ys);
	}
	*/

	// add the extra left-wrap polys
	for (int i=1; i<=min; i++) {
	    altx = new int[xs.length];
	    xadj = i*world.x;//shift opposite
	    for (int j=0; j<altx.length; j++) {
		altx[j] = xs[j] + xadj;
	    }
	    ret_val.add(altx);
	    ret_val.add(ys);
	    /*
	    if (Debug.debugging("proj")) {
		dumpPoly(rawllpts, altx, ys);
	    }
	    */
	}

	// add the extra right-wrap polys
	for (int i=1; i<=max; i++) {
	    altx = new int[xs.length];
	    xadj = -i*world.x;//shift opposite
	    for (int j=0; j<altx.length; j++) {
		altx[j] = xs[j] + xadj;
	    }
	    ret_val.add(altx);
	    ret_val.add(ys);
	    /*
	    if (Debug.debugging("proj")) {
		dumpPoly(rawllpts, altx, ys);
	    }
	    */
	}

	return ret_val;
    }//_forwardPoly()


    // print out polygon
    private static final void dumpPoly(
	    float[] rawllpts, int[] xs, int[] ys)
    {
	Debug.output("poly:");
	for (int i=0, j=0; j<xs.length; i+=2, j++) {
	    System.out.print("["+
		    ProjMath.radToDeg(rawllpts[i])+","+
		    ProjMath.radToDeg(rawllpts[i+1])+"]=");
	    Debug.output("("+xs[j]+","+ys[j]+")");
	}
	Debug.output("");
    }


    /**
     * Draw the background for the projection.
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
     */
    public void drawBackground(Graphics2D g, java.awt.Paint paint) {
	g.setPaint(paint);
	g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
	return "Cylindrical";
    }
}
