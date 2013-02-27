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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/NatCubicClosedSpline.java,v $
//$RCSfile: NatCubicClosedSpline.java,v $
//$Revision: 1.5 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import com.bbn.openmap.MoreMath;

/**
 * A natural cubic closed spline calculation.
 * 
 * @author Eric LEPICIER
 * @see <a href="http://www.cse.unsw.edu.au/~lambert/splines/">Splines </a>
 * @version 21 juil. 2002
 */
public class NatCubicClosedSpline extends NatCubicSpline {

    /**
     * Calculates the closed natural cubic spline that interpolates x[0], x[1],
     * ... x[n]. The first segment is returned as C[0].a + C[0].b*u + C[0].c*u^2
     * + C[0].d*u^3 0 <=u <1 the other segments are in C[1], C[2], ... C[n]
     * 
     * @see com.bbn.openmap.omGraphics.NatCubicSpline#calcNaturalCubic(int,
     *      int[])
     */
    Cubic[] calcNaturalCubic(int n, int[] x) {
        float[] w = new float[n + 1];
        float[] v = new float[n + 1];
        float[] y = new float[n + 1];
        float[] D = new float[n + 1];
        float z, F, G, H;
        int k;
        /*
         * We solve the equation [4 1 1] [D[0]] [3(x[1] - x[n]) ] |1 4 1 |
         * |D[1]| |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | .
         * | | 1 4 1| | . | |3(x[n] - x[n-2])| [1 1 4] [D[n]] [3(x[0] - x[n-1])]
         * 
         * by decomposing the matrix into upper triangular and lower matrices
         * and then back substitution. See Spath "Spline Algorithms for Curves
         * and Surfaces" pp 19--21. The D[i] are the derivatives at the knots.
         */
        w[1] = v[1] = z = 1.0f / 4.0f;
        y[0] = z * 3 * (x[1] - x[n]);
        H = 4;
        F = 3 * (x[0] - x[n - 1]);
        G = 1;
        for (k = 1; k < n; k++) {
            v[k + 1] = z = 1 / (4 - v[k]);
            w[k + 1] = -z * w[k];
            y[k] = z * (3 * (x[k + 1] - x[k - 1]) - y[k - 1]);
            H -= G * w[k];
            F -= G * y[k - 1];
            G = -v[k] * G;
        }
        H -= (G + 1) * (v[n] + w[n]);
        y[n] = F - (G + 1) * y[n - 1];

        D[n] = y[n] / H;
        D[n - 1] = y[n - 1] - (v[n] + w[n]) * D[n];
        /* This equation is WRONG! in my copy of Spath */
        for (k = n - 2; k >= 0; k--) {
            D[k] = y[k] - v[k + 1] * D[k + 1] - w[k + 1] * D[n];
        }

        /* now compute the coefficients of the cubics */
        Cubic[] C = new Cubic[n + 1];
        for (k = 0; k < n; k++) {
            C[k] = new Cubic((float) x[k], D[k], 3 * (x[k + 1] - x[k]) - 2 * D[k] - D[k + 1], 2
                    * (x[k] - x[k + 1]) + D[k] + D[k + 1]);
        }
        C[n] = new Cubic((float) x[n], D[n], 3 * (x[0] - x[n]) - 2 * D[n] - D[0], 2 * (x[n] - x[0])
                + D[n] + D[0]);
        return C;
    }

    /**
     * @see com.bbn.openmap.omGraphics.NatCubicSpline#calc(int[], int[])
     */
    public float[][] calc(int[] xpoints, int[] ypoints) {

        int[] xpts = xpoints;
        int[] ypts = ypoints;
        int l = xpoints.length;
        if (xpoints.length > 2) {
            if (xpoints[0] == xpoints[l - 1] && ypoints[0] == ypoints[l - 1]) {
                xpts = new int[l - 1];
                System.arraycopy(xpoints, 0, xpts, 0, l - 1);
                ypts = new int[l - 1];
                System.arraycopy(ypoints, 0, ypts, 0, l - 1);
            }
        }
        return super.calc(xpts, ypts);
    }

    /**
     * @see NatCubicSpline#calc(double[], double)
     */
    public double[] calc(double[] llpoints, double precision) {

        double[] llpts = llpoints;
        int l = llpoints.length;
        if (l > 4) {
            if (MoreMath.approximately_equal(llpoints[0], llpoints[l - 2])
                    && MoreMath.approximately_equal(llpoints[1], llpoints[l - 1])) {
                llpts = new double[l - 2];
                System.arraycopy(llpoints, 0, llpts, 0, l - 2);
            }
        }

        return super.calc(llpts, precision);
    }
}