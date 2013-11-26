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
// $Revision: 1.7 $
// $Date: 2009/02/25 22:34:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The OMEllipse is a lat/lon ellipse, made up of a center lat/lon point, and
 * some length described for the x and y axis. If you want to create ellipses in
 * X/Y space, use OMCircle. Ellipse arcs are not available yet, and this class
 * doesn't really work with the EditableOMCircle. You can use EditableOMCircles
 * to move and delete OMEllipses, but you can't change the axis dimensions.
 */
public class OMEllipse extends OMCircle {

    protected double majorAxisSpan;
    protected double minorAxisSpan;
    protected transient double[] rawllpts;

    /**
     * Create a OMEllipse, positioned with a lat-lon center and a lat-lon axis.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param centerPoint latitude/longitude of center point, decimal degrees
     * @param majorAxisSpan horizontal diameter of circle/ellipse, pixels
     * @param minorAxisSpan vertical diameter of circle/ellipse, in given units
     * @param units com.bbn.openmap.proj.Length object.
     * @param rotateAngle angle of rotation in Radians
     */
    public OMEllipse(LatLonPoint centerPoint, double majorAxisSpan,
            double minorAxisSpan, Length units, double rotateAngle) {
        setRenderType(RENDERTYPE_LATLON);
        setLineType(LINETYPE_GREATCIRCLE);

        setCenter(centerPoint);
        setAxis(majorAxisSpan, minorAxisSpan, units);
        setRotationAngle(rotateAngle);
    }

    /**
     * Create a OMEllipse, positioned with a x-y center with x-y axis.
     * Rendertype is RENDERTYPE_XY.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param majorAxisSpan horizontal diameter of circle/ellipse, pixels
     * @param minorAxisSpan vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians
     */
    public OMEllipse(int x1, int y1, int majorAxisSpan, int minorAxisSpan,
            double rotateAngle) {
        super(x1, y1, majorAxisSpan, minorAxisSpan);
        setRotationAngle(rotateAngle);
    }

    /**
     * Create a OMEllipse, positioned with a lat-lon center and x-y axis.
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param centerPoint latitude/longitude of center point, decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians
     */
    public OMEllipse(LatLonPoint centerPoint, int w, int h, double rotateAngle) {
        // Use circle constructor
        super(centerPoint.getY(), centerPoint.getX(), 0, 0, w, h);
        setRotationAngle(rotateAngle);
    }

    /**
     * Create a OMEllipse, positioned at a Lat-lon location, x-y offset, x-y
     * axis. Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param centerPoint latitude/longitude of center point, decimal degrees
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of circle/ellipse, pixels.
     * @param h vertical diameter of circle/ellipse, pixels.
     */
    public OMEllipse(LatLonPoint centerPoint, int offset_x1, int offset_y1,
            int w, int h, double rotateAngle) {
        super(centerPoint.getY(),
              centerPoint.getX(),
              offset_x1,
              offset_y1,
              w,
              h);
        setRotationAngle(rotateAngle);
    }

    /**
     * Set the axis lengths of the ellipse.
     * 
     * @param majorAxis x direction of ellipse.
     * @param minorAxis y direction of ellipse.
     * @param units com.bbn.openmap.proj.Length object describing units of axis
     *        values.
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
     * Get the float[] of points that make up the ellipse. In radians, lat, lon,
     * lat, lon, etc. May be null if generate hasn't been called.
     */
    public double[] getLatLonPoints() {
        return rawllpts;
    }

    /**
     * Given that the center point and the axis are set, calculate the new
     * lat/lon points all around the ellipse from the center.
     */
    public double[] createLatLonPoints() {
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
        double[] azimuth = new double[nMax + 1];
        double[] llPoints = new double[2 * (nMax + 1)];

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
            azimuth[i] = angle + com.bbn.openmap.MoreMath.HALF_PI
                    + getRotationAngle();

            if (Debug.debugging("ellipse")) {
                Debug.output(" "
                        + i
                        + " "
                        + Math.toDegrees(azimuth[i])
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

            LatLonPoint llPt = LatLonPoint.getDouble(center).getPoint((float) distance[i], azimuth[i]);
            llPoints[nCounter++] = llPt.getRadLat();
            llPoints[nCounter++] = llPt.getRadLon();
        }

        return llPoints;
    }

    public boolean generate(Projection proj) {
        if (renderType == RENDERTYPE_XY || renderType == RENDERTYPE_OFFSET) {
            return super.generate(proj); // generate using circle's generate
        }
        
        setNeedToRegenerate(true);        

        if (proj == null) {
            Debug.message("omgraphic",
                    "OMEllipse: null projection in generate!");
            return false;
        }

        if (rawllpts == null) {
            rawllpts = createLatLonPoints();
        }

        ArrayList<float[]> vector = null;

        // polygon/polyline project the polygon/polyline.
        // Vertices should already be in radians.ArrayList vector;
        if (proj instanceof GeoProj) {
            vector = ((GeoProj) proj).forwardPoly(rawllpts,
                    getLineType(),
                    -1,
                    true);

            int size = vector.size();
            GeneralPath projectedShape = null;
            // We could call create shape, but this is more efficient.
            for (int i = 0; i < size; i += 2) {
                GeneralPath gp = createShape(vector.get(i),
                        vector.get(i + 1),
                        true);

                projectedShape = appendShapeEdge(projectedShape, gp, false);
            }
            
            setShape(projectedShape);
        } else {
            // Create an ellipse in projected space using java2d
            Ellipse2D ellipse = new Ellipse2D.Float((float) center.getX(), (float) center.getY(), (float) majorAxisSpan, (float) minorAxisSpan);
            setShape(new GeneralPath(proj.forwardShape(ellipse)));
        }

        setNeedToRegenerate(false);
        return true;
    }
    
    public void restore(OMGeometry source){
        super.restore(source);
        if (source instanceof OMEllipse) {
            OMEllipse ellipse = (OMEllipse) source;
            this.majorAxisSpan = ellipse.majorAxisSpan;
            this.minorAxisSpan = ellipse.minorAxisSpan;
        }
    }
}
