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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMLine.java,v $
// $RCSfile: OMLine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import java.util.ArrayList;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.util.ArcCalc;

/**
 * Graphic object that represents a simple line.
 * <p>
 * The OMLine is used to create simple lines, from one point on the
 * window to the other.  If you want to have a line with several
 * parts, use OMPoly as a polyline with no fillColor.
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#line_restrictions">
 * RESTRICTIONS</a> on Lat/Lon lines.  Not following the guidelines
 * listed may result in ambiguous/undefined shapes!  Similar
 * assumptions apply to the other vector graphics that we define:
 * circles, ellipses, rects, polys.
 * <p>
 * @see OMPoly
 */
public class OMLine extends OMGraphic implements Serializable {

    protected boolean isPolyline = false;

    /** latlons is a array of 4 floats - lat1, lon1, lat2, lon2. */
    protected float[] latlons = null;

    /** pts is an array of 4 ints - px1, py1, px2, py2. */
    protected int[] pts = null;
  
    /**
     * X coordinate arrays of the projected points.
     */
    protected int[][] xpoints = new int[0][0];

    /**
     * Y coordinate arrays of the projected points.
     */
    protected int[][] ypoints = new int[0][0];

    /** Flag used to create arrow heads on lines. */
    protected boolean doArrowHead = false;

    /**
     * Used to draw the ArrowHead on the finishing end, the starting
     * end, or both. 
     */
    protected int arrowDirectionType = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD;
    /**
     * Where on the line to put the ArrowHead, from 0-100.  100 is at
     * the normal endpoint, and 0 is at the normal starting
     * point. For BACKWARDS directions, 100 would be at the
     * beginning of the line.
     */
    protected int arrowLocation = 100;

    /**
     * For arrowhead creation, the width of half of the base of the
     * arrowhead.
     */
    protected int wingTip = OMArrowHead.DEFAULT_WINGTIP;

    /**
     * For arrowhead creation, the pixel distance from the tip of the
     * arrowhead to the base.
     */
    protected int wingLength = OMArrowHead.DEFAULT_WINGLENGTH;
    
    /**
     * Number of segments to draw (used only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB lines).
     */
    protected int nsegs=-1;

    /**
     * For x-y and offset lines, there is the ability to put a curve
     * in the line.  This setting is the amount of an angle, limited
     * to a semi-circle (PI) that the curve will represent.  In other
     * words, the arc between the two end points is going to look like
     * a 0 degrees of a circle (straight line, which is the default),
     * or 180 degrees of a circle (full semi-circle).  Given in
     * radians, though, not degrees.  The ArcCalc object handles all
     * the details.
     */
    protected ArcCalc arc = null;

    public final static int STRAIGHT_LINE = 0;
    public final static int CURVED_LINE = 1;

