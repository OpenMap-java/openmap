//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMSpline.java,v $
//$RCSfile: OMSpline.java,v $
//$Revision: 1.11 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A splined OMPoly. With RENDERTYPE_LATLON mode, spline is computed on
 * geographic locations, and then projected. Very few changes from OMPoly source
 * code, just needed to calculate the spline before projecting in generate().
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
     * <li>llPoints array is converted into radians IN PLACE for more efficient
     * handling internally if it's not already in radians! For even better
     * performance, you should send us an array already in radians format!
     * <li>If you want the curve to be connected (as a potato), you need to
     * ensure that the first and last coordinate pairs are the same.
     * </ul>
     * 
     * @param llPoints array of lat/lon points, arranged lat, lon, lat, lon,
     *        etc.
     * @param units radians or decimal degrees. Use OMGraphic.RADIANS or
     *        OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     */
    public OMSpline(double[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
    }

    /**
     * Create an OMSpline from a list of float lat/lon pairs.
     * <p>
     * NOTES:
     * <ul>
     * <li>llPoints array is converted into radians IN PLACE for more efficient
     * handling internally if it's not already in radians! For even better
     * performance, you should send us an array already in radians format!
     * <li>If you want the curve to be connected (as a potato), you need to
     * ensure that the first and last coordinate pairs are the same.
     * </ul>
     * 
     * @param llPoints array of lat/lon points, arranged lat, lon, lat, lon,
     *        etc.
     * @param units radians or decimal degrees. Use OMGraphic.RADIANS or
     *        OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     * @param nsegs number of segment points (only for LINETYPE_GREATCIRCLE or
     *        LINETYPE_RHUMB line types, and if &lt; 1, this value is generated
     *        internally)
     */
    public OMSpline(double[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
    }

    /**
     * Create an OMSpline from a list of xy pairs. If you want the curve to be
     * connected, you need to ensure that the first and last coordinate pairs
     * are the same.
     * 
     * @param xypoints array of x/y points, arranged x, y, x, y, etc.
     */
    public OMSpline(int[] xypoints) {
        super(xypoints);
    }

    /**
     * Create an x/y OMSpline. If you want the curve to be connected, you need
     * to ensure that the first and last coordinate pairs are the same.
     * 
     * @param xPoints int[] of x coordinates
     * @param yPoints int[] of y coordinates
     */
    public OMSpline(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
    }

    /**
     * Create an x/y OMSpline at an offset from lat/lon. If you want the curve
     * to be connected, you need to ensure that the first and last coordinate
     * pairs are the same.
     * 
     * @param latPoint latitude in decimal degrees
     * @param lonPoint longitude in decimal degrees
     * @param xypoints int[] of x,y pairs
     * @param cMode offset coordinate mode
     */
    public OMSpline(double latPoint, double lonPoint, int[] xypoints, int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Create an x/y OMSpline at an offset from lat/lon. If you want the curve
     * to be connected, you need to ensure that the first and last coordinate
     * pairs are the same.
     * 
     * @param latPoint latitude in decimal degrees
     * @param lonPoint longitude in decimal degrees
     * @param xPoints int[] of x coordinates
     * @param yPoints int[] of y coordinates
     * @param cMode offset coordinate mode
     */
    public OMSpline(double latPoint, double lonPoint, int[] xPoints,
            int[] yPoints, int cMode) {
        super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * Prepare the spline for rendering.
     * 
     * @see com.bbn.openmap.omGraphics.OMGeometry#generate(Projection)
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {

        setNeedToRegenerate(true);

        if (proj == null) {
            Debug.message("omspline", "OMSpline: null projection in generate!");
            return false;
        }

        NatCubicSpline spline = isGeometryClosed() ? natCubicClosed : natCubic;
        // HACK : should use something else than nsegs
        spline.setSteps(nsegs);

        float[][] splinePoints;

        switch (renderType) {

        case RENDERTYPE_XY:
            if (xs == null) {
                Debug.message("omspline",
                        "OMSpline x/y rendertype null coordinates");
                setNeedToRegenerate(true);
                return false;
            }

            splinePoints = spline.calc(xs, ys);
            xpoints = new float[1][0];
            xpoints[0] = splinePoints[0];
            ypoints = new float[1][0];
            ypoints[0] = splinePoints[1];

            break;

        case RENDERTYPE_OFFSET:
            if (xs == null) {
                Debug.message("omspline",
                        "OMSpline offset rendertype null coordinates");
                setNeedToRegenerate(true);
                return false;
            }

            int npts = xs.length;
            float[] _x = new float[npts];
            float[] _y = new float[npts];

            // forward project the radian point
            Point origin = new Point();
            if (proj instanceof GeoProj) {
                ((GeoProj) proj).forward(lat, lon, origin, true);// radians
            } else {
                proj.forward(Math.toDegrees(lat), Math.toDegrees(lon), origin);
            }

            if (coordMode == COORDMODE_ORIGIN) {
                for (int i = 0; i < npts; i++) {
                    _x[i] = (float) (xs[i] + origin.getX());
                    _y[i] = (float) (ys[i] + origin.getY());
                }
            } else { // CModePrevious offset deltas
                _x[0] = xs[0] + origin.x;
                _y[0] = ys[0] + origin.y;

                for (int i = 1; i < npts; i++) {
                    _x[i] = xs[i] + _x[i - 1];
                    _y[i] = ys[i] + _y[i - 1];
                }
            }

            splinePoints = spline.calc(_x, _y);

            xpoints = new float[1][0];
            xpoints[0] = splinePoints[0];
            ypoints = new float[1][0];
            ypoints[0] = splinePoints[1];

            break;

        case RENDERTYPE_LATLON:
            if (rawllpts == null) {
                Debug.message("omspline",
                        "OMSpline latlon rendertype null coordinates");
                setNeedToRegenerate(true);
                return false;
            }

            // spline creation ; precision 1e-8 rad = 0.002"
            double[] splinellpts = spline.calc(rawllpts, 1e-8f);

            // polygon/polyline project the polygon/polyline.
            // Vertices should already be in radians.
            ArrayList<float[]> vector;
            if (proj instanceof GeoProj) {
                vector = ((GeoProj) proj).forwardPoly(splinellpts,
                        lineType,
                        nsegs,
                        isPolygon);
            } else {
                vector = proj.forwardPoly(rawllpts, isPolygon);
            }
            int size = vector.size();

            xpoints = new float[(int) (size / 2)][0];
            ypoints = new float[xpoints.length][0];

            for (int i = 0, j = 0; i < size; i += 2, j++) {
                xpoints[j] = vector.get(i);
                ypoints[j] = vector.get(i + 1);
            }

            if (!doShapes && size > 1) {
                setNeedToRegenerate(false);
                initLabelingDuringGenerate();
                setLabelLocation(xpoints[0], ypoints[0]);
                return true;
            }

            break;

        case RENDERTYPE_UNKNOWN:
            Debug.error("OMSpline.generate: invalid RenderType");
            return false;
        }

        setNeedToRegenerate(false);
        setShape(createShape());
        return true;
    }

    /**
     * Write this object to a stream.
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // Include the writeObject() method for consistency.
        // even though we only do the default serialization.
        // Its just good practice to have both writeObject()
        // and readObject() when performing custom serialization.
    }

    /**
     * Read this object from a stream.
     */
    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        // Instantiate the objects that are transient
        // These are not creating on the deserialization
        // candidate object.
        natCubic = new NatCubicSpline();
        natCubicClosed = new NatCubicClosedSpline();
    }

}
