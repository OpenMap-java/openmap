package com.bbn.openmap.omGraphics;

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A splined OMPoly.
 * With RENDERTYPE_LATLON mode, spline is computed on
 * geographic locations, and then projected.
 * Very few changes from OMPoly source code, just needed to
 * calculate the spline before projecting in generate().
 * Look for HACK in source code ...
 * 
 * @author Eric LEPICIER
 * @version 15 juil. 2002
 */
public class OMSpline extends OMPoly {

    private transient NatCubicSpline natCubic = new NatCubicSpline();
    private transient NatCubicClosedSpline natCubicClosed = new NatCubicClosedSpline();

    /**
     * Default constructor.
     */
    public OMSpline() {
	super();
    }

    /**
     * Create an OMSpline from a list of float lat/lon pairs.
     * <p>
     * NOTES:
     * <ul>
     * <li>llPoints array is converted into radians IN PLACE for more
     * efficient handling internally if it's not already in radians!
     * For even better performance, you should send us an array
     * already in radians format!
     * <li>If you want the curve to be connected (as a potato), you
     * need to ensure that the first and last coordinate pairs are the
     * same.
     * </ul>
     *
     * @param llPoints array of lat/lon points, arranged lat, lon, 
     * lat, lon, etc.
     * @param units radians or decimal degrees.  Use OMGraphic.RADIANS 
     * or OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     */
    public OMSpline(float[] llPoints, int units, int lType) {
	super(llPoints, units, lType);
    }

    /**
     * Create an OMSpline from a list of float lat/lon pairs.
     * <p>
     * NOTES:
     * <ul>
     * <li>llPoints array is converted into radians IN PLACE for more
     * efficient handling internally if it's not already in radians!
     * For even better performance, you should send us an array
     * already in radians format!
     * <li>If you want the curve to be connected (as a potato), you
     * need to ensure that the first and last coordinate pairs are the
     * same.
     * </ul>
     *
     * @param llPoints array of lat/lon points, arranged lat, lon,
     * lat, lon, etc.
     * @param units radians or decimal degrees.  Use OMGraphic.RADIANS
     * or OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     * @param nsegs number of segment points (only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types, and if &lt;
     * 1, this value is generated internally)
     */
    public OMSpline(float[] llPoints, int units, int lType, int nsegs) {
	super(llPoints, units, lType, nsegs);
    }

    /**
     * Create an OMSpline from a list of xy pairs.
     * If you want the curve to be connected, you need to ensure that
     * the first and last coordinate pairs are the same.
     *
     * @param xypoints array of x/y points, arranged x, y, x, y, etc.
     */
    public OMSpline(int[] xypoints) {
	super(xypoints);
    }

    /**
     * Create an x/y OMSpline.
     * If you want the curve to be connected, you need to ensure that
     * the first and last coordinate pairs are the same.
     *
     * @param xPoints int[] of x coordinates
     * @param yPoints int[] of y coordinates
     */
    public OMSpline(int[] xPoints, int[] yPoints) {
	super(xPoints, yPoints);
    }

