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
//$Revision: 1.4 $
//$Date: 2007/06/21 21:38:59 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * A default implementation of OMLabeler that extends from OMText.
 * 
 * @author dietrick
 */
public class OMTextLabeler extends OMText implements OMLabeler {

    public final static int ANCHOR_TOPLEFT = 0;
    public final static int ANCHOR_TOP = 1;
    public final static int ANCHOR_TOPRIGHT = 2;
    public final static int ANCHOR_LEFT = 3;
    public final static int ANCHOR_CENTER = 4;
    public final static int ANCHOR_RIGHT = 5;
    public final static int ANCHOR_BOTTOMLEFT = 6;
    public final static int ANCHOR_BOTTOM = 7;
    public final static int ANCHOR_BOTTOMRIGHT = 8;

    protected int anchor = ANCHOR_CENTER;

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
        this(stuff, DEFAULT_FONT, just, ANCHOR_CENTER);
    }

    public OMTextLabeler(String stuff, int just, int loc) {
        this(stuff, DEFAULT_FONT, just, loc);
    }

    /**
     * @param stuff
     * @param font
     * @param just
     */
    public OMTextLabeler(String stuff, Font font, int just) {
        this(stuff, font, just, ANCHOR_CENTER);
    }

    public OMTextLabeler(String stuff, Font font, int just, int loc) {
        setRenderType(RENDERTYPE_XY);
        setData(stuff);
        setFont(font);
        setJustify(just);
        setAnchor(loc);
    }

    public void setLocation(GeneralPath gp) {
        if (gp != null) {
            Rectangle rect = gp.getBounds();

            double x = rect.getX();
            double y = rect.getY();

            if (anchor == ANCHOR_TOP || anchor == ANCHOR_CENTER
                    || anchor == ANCHOR_BOTTOM) {
                x += rect.getWidth() / 2;
            } else if (anchor == ANCHOR_TOPRIGHT || anchor == ANCHOR_RIGHT
                    || anchor == ANCHOR_BOTTOMRIGHT) {
                x += rect.getWidth();
            }

            if (anchor == ANCHOR_LEFT || anchor == ANCHOR_CENTER
                    || anchor == ANCHOR_RIGHT) {
                y += rect.getHeight() / 2;
            } else if (anchor == ANCHOR_BOTTOMLEFT || anchor == ANCHOR_BOTTOM
                    || anchor == ANCHOR_BOTTOMRIGHT) {
                y += rect.getHeight();
            }

            setLocation(new Point((int) x, (int) y));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.omGraphics.OMLabeler#setLocation(java.awt.geom.Point2D)
     */
    public void setLocation(Point2D p) {
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
     * @see com.bbn.openmap.omGraphics.OMLabeler#setLocation(int[][], int[][])
     */
    public void setLocation(int[] xpoints, int[] ypoints) {
        setLocation(getCenter(xpoints, ypoints));
    }

    /**
     * Calculate the projected area of the poly. Algorithm used is from some
     * australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    protected static double calculateProjectedArea(int[] xpts, int[] ypts) {
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
     * Get the calculated center where the label string is drawn. Algorithm used
     * is from some australian astronomy website =)
     * http://astronomy.swin.edu.au/~pbourke/geometry/polyarea
     */
    protected static Point getCenter(int[] xpts, int[] ypts) {


        int npoints = xpts.length;
        if (npoints == 1) {
            Point center = new Point(xpts[0], ypts[0]);
            return center;
        }
        if (npoints == 2) {
            // rmcneil - two points, A=0, div zero below
            int x;
            if (xpts[1] > xpts[0])
                x = xpts[0] + ((xpts[1] - xpts[0]) / 2);
            else
                x = xpts[1] + ((xpts[0] - xpts[1]) / 2);
            int y;
            if (ypts[1] > ypts[0])
                y = ypts[0] + ((ypts[1] - ypts[0]) / 2);
            else
                y = ypts[1] + ((ypts[0] - ypts[1]) / 2);
            Point center = new Point(x, y);
            return center;
        }

        double factor = 0;
        double cx = 0.0f;
        double cy = 0.0f;
        
        for (int i = 0; i < npoints; ++i) {
            int j = (i + 1) % npoints;

            factor = xpts[i] * ypts[j] - xpts[j] * ypts[i];
            cx += (xpts[i] + xpts[j]) * factor;
            cy += (ypts[i] + ypts[j]) * factor;
        }

        double A = calculateProjectedArea(xpts, ypts);
        A *= 6.0;
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

        Point center = new Point((int)Math.round(cx), (int)Math.round(cy));
        return center;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setAnchor(int anchor) {
        this.anchor = anchor;
    }
    
    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMTextLabeler) {
            OMTextLabeler labeler = (OMTextLabeler) source;
            this.anchor = labeler.anchor;
        }
    }

}