    /** Generic constructor, attributes need to filled later. */
    public OMLine () {
	super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Create a line from lat lon points.
     *
     * @param lat_1 latitude of first point, decimal degrees.
     * @param lon_1 longitude of first point, decimal degrees.
     * @param lat_2 latitude of second point, decimal degrees.
     * @param lon_2 longitude of second point, decimal degrees.
     * @param lineType a choice between LINETYPE_STRAIGHT,
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB.
     */
    public OMLine(float lat_1, float lon_1, float lat_2, float lon_2, 
		  int lineType)	{
	this (lat_1, lon_1, lat_2, lon_2, lineType, -1);
    }

    /**
     * Create a line from lat lon points.
     *
     * @param lat_1 latitude of first point, decimal degrees.
     * @param lon_1 longitude of first point, decimal degrees.
     * @param lat_2 latitude of second point, decimal degrees.
     * @param lon_2 longitude of second point, decimal degrees.
     * @param lineType a choice between LINETYPE_STRAIGHT,
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB.
     * @param nsegs number of segment points (only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types, and if &lt;
     * 1, this value is generated internally)
     */
    public OMLine(float lat_1, float lon_1, float lat_2, float lon_2, 
		  int lineType, int nsegs) {
	super(RENDERTYPE_LATLON, lineType, DECLUTTERTYPE_NONE);
	latlons = new float[4];
	latlons[0] = lat_1;
	latlons[2] = lat_2;
	latlons[1] = lon_1;
	latlons[3] = lon_2;
	this.nsegs = nsegs;
    }

    /**
     * Create a line between two xy points on the window.
     * @param x1 the x location of the first point, in pixels from the
     * left of the window.
     * @param y1 the y location of the first point, in pixels from the
     * top of the window.
     * @param x2 the x location of the second point, in pixels from
     * the left of the window.
     * @param y2 the y location of the second point, in pixels from
     * the top of the window.
     */
    public OMLine(int x1, int y1, 
		  int x2, int y2) {
	super(RENDERTYPE_XY, LINETYPE_STRAIGHT, DECLUTTERTYPE_NONE);
	pts = new int[4];
	pts[0] = x1;
	pts[1] = y1;
	pts[2] = x2;
	pts[3] = y2;	
    }

    /**
     * Create a line between two x-y points on the window, where the
     * x-y points are offsets from a lat-lon point.  It assumes that
     * you'll want a straight window line between the points, so if
     * you don't, use the setLineType() method to change it.
     *
     * @param lat_1 the latitude of the reference point of the line,
     * in decimal degrees.
     * @param lon_1 the longitude of the reference point of the line,
     * in decimal degrees.
     * @param x1 the x location of the first point, in pixels from the
     * longitude point.
     * @param y1 the y location of the first point, in pixels from the
     * latitude point.
     * @param x2 the x location of the second point, in pixels from
     * the longitude point.
     * @param y2 the y location of the second point, in pixels from
     * the latitude point.
     */
    public OMLine(float lat_1, float lon_1, 
		  int x1, int y1, 
		  int x2, int y2) {

	super(RENDERTYPE_OFFSET, LINETYPE_STRAIGHT, DECLUTTERTYPE_NONE);
	latlons = new float[4];
	pts = new int[4];
        latlons[0] = lat_1;
        latlons[1] = lon_1;
	pts[0] = x1;
	pts[1] = y1;
	pts[2] = x2;
	pts[3] = y2;
    }

    /**
     * Set the lat lon values of the end points of the line from an
     * array of floats - lat1, lon1, lat2, lon2. This does not look at
     * the line render type, so it acts accordingly.  LL1 is only used
     * in RENDERTYPE_LATLON, RENDERTYPE_OFFSET, and LL2 is only used in
     * RENDERTYPE_LATLON.  
     * @param latlons array of floats - lat1, lon1, lat2, lon2 
     */
    public void setLL(float[] lls) {
	latlons = lls;
	setNeedToRegenerate(true);
    }

    /**
     * Get the lat lon values of the end points of the line in an
     * array of floats - lat1, lon1, lat2, lon2. Again, this does not
     * look at the line render type, so it acts accordingly.  LL1 is
     * only used in RENDERTYPE_LATLON, RENDERTYPE_OFFSET, and LL2 is
     * only used in RENDERTYPE_LATLON.
     * @return the lat lon array, and all are decimal degrees.
     */
    public float[] getLL() {
	return latlons;
    }

    /**
     * Set the xy values of the end points of the line from an array
     * of ints - x1, y1, x2, y2 .  This does not look at the line render
     * type, so it acts accordingly.  p1 and p2 are only used in
     * RENDERTYPE_XY, RENDERTYPE_OFFSET.
     * @param xys array of ints for the points - x1, y1, x2, y2
     */
    public void setPts(int[] xys) {
	pts = xys;
	setNeedToRegenerate(true);
    }

    /**
     * Get the xy values of the end points of the line in an array of
     * ints - x1, y1, x2, y2 . This does not look at the line render
     * type, so it acts accordingly.  p1 and p2 are only used in
     * RENDERTYPE_XY, RENDERTYPE_OFFSET.
     * @return the array of x-y points, and all are pixel values
     */
    public int[] getPts() {
        return pts;
    }

    /**
     * Check to see if this line is a polyline.
     * This is a polyline if it is LINETYPE_GREATCIRCLE or
     * LINETYPE_RHUMB for RENDERTYPE_LATLON polys.
     * @return true if polyline false if not
     */
    public boolean isPolyline() {
	return isPolyline;
    }

    /**
     * Set the number of segments of the lat/lon line.
     * (This is only for LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line
     * types, and if &lt; 1, this value is generated internally).
     * @param nsegs number of segment points
     */
    public void setNumSegs(int nsegs) {
	this.nsegs = nsegs;
    }

    /**
     * Get the number of segments of the lat/lon line.
     * (This is only for LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line
     * types).
     * @return int number of segment points
     */
    public int getNumSegs() {
	return nsegs;
    }

    /**
     * Turn the ArrowHead on/off.  The ArrowHead is placed on the
     * finishing end.
     * @param value on/off
     */
    public void addArrowHead(boolean value) {
        doArrowHead = value;
	if (doArrowHead) {
	    addArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD, 100);
	}
    }

