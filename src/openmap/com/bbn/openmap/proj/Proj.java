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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Proj.java,v $
// $RCSfile: Proj.java,v $
// $Revision: 1.3 $
// $Date: 2003/07/28 20:05:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;


/**
 * Proj is the base class of all Projections.
 * <p>
 * You probably don't want to use this class unless you are hacking your own
 * projections, or need extended functionality.  To be safe you will want to
 * use the Projection interface.
 *
 * <h3>Notes:</h3>
 *
 * <ul>
 *
 * <li>We deal in radians internally.  The outside world usually deals
 * in decimal degrees.  If you have data in radians, DON'T bother
 * converting it into DD's since we'll convert it right back into radians for
 * the projection step.  For more optimization tips, see the OMPoly class.
 *
 * <li>We default to projecting our data using the WGS84 datum.  You
 * can change the appropriate parameters of the projection after
 * construction if you need to use a different datum.  And of course
 * you can derive your own projections from this class as you see fit.
 *
 * <li>The forward() and inverse() methods are currently implemented
 * using the algorithms given in John Synder's <i>Map Projections --A
 * Working Manual</i> for the sphere.  This is sufficient for display
 * purposes, but you should use ellipsoidal algorithms in the
 * GreatCircle class to calculate distance and azimuths on the
 * ellipsoid.  See each projection individually for more information.
 *
 * <li>This class is not thread safe.  If two or more threads are
 * using the same Proj, then they could disrupt each other.
 * Occasionally you may need to call a <code>set</code> method of this
 * class.  This might interfere with another thread that's using the
 * same projection for <code>forwardPoly</code> or another Projection
 * interface method.  In general, you should not need to call any of
 * the <code>set</code> methods directly, but let the MapBean do it
 * for you.
 *
 * <li>All the various <code>forwardOBJ()</code> methods for ArrayList
 * graphics ultimately go through <code>forwardPoly()</code>.
 *
 * </ul>
 *
 * @see Projection
 * @see Cylindrical
 * @see Mercator
 * @see CADRG
 * @see Azimuth
 * @see Orthographic
 * @see Planet
 * @see GreatCircle
 * @see com.bbn.openmap.omGraphics.OMPoly
 *
 */
public abstract class Proj implements Projection, Cloneable {
  
    // SOUTH_POLE <= phi <= NORTH_POLE   (radians)
    // -DATELINE <= lambda <= DATELINE   (radians)

    /**
     * North pole latitude in radians.
     */
    public final static transient float NORTH_POLE = ProjMath.NORTH_POLE_F;


    /**
     * South pole latitude in radians.
     */
    public final static transient float SOUTH_POLE = ProjMath.SOUTH_POLE_F;


    /**
     * Dateline longitude in radians.
     */
    public final static transient float DATELINE = ProjMath.DATELINE_F;


    /**
     * Minimum width of projection.
     */
    public final static transient int MIN_WIDTH = 10;	// pixels


    /**
     * Minimum height of projection.
     */
    public final static transient int MIN_HEIGHT = 10;	// pixels

    // Used for generating segments of ArrayList objects
    protected static transient int NUM_DEFAULT_CIRCLE_VERTS = 64;
    protected static transient int NUM_DEFAULT_GREAT_SEGS = 512;


    // pixels per meter (an extra scaling factor).
    protected int pixelsPerMeter = Planet.defaultPixelsPerMeter; // PPM
    protected float planetRadius = Planet.wgs84_earthEquatorialRadiusMeters;// EARTH_RADIUS
    protected float planetPixelRadius = planetRadius*pixelsPerMeter; // EARTH_PIX_RADIUS
    protected float planetPixelCircumference = MoreMath.TWO_PI*planetPixelRadius; // EARTH_PIX_CIRCUMFERENCE

    protected int width = 640, height = 480;
    protected float minscale = 1.0f;	// 1:minscale
    protected float maxscale = (float)planetPixelCircumference/(float)width;// good for cylindrical
    protected float scale = maxscale;
    protected float scaled_radius = planetPixelRadius/scale;
    protected float ctrLat=0.0f;		// center latitude in radians
    protected float ctrLon=0.0f;		// center longitude in radians
    protected int type = Mercator.MercatorType;	// Mercator is default
    protected String projID = null;		// identifies this projection
    protected Mercator mercator = null;		// for rhumbline calculations (if needed)
    public final static Color defaultBackgroundColor = new Color(191,239,255);
    protected Color backgroundColor = defaultBackgroundColor;

