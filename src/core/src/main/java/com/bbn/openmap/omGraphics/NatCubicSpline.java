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

import com.bbn.openmap.util.MoreMath;

/**
 * A natural cubic spline calculation.
 * 
 * @author Eric LEPICIER
 * @see <a href="http://www.cse.unsw.edu.au/~lambert/splines/">Splines </a>
 * @version 21 juil. 2002
 */
public class NatCubicSpline {

	/**
	 * The proper access for these classes, using default steps.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @return
	 */
	public static float[][] calc(int[] xpoints, int[] ypoints, boolean geometryClosed) {
		return calc(xpoints, ypoints, geometryClosed, 12);
	}

	/**
	 * The proper access for these classes.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @param steps the number of segments the spline curve should be broken
	 *            into (default 12)
	 * @return
	 */
	public static float[][] calc(int[] xpoints, int[] ypoints, boolean geometryClosed, int steps) {
		if (geometryClosed) {
			return new NatCubicSpline.CLOSED().withSteps(steps).calc(xpoints, ypoints);
		}
		return new NatCubicSpline().withSteps(steps).calc(xpoints, ypoints);
	}

	/**
	 * The proper access for these classes, using default steps.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @return
	 */
	public static float[][] calc(float[] xpoints, float[] ypoints, boolean geometryClosed) {
		return calc(xpoints, ypoints, geometryClosed, 12);
	}

	/**
	 * The proper access for these classes.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @param steps the number of segments the spline curve should be broken
	 *            into (default 12)
	 * @return
	 */
	public static float[][] calc(float[] xpoints, float[] ypoints, boolean geometryClosed, int steps) {
		if (geometryClosed) {
			return new NatCubicSpline.CLOSED().withSteps(steps).calc(xpoints, ypoints);
		}
		return new NatCubicSpline().withSteps(steps).calc(xpoints, ypoints);
	}

	/**
	 * The proper access for these classes, using default steps.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @return
	 */
	public static double[] calc(double[] llpoints, double precision, boolean geometryClosed) {
		return calc(llpoints, precision, geometryClosed, 12);
	}

	/**
	 * The proper access for these classes.
	 * 
	 * @param xpoints projected x points
	 * @param ypoints projected y points
	 * @param geometryClosed whether the spline is a closed shape
	 * @param steps the number of segments the spline curve should be broken
	 *            into (default 12)
	 * @return
	 */
	public static double[] calc(double[] llpoints, double precision, boolean geometryClosed, int steps) {
		if (geometryClosed) {
			return new NatCubicSpline.CLOSED().withSteps(steps).calc(llpoints, precision);
		}
		return new NatCubicSpline().withSteps(steps).calc(llpoints, precision);
	}

