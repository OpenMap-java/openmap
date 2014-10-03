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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/NatCubicSpline.java,v $
//$RCSfile: NatCubicSpline.java,v $
//$Revision: 1.5 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Polygon;

/**
 * A natural cubic spline calculation.
 * 
 * @author Eric LEPICIER
 * @see <a href="http://www.cse.unsw.edu.au/~lambert/splines/">Splines
 *      </a>
 * @version 21 juil. 2002
 */
public class NatCubicSpline {

    /**
     * Calculates the natural cubic spline that interpolates y[0],
     * y[1], ... y[n]. The first segment is returned as C[0].a +
     * C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0 <=u <1 the other segments
     * are in C[1], C[2], ... C[n-1]
     * 
     * @param n
     * @param x
     * @return Cubic[]
     */
    Cubic[] calcNaturalCubic(int n, int[] x) {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /*
         * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4
         * 1 | |D[1]| |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | |
         * ..... | | . | | . | | 1 4 1| | . | |3(x[n] - x[n-2])| [ 1
         * 2] [D[n]] [3(x[n] - x[n-1])]
         * 
         * by using row operations to convert the matrix to upper
         * triangular and then back substitution. The D[i] are the
         * derivatives at the knots.
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x[1] - x[0]) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n];
        for (i = 0; i < n; i++) {
            C[i] = new Cubic((float) x[i], D[i], 3 * (x[i + 1] - x[i]) - 2
                    * D[i] - D[i + 1], 2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
        }
        return C;
    }

    /**
     * Calculates the natural cubic spline that interpolates y[0],
     * y[1], ... y[n]. The first segment is returned as C[0].a +
     * C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0 <=u <1 the other segments
     * are in C[1], C[2], ... C[n-1]
     * 
     * @param n
     * @param x
     * @return Cubic[]
     */
    Cubic[] calcNaturalCubic(int n, float[] x) {
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /*
         * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4
         * 1 | |D[1]| |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | |
         * ..... | | . | | . | | 1 4 1| | . | |3(x[n] - x[n-2])| [ 1
         * 2] [D[n]] [3(x[n] - x[n-1])]
         * 
         * by using row operations to convert the matrix to upper
         * triangular and then back substitution. The D[i] are the
         * derivatives at the knots.
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x[1] - x[0]) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n];
        for (i = 0; i < n; i++) {
            C[i] = new Cubic((float) x[i], D[i], 3 * (x[i + 1] - x[i]) - 2
                    * D[i] - D[i + 1], 2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
        }
        return C;
    }

    /**
     * Calculates a cubic spline polyline
     * 
     * @param xpoints
     * @param ypoints
     * @return float[][]
     */
    public float[][] calc(int[] xpoints, int[] ypoints) {
        float[][] res = new float[2][0];
        if (xpoints.length > 2) {
            Cubic[] X = calcNaturalCubic(xpoints.length - 1, xpoints);
            Cubic[] Y = calcNaturalCubic(ypoints.length - 1, ypoints);

            /*
             * very crude technique just break each segment up into
             * steps lines
             */
            Polygon p = new Polygon();
            p.addPoint((int) Math.round(X[0].eval(0)),
                    (int) Math.round(Y[0].eval(0)));
            for (int i = 0; i < X.length; i++) {
                for (int j = 1; j <= steps; j++) {
                    float u = j / (float) steps;
                    p.addPoint(Math.round(X[i].eval(u)),
                            Math.round(Y[i].eval(u)));
                }
            }

            // copy polygon points to the return array
            res[0] = new float[p.npoints];
            res[1] = new float[p.npoints];
            
            for (int i = 0; i < p.npoints; i++) {
                res[0][i] = p.xpoints[i];
                res[1][i] = p.ypoints[i];
            }

            p = null;
        } else {
            //Need to convert to float[]
            float[] xfs = new float[xpoints.length];
            float[] yfs = new float[ypoints.length];
            
            for (int i = 0; i < xpoints.length; i++) {
                xfs[i] = xpoints[i];
                yfs[i] = ypoints[i];
            }
            
            res[0] = xfs;
            res[1] = yfs;
        }
        return res;
    }

    /**
     * Calculates a cubic spline polyline
     * 
     * @param xpoints in float precision.
     * @param ypoints in float precision.
     * @return flaot[][]
     */
    public float[][] calc(float[] xpoints, float[] ypoints) {
        float[][] res = new float[2][0];
        if (xpoints.length > 2) {
            Cubic[] X = calcNaturalCubic(xpoints.length - 1, xpoints);
            Cubic[] Y = calcNaturalCubic(ypoints.length - 1, ypoints);

            /*
             * very crude technique just break each segment up into
             * steps lines
             */
            Polygon p = new Polygon();
            p.addPoint((int) Math.round(X[0].eval(0)),
                    (int) Math.round(Y[0].eval(0)));
            for (int i = 0; i < X.length; i++) {
                for (int j = 1; j <= steps; j++) {
                    float u = j / (float) steps;
                    p.addPoint(Math.round(X[i].eval(u)),
                            Math.round(Y[i].eval(u)));
                }
            }

            // copy polygon points to the return array
            res[0] = new float[p.npoints];
            res[1] = new float[p.npoints];
            
            for (int i = 0; i < p.npoints; i++) {
                res[0][i] = p.xpoints[i];
                res[1][i] = p.ypoints[i];
            }
            
            p = null;
        } else {
            res[0] = xpoints;
            res[1] = ypoints;
        }
        return res;
    }
    
    /**
     * Calculates a float lat/lon cubic spline
     * 
     * @param llpoints
     * @param precision for dividing floating coordinates to become
     *        int, e.g 0.01 means spline to be calculated with
     *        coordinates * 100
     * @return float[]
     */
    public double[] calc(double[] llpoints, double precision) {
        double[] res;
        if (llpoints.length > 4) { // 2 points

            int[] xpoints = new int[(int) (llpoints.length / 2)];
            int[] ypoints = new int[xpoints.length];
            for (int i = 0, j = 0; i < llpoints.length; i += 2, j++) {
                xpoints[j] = (int) (llpoints[i] / precision);
                ypoints[j] = (int) (llpoints[i + 1] / precision);
            }

            Cubic[] X = calcNaturalCubic(xpoints.length - 1, xpoints);
            Cubic[] Y = calcNaturalCubic(ypoints.length - 1, ypoints);

            /*
             * very crude technique just break each segment up into
             * steps lines
             */
            Polygon p = new Polygon();
            p.addPoint((int) Math.round(X[0].eval(0)),
                    (int) Math.round(Y[0].eval(0)));
            for (int i = 0; i < X.length; i++) {
                for (int j = 1; j <= steps; j++) {
                    float u = j / (float) steps;
                    p.addPoint(Math.round(X[i].eval(u)),
                            Math.round(Y[i].eval(u)));
                }
            }

            res = new double[p.npoints * 2];
            for (int i = 0, j = 0; i < p.npoints; i++, j += 2) {
                res[j] = (double) p.xpoints[i] * precision;
                res[j + 1] = (double) p.ypoints[i] * precision;
            }

            p = null;
        } else {
            res = llpoints;
        }
        return res;
    }

    /**
     * Returns the steps.
     * 
     * @return int
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Sets the number of points (steps) interpolated on the curve
     * between the original points to draw it as a polyline.
     * 
     * @param steps The steps to set
     */
    public void setSteps(int steps) {
        this.steps = steps > 0 ? steps : 12;
    }

    private int steps = 12;
}