    /**
     * Turn the ArrowHead on.  The ArrowHead is placed on the
     * finishing end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD),
     * beginning end (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or
     * both ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH). 
     * @param directionType which way to point the arrow head.
     */
    public void addArrowHead(int directionType) {
	addArrowHead(directionType, 100);
    }

    /**
     * Turn the ArrowHead on.  The ArrowHead is placed on the
     * finishing end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD),
     * beginning end (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or
     * both ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH). 
     * @param directionType which way to point the arrow head.
     * @param location where on the line to put the arrow head - 0 for
     * the starting point, 100 for the end.
     */
    public void addArrowHead(int directionType, int location) {
	addArrowHead(directionType, location, wingTip, wingLength); 
    }

    /**
     * Turn the ArrowHead on.  The ArrowHead is placed on the
     * finishing end (OMArrowHead.ARROWHEAD_DIRECTION_FORWARD),
     * beginning end (OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD), or
     * both ends(OMArrowHead.ARROWHEAD_DIRECTION_BOTH). 
     * @param directionType which way to point the arrow head.
     * @param location where on the line to put the arrow head - 0 for
     * the starting point, 100 for the end.
     * @param tipWidth the width factor for the base of the arrowhead,
     * on one side of the line. (Default is 5)
     * @param arrowLength the length factor of the arrowhead, from the
     * tip of the line to the base of the arrowhead. (Default is 20)
     */
    public void addArrowHead(int directionType, int location, int tipWidth, int arrowLength) {
	doArrowHead = true;

	arrowDirectionType = directionType;

	if (location < 1) arrowLocation = 1;
	else if (location > 100) arrowLocation = 100;
	else arrowLocation = location;

	wingTip = tipWidth;
	wingLength = arrowLength;

	if (wingTip <= 0 || wingLength <= 0) {
	    Debug.error("OMLine.addArrowHead: Bad parameters in for arrowhead width: " +
			tipWidth + ", or arrowhead length: " + arrowLength);
	    doArrowHead = false;
	}
    }

    /**
     * Arrowhead function, to find out the wing tip width.
     */
    public int getWingTip() {
	return wingTip;
    }

    /**
     * Arrowhead function, to find out the arrowhead length.
     */
    public int getWingLength() {
	return wingLength;
    }

    /**
     * Set the arc that is drawn between the points of a x-y or offset
     * line.  
     */
    public void setArc(ArcCalc ac) {
	arc = ac;
    }

    /**
     * Return the arc angle set for this line.  Will only be set if it
     * was set externally.
     * @return arc angle in radians.  
     */
    public ArcCalc getArc() {
	return arc;
    }