	/**
	 * Calculates the natural cubic spline that interpolates y[0], y[1], ...
	 * y[n]. The first segment is returned as C[0].a + C[0].b*u + C[0].c*u^2 +
	 * C[0].d*u^3 0 <=u <1 the other segments are in C[1], C[2], ... C[n-1]
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
		 * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
		 * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
		 * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])] by using
		 * row operations to convert the matrix to upper triangular and then
		 * back substitution. The D[i] are the derivatives at the knots.
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
			C[i] = new Cubic((float) x[i], D[i], 3 * (x[i + 1] - x[i]) - 2 * D[i] - D[i + 1],
					2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
		}
		return C;
	}

	/**
	 * Calculates the natural cubic spline that interpolates y[0], y[1], ...
	 * y[n]. The first segment is returned as C[0].a + C[0].b*u + C[0].c*u^2 +
	 * C[0].d*u^3 0 <=u <1 the other segments are in C[1], C[2], ... C[n-1]
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
		 * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
		 * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
		 * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])] by using
		 * row operations to convert the matrix to upper triangular and then
		 * back substitution. The D[i] are the derivatives at the knots.
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
			C[i] = new Cubic((float) x[i], D[i], 3 * (x[i + 1] - x[i]) - 2 * D[i] - D[i + 1],
					2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
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
			 * very crude technique just break each segment up into steps lines
			 */
			Polygon p = new Polygon();
			p.addPoint((int) Math.round(X[0].eval(0)), (int) Math.round(Y[0].eval(0)));
			for (int i = 0; i < X.length; i++) {
				for (int j = 1; j <= steps; j++) {
					float u = j / (float) steps;
					p.addPoint(Math.round(X[i].eval(u)), Math.round(Y[i].eval(u)));
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
			// Need to convert to float[]
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
			 * very crude technique just break each segment up into steps lines
			 */
			Polygon p = new Polygon();
			p.addPoint((int) Math.round(X[0].eval(0)), (int) Math.round(Y[0].eval(0)));
			for (int i = 0; i < X.length; i++) {
				for (int j = 1; j <= steps; j++) {
					float u = j / (float) steps;
					p.addPoint(Math.round(X[i].eval(u)), Math.round(Y[i].eval(u)));
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
	 * @param precision for dividing floating coordinates to become int, e.g
	 *            0.01 means spline to be calculated with coordinates * 100
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
			 * very crude technique just break each segment up into steps lines
			 */
			Polygon p = new Polygon();
			p.addPoint((int) Math.round(X[0].eval(0)), (int) Math.round(Y[0].eval(0)));
			for (int i = 0; i < X.length; i++) {
				for (int j = 1; j <= steps; j++) {
					float u = j / (float) steps;
					p.addPoint(Math.round(X[i].eval(u)), Math.round(Y[i].eval(u)));
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
	 * Sets the number of points (steps) interpolated on the curve between the
	 * original points to draw it as a polyline.
	 * 
	 * @param steps The steps to set
	 */
	public void setSteps(int steps) {
		this.steps = steps > 0 ? steps : 12;
	}

	/**
	 * Set the steps and return this object.
	 * 
	 * @param steps
	 * @return this
	 */
	public NatCubicSpline withSteps(int steps) {
		setSteps(steps);
		return this;
	}

	private int steps = 12;

	/**
	 * Moved from an outside class, the closed case of a NatCubicSpline.
	 */
	public static class CLOSED extends NatCubicSpline {

		/**
		 * Calculates the closed natural cubic spline that interpolates x[0],
		 * x[1], ... x[n]. The first segment is returned as C[0].a + C[0].b*u +
		 * C[0].c*u^2 + C[0].d*u^3 0 <=u <1 the other segments are in C[1],
		 * C[2], ... C[n]
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
			 * |D[1]| |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . |
			 * | . | | 1 4 1| | . | |3(x[n] - x[n-2])| [1 1 4] [D[n]] [3(x[0] -
			 * x[n-1])] by decomposing the matrix into upper triangular and
			 * lower matrices and then back substitution. See Spath "Spline
			 * Algorithms for Curves and Surfaces" pp 19--21. The D[i] are the
			 * derivatives at the knots.
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
				C[k] = new Cubic((float) x[k], D[k], 3 * (x[k + 1] - x[k]) - 2 * D[k] - D[k + 1],
						2 * (x[k] - x[k + 1]) + D[k] + D[k + 1]);
			}
			C[n] = new Cubic((float) x[n], D[n], 3 * (x[0] - x[n]) - 2 * D[n] - D[0], 2 * (x[n] - x[0]) + D[n] + D[0]);
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

	/**
	 * A cubic polynomial
	 */
	class Cubic {

		float a, b, c, d; /* a + b*u + c*u^2 +d*u^3 */

		/**
		 * Constructor.
		 * 
		 * @param a
		 * @param b
		 * @param c
		 * @param d
		 */
		public Cubic(float a, float b, float c, float d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		/**
		 * evaluate cubic for this value.
		 * 
		 * @param u
		 * @return float
		 */
		public float eval(float u) {
			return (((d * u) + c) * u + b) * u + a;
		}
	}
}