    /**
     * Create an x/y OMSpline at an offset from lat/lon.
     * If you want the curve to be connected, you need to ensure that
     * the first and last coordinate pairs are the same.
     *
     * @param latPoint latitude in decimal degrees
     * @param lonPoint longitude in decimal degrees
     * @param xypoints int[] of x,y pairs
     * @param cmode offset coordinate mode
     */
    public OMSpline(
	float latPoint,
	float lonPoint,
	int[] xypoints,
	int cMode) {
	super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Create an x/y OMSpline at an offset from lat/lon.
     * If you want the curve to be connected, you need to ensure that
     * the first and last coordinate pairs are the same.
     *
     * @param latPoint latitude in decimal degrees
     * @param lonPoint longitude in decimal degrees
     * @param xPoints int[] of x coordinates
     * @param yPoints int[] of y coordinates
     * @param cmode offset coordinate mode
     */
    public OMSpline(
	float latPoint,
	float lonPoint,
	int[] xPoints,
	int[] yPoints,
	int cMode) {
	super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * Prepare the spline for rendering.
     * @see com.bbn.openmap.omGraphics.OMGeometry#generate(Projection)
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
	int i, j, npts;
	shape = null;

	if (proj == null) {
	    Debug.message("omspline", "OMSpline: null projection in generate!");
	    return false;
	}

	NatCubicSpline spline = isGeometryClosed() ? natCubicClosed : natCubic;

	// HACK : should use something else than nsegs
	spline.setSteps(nsegs);

	int[][] splinePoints;

	switch (renderType) {

	case RENDERTYPE_XY :
	    if (xs == null) {
		Debug.message("omspline", "OMSpline x/y rendertype null coordinates");
		setNeedToRegenerate(true);
		return false;
	    }

	    // spline creation
	    splinePoints = spline.calc(xs, ys);

	    // Need to keep these around for the LabeledOMSpline
	    xpoints = new int[1][0];
	    xpoints[0] = splinePoints[0];
	    ypoints = new int[1][0];
	    ypoints[0] = splinePoints[1];

	    if (doShapes) {
		setNeedToRegenerate(false);
		createShape();
	    }
	    break;

	case RENDERTYPE_OFFSET :
	    if (xs == null) {
		Debug.message("omspline", "OMSpline offset rendertype null coordinates");
		setNeedToRegenerate(true);
		return false;
	    }

	    npts = xs.length;
	    int[] _x = new int[npts];
	    int[] _y = new int[npts];

	    // forward project the radian point
	    Point origin = proj.forward(lat, lon, new Point(0, 0), true);
	    //radians

	    if (coordMode == COORDMODE_ORIGIN) {
		for (i = 0; i < npts; i++) {
		    _x[i] = xs[i] + origin.x;
		    _y[i] = ys[i] + origin.y;
		}
	    }
	    else { // CModePrevious offset deltas
		_x[0] = xs[0] + origin.x;
		_y[0] = ys[0] + origin.y;

		for (i = 1; i < npts; i++) {
		    _x[i] = xs[i] + _x[i - 1];
		    _y[i] = ys[i] + _y[i - 1];
		}
	    }

	    // spline creation
	    splinePoints = spline.calc(_x, _y);

	    // Need to keep these around for the LabeledOMSpline
	    xpoints = new int[1][0];
	    xpoints[0] = splinePoints[0];
	    ypoints = new int[1][0];
	    ypoints[0] = splinePoints[1];

	    if (doShapes) {
		setNeedToRegenerate(false);
		createShape();
	    }
	    break;

	case RENDERTYPE_LATLON :
	    if (rawllpts == null) {
		Debug.message("omspline", "OMSpline latlon rendertype null coordinates");
		setNeedToRegenerate(true);
		return false;
	    }

	    // spline creation ; precision 1e-8 rad = 0.002"
	    float[] splinellpts = spline.calc(rawllpts, 1e-8f);

	    // polygon/polyline project the polygon/polyline.
	    // Vertices should already be in radians.
	    ArrayList vector =
		proj.forwardPoly(splinellpts, lineType, nsegs, isPolygon);
	    int size = vector.size();

	    if (!doShapes) {
		xpoints = new int[(int) (size / 2)][0];
		ypoints = new int[xpoints.length][0];
	    }

	    // We could call create shape, but this is more efficient.

	    for (i = 0, j = 0; i < size; i += 2, j++) {
		if (doShapes) {
		    GeneralPath gp =
			createShape(
			    (int[]) vector.get(i),
			    (int[]) vector.get(i + 1),
			    isPolygon);

		    if (shape == null) {
			shape = gp;
		    }
		    else {
			((GeneralPath) shape).append(gp, false);
		    }
		}
		else {
		    xpoints[j] = (int[]) vector.get(i);
		    ypoints[j] = (int[]) vector.get(i + 1);
		}
	    }
	    break;

	case RENDERTYPE_UNKNOWN :
	    Debug.error("OMSpline.generate: invalid RenderType");
	    return false;
	}

	setNeedToRegenerate(false);
	return true;
    }

}
