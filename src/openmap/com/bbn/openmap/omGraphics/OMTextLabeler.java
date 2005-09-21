// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: OMTextLabeler.java,v $
//$Revision: 1.2 $
//$Date: 2005/09/21 13:56:12 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

/**
 * A default implementation of OMLabeler that extends from OMText.
 * 
 * @author dietrick
 */
public class OMTextLabeler extends OMText implements OMLabeler {

    /**
     * 
     */
    public OMTextLabeler(String stuff) {
        this(stuff, DEFAULT_FONT, JUSTIFY_LEFT);
    }

    /**
     * @param stuff
     * @param just
     */
    public OMTextLabeler(String stuff, int just) {
        this(stuff, DEFAULT_FONT, just);
    }

    /**
     * @param stuff
     * @param font
     * @param just
     */
    public OMTextLabeler(String stuff, Font font, int just) {
        setRenderType(RENDERTYPE_XY);
        setData(stuff);
        setFont(font);
        setJustify(just);
    }

    public void setLocation(GeneralPath gp) {
        if (gp != null) {
            Rectangle rect = gp.getBounds();
            setLocation(new Point((int) (rect.getX() + rect.getWidth() / 2), (int) (rect.getY() + rect.getHeight() / 2)));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.omGraphics.OMLabeler#setLocation(java.awt.Point)
     */
    public void setLocation(Point p) {
        polyBounds = null;
        setX((int) p.getX());
        setY((int) p.getY());
        setMapLocation(p);
        computeBounds();
        setNeedToRegenerate(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.omGraphics.OMLabeler#setLocation(int[][],
     *      int[][])
     */
    public void setLocation(int[] xpoints, int[] ypoints) {
        setLocation(getCenter(xpoints, ypoints));
    }

    /**
     * Calculate the projected area of the poly. Algorithm used is
     * from some australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    protected double calculateProjectedArea(int[] xpts, int[] ypts) {
        int j = 0;
        double area = 0.0;

        int npoints = xpts.length;

        for (int i = 0; i < npoints; ++i) {
            j = (i + 1) % npoints;
            area += xpts[i] * ypts[j];
            area -= ypts[i] * xpts[j];
        }

        return area / 2.0;
    }

    /**
     * Get the calculated center where the label string is drawn.
     * Algorithm used is from some australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    protected Point getCenter(int[] xpts, int[] ypts) {
        float cx = 0.0f;
        float cy = 0.0f;
        double A = calculateProjectedArea(xpts, ypts);
        int j = 0;
        double factor = 0;

        int npoints = xpts.length;

        for (int i = 0; i < npoints; ++i) {
            j = (i + 1) % npoints;

            factor = xpts[i] * ypts[j] - xpts[j] * ypts[i];
            cx += (xpts[i] + xpts[j]) * factor;
            cy += (ypts[i] + ypts[j]) * factor;
        }

        A = A * 6.0;
        factor = 1.0 / A;

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

}