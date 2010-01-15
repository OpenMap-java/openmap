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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Clip.java,v $
// $RCSfile: Clip.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

/**
 * Clipping functions.
 */
public class Clip {

    // cannot construct
    private Clip() {}

    /**
     * Calculate the float[] x,y buffer length for nverts.
     * <p>
     * 
     * @param nverts number of verts
     * @return int length required
     *  
     */
    public static final int liang_get_buflen(int nverts) {
        //*2 for x,y pairs, *2 for potential extra points, +2 for
        // closure,
        //and +1 for return length
        return (nverts << 2) + 3;
    }

    /**
     * Liang Barsy polygon clipping algorithm.
     * <p>
     * Viewport: xleft &lt; xright, ybottom &lt; ytop
     * <p>
     * Set up with: <code>
     * float[] ret_val = new float[n*2*2];
     * ret_val = liang_clip(,...,ret_val);
     * </code>
     * <p>
     * Points: xpoints, ypoints, npts HACK: closure on these points?
     * 
     * @param xleft left side of viewport
     * @param xright right side of viewport
     * @param ytop top of viewport
     * @param ybottom bottom of viewport
     * @param x float[] x coords
     * @param y float[] y coords
     * @param n numcoords
     * @param ret_val float[] clipped polygon
     * @see #liang_get_buflen
     *  
     */
    public static final float[] liang_clip(int xleft, int xright, int ytop,
                                         int ybottom, float[] x, float[] y, int n,
                                         float[] ret_val) {
        int i, num = 0;
        double dx, dy, xin, xout, yin, yout, tinx, tiny, tin1, tin2, toutx, touty, tout1;

        /*
         * If you put in an n-sided polygon, you will never get more
         * than a 2n-sided polygon out (actually you get less, but we
         * are being conservative)
         */
        for (i = 0; i < n; i++) {
            dx = x[i + 1] - x[i]; // (assuming closure of polygon)
            dy = y[i + 1] - y[i];

            /* line points right */
            if ((dx > 0) || ((dx == 0) && (x[i] > xright))) {
                xin = xleft;
                xout = xright;
            }
            /* line points left */
            else {
                xin = xright;
                xout = xleft;
            }
            /* line points up */
            if ((dy > 0) || ((dy == 0) && (y[i] > ytop))) {
                yin = ybottom;
                yout = ytop;
            }
            /* line points down */
            else {
                yin = ytop;
                yout = ybottom;
            }

            tinx = (dx != 0) ? (xin - x[i]) / dx : Double.NEGATIVE_INFINITY;
            tiny = (dy != 0) ? (yin - y[i]) / dy : Double.NEGATIVE_INFINITY;

            if (tinx < tiny) { /* first entry at x then y */
                tin1 = tinx;
                tin2 = tiny;
            } else { /* first entry at y then x */
                tin1 = tiny;
                tin2 = tinx;
            }

            if (tin1 <= 1) { /* case 2, 3, 4, 6 */
                if (tin1 > 0) { /* case 5 - turning vertex */
                    ret_val[num++] = (int) ProjMath.qint((double) (xin));
                    ret_val[num++] = (int) ProjMath.qint((double) (yin));
                }
                if (tin2 <= 1) { /* case 3, 4, 6 */
                    if (dx != 0)
                        toutx = (xout - x[i]) / dx;
                    else
                        /* vertical */
                        toutx = ((xleft <= x[i]) && (x[i] <= xright)) ? Double.POSITIVE_INFINITY
                                : Double.NEGATIVE_INFINITY;

                    if (dy != 0)
                        touty = (yout - y[i]) / dy;
                    else
                        /* horizontal */
                        touty = ((ybottom <= y[i]) && (y[i] <= ytop)) ? Double.POSITIVE_INFINITY
                                : Double.NEGATIVE_INFINITY;

                    tout1 = (toutx < touty) ? toutx : touty;

                    if ((tin2 > 0) || (tout1 > 0)) { /* case 4, 6 */
                        if (tin2 <= tout1) { /*
                                              * case 4 - visible
                                              * segment
                                              */
                            if (tin2 > 0) /* v[i] outside window */
                                if (tinx > tiny) { /*
                                                    * vertical
                                                    * boundary
                                                    */
                                    ret_val[num++] = (int) ProjMath.qint(xin);
                                    ret_val[num++] = (int) ProjMath.qint(y[i]
                                            + (tinx * dy));
                                } else { /* horiz boundary */
                                    ret_val[num++] = (int) ProjMath.qint(x[i]
                                            + (tiny * dx));
                                    ret_val[num++] = (int) ProjMath.qint(yin);
                                }
                            if (tout1 < 1) { /* v[i+1] outside window */
                                if (toutx < touty) { /*
                                                      * vertical
                                                      * boundary
                                                      */
                                    ret_val[num++] = (int) ProjMath.qint(xout);
                                    ret_val[num++] = (int) ProjMath.qint(y[i]
                                            + (toutx * dy));
                                } else { /* horiz boundary */
                                    ret_val[num++] = (int) ProjMath.qint(x[i]
                                            + (touty * dx));
                                    ret_val[num++] = (int) ProjMath.qint(yout);
                                }
                            } else {
                                ret_val[num++] = (int) (x[i + 1]);
                                ret_val[num++] = (int) (y[i + 1]);
                            }
                        } else { /* case 6 - turning vertex */
                            if (tinx > tiny) { /* second entry at x */
                                ret_val[num++] = (int) ProjMath.qint(xin);
                                ret_val[num++] = (int) ProjMath.qint(yout);
                            } else { /* second entry at y */
                                ret_val[num++] = (int) ProjMath.qint(xout);
                                ret_val[num++] = (int) ProjMath.qint(yin);
                            }
                        }
                    }
                }
            }
        }
        if (num != 0) {
            // Close the polygon
            ret_val[num++] = ret_val[0];/* lat */
            ret_val[num++] = ret_val[1];/* lon */
            // note the total length
            ret_val[ret_val.length - 1] = num;
        } else
            ret_val[ret_val.length - 1] = num;

        return ret_val;
    }

    // enum Values
    static final class Values {
        final static char LEFT = 0x1;
        final static char RIGHT = 0x2;
        final static char BOTTOM = 0x4;
        final static char TOP = 0x8;
    }

    private static final int _quick_code(int x, int y, int xleft, int xright,
                                         int ytop, int ybottom) {

        int val = 0x0;

        if (x < xleft)
            val = Values.LEFT;
        else if (x > xright)
            val = Values.RIGHT;
        if (y < ytop)
            val |= Values.TOP;
        else if (y > ybottom)
            val |= Values.BOTTOM;

        return val;
    }

    /**
     * Check if a line is completely inside or completely outside
     * viewport.
     * <p>
     * 
     * @param x1 pt1.x
     * @param y1 pt1.y
     * @param x2 pt2.x
     * @param y2 pt2.y
     * @param xleft left
     * @param xright right
     * @param ytop ytop &lt; ybottom
     * @param ybottom ybottom &lt; ytop
     * @return int -1=outside, 1=inside, 0=undetermined
     *  
     */
    public final static int quickCheckLineClip(int x1, int y1, int x2, int y2,
                                               int xleft, int xright, int ytop,
                                               int ybottom)
    // xleft < xright, ytop < ybottom
    {
        /*
         * return values: 1 = line is fully in window 0 = line is NOT
         * fully in window. THIS DOES NOT IMPLY THAT LINE IS PARTIALLY
         * INSIDE WINDOW. -1 = line is NOT in window at all
         */
        int pt1 = _quick_code(x1, y1, xleft, xright, ytop, ybottom);
        int pt2 = _quick_code(x2, y2, xleft, xright, ytop, ybottom);

        if ((pt1 & pt2) != 0)
            return -1; // completely outside
        else if ((pt1 | pt2) == 0)
            return 1; // completely inside
        else
            return 0; // partially inside?
    }

    /*
     * public static void main(String[] args) { int xleft = 0; int
     * xright = 1024; int ytop = 0; int ybottom = 1024; float[] ret_val =
     * null; float[] xs = null, ys = null;
     * 
     * int n = 4; xs = new float[n+1]; ys = new float[n+1]; xs[0] = -100;
     * ys[0] = 400; xs[1] = 700; ys[1] = -100; xs[2] = 1100; ys[2] =
     * 400; xs[3] = 700; ys[3] = 1100; xs[4] = -100; ys[4] = 400;
     * ret_val = new float[liang_get_buflen(n)];
     * 
     * ret_val = liang_clip( xleft, xright, ybottom, ytop, xs, ys, n,
     * ret_val); n = ret_val[ret_val.length-1]-2;// unclosed length
     * 
     * Debug.output("num pairs = " + n); Debug.output("total buffer
     * len = " + ret_val.length); for (int i = 0; i < n; i+=2) {
     * Debug.output( "(" + ret_val[i] + "," + ret_val[i+1] + ")"); } }
     */
}
