package com.bbn.openmap.omGraphics.labeled;

import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;

import com.bbn.openmap.omGraphics.OMSpline;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * LabeledOMSpline Copied from LabeledOMPoly, because both OMSpline
 * and LabeledOMPoly inherits from OMPoly
 * 
 * @author Eric LEPICIER
 * @version 15 juil. 2002
 */
public class LabeledOMSpline extends OMSpline implements LabeledOMGraphic {

    protected OMText label;
    protected Point offset;
    protected boolean locateAtCenter = false;
    protected int index = 0;

    /**
     * Default constructor.
     */
    public LabeledOMSpline() {
        super();
    }

    /**
     * Create an LabeledOMSpline from a list of float lat/lon pairs.
     */
    public LabeledOMSpline(double[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
    }

    /**
     * Create an LabeledOMSpline from a list of float lat/lon pairs.
     */
    public LabeledOMSpline(double[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
    }

    /**
     * Create an LabeledOMSpline from a list of xy pairs.
     */
    public LabeledOMSpline(int[] xypoints) {
        super(xypoints);
    }

    /**
     * Create an x/y LabeledOMSpline.
     */
    public LabeledOMSpline(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
    }

    /**
     * Create an x/y LabeledOMSpline at an offset from lat/lon.
     */
    public LabeledOMSpline(float latPoint, float lonPoint, int[] xypoints,
            int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Create an x/y LabeledOMSpline at an offset from lat/lon.
     */
    public LabeledOMSpline(float latPoint, float lonPoint, int[] xPoints,
                           int[] yPoints, int cMode) {
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
     * Tell the LabeledOMGraphic to calculate the location of the
     * String that would put it in the middle of the OMGraphic.
     */
    public void setLocateAtCenter(boolean set) {
        locateAtCenter = set;
        if (set) {
            setJustify(OMText.JUSTIFY_CENTER);
            getLabel().setFMHeight(OMText.ASCENT);
        }
    }

    /**
     * Get whether the LabeledOMGraphic is placing the label String in
     * the center of the OMGraphic.
     */
    public boolean isLocateAtCenter() {
        return locateAtCenter;
    }

    /**
     * Get the calculated center where the label string is drawn.
     */
    public Point getCenter() {
        return new Point();
    }

    /**
     * Set the index of the OMGraphic coordinates where the drawing
     * point of the label should be attached. The meaning of the point
     * differs between OMGraphic types.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the index of the OMGraphic where the String will be
     * rendered. The meaning of the index differs from OMGraphic type
     * to OMGraphic type.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the x, y pixel offsets where the String should be rendered,
     * from the location determined from the index point, or from the
     * calculated center point. Point.x is the horizontal offset,
     * Point.y is the vertical offset.
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
     * @param angle the number of radians the text is to be rotated.
     *        Measured clockwise from horizontal. Positive numbers
     *        move the positive x axis toward the positive y axis.
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
     * Set the line paint for the polygon. If the text paint hasn't
     * been explicitly set, then the text paint will be set to this
     * paint, too.
     */
    public void setLinePaint(Paint paint) {
        super.setLinePaint(paint);
        if (matchPolyPaint) {
            getLabel().setLinePaint(paint);
        }
    }

    /**
     * If not set to null, the text will be painted in a different
     * color. If set to null, the text paint will match the poly edge
     * paint.
     * 
     * @param paint the Paint object for the text
     */
    public void setTextPaint(Paint paint) {
        if (paint != null) {
            matchPolyPaint = false;
            getLabel().setLinePaint(paint);
        }
    }

    Point handyPoint = new Point();

    protected Point getTextPoint(Projection proj) {
        int i;
        int avgx = 0;
        int avgy = 0;

        // Assuming that the rendertype is not unknown...
        if (renderType == RENDERTYPE_LATLON && proj instanceof GeoProj) {
            int numPoints = rawllpts.length / 2;
            if (rawllpts.length < 2) {
                // off screen...
                handyPoint.setLocation(-10, -10);
                return handyPoint;
            }
            if (locateAtCenter) {
                for (i = 0; i < rawllpts.length; i += 2) {
                    ((GeoProj) proj).forward(rawllpts[i],
                            rawllpts[i + 1],
                            handyPoint,
                            true);

                    avgy += handyPoint.getY();
                    avgx += handyPoint.getX();
                }
                avgy /= numPoints;
                avgx /= numPoints;
                handyPoint.setLocation(avgx, avgy);
            } else {
                if (index < 0)
                    index = 0;
                if (index > numPoints)
                    index = numPoints - 1;
                ((GeoProj) proj).forward(rawllpts[2 * index],
                        rawllpts[2 * index + 1],
                        handyPoint,
                        true);
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
                for (i = 0; i < x[0].length; i++) {
                    avgx += x[0][i];
                    avgy += y[0][i];
                }
                handyPoint.setLocation(avgx / x[0].length, avgy / x[0].length);
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
        label.setX((int) (p.getX() + getOffset().getX()));
        label.setY((int) (p.getY() + getOffset().getY()));

        if (Debug.debugging("labeled")) {
            Debug.output("Setting label(" + label.getData() + ") to " + p);
        }

        label.generate(proj);
        return ret;
    }

    public void render(java.awt.Graphics g) {
        super.render(g);
        label.render(g);
    }

}