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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SColor.java,v $
// $RCSfile: SColor.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist._CColorStub;
import com.bbn.openmap.corba.CSpecialist.CColorPackage.EColor;

/**
 * A SColor is a specialist graphic object parameter that lets you
 * represent a RGB color.
 * 
 * <p>
 * RGB range from 0 - 65535;
 */
public class SColor extends _CColorStub {

    protected EColor self;

    /** construct a (0,0,0) color */
    public SColor() {
        this((short) 0, (short) 0, (short) 0);
    }

    /**
     * construct a color with (r, g, b) for red, green and blue
     * 
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     */
    public SColor(short r, short g, short b) {
        self = new EColor(this, r, g, b);
    }

    /**
     * construct a color with (r, g, b) from ints. This does NO range
     * checking.
     * 
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     */
    public SColor(int r, int g, int b) {
        this((short) r, (short) g, (short) b);
    }

    /**
     * set the red value
     * 
     * @param r the new red value
     */
    public void red(short r) {
        self.red = r;
    }

    /**
     * get the red value
     * 
     * @return the new red value
     */
    public short red() {
        return self.red;
    }

    /**
     * set the green value
     * 
     * @param g the new green value
     */
    public void green(short g) {
        self.green = g;
    }

    /**
     * get the green value
     * 
     * @return the new green value
     */
    public short green() {
        return self.green;
    }

    /**
     * set the blue value
     * 
     * @param b the new blue value
     */
    public void blue(short b) {
        self.blue = b;
    }

    /**
     * get the blue value
     * 
     * @return the new blue value
     */
    public short blue() {
        return self.blue;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("SColor: r = " + red() + ", g = " + green() + ", b = "
                + blue());
        return s.toString();
    }

    /**
     * get the filled representation of this object. <b>modifying this
     * struct will modify the object that created it </b>
     */
    public EColor fill() {
        return self;
    }
}