    /**
     * Prepare the line for rendering.
     * @param proj Projection
     * @return true if generate was successful */
    public boolean generate(Projection proj) {
	shape = null;

	if (proj == null) {
	    Debug.message("omgraphic", "OMLine: null projection in generate!");
	    return false;
	}

	// reset the internals
	isPolyline = false;

	switch (renderType) {
	case RENDERTYPE_XY:
	    if (arc != null) {
		xpoints = new int[1][];
		ypoints = new int[1][];
		arc.generate(pts[0], pts[1],
			     pts[2], pts[3]);
		xpoints[0] = arc.getXPoints();
		ypoints[0] = arc.getYPoints();
	    } else {
		xpoints = new int[1][2];
		ypoints = new int[1][2];

		if (pts == null) return false;

		xpoints[0][0] = pts[0];
		ypoints[0][0] = pts[1];
		xpoints[0][1] = pts[2];
		ypoints[0][1] = pts[3];
	    }
	    shape = createShape(xpoints[0], ypoints[0], false);
	    break;
	case RENDERTYPE_OFFSET:
	    if (!proj.isPlotable(latlons[0], latlons[1])) {
		setNeedToRegenerate(true);//HMMM not the best flag
		return false;
	    }
	    Point p1 = proj.forward(latlons[0], latlons[1]);
	    if (arc != null) {
		xpoints = new int[1][];
		ypoints = new int[1][];
		arc.generate(p1.x + pts[0], p1.y + pts[1],
			     p1.x + pts[2], p1.y + pts[3]);

		xpoints[0] = arc.getXPoints();
		ypoints[0] = arc.getYPoints();
	    } else {
		xpoints = new int[1][2];
		ypoints = new int[1][2];
		  
		xpoints[0][0] = p1.x + pts[0];
		ypoints[0][0] = p1.y + pts[1];
		xpoints[0][1] = p1.x + pts[2];
		ypoints[0][1] = p1.y + pts[3];
	    }
	    shape = createShape(xpoints[0], ypoints[0], false);
	    break;
	case RENDERTYPE_LATLON:
	    if (arc != null) {
		p1 = proj.forward(latlons[0], latlons[1]);
		Point p2 = proj.forward(latlons[2], latlons[3]);
		xpoints = new int[1][];
		ypoints = new int[1][];
		arc.generate(p1.x, p1.y, p2.x, p2.y);

		xpoints[0] = arc.getXPoints();
		ypoints[0] = arc.getYPoints();

		shape = createShape(xpoints[0], ypoints[0], false);

		isPolyline = true;
		
	    } else {
		ArrayList lines =
		    proj.forwardLine(
			new LatLonPoint(latlons[0], latlons[1]), 
			new LatLonPoint(latlons[2], latlons[3]), 
			lineType, nsegs);
		int size = lines.size();

		xpoints = new int[(int)(size/2)][0];
		ypoints = new int[xpoints.length][0];
		  
		for (int i=0, j=0; i<size; i+=2, j++) {
		    int[] xps = (int[])lines.get(i);
		    int[] yps = (int[])lines.get(i+1);

		    xpoints[j] = xps;
		    ypoints[j] = yps;

		    GeneralPath gp = createShape(xps, yps, false);
		    if (shape == null) {
			shape = gp;
		    } else {
			((GeneralPath)shape).append(gp, false);
		    }
		}
		isPolyline = (lineType != LINETYPE_STRAIGHT);
	    }
	    break;
	case RENDERTYPE_UNKNOWN:
	    System.err.println("OMLine.generate: invalid RenderType");
	    return false;
	}

	if (doArrowHead) {
 	    arrowhead = createArrowHeads();
	}

	if (Debug.debugging("arc") && arc != null) {
	    OMGraphicList arcGraphics = arc.getArcGraphics();
	    Debug.output("OMLine generating arcGraphics. " + arcGraphics);
	    arcGraphics.generate(proj);
	}

	setNeedToRegenerate(false);
	return true;
    }

    GeneralPath arrowhead = null;

    /**
     * This is a method that you can extend to create the GeneralPath
     * for the arrowheads, if you want a different way of doing it.
     * By default, it calls OMArrowHead.createArrowHeads(), using the
     * different arrowhead variables set in the OMLine.
     */
    public GeneralPath createArrowHeads() {
	return OMArrowHead.createArrowHeads(arrowDirectionType, arrowLocation, this, 
					    wingTip, wingLength);
    }

    /**
     * Paint the line.
     *
     * @param g Graphics context to render into
     */
    public void render(Graphics g) {

	if (!isRenderable()) {
	    return;
	}

	// Just to draw the matting for the arrowhead.  The matting
	// for the rest of the line will be taken care of in
	// super.render().
	if (arrowhead != null && isMatted() && 
	    g instanceof Graphics2D && stroke instanceof BasicStroke) {
	    ((Graphics2D)g).setStroke(new BasicStroke(((BasicStroke)stroke).getLineWidth() + 2f));
	    setGraphicsColor(g, Color.black);
	    ((Graphics2D)g).draw(arrowhead);
	}

	super.render(g);

	if (arrowhead != null) {
	    setGraphicsForEdge(g);
	    ((Graphics2D)g).fill(arrowhead);
	}

	if (Debug.debugging("arc") && arc != null) {
	    OMGraphicList arcGraphics = arc.getArcGraphics();
	    Debug.output("OMLine rendering " +  
			 arcGraphics.size() + 
			 " arcGraphics.");
	    arcGraphics.render(g);
	}

    }

    /**
     * The OMLine should never render fill.  It can think it does, if
     * the geometry turns out to be curved.  Returning false affects
     * distance() and contains() methods.
     */
    public boolean shouldRenderFill() {
	return false;
    }

    /**
     * This takes the area out of OMLines that may look like they have
     * area, depending on their shape.  Checks to see what
     * shouldRenderFill() returns (false by default) to decide how to
     * measure this.  If shouldRenderFill == true, the
     * super.contains() method is returned, which assumes the line
     * shape has area if it is curved.  Otherwise, it returns true if
     * the point is on the line.
     */
    public boolean contains(int x, int y) {
	if (shouldRenderFill()) {
	    return super.contains(x, y);
	} else {
	    return (distance(x, y) > 0);
	}
    }

}
