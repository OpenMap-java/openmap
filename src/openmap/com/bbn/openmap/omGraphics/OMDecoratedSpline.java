package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Paint;

import com.bbn.openmap.util.Debug;

import com.bbn.openmap.omGraphics.awt.Revertable;
import com.bbn.openmap.omGraphics.awt.ShapeDecorator;

/**
 * A decorated splined OMPoly.
 * Decoration list is empty, but accessible via getDecorator()
 * Code from OMPoly, few changes : render() just need to delegate
 * the drawing of the polyline to the ShapeDecorator
 * 
 * @author Eric LEPICIER
 * @version 27 juil. 2002
 */
public class OMDecoratedSpline extends OMSpline implements Revertable {

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
     * @param llPoints
     * @param units
     * @param lType
     */
    public OMDecoratedSpline(float[] llPoints, int units, int lType) {
	super(llPoints, units, lType);
	initDecorations();
    }

    /**
     * Constructor.
     * @param llPoints
     * @param units
     * @param lType
     * @param nsegs
     */
    public OMDecoratedSpline(
	float[] llPoints,
	int units,
	int lType,
	int nsegs) {
	super(llPoints, units, lType, nsegs);
	initDecorations();
    }

    /**
     * Constructor.
     * @param xypoints
     */
    public OMDecoratedSpline(int[] xypoints) {
	super(xypoints);
	initDecorations();
    }

    /**
     * Constructor.
     * @param xPoints
     * @param yPoints
     */
    public OMDecoratedSpline(int[] xPoints, int[] yPoints) {
	super(xPoints, yPoints);
	initDecorations();
    }

    /**
     * Constructor.
     * @param latPoint
     * @param lonPoint
     * @param xypoints
     * @param cMode
     */
    public OMDecoratedSpline(
	float latPoint,
	float lonPoint,
	int[] xypoints,
	int cMode) {
	super(latPoint, lonPoint, xypoints, cMode);
	initDecorations();
    }

    /**
     * Constructor.
     * @param latPoint
     * @param lonPoint
     * @param xPoints
     * @param yPoints
     * @param cMode
     */
    public OMDecoratedSpline(
	float latPoint,
	float lonPoint,
	int[] xPoints,
	int[] yPoints,
	int cMode) {
	super(latPoint, lonPoint, xPoints, yPoints, cMode);
	initDecorations();
    }

    /**
     * Paint the poly. 
     * This works if generate() has been successful.
     * Same code than OMPoly, just delegates the drawing
     * of the polyline to the ShapeDecorator
     *
     * @param g java.awt.Graphics to paint the poly onto.
     */
    public void render(Graphics g) {
	if (shape != null) {
	    decorator.draw(g, shape);
	    return;
	}

	if (getNeedToRegenerate() || !isVisible())
	    return;

	// safety: grab local reference of projected points
	int[][] xpts = xpoints;
	int[][] ypts = ypoints;
	int[] _x, _y;
	int i;
	int len = xpts.length;

	Paint displayPaint = getDisplayPaint();
	Paint fillPaint = getFillPaint();
	boolean isFillClear = isClear(fillPaint);
	boolean isLineClear = isClear(displayPaint);

	// If shapes are null, then we have to do things the old way.
	try {
	    for (i = 0; i < len; i++) {
		_x = xpts[i];
		_y = ypts[i];

		// render polygon
		if (isPolygon) {

		    // fill main polygon
		    if (!isFillClear) {
			// set the interior coloring parameters
			setGraphicsForFill(g);
			g.fillPolygon(_x, _y, _x.length);
		    }

		    // only draw outline if different color
		    if (!isLineClear || !edgeMatchesFill) {
			setGraphicsForEdge(g);
			// for some reason, this used to be drawPolygon
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
	}
	catch (Exception e) {
	    // Trying to catch any clipping problems from within a JRE
	    Debug.output(
		"OMDecoratedSpline: caught Java rendering exception\n" + e.getMessage());
	}
    }

    /**
     * Returns the decorator.
     * @return ShapeDecorator
     */
    public ShapeDecorator getDecorator() {
	return decorator;
    }

    /**
     * Sets the decorator.
     * @param decorator The decorator to set
     */
    public void setDecorator(ShapeDecorator decorator) {
	this.decorator = decorator;
    }
	
    /**
     * Called by constructor, may be overriden.
     * @param decorator The decorator to set
     */
    protected void initDecorations() {}

    /**
     * @see fr.free.lepicier.awt.Revertable#revert()
     */
    public void revert() {
	decorator.revert();
    }
}
