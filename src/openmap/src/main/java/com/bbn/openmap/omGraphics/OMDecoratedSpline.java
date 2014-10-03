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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMDecoratedSpline.java,v $
//$RCSfile: OMDecoratedSpline.java,v $
//$Revision: 1.8 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import com.bbn.openmap.omGraphics.awt.Revertable;
import com.bbn.openmap.omGraphics.awt.ShapeDecorator;
import com.bbn.openmap.util.Debug;

/**
 * A decorated splined OMPoly. Decoration list is empty, but accessible via
 * getDecorator() Code from OMPoly, few changes : render() just need to delegate
 * the drawing of the polyline to the ShapeDecorator
 * 
 * @author Eric LEPICIER
 * @version 27 juil. 2002
 */
public class OMDecoratedSpline
        extends OMSpline
        implements Revertable {

    private ShapeDecorator decorator = new ShapeDecorator();

    /**
     * Constructor.
     */
    public OMDecoratedSpline() {
        super();
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param llPoints
     * @param units
     * @param lType
     */
    public OMDecoratedSpline(double[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param llPoints
     * @param units
     * @param lType
     * @param nsegs
     */
    public OMDecoratedSpline(double[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param xypoints
     */
    public OMDecoratedSpline(int[] xypoints) {
        super(xypoints);
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param xPoints
     * @param yPoints
     */
    public OMDecoratedSpline(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param latPoint
     * @param lonPoint
     * @param xypoints
     * @param cMode
     */
    public OMDecoratedSpline(double latPoint, double lonPoint, int[] xypoints, int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
        initDecorations();
    }

    /**
     * Constructor.
     * 
     * @param latPoint
     * @param lonPoint
     * @param xPoints
     * @param yPoints
     * @param cMode
     */
    public OMDecoratedSpline(double latPoint, double lonPoint, int[] xPoints, int[] yPoints, int cMode) {
        super(latPoint, lonPoint, xPoints, yPoints, cMode);
        initDecorations();
    }

    /**
     * Paint the poly. This works if generate() has been successful. Same code
     * than OMPoly, just delegates the drawing of the polyline to the
     * ShapeDecorator
     * 
     * @param g java.awt.Graphics to paint the poly onto.
     */
    public void render(Graphics g) {
        if (decorator == null) {
            super.render(g);
            return;
        }

        Shape projectedShape = getShape();
        if (projectedShape != null) {
            decorator.draw(g, projectedShape);
            return;
        }

        if (getNeedToRegenerate() || !isVisible())
            return;

        // safety: grab local reference of projected points
        float[][] xpts = xpoints;
        float[][] ypts = ypoints;
        int len = xpts.length;

        Paint displayPaint = getDisplayPaint();
        Paint fillPaint = getFillPaint();
        boolean isFillClear = isClear(fillPaint);
        boolean isLineClear = isClear(displayPaint);

        // If shapes are null, then we have to do things the old way.
        try {
            for (int i = 0; i < len; i++) {
                float[] _x = xpts[i];
                float[] _y = ypts[i];

                // render polygon
                if (isPolygon) {

                    // fill main polygon
                    if (!isFillClear) {
                        // set the interior coloring parameters
                        setGraphicsForFill(g);
                        GeneralPath poly = new GeneralPath();
                        for (int j = 0; j < _x.length; j++) {
                            if (j == 0) {
                                poly.moveTo(_x[0], _y[0]);
                            } else {
                                poly.lineTo(_x[j], _y[j]);
                            }
                        }
                        ((Graphics2D) g).fill(poly);
                        // g.fillPolygon(_x, _y, _x.length);
                    }

                    // only draw outline if different color
                    if (!isLineClear || !edgeMatchesFill) {
                        setGraphicsForEdge(g);
                        // for some reason, this used to be
                        // drawPolygon
                        decorator.draw(g, _x, _y);
                    }
                }

                // render polyline
                else {
                    // draw main outline
                    setGraphicsForEdge(g);
                    decorator.draw(g, _x, _y);
                }
            }
        } catch (Exception e) {
            // Trying to catch any clipping problems from within a JRE
            Debug.output("OMDecoratedSpline: caught Java rendering exception\n" + e.getMessage());
        }
    }

    /**
     * Returns the decorator.
     * 
     * @return ShapeDecorator
     */
    public ShapeDecorator getDecorator() {
        return decorator;
    }

    /**
     * Sets the decorator.
     * 
     * @param decorator The decorator to set
     */
    public void setDecorator(ShapeDecorator decorator) {
        this.decorator = decorator;
    }

    /**
     * Called by constructor, may be overriden.
     */
    protected void initDecorations() {
    }

    /**
     * @see com.bbn.openmap.omGraphics.awt.Revertable#revert()
     */
    public void revert() {
        decorator.revert();
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMDecoratedSpline) {
            OMDecoratedSpline spline = (OMDecoratedSpline) source;
            this.decorator = spline.decorator;
        }
    }
}
