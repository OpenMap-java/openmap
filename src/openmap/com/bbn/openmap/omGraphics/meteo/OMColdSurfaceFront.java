package com.bbn.openmap.omGraphics.meteo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.awt.AbstractShapeDecoration;
import com.bbn.openmap.omGraphics.awt.LineShapeDecoration;
import com.bbn.openmap.omGraphics.awt.TextShapeDecoration;
import com.bbn.openmap.omGraphics.OMDecoratedSpline;

/**
 * OMColdSurfaceFront.
 * Just need to init the decorations.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class OMColdSurfaceFront extends OMDecoratedSpline {

    public static int LENGTH = 10;
    public static int WIDTH = 10;
    public static int SPACING = 36;

    /**
     * Constructor.
     */
    public OMColdSurfaceFront() {
	super();
    }

    /**
     * Constructor.
     * @param llPoints
     * @param units
     * @param lType
     */
    public OMColdSurfaceFront(float[] llPoints, int units, int lType) {
	super(llPoints, units, lType);
    }

    /**
     * Constructor.
     * @param llPoints
     * @param units
     * @param lType
     * @param nsegs
     */
    public OMColdSurfaceFront(
	float[] llPoints,
	int units,
	int lType,
	int nsegs) {
	super(llPoints, units, lType, nsegs);
    }

    /**
     * Constructor.
     * @param xypoints
     */
    public OMColdSurfaceFront(int[] xypoints) {
	super(xypoints);
    }

    /**
     * Constructor.
     * @param xPoints
     * @param yPoints
     */
    public OMColdSurfaceFront(int[] xPoints, int[] yPoints) {
	super(xPoints, yPoints);
    }

    /**
     * Constructor.
     * @param latPoint
     * @param lonPoint
     * @param xypoints
     * @param cMode
     */
    public OMColdSurfaceFront(
	float latPoint,
	float lonPoint,
	int[] xypoints,
	int cMode) {
	super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Constructor.
     * @param latPoint
     * @param lonPoint
     * @param xPoints
     * @param yPoints
     * @param cMode
     */
    public OMColdSurfaceFront(
	float latPoint,
	float lonPoint,
	int[] xPoints,
	int[] yPoints,
	int cMode) {
	super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * @see com.bbn.openmap.omGraphics.OMDecoratedSpline#initDecorations()
     */
    protected void initDecorations() {
	getDecorator().addDecoration(
	    new LineShapeDecoration(SPACING, ColdFrontShapeDecoration.COLOR));
	getDecorator().addDecoration(
	    new ColdFrontShapeDecoration(
		LENGTH,
		WIDTH,
		ColdFrontShapeDecoration.LEFT));
    }

}