    /**
     * Construct a projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param w width of screen
     * @param h height of screen
     * @param type projection type
     * @see ProjectionFactory
     */
    public Proj(LatLonPoint center, float s, int w, int h, int type)
    {
	if (Debug.debugging("proj")) {
	    Debug.output("Proj()");
	}
	this.type = type;
	setParms(center, s, w, h);
	projID = null;

	// for rhumbline projecting
	if (!(this instanceof Mercator)) {
	    mercator = new Mercator(center, scale, width, height);
	}

	Color bc = Environment.getCustomBackgroundColor();
	if (bc != null) {
	    backgroundColor = bc;
	}
    }

    /**
     * Set the pixels per meter constant.
     * @param ppm int Pixels Per Meter scale-factor constant
     */
    public void setPPM(int ppm) {
	pixelsPerMeter = ppm;
	if (pixelsPerMeter < 1) {
	    pixelsPerMeter = 1;
	}
	computeParameters();
    }

    /**
     * Get the pixels-per-meter constant.
     * @return int Pixels Per Meter scale-factor constant
     */
    public int getPPM() {
	return pixelsPerMeter;
    }

    /**
     * Set the planet radius.
     * @param radius float planet radius in meters
     */
    public void setPlanetRadius(float radius) {
	planetRadius = radius;
	if (planetRadius < 1.0f) {
	    planetRadius = 1.0f;
	}
	computeParameters();
    }

    /**
     * Get the planet radius.
     * @return float radius of planet in meters
     */
    public float getPlanetRadius() {
	return planetRadius;
    }

    /**
     * Get the planet pixel radius.
     * @return float radius of planet in pixels 
     */
    public float getPlanetPixelRadius() {
	return planetPixelRadius;
    }

    /**
     * Get the planet pixel circumference.
     * @return float circumference of planet in pixels
     */
    public float getPlanetPixelCircumference() {
	return planetPixelCircumference;
    }

    /**
     * Set the scale of the projection.
     * <p>
     * Sets the projection to the scale 1:s iff minscale &lt; s &lt;
     * maxscale.<br>
     * If s &lt; minscale, sets the projection to minscale.<br>
     * If s &gt; maxscale, sets the projection to maxscale.<br>
     * @param s float scale
     */
    public void setScale(float s) {
	scale = s;
	if (scale < minscale) {
	    scale = minscale;
	    computeParameters();
	} else if (scale > maxscale) {
	    scale = maxscale;
	    computeParameters();
	}
	computeParameters();
	projID = null;
    }

    /**
     * Set the minscale of the projection.
     * <p>
     * Usually you will not need to do this.
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
     * <p>
     * @return float scale value
     */
    public float getScale() {
	return scale;
    }

    /**
     * Get the maximum scale of the projection.
     * <p>
     * @return float max scale value
     */
    public float getMaxScale() {
	return maxscale;
    }

    /**
     * Get minimum scale of the projection.
     * @return float min scale value
     */
    public float getMinScale() {
	return minscale;
    }

    /**
     * Set center point of projection.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     */
    public void setCenter(float lat, float lon) {
	ctrLat = normalize_latitude(ProjMath.degToRad(lat));
	ctrLon = wrap_longitude(ProjMath.degToRad(lon));
	computeParameters();
	projID = null;
    }

    /**
     * Set center point of projection.
     * @param pt LatLonPoint
     */
    public void setCenter(LatLonPoint pt) {
	setCenter(pt.getLatitude(), pt.getLongitude());
    }

    /**
     * Get center point of projection.
     * @return LatLonPoint center of projection
     */
    public LatLonPoint getCenter() {
	return new LatLonPoint(ctrLat, ctrLon, true);
    }

    /**
     * Set projection width.
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
     * @return width of projection screen
     */
    public int getWidth() {
	return width;
    }

    /**
     * Get projection height.
     * @return height of projection screen
     */
    public int getHeight() {
	return height;
    }

