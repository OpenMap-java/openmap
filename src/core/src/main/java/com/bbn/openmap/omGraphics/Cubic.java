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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/Cubic.java,v $
//$RCSfile: Cubic.java,v $
//$Revision: 1.3 $
//$Date: 2004/10/14 18:06:10 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

/**
 * A cubic polynomial
 */
public class Cubic {

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