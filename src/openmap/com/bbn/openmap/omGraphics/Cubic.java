package com.bbn.openmap.omGraphics;

/** 
 * A cubic polynomial 
 */
public class Cubic {

    float a, b, c, d; /* a + b*u + c*u^2 +d*u^3 */

    /**
     * Constructor.
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
     * @param u
     * @return float
     */
    public float eval(float u) {
	return (((d * u) + c) * u + b) * u + a;
    }
}
