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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/labeled/LabeledOMPoly.java,v $
// $RCSfile: LabeledOMPoly.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.labeled;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;

import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * This is an OMPoly that has been extended to manage a text label.
 */
public class LabeledOMPoly
        extends OMPoly
        implements LabeledOMGraphic {

    protected OMText label;
    protected Point offset;
    protected boolean locateAtCenter = false;
    protected int index = 0;

    public LabeledOMPoly() {
        super();
    }

    /**
     * Create an LabeledOMPoly from a list of float lat/lon pairs.
     * 
     * @see OMPoly#OMPoly(double[], int, int)
     */
    public LabeledOMPoly(double[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
    }

    /**
     * Create an LabeledOMPoly from a list of float lat/lon pairs.
     * 
     * @see OMPoly#OMPoly(double[], int, int, int)
     */
    public LabeledOMPoly(double[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
    }

    /**
     * Create an LabeledOMPoly from a list of xy pairs.
     * 
     * @see com.bbn.openmap.omGraphics.OMPoly#OMPoly(int[])
     */
    public LabeledOMPoly(int[] xypoints) {
        super(xypoints);
    }

    /**
     * Create an x/y LabeledOMPoly.
     * 
     * @see OMPoly#OMPoly(int[], int[])
     */
    public LabeledOMPoly(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
    }

    /**
     * Create an x/y LabeledOMPoly at an offset from lat/lon.
     * 
     * @see OMPoly#OMPoly(double, double, int[], int)
     */
    public LabeledOMPoly(double latPoint, double lonPoint, int[] xypoints, int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Create an x/y LabeledOMPoly at an offset from lat/lon.
     * 
     * @see OMPoly#OMPoly(double, double, int[], int[], int)
     */
    public LabeledOMPoly(double latPoint, double lonPoint, int[] xPoints, int[] yPoints, int cMode) {
        super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * Set the String for the label.
     */
    public void setText(String label) {
        getLabel().setData(label);
    }

    /**
     * Get the String for the label.
     */
    public String getText() {
        return getLabel().getData();
    }

    protected OMText getLabel() {
        if (label == null) {
            label = new OMText(-1, -1, "", OMText.JUSTIFY_LEFT);
        }
        return label;
    }

    /**
     * Set the Font for the label.
     */
    public void setFont(Font f) {
        getLabel().setFont(f);
    }

    /**
     * Get the Font for the label.
     */
    public Font getFont() {
        return getLabel().getFont();
    }

    /**
     * Set the justification setting for the label.
     * 
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_LEFT
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_CENTER
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_RIGHT
     */
    public void setJustify(int just) {
        getLabel().setJustify(just);
    }

    /**
     * Get the justification setting for the label.
     * 
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_LEFT
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_CENTER
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_RIGHT
     */
    public int getJustify() {
        return getLabel().getJustify();
    }

    /**
     * Tell the LabeledOMGraphic to calculate the location of the String that
     * would put it in the middle of the OMGraphic.
     */
    public void setLocateAtCenter(boolean set) {
        locateAtCenter = set;
        if (set) {
            setJustify(OMText.JUSTIFY_CENTER);
            getLabel().setFMHeight(OMText.ASCENT);
        }
    }

    /**
     * Get whether the LabeledOMGraphic is placing the label String in the
     * center of the OMGraphic.
     */
    public boolean isLocateAtCenter() {
        return locateAtCenter;
    }

    /**
     * Calculate the projected area of the poly. Algorithm used is from some
     * australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    protected double calculateProjectedArea() {
        int j = 0;
        double area = 0.0;
        float[] xpts = xpoints[0];
        float[] ypts = ypoints[0];
        int npoints = xpts.length;

        for (int i = 0; i < npoints; ++i) {
            j = (i + 1) % npoints;
            area += xpts[i] * ypts[j];
            area -= ypts[i] * xpts[j];
        }

        return area / 2.0;

        // area = area / 2.0;
        // return (area < 0.0 ? -area : area);
    }

    /**
     * Get the calculated center where the label string is drawn. Algorithm used
     * is from some australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    public Point getCenter() {
        // if the OMPoly isn't generated, then you can't calculate it.
        // We're working in x/y space here, so it looks right.
        if (getNeedToRegenerate()) {
            return null;
        }

        float cx = 0.0f;
        float cy = 0.0f;
        float A = (float) calculateProjectedArea();
        int j = 0;
        float factor = 0;

        float[] xpts = xpoints[0];
        float[] ypts = ypoints[0];
        int npoints = xpts.length;

        for (int i = 0; i < npoints; ++i) {
            j = (i + 1) % npoints;

            factor = xpts[i] * ypts[j] - xpts[j] * ypts[i];
            cx += (xpts[i] + xpts[j]) * factor;
            cy += (ypts[i] + ypts[j]) * factor;
        }

        A *= 6.0f;
        factor = 1 / A;

        // bbenyo: take the absolute value cause I was getting
        // negative values
        // for polys with all positive vertices
        // cx = Math.abs(cx * factor);
        // cy = Math.abs(cy * factor);

        // DFD and RS - let the area calculation return negative
        // values, and don't do this absolute value calculation.
        // Negative values get returned when the points are
        // counterclockwise, indicating holes. We may want labels
        // offscreen however, and the abs pushes them onscreen.

        cx *= factor;
        cy *= factor;

        Point center = new Point(Math.round(cx), Math.round(cy));
        return center;
    }

    /**
     * Set the index of the OMGraphic coordinates where the drawing point of the
     * label should be attached. The meaning of the point differs between
     * OMGraphic types.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the index of the OMGraphic where the String will be rendered. The
     * meaning of the index differs from OMGraphic type to OMGraphic type.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the x, y pixel offsets where the String should be rendered, from the
     * location determined from the index point, or from the calculated center
     * point. Point.x is the horizontal offset, Point.y is the vertical offset.
     */
    public void setOffset(Point p) {
        offset = p;
    }

    /**
     * Get the x, y pixel offsets set for the rendering of the point.
     */
    public Point getOffset() {
        if (offset == null) {
            offset = new Point();
        }
        return offset;
    }

    /**
     * Set the angle by which the text is to rotated.
     * 
     * @param angle the number of radians the text is to be rotated. Measured
     *        clockwise from horizontal. Positive numbers move the positive x
     *        axis toward the positive y axis.
     */
    public void setRotationAngle(double angle) {
        getLabel().setRotationAngle(angle);
    }

    /**
     * Get the current rotation of the text.
     * 
     * @return the text rotation.
     */
    public double getRotationAngle() {
        return getLabel().getRotationAngle();
    }

    boolean matchPolyPaint = true;

    /**
     * Set the line paint for the polygon. If the text paint hasn't been
     * explicitly set, then the text paint will be set to this paint, too.
     */
    public void setLinePaint(Paint paint) {
        super.setLinePaint(paint);
        if (matchPolyPaint) {
            getLabel().setLinePaint(paint);
        }
    }

    /**
     * If not set to null, the text will be painted in a different color. If set
     * to null, the text paint will match the poly edge paint.
     * 
     * @param paint the Paint object for the text
     */
    public void setTextPaint(Paint paint) {
        if (paint != null) {
            matchPolyPaint = false;
            getLabel().setLinePaint(paint);
        }
    }

    /**
     * Used for the actual text location.
     */
    Point handyPoint = new Point();

    /**
     * Calculate where the text point ought to go.
     */
    protected Point getTextPoint(Projection proj) {

        // Assuming that the rendertype is not unknown...
        if (renderType == RENDERTYPE_LATLON && proj instanceof GeoProj) {
            int numPoints = rawllpts.length / 2;
            if (rawllpts.length < 2) {
                // off screen...
                handyPoint.setLocation(-10, -10);
                return handyPoint;
            }
            if (locateAtCenter) {
                handyPoint = getCenter();

                // New getCenter algorithm works better.
                // for (i = 0; i < rawllpts.length; i+=2) {
                // proj.forward(rawllpts[i], rawllpts[i+1],
                // handyPoint, true);

                // avgy += handyPoint.getY();
                // avgx += handyPoint.getX();
                // }
                // avgy = avgy/numPoints;
                // avgx = avgx/numPoints;
                // handyPoint.setLocation(avgx, avgy);
            } else {
                if (index < 0)
                    index = 0;
                if (index > numPoints)
                    index = numPoints - 1;
                ((GeoProj) proj).forward(rawllpts[2 * index], rawllpts[2 * index + 1], handyPoint, true);
            }
        } else {
            float[][] x = xpoints;
            float[][] y = ypoints;

            if (x[0].length < 2) {
                // off screen...
                handyPoint.setLocation(-10, -10);
                return handyPoint;
            }

            if (locateAtCenter) {
                handyPoint = getCenter();

                // New getCenter algorithm works better.
                // for (i = 0; i < x[0].length; i++) {
                // avgx += x[0][i];
                // avgy += y[0][i];
                // }
                // handyPoint.setLocation(avgx/x[0].length,
                // avgy/x[0].length);
            } else {
                if (index < 0)
                    index = 0;
                if (index >= x[0].length)
                    index = x[0].length - 1;
                handyPoint.setLocation(x[0][index], y[0][index]);
            }
        }
        return handyPoint;
    }

    public boolean generate(Projection proj) {
        boolean ret = super.generate(proj);

        Point p = getTextPoint(proj);

        if (p != null) {
            label.setX((int) (p.getX() + getOffset().getX()));
            label.setY((int) (p.getY() + getOffset().getY()));

            if (Debug.debugging("labeled")) {
                Debug.output("Setting label(" + label.getData() + ") to " + p);
            }

            label.generate(proj);
        }
        return ret;
    }

    public void render(java.awt.Graphics g) {
        super.render(g);
        label.render(g);
    }
}