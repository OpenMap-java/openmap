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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/FanCompress.java,v $
// $RCSfile: FanCompress.java,v $
// $Revision: 1.4 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import com.bbn.openmap.MoreMath;

/**
 * Class to perform fan compression on points.
 * <p>
 * <h3>FAN COMPRESSION</h3>
 * <ul>
 * <li>R.A. Fowell, and D.D. McNeil, Faster Plots by Fan
 * Data-Compression, IEEE Computer Graphics &amp; Applications, 1989,
 * 58-66.
 * <li>See also, J.G. Dunham, Optimum uniformr piecewise linear
 * approximation of planar curves, IEEE Trans. Pat. Anal and Mach.
 * Intel, Vol, PAMI-8, No 1, 1986, p. 67-75.
 * </ul>
 */
public abstract class FanCompress {

    public static class FanPoint {
        public double x;
        public double y;
    }

    /**
     * FanCompress class for float values.
     */
    public static class FloatCompress extends FanCompress {

        private int read = 0;
        private int write = 0;
        protected double[] array;
        protected double zero_eps = 0.0001;

        /**
         * Construct a FanCompress object which deals in floats.
         * 
         * @param array float[] array of coordinate pairs.
         */
        public FloatCompress(double[] array) {
            this.array = array;
        }

        /**
         * Get the next point.
         * 
         * @param p FanPoint
         * @return boolean false if no more points.
         */
        public boolean next_point(FanPoint p) {
            if (read + 1 < array.length) {
                p.x = (double) array[read++];
                p.y = (double) array[read++];
                return true;
            } else {
                return false;
            }
        }

        /**
         * Save coordinates. This routine coalesces adjacent points
         * which have the same coordinates.
         * 
         * @param x coordinate
         * @param y coordinate
         */
        public void save_point(double x, double y) {
            if (write + 1 < read) {
                // coalesce points
                if ((write > 1)
                        && MoreMath.approximately_equal((float) x,
                                array[write - 2],
                                zero_eps)
                        && MoreMath.approximately_equal((float) y,
                                array[write - 1],
                                zero_eps)) {
                    return;
                }
                array[write++] = (float) x;
                array[write++] = (float) y;
            } else {
                System.err.println("FanCompress.FloatCompress.save_point(): "
                        + "ignoring extra...");
            }
        }

        /**
         * Save only unique coordinates. If several points are the
         * same, then coalesce them into one point.
         * 
         * @param epsilon threshold used to determine uniqueness of
         *        coordinates.
         */
        public void set_coalesce_points(double epsilon) {
            zero_eps = (float) epsilon;
        }

        /**
         * Return a copy of the internal array. Invoke this method
         * after running <code>fan_compress()</code> on this object.
         * 
         * @return float[]
         */
        public double[] getArray() {
            double[] ret_val = new double[write];
            System.arraycopy(array, 0, ret_val, 0, write);
            return ret_val;
        }
    }

    /**
     * Get the next point.
     * 
     * @param p FanPoint
     * @return boolean false if no more points.
     */
    public abstract boolean next_point(FanPoint p);

    /**
     * Save coordinates.
     * 
     * @param x coordinate
     * @param y coordinate
     */
    public abstract void save_point(double x, double y);

    /**
     * Save only unique coordinates. If several points are the same,
     * then coalesce them into one point.
     * 
     * @param epsilon threshold used to determine uniqueness of
     *        coordinates.
     */
    public abstract void set_coalesce_points(double epsilon);

    /**
     * Perform fan compression. IF there is only 1 point, save-point
     * will be run on it twice, sorry.
     * 
     * @param fan FanCompress object
     * @param epsilon double
     */
    public static final void fan_compress(FanCompress fan, double epsilon) {
        double epsilon_squared; /* epsilon * epsilon */
        double p0x, p0y; /* Start of segment. */
        FanPoint p = new FanPoint();
        //      double px, py; /* Current point x,y */

        epsilon_squared = epsilon * epsilon;

        if (!fan.next_point(p)) /* Get first point */
            return;

        fan.save_point(p.x, p.y); /* Save it. */
        p0x = p.x; /* p0 <- p. */
        p0y = p.y;

        while (true) {
            double distance;
            double pu;
            double ux, uy;
            double vx, vy;
            double f1, f2, f3;
            double xok, yok;

            /* Find first point > epsilon from p0. */
            while (true) {
                double dx;
                double dy;

                dx = p.x - p0x;
                dy = p.y - p0y;

                distance = dx * dx + dy * dy;

                if (distance > epsilon_squared)
                    break;

                if (!fan.next_point(p)) {
                    //                  goto finish;
                    fan.save_point(p.x, p.y); /* Save last point. */
                    return;
                }

            }

            distance = Math.sqrt(distance);

            pu = distance; /* U component of p. */

            ux = (p.x - p0x) / pu; /* U unit vector. */
            uy = (p.y - p0y) / pu;

            vx = -uy; /* V unit vector. */
            vy = ux;

            f1 = pu; /* Region. */
            f2 = epsilon / pu;

            f3 = -f2;

            xok = p.x; /* Last point we know is OK. */
            yok = p.y;

            while (true) {
                double dx, dy;
                double /* pu, */pv;
                boolean keep;

                if (!fan.next_point(p)) {
                    //                  goto finish;
                    fan.save_point(p.x, p.y); /* Save last point. */
                    return;
                }

                keep = true;
                dx = p.x - p0x;
                dy = p.y - p0y;
                pu = ux * dx + uy * dy;
                pv = vx * dx + vy * dy;

                if (pu >= f1) {
                    /* Still moving away from p0? */
                    double slope;

                    slope = pv / pu;

                    if (slope <= f2 && slope >= f3) {
                        double temp;

                        /* Still in region? */
                        double dslope;

                        dslope = epsilon / pu;

                        keep = false;

                        f1 = pu;
                        /* Adjust region. */
                        temp = slope + dslope;

                        if (temp < f2)
                            f2 = temp; /* Min. */
                        temp = slope - dslope;

                        if (temp > f3)
                            f3 = temp; /* Max. */
                    }
                }

                if (keep) {
                    fan.save_point(xok, yok);

                    p0x = xok; /* p0 <- pOK. */
                    p0y = yok;

                    break;
                } else {
                    xok = p.x;
                    yok = p.y;
                }
            }
        }

        //      System.out.println("should not reach here");

        //      finish:
        //      
        //      save_point(p.x, p.y); /* Save last point. */
        //             return;
    }
}