    /**
     * Sets all the projection variables at once before calling
     * computeParameters().
     * @param center LatLonPoint center
     * @param scale float scale
     * @param width width of screen
     * @param height height of screen
     */
    protected void setParms(
	LatLonPoint center, float scale, int width, int height)
    {
	ctrLat = normalize_latitude(center.radlat_);
	ctrLon = wrap_longitude(center.radlon_);

	this.scale = scale;
	if (this.scale < minscale) {
	    this.scale = minscale;
	} else if (this.scale > maxscale) {
	    this.scale = maxscale;
	}

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

	computeParameters();
    }


    /**
     * Gets the projection type.
     * @return int projection type
     */
    public int getProjectionType() {
	return type;
    }


    /**
     * Sets the projection ID used for determining equality.
     * The projection ID String is intern()ed for efficient comparison.
     */
    protected void setProjectionID() {
	projID = (":" + type + ":" + scale + ":" + ctrLat + ":" 
		  + ctrLon + ":" + width + ":" + height + ":").intern();
    }


    /**
     * Gets the projection ID used for determining equality.
     * @return the projection ID, as an intern()ed String
     */
    public String getProjectionID() {
	if (projID == null) setProjectionID();
	return projID;
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.
     */
    protected abstract void computeParameters();


    /**
     * Sets radian latitude to something sane.
     * <p>
     * Normalizes the latitude according to the particular projection.
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see ProjMath#normalize_latitude(float, float)
     * @see LatLonPoint#normalize_latitude(float)
     */
    public abstract float normalize_latitude(float lat);


    /**
     * Sets radian longitude to something sane.
     * @param lon float longitude in radians
     * @return float longitude (-PI &lt;= x &lt; PI)
     * @see ProjMath#wrap_longitude(float)
     * @see LatLonPoint#wrap_longitude(float)
     */
    public final static float wrap_longitude(float lon) {
	return ProjMath.wrap_longitude(lon);
    }


    /**
     * Stringify the projection.
     * @return stringified projection
     * @see #getProjectionID
     */
    public String toString() {
	return (" radius=" + planetRadius +
		" ppm=" + pixelsPerMeter + " center(" +
		ProjMath.radToDeg(ctrLat) + "," + ProjMath.radToDeg(ctrLon) +
		") scale=" + scale + " maxscale=" + maxscale +
		" minscale=" + minscale +
		" width=" + width + " height=" + height + "]");
    }


    /**
     * Test for equality.
     * @param p Object to compare.
     * @return boolean comparison
     */
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (o instanceof Projection)
	    return getProjectionID() == ((Projection)o).getProjectionID();
	return false;
    }


    /**
     * Return hashcode value of projection.
     * @return int hashcode
     */
    public int hashCode() {
	return getProjectionID().hashCode();
    }


    /**
     * Clone the projection.
     * @return Projection clone of this one.
     */
    public Projection makeClone() {
	return (Projection)this.clone();
    }


