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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/meteo/OMHotSurfaceFront.java,v $
//$RCSfile: OMHotSurfaceFront.java,v $
//$Revision: 1.4 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.meteo;

import com.bbn.openmap.omGraphics.OMDecoratedSpline;
import com.bbn.openmap.omGraphics.awt.LineShapeDecoration;

/**
 * OMHotSurfaceFront Just need to init the decorations.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class OMHotSurfaceFront extends OMDecoratedSpline {

    public static int LENGTH = 10;
    public static int WIDTH = 5;
    public static int SPACING = 36;

    /**
     * Constructor.
     */
    public OMHotSurfaceFront() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param llPoints
     * @param units
     * @param lType
     */
    public OMHotSurfaceFront(double[] llPoints, int units, int lType) {
        super(llPoints, units, lType);
    }

    /**
     * Constructor.
     * 
     * @param llPoints
     * @param units
     * @param lType
     * @param nsegs
     */
    public OMHotSurfaceFront(double[] llPoints, int units, int lType, int nsegs) {
        super(llPoints, units, lType, nsegs);
    }

    /**
     * Constructor.
     * 
     * @param xypoints
     */
    public OMHotSurfaceFront(int[] xypoints) {
        super(xypoints);
    }

    /**
     * Constructor.
     * 
     * @param xPoints
     * @param yPoints
     */
    public OMHotSurfaceFront(int[] xPoints, int[] yPoints) {
        super(xPoints, yPoints);
    }

    /**
     * Constructor.
     * 
     * @param latPoint
     * @param lonPoint
     * @param xypoints
     * @param cMode
     */
    public OMHotSurfaceFront(float latPoint, float lonPoint, int[] xypoints,
            int cMode) {
        super(latPoint, lonPoint, xypoints, cMode);
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
    public OMHotSurfaceFront(float latPoint, float lonPoint, int[] xPoints,
            int[] yPoints, int cMode) {
        super(latPoint, lonPoint, xPoints, yPoints, cMode);
    }

    /**
     * @see com.bbn.openmap.omGraphics.OMDecoratedSpline#initDecorations()
     */
    protected void initDecorations() {
        getDecorator().addDecoration(new LineShapeDecoration(SPACING, HotFrontShapeDecoration.COLOR));
        getDecorator().addDecoration(new HotFrontShapeDecoration(LENGTH, WIDTH, HotFrontShapeDecoration.LEFT));
    }

}