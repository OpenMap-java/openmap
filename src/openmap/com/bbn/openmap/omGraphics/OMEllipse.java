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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMEllipse.java,v $
// $RCSfile: OMEllipse.java,v $
// $Revision: 1.3 $
// $Date: 2005/01/10 16:58:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The OMEllipse is a lat/lon ellipse, made up of a center lat/lon
 * point, and some length described for the x and y axis. If you want
 * to create ellipses in X/Y space, use OMCircle. Ellipse arcs are not
 * available yet, and this class doesn't really work with the
 * EditableOMCircle. You can use EditableOMCircles to move and delete
 * OMEllipses, but you can't change the axis dimensions.
 */
public class OMEllipse extends OMCircle {

    protected double majorAxisSpan;
    protected double minorAxisSpan;
    protected float[] rawllpts;

    public OMEllipse(LatLonPoint centerPoint, double majorAxisSpan,
            double minorAxisSpan, Length units, double rotateAngle) {
        setRenderType(RENDERTYPE_LATLON);
        setLineType(LINETYPE_GREATCIRCLE);

        setCenter(centerPoint);
        setAxis(majorAxisSpan, minorAxisSpan, units);
        setRotationAngle(rotateAngle);
    }

    /**
     * Set the axis lengths of the ellipse.
     * 
     * @param majorAxis x direction of ellipse.
     * @param minorAxis y direction of ellipse.
     * @param units com.bbn.openmap.proj.Length object describing
     *        units of axis values.
     */
    public void setAxis(double majorAxis, double minorAxis, Length units) {
        if (units == null) {
            units = Length.RADIAN;
        }

        this.majorAxisSpan = units.toRadians(majorAxis);
        this.minorAxisSpan = units.toRadians(minorAxis);
        rawllpts = null;
        setNeedToRegenerate(true);
    }

    public void setCenter(LatLonPoint llp) {
        super.setCenter(llp);
        rawllpts = null;
    }

    /**
     * Get the x axis value.
     */
    public double getMajorAxis() {
        return majorAxisSpan;
    }

    /**
     * Get the y axis value.
     */
    public double getMinorAxis() {
        return minorAxisSpan;
    }

    /**
     * Get the float[] of points that make up the ellipse. In radians,
     * lat, lon, lat, lon, etc. May be null if generate hasn't been
     * called.
     */
    public float[] getLatLonPoints() {
        return rawllpts;
    }

    /**
     * Given that the center point and the axis are set, calculate the
     * new lat/lon points all around the ellipse from the center.
     */
    public float[] createLatLonPoints() {
        // First, need to calculate the lat/lon points for the
        // ellipse.
        int i;
        int nMax = 72;
        double angle = -Math.PI;
        double angleInc = 2.0 * Math.PI / nMax;
        double[] distance = new double[nMax + 1];
        double x;
        double y;
        double a;
        double b;
        float[] azimuth = new float[nMax + 1];
        float[] llPoints = new float[2 * (nMax + 1)];

        a = majorAxisSpan / 2.0;
        b = minorAxisSpan / 2.0;

        for (i = 0; i < nMax; i++) {

            x = Math.sqrt((a * a * b * b)
                    / ((b * b) + ((a * a) * Math.pow(Math.tan(angle), 2))));
            double yt = (x * x) / (a * a);
            if (yt > 1.0) {
                yt = 1.0;
            }
            y = Math.sqrt((1.0 - yt) * (b * b));

            distance[i] = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            azimuth[i] = (float) angle + com.bbn.openmap.MoreMath.HALF_PI
                    + (float) getRotationAngle();

            if (Debug.debugging("ellipse")) {
                Debug.output(" "
                        + i
                        + " "
                        + (azimuth[i] * 180 / Math.PI)
                        + " ( "
                        + distance[i]
                        + " ) "
                        + (Debug.debugging("ellipsedetail") ? ("[from x:" + x
                                + ", y:" + y + ", a:" + a + ", b:" + b + "]")
                                : ""));
            }
            angle += angleInc;
        }

        distance[nMax] = distance[0];
        azimuth[nMax] = azimuth[0];
        int nCounter = 0;

        for (i = 0; i < nMax + 1; i++) {

            LatLonPoint llPt = GreatCircle.spherical_between(center.radlat_,
                    center.radlon_,
                    (float) distance[i],
                    azimuth[i]);
            llPoints[nCounter++] = llPt.radlat_;
            llPoints[nCounter++] = llPt.radlon_;
        }

        return llPoints;
    }

    public boolean generate(Projection proj) {
        setShape(null);

        if (proj == null) {
            Debug.message("omgraphic",
                    "OMEllipse: null projection in generate!");
            return false;
        }


        if (rawllpts == null) {
            rawllpts = createLatLonPoints();
        }

        ArrayList vector = null;

        // polygon/polyline project the polygon/polyline.
        // Vertices should already be in radians.
        vector = proj.forwardPoly(rawllpts, getLineType(), -1, true);
        int size = vector.size();

        // We could call create shape, but this is more efficient.
        int i, j;
        for (i = 0, j = 0; i < size; i += 2, j++) {
            GeneralPath gp = createShape((int[]) vector.get(i),
                    (int[]) vector.get(i + 1),
                    true);

            if (shape == null) {
                setShape(gp);
            } else {
                ((GeneralPath) shape).append(gp, false);
            }
        }

        setNeedToRegenerate(false);
        return true;
    }
}