package com.bbn.openmap.omGraphics.meteo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.awt.AbstractShapeDecoration;
import com.bbn.openmap.omGraphics.awt.LineShapeDecoration;
import com.bbn.openmap.omGraphics.OMDecoratedSpline;

/**
 * OMOcclusion
 * Just need to init the decorations.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class OMOcclusion extends OMDecoratedSpline {

    public static Color COLOR = new Color(255,0,255);
    public static int LENGTH = 10;
    public static int WIDTH = 5;
    public static int SPACING = 36;

    /**
     * Constructor for OMOcclusion.
     */
    public OMOcclusion() {
        super();
    }

    /**
     * Constructor for OMOcclusion.
     * @param llPoints
     * @param units
     * @param lType
     */
    public OMOcclusion(float[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
    }

    /**
     * Constructor for OMOcclusion.
     * @param llPoints
     * @param units
     * @param lType
     * @param nsegs
     */
    public OMOcclusion(float[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
    }

    /**
     * Constructor for OMOcclusion.
     * @param xypoints
     */
    public OMOcclusion(int[] xypoints) {
        super(xypoints);
    }

    /**
     * Constructor for OMOcclusion.
     * @param xPoints
     * @param yPoints
     */
    public OMOcclusion(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
    }

    /**
     * Constructor for OMOcclusion.
     * @param latPoint
     * @param lonPoint
     * @param xypoints
     * @param cMode
     */
    public OMOcclusion(
        float latPoint,
        float lonPoint,
        int[] xypoints,
        int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
    }

    /**
     * Constructor for OMOcclusion.
     * @param latPoint
     * @param lonPoint
     * @param xPoints
     * @param yPoints
     * @param cMode
     */
    public OMOcclusion(
        float latPoint,
        float lonPoint,
        int[] xPoints,
        int[] yPoints,
        int cMode) {
        super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * @see fr.shom.jcirrus.openmap.omGraphics.OMDecoratedSpline#initDecorations()
     */
    protected void initDecorations() {
        getDecorator().addDecoration(new LineShapeDecoration(SPACING, COLOR));
        ColdFrontShapeDecoration cdec =
            new ColdFrontShapeDecoration(
                LENGTH,
                WIDTH * 2,
                ColdFrontShapeDecoration.LEFT);
        cdec.setPaint(COLOR);
        getDecorator().addDecoration(cdec);
        HotFrontShapeDecoration hdec =
            new HotFrontShapeDecoration(
                LENGTH,
                WIDTH,
                ColdFrontShapeDecoration.LEFT);
        hdec.setPaint(COLOR);
        getDecorator().addDecoration(hdec);
    }

}