    /**
     * Copies this projection.
     * @return a copy of this projection.
     */
    public Object clone() {
	try {
	    Proj proj = (Proj)super.clone();
	    if (mercator != null) {
		proj.mercator = (Mercator)mercator.clone();
	    }
	    return proj;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }


    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * Call this to check and see if a LatLonPoint can be plotted.  This is
     * meant to be used for checking before projecting and rendering Point
     * objects (bitmaps for instance).
     * @param llpoint LatLonPoint
     * @return boolean
     */
    public boolean isPlotable(LatLonPoint llpoint) {
	return isPlotable(llpoint.getLatitude(), llpoint.getLongitude());
    }


    /**
     * Forward project a LatLonPoint.
     * <p>
     * Forward projects a LatLon point into XY space.  Returns a
     * Point.
     * @param point LatLonPoint
     * @param llp LatLonPoint to be projected
     * @return Point (new)
     */
    public final Point forward(LatLonPoint llp) {
	return forward(llp.radlat_, llp.radlon_, new Point(0,0), true);
    }


    /**
     * Forward project lat,lon coordinates.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return Point (new)
     */
    public final Point forward(float lat, float lon) {
	return forward(lat, lon, new Point(0,0));
    }


    /**
     * Inverse project a Point from x,y space to LatLon space.
     * @param point x,y Point
     * @return LatLonPoint (new)
     */
    public final LatLonPoint inverse(Point point) {
	return inverse(point, new LatLonPoint());
    }


    /**
     * Inverse project x,y coordinates.
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @return LatLonPoint (new)
     * @see #inverse(Point)
     */
    public final LatLonPoint inverse(int x, int y) {
	return inverse(x, y, new LatLonPoint());
    }


    /**
     * Forward project a line.
     * @param ll1 LatLonPoint
     * @param ll2 LatLonPoint
     * @param ltype LineType
     * @param nsegs number of segments
     * @return ArrayList
     */
    public ArrayList forwardLine(LatLonPoint ll1, LatLonPoint ll2,
			      int ltype, int nsegs) {
        float[] rawllpts = {ll1.radlat_, ll1.radlon_,
	     		    ll2.radlat_, ll2.radlon_};
        return forwardPoly(rawllpts, ltype, nsegs, false);
    }
  
  
    /**
     * Forward project a lat/lon Line.
     * @see #forwardLine(LatLonPoint, LatLonPoint, int, int)
     */
    public ArrayList forwardLine(LatLonPoint ll1, LatLonPoint ll2, int ltype) {
	return forwardLine(ll1, ll2, ltype, -1);
    }


    /**
     * Forward project a rectangle.
     * @param ll1 LatLonPoint
     * @param ll2 LatLonPoint
     * @param ltype LineType
     * @param nsegs number of segments
     * @param isFilled filled poly?
     * @return ArrayList
     */
    public ArrayList forwardRect(LatLonPoint ll1, LatLonPoint ll2,
			       int ltype, int nsegs, boolean isFilled) {
        float[] rawllpts = {ll1.radlat_, ll1.radlon_,
			    ll1.radlat_, ll2.radlon_,
			    ll2.radlat_, ll2.radlon_,
			    ll2.radlat_, ll1.radlon_,
			    // connect:
			    ll1.radlat_, ll1.radlon_};
	return forwardPoly(rawllpts, ltype, nsegs, isFilled);
    }


    public ArrayList forwardRect(
	    LatLonPoint ll1, LatLonPoint ll2, int ltype, int nsegs) {
	return forwardRect(ll1, ll2, ltype, nsegs, false);
    }

    /**
     * Forward project a lat/lon Rectangle.
     * @see #forwardRect(LatLonPoint, LatLonPoint, int, int)
     */
    public ArrayList forwardRect(LatLonPoint ll1, LatLonPoint ll2, int ltype) {
	return forwardRect(ll1, ll2, ltype, -1, false);
    }


    /**
     * Forward project an arc.
     * @param c LatLonPoint center
     * @param radians boolean radius in radians?
     * @param radius radius in radians or decimal degrees
     * @param start the starting angle of the arc, zero being North
     * up.  Units are dependent on radians parameter - the start
     * paramter is in radians if radians equals true, decimal degrees
     * if not.
     * @param extent the angular extent angle of the arc, zero being
     * no length.  Units are dependent on radians parameter -
     * the extent paramter is in radians if radians equals true,
     * decimal degrees if not.
     */
    public ArrayList forwardArc(LatLonPoint c, boolean radians, float radius,
                                float start, float extent) {
        return forwardArc(c, radians, radius, -1, start, extent, 
			  java.awt.geom.Arc2D.OPEN);
    }
    public ArrayList forwardArc(
            LatLonPoint c, boolean radians, float radius, int nverts,
            float start, float extent) {
        return forwardArc(c, radians, radius, nverts, start, extent, 
			  java.awt.geom.Arc2D.OPEN);
    }

    /**
     * Forward project a Lat/Lon Arc.
     * <p>
     * Arcs have the same restrictions as <a href="#poly_restrictions">
     * polys</a>.
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @param start the starting angle of the arc, zero being North
     * up.  Units are dependent on radians parameter - the start
     * paramter is in radians if radians equals true, decimal degrees
     * if not.
     * @param extent the angular extent angle of the arc, zero being
     * no length.  Units are dependent on radians parameter -
     * the extent paramter is in radians if radians equals true,
     * decimal degrees if not.
     * @param arcType type of arc to create - see java.awt.geom.Arc2D
     * for (OPEN, CHORD, PIE).  Arc2D.OPEN means that the just the
     * points for the curved edge will be provided.  Arc2D.PIE means
     * that addition lines from the edge of the curve to the center
     * point will be added.  Arc2D.CHORD means a single line from each
     * end of the curve will be drawn.
     */
    public ArrayList forwardArc(
            LatLonPoint c, boolean radians, float radius, int nverts,
            float start, float extent, int arcType)
    {
        // HACK-need better decision for number of vertices.
        if (nverts < 3)
            nverts = NUM_DEFAULT_CIRCLE_VERTS;

        float[] rawllpts;

	switch (arcType) {
	case Arc2D.PIE:
	    rawllpts = new float[(nverts<<1)+4];//*2 for pairs +4 connect
	    break;
	case Arc2D.CHORD:
	    rawllpts = new float[(nverts<<1)+2];//*2 for pairs +2 connect
	    break;
	default:
	    rawllpts = new float[(nverts<<1)];//*2 for pairs, no connect
	}

        GreatCircle.earth_circle(
                c.radlat_, c.radlon_,
                (radians) ? radius : ProjMath.degToRad(radius),
                (radians) ? start : ProjMath.degToRad(start),
                (radians) ? extent : ProjMath.degToRad(extent),
                nverts, rawllpts);

	int linetype = LineType.Straight;
	boolean isFilled = false;

	switch (arcType) {
	case Arc2D.PIE:
	    rawllpts[rawllpts.length-4] = c.radlat_;
	    rawllpts[rawllpts.length-3] = c.radlon_;
	case Arc2D.CHORD:
	    rawllpts[rawllpts.length-2] = rawllpts[0];
	    rawllpts[rawllpts.length-1] = rawllpts[1];
	    linetype = LineType.GreatCircle;
	    isFilled = true;
	    break;
	default:
	    // Don't need to do anything, defaults already set.
	}

        // forward project the arc-poly.
	return forwardPoly(rawllpts, linetype, -1, isFilled);
    }

    /**
     * Forward project a circle.
     * @param c LatLonPoint center
     * @param radians boolean radius in radians?
     * @param radius radius in radians or decimal degrees
     */
    public ArrayList forwardCircle(LatLonPoint c, boolean radians, float radius) {
	return forwardCircle(c, radians, radius, -1, false);
    }
    public ArrayList forwardCircle(
	    LatLonPoint c, boolean radians, float radius, int nverts) {
	return forwardCircle(c, radians, radius, nverts, false);
    }


    /**
     * Forward project a Lat/Lon Circle.
     * <p>
     * Circles have the same restrictions as <a href="#poly_restrictions">
     * polys</a>.
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @param isFilled filled poly?
     */
    public ArrayList forwardCircle(
	    LatLonPoint c, boolean radians, float radius, 
	    int nverts, boolean isFilled)
    {
	// HACK-need better decision for number of vertices.
	if (nverts < 3)
	    nverts = NUM_DEFAULT_CIRCLE_VERTS;

	float[] rawllpts = new float[(nverts<<1)+2];//*2 for pairs +2 connect
	GreatCircle.earth_circle(
		c.radlat_, c.radlon_,
		(radians) ? radius : ProjMath.degToRad(radius),
		nverts, rawllpts);
	// connect the vertices.
	rawllpts[rawllpts.length-2] = rawllpts[0];
	rawllpts[rawllpts.length-1] = rawllpts[1];

	// forward project the circle-poly
	return forwardPoly(rawllpts, LineType.Straight, -1, isFilled);
    }


    //HACK
    protected transient static int XTHRESHOLD = 16384;//half range
    protected transient int XSCALE_THRESHOLD = 1000000;//dynamically calculated

    public ArrayList forwardPoly(
	float[] rawllpts, int ltype, int nsegs) {
	return forwardPoly(rawllpts, ltype, nsegs, false);
    }

    /**
     * Forward project a lat/lon Poly.
     * <p>
     * Delegates to _forwardPoly(), and may do additional clipping for Java
     * XWindows problem.  Remember to specify vertices in radians!
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see #forwardRaw
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     */
    public ArrayList forwardPoly(
	float[] rawllpts, int ltype, int nsegs, boolean isFilled)
    {
	ArrayList stuff = _forwardPoly(rawllpts, ltype, nsegs, isFilled);
	// @HACK: workaround XWindows bug.  simple clip to a boundary.  this
	// is ugly.
	if (Environment.doingXWindowsWorkaround && (scale <= XSCALE_THRESHOLD)) {
	    int i, j, size = stuff.size();
	    int[] xpts, ypts;
	    for (i=0; i<size; i+=2) {
		xpts = (int[])stuff.get(i);
		ypts = (int[])stuff.get(i+1);
		for (j=0; j<xpts.length; j++) {
		    if (xpts[j] <= -XTHRESHOLD) {
			xpts[j] = -XTHRESHOLD;
		    }
		    else
		    if (xpts[j] >= XTHRESHOLD) {
			xpts[j] = XTHRESHOLD;
		    }
		    if (ypts[j] <= -XTHRESHOLD) {
			ypts[j] = -XTHRESHOLD;
		    }
		    else
		    if (ypts[j] >= XTHRESHOLD) {
			ypts[j] = XTHRESHOLD;
		    }
		}
		stuff.set(i, xpts);
		stuff.set(i+1, ypts);
	    }
	}
	return stuff;
    }


    /**
     * Forward project a lat/lon Poly.
     * Remember to specify vertices in radians!
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     */
    protected abstract ArrayList _forwardPoly(
	float[] rawllpts, int ltype, int nsegs, boolean isFilled);


    /**
     * Forward project a rhumbline poly.
     * <p>
     * Draws rhumb lines between vertices of poly.  Remember to
     * specify vertices in radians!  Check in-code comments for
     * details about the algorithm.
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle
     * or rhumb lines (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see Projection#forwardPoly(float[], int, int, boolean)
     */
    protected ArrayList forwardRhumbPoly(float[] rawllpts, int nsegs, boolean isFilled) {

	// IDEA:
	//	Rhumblines are straight in the Mercator projection.
	//	So we can use the Mercator projection to calculate
	//	vertices along the rhumbline between two points.  But
	//	if there's a better way to calculate loxodromes,
	//	someone please chime in (openmap@bbn.com)...
	//
	// ALG:
	//	Project pairs of vertices through the Mercator
	//	projection into screen XY space, pick intermediate
	//	segment points along the straight XY line, then
	//	convert all vertices back into LatLon space.  Pass the
	//	augmented vertices to _forwardPoly() to be drawn as
	//	straight segments.
	//
	// WARNING:
	//	The algorithm fixes the Cylindrical-wrapping
	//	problem, and thus duplicates some code in
	//	Cylindrical._forwardPoly()
	//
	if (this instanceof Mercator) {//simple
	    return _forwardPoly(
		    rawllpts, LineType.Straight, nsegs, isFilled);
	}

	int i, n, xp, flag=0, xadj=0, totalpts=0;
	Point from = new Point(0,0);
	Point to = new Point(0,0);
	LatLonPoint llp=null;
	int len = rawllpts.length;

	float[][] augllpts = new float[len>>>1][0];

	// lock access to object global, since this is probably not
	// cloned and different layers may be accessing this.
//    	synchronized (mercator) {

	// we now create a clone of the mercator variable in
	// makeClone(), so since different objects should be using
	// their clone instead of the main projection, the
	// synchronization should be unneeded.

	    // use mercator projection to calculate rhumblines.
//  	    mercator.setParms(
//  		    new LatLonPoint(ctrLat, ctrLon, true),
//  		    scale, width, height);

	    // Unnecessary to set parameters !! ^^^^^

	    // project everything through the Mercator projection,
	    // building up lat/lon points along the original rhumb
	    // line between vertices.
	    mercator.forward(rawllpts[0], rawllpts[1], from, true);
	    xp = from.x;
	    for (i=0, n=2; n<len; i++, n+=2) {
		mercator.forward(rawllpts[n], rawllpts[n+1], to, true);
		// segment crosses longitude along screen edge
		if (Math.abs(xp - to.x) >= mercator.half_world) {
		    flag += (xp < to.x) ? -1 : 1;//inc/dec the wrap count
		    xadj = flag * mercator.world.x;//adjustment to x coordinates
//		    Debug.output("flag=" + flag + " xadj=" + xadj);
		}
		xp = to.x;
		if (flag != 0) {
		    to.x += xadj;//adjust x coordinate
		}

		augllpts[i] = mercator.rhumbProject(from, to, false, nsegs);
		totalpts+=augllpts[i].length;
		from.x = to.x; from.y = to.y;
	    }
	    llp = mercator.inverse(from);
//    	}// end synchronized around mercator

	augllpts[i] = new float[2];
	augllpts[i][0] = llp.radlat_;
	augllpts[i][1] = llp.radlon_;
	totalpts+=2;

	// put together all the points
	float[] newllpts = new float[totalpts];
	int pos=0;
	for (i=0; i<augllpts.length; i++) {
//	    Debug.output("copying " + augllpts[i].length + " floats");
	    System.arraycopy(
		    /*src*/augllpts[i], 0,
		    /*dest*/newllpts, pos, augllpts[i].length);
	    pos+=augllpts[i].length;
	}
//	Debug.output("done copying " + totalpts + " total floats");

	// free unused variables
	augllpts = null;

	// now delegate the work to the regular projection code.
	return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }


    /**
     * Forward project a greatcircle poly.
     * <p>
     * Draws great circle lines between vertices of poly.  Remember to
     * specify vertices in radians!
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle or
     * rhumb lines (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see Projection#forwardPoly(float[], int, int, boolean)
     */
    protected ArrayList forwardGreatPoly(float[] rawllpts, int nsegs, boolean isFilled) {
 	int i, j, k, totalpts=0;

	Point from = new Point();
	Point to = new Point();

	int end = rawllpts.length>>>1;
	float[][] augllpts = new float[end][0];
	end-=1;//stop before last segment

	// calculate extra vertices between all the original segments.
	forward(rawllpts[0], rawllpts[1], from, true);
	for (i=0, j=0, k=2; i<end; i++, j+=2, k+=2) {
	    forward(rawllpts[k], rawllpts[k+1], to, true);
	    augllpts[i] = getGreatVertices(
		    rawllpts[j], rawllpts[j+1], rawllpts[k], rawllpts[k+1],
		    from, to, false, nsegs);
	    from.x = to.x; from.y = to.y;
	    totalpts += augllpts[i].length;
	}
	augllpts[i] = new float[2];
	augllpts[i][0] = rawllpts[j];
	augllpts[i][1] = rawllpts[j+1];
	totalpts += 2;

	// put together all the points
	float[] newllpts = new float[totalpts];
	int pos=0;
	for (i=0; i<augllpts.length; i++) {
	    System.arraycopy(
		    /*src*/augllpts[i], 0,
		    /*dest*/newllpts, pos, augllpts[i].length);
	    pos+=augllpts[i].length;
	}

	// free unused variables
	augllpts = null;

	// now delegate the work to the regular projection code.
	return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }


    /**
     * Get the vertices along the great circle between two points.
     * @param latp previous float latitude
     * @param lonp previous float longitude
     * @param latn next float latitude
     * @param lonn next float longitude
     * @param from Point
     * @param to Point
     * @param include_last include n or n+1 points of the n segments?
     * @return float[] lat/lon points in RADIANS!
     *
     */
    private float[] getGreatVertices(
	    float latp, float lonp, float latn, float lonn,
	    Point from, Point to, boolean include_last, int nsegs)
    {
	if (nsegs < 1) {
	    // calculate pixel distance
	    int dist = DrawUtil.pixel_distance(from.x, from.y, to.x, to.y);

	    // determine what would be a decent number of segments to draw.
	    // HACK: this is hardcoded calculated by what might look ok on
	    // screen.  We also put a cap on the number of extra segments we
	    // draw.
	    nsegs = dist>>3;// dist/8
	    if (nsegs == 0) {
		nsegs = 1;
	    }
	    else if (nsegs > NUM_DEFAULT_GREAT_SEGS) {
		nsegs = NUM_DEFAULT_GREAT_SEGS;
	    }

//	    Debug.output(
//		    "("+from.x+","+from.y+")("+to.x+","+to.y+") dist="+dist+" nsegs="+nsegs);
	}

	// both of these return float[] radian coordinates!
	float[] radpts;
	return GreatCircle.great_circle(
		latp, lonp, latn, lonn, nsegs, include_last);
    }


    /**
     * Forward projects a raster.
     * <p>
     * HACK: not implemented yet.
     * @param llNW LatLonPoint of NorthWest corner of Image
     * @param llSE LatLonPoint of SouthEast corner of Image
     * @param image raster image
     */
    public ArrayList forwardRaster(LatLonPoint llNW, LatLonPoint llSE, Image image) {
	Debug.error("Proj.forwardRaster(): unimplemented!");
	return null;
    }


    /**
     * Check for complicated linetypes.
     * <p>
     * This depends on the line and this projection.
     * @param ltype int LineType
     * @return boolean
     */
    public boolean isComplicatedLineType(int ltype) {
	switch (ltype) {
	  case LineType.Straight:
	      return false;
	  case LineType.Rhumb:
	      return (getProjectionType() == Mercator.MercatorType) ? false : true;
	  case LineType.GreatCircle:
	      return true/*(getProjectionType() == Gnomonic.GnomonicType) ? false : true*/;
	  default:
	      Debug.error(
		  "Proj.isComplicatedLineType: invalid LineType!");
	      return false;
	}
    }

    /**
     * Generates a complicated poly.
     * @param llpts LatLonPoint[]
     * @param ltype line type
     * @param connect polygon or polyline
     * @param nsegs number of segments to draw for greatcircle or
     * rhumb lines (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList
     * @see Projection#forwardPoly
     */ 
    protected ArrayList doPolyDispatch(
	float[] rawllpts, int ltype, int nsegs, boolean isFilled)
    {
	switch (ltype) {
	  case LineType.Rhumb:
	      return forwardRhumbPoly(rawllpts, nsegs, isFilled);
	  case LineType.GreatCircle:
	      return forwardGreatPoly(rawllpts, nsegs, isFilled);
	  case LineType.Straight:
	      Debug.error(
		  "Proj.doPolyDispatch: Bad Dispatch!\n");
	      return new ArrayList(0);
	  default:
	      Debug.error(
		  "Proj.doPolyDispatch: Invalid LType!\n");
	      return new ArrayList(0);
	}
    }


    /**
     * Pan the map/projection.
     * <p>
     * Example pans:
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
    public void pan(float Az, float c) {
	setCenter(GreatCircle.spherical_between(
		    ctrLat, ctrLon, ProjMath.degToRad(c),
		    ProjMath.degToRad(Az)));
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
     * Draw the background for the projection.
     * @param g Graphics
     *
     */
    abstract public void drawBackground(Graphics g);


    /**
     * Draw the background for the projection.
     * @param g Graphics2D
     * @param p Paint to use for the background
     */
    abstract public void drawBackground(Graphics2D g, Paint p);

    /**
     * Get the background color.
     * @return Color
     */
    public Color getBackgroundColor() {
	return backgroundColor;
    }


    /**
     * Set the background color.
     * @param color Color
     */
    public void setBackgroundColor(Color color) {
	backgroundColor = color;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
	return "Proj";
    }

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
    public float getScale(LatLonPoint ll1, LatLonPoint ll2, 
			  Point point1, Point point2) {

	try {
	    
	    float deltaDegrees;
	    float pixPerDegree;
	    int deltaPix;
	    float dx = Math.abs(point2.x - point1.x);
	    float dy = Math.abs(point2.y - point1.y);
	
	    if (dx < dy) {
		float dlat = Math.abs(ll1.getLatitude() - ll2.getLatitude());
		deltaDegrees = dlat;
		deltaPix = getHeight();

		// This might not be correct for all projection types
		pixPerDegree = getPlanetPixelCircumference()/360f;
	    } else {
		float dlon;
		float lat1, lon1, lon2;
		
		// point1 is to the right of point2. switch the
		// LatLonPoints so that ll1 is west (left) of ll2.
		if (point1.x > point2.x) {
		    lat1 = ll1.getLatitude();
		    lon1 = ll1.getLongitude();
		    ll1.setLatLon(ll2);
		    ll2.setLatLon(lat1, lon1);
		}
		
		lon1 = ll1.getLongitude();
		lon2 = ll2.getLongitude();
		
		// allow for crossing dateline
		if (lon1 > lon2) {
		    dlon = (180-lon1) + (180+lon2);
		} else {
		    dlon = lon2-lon1;
		}
		
		deltaDegrees = dlon;
		deltaPix = getWidth();

		// This might not be correct for all projection types
		pixPerDegree = getPlanetPixelCircumference()/360f;
	    }

	    // The new scale...
	    return pixPerDegree/(deltaPix/deltaDegrees);
	} catch (NullPointerException npe) {
	    com.bbn.openmap.util.Debug.error("ProjMath.getScale(): caught null pointer exception.");
	    return Float.MAX_VALUE;
	}
    }
}
