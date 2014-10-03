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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMColor.java,v $
// $RCSfile: OMColor.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;

/**
 * The OMColor exists to provide a way to make a color that can have
 * some degree of transparency. The class lets you set the alpha value
 * of the color which lets the color be invisible (0) to opaque (255).
 * A while ago, the java.awt.Graphics class did not support
 * transparency for drawing objects (OMLines, OMRects, OMCircles,
 * etc.) but the transparent colors work for OMBitmap and OMRaster
 * pixel values.
 * <p>
 * 
 * The OMColor object captures all calls that reference the
 * package-internal <code>java.awt.Color.value</code> slot, and
 * re-route them through the local argb slot.
 * <p>
 * 
 * NOTE concerning the OpenMap 4.0 release. As of 4.0, OpenMap now has
 * a minimum jdk 1.2 requirement, which means that OMColor seems to
 * duplicate java.awt.Color. We're going to keep this class around,
 * however, in case someone needs a mutable Color.
 * 
 * @see com.bbn.openmap.util.ColorFactory
 */
public class OMColor extends Color {

    /**
     * Does this Java version support alpha for java.awt.Colors?. This
     * is true if java.version &gt;= 1.2, and false otherwise.
     */
//    public final static transient boolean nativeAlpha;

    /**
     * Default transparent color. Clear is a standard color that has
     * all the bit values set to zero. This will give you a
     * transparent pixel for images, and black for situations where
     * transparency is not supported.
     */
    public final static transient Color clear = new Color(0, true);

    /**
     * A constructor object which can be used to create new
     * java.awt.Colors for <code>java.version &gt;= 1.2</code>.
     */
//    public final static transient Constructor alphaValueConstructor;

    // check to see if alpha values are supported by the underlying
    // java.awt.Color class.
//    static {
//        Color c;
//        Constructor cons;
//        boolean b;
//        try {
//            // we prefer the Java 2 solution (java.awt.Color supports
//            // alpha)
//            cons = Color.class.getConstructor(new Class[] { Integer.TYPE,
//                    Boolean.TYPE });
//            if (com.bbn.openmap.util.Debug.debugging("env")) {
//                System.out.println("Alpha supported by java.awt.Color");
//            }
//            c = (Color) cons.newInstance(new Object[] { new Integer(0),
//                    new Boolean(true) });
//            b = true;
//        } catch (Exception e) {
//            // this means we need to hack alphas.
//            if (com.bbn.openmap.util.Debug.debugging("env")) {
//                System.out.println("Alpha NOT SUPPORTED by java.awt.Color");
//            }
//            cons = null;
//            c = new OMColor(0);
//            b = false;
//        }
//        clear = c;
//        alphaValueConstructor = cons;
//        nativeAlpha = b;
//    }

    /**
     * The 32bit ARGB value used.
     */
    protected int argb;

    /**
     * Create a color with the specified alpha, red, green, and blue
     * components. The four arguments must each be in the range 0-255. // *
     * 
     * @deprecated This function does not correctly override the JDK // *
     *             1.2 java.awt.Color constructor with the same
     *             type/number of // * arguments. It should be
     *             OMColor(int r, int g, int b, int a).
     */
    public OMColor(int a, int r, int g, int b) {
        super(r, g, b);//HACK unnecessary?...
        argb = (a << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)
                | ((b & 0xFF) << 0);
    }

    /**
     * Create a color with the specified ARGB (Alpha, Red, Green, and
     * Blue) values. The alpha component is in bits 24-31 of the
     * argument, the red component is in bits 16-23 of the argument,
     * the green component is in bits 8-15 of the argument, and the
     * blue component is in bits 0-7.
     * 
     * @param argb 32bit Hex ARGB value
     */
    public OMColor(int argb) {
        super(argb);//HACK unnecessary?...
        this.argb = argb;
    }

    /**
     * Create a color with the specified red, green, and blue values,
     * where each of the values is in the range 0.0-1.0. The value 0.0
     * indicates no contribution from the primary color component. The
     * value 1.0 indicates the maximum intensity of the primary color
     * component. // *
     * 
     * @deprecated This function does not correctly override the JDK // *
     *             1.2 java.awt.Color constructor with the same
     *             type/number of // * arguments. It should be
     *             OMColor(float r, float g, float b, float a).
     */
    public OMColor(float a, float r, float g, float b) {
        super(r, g, b);//HACK unnecessary?...
        argb = (((int) (a * 255)) << 24) | (((int) (r * 255) & 0xFF) << 16)
                | (((int) (g * 255) & 0xFF) << 8)
                | (((int) (b * 255) & 0xFF) << 0);
    }

    /**
     * Get the ARGB (alpha, red, green and blue) value representing
     * the color in the default RGB ColorModel. The alpha, red, green,
     * and blue components of the color are each scaled to be a value
     * between 0 (absence of the color) and 255 (complete
     * saturation). Bits 24-31 of the returned integer are the alpha
     * value, bits 16-23 are the red value, bit 8-15 are the green
     * value, and bits 0-7 are the blue value.
     * 
     * @return the integer value of the color.
     */
    public int getRGB() {
        return argb;
    }

    /**
     * Change the ARGB value of the color the input value. See the
     * constructor comments that accepts the int parameter.
     * 
     * @param value the transparency value between 0-255.
     */
    public void setRGB(int value) {
        argb = value;
    }

    /**
     * Return the red value of the color (the value of the 16-23
     * bits).
     * 
     * @return the integer red value.
     */
    public int getRed() {
        return (argb >> 16) & 0xff;
    }

    /**
     * Set the red value of the OMColor.
     * 
     * @param value the red value between 0-255.
     */
    public void setRed(int value) {
        argb = (argb & 0xFF00FFFF) | ((value & 0xff) << 16);
    }

    /**
     * Return the green value of the color (the value of the 8-15
     * bits).
     * 
     * @return the integer green value.
     */
    public int getGreen() {
        return (argb >> 8) & 0xff;
    }

    /**
     * Set the green value of the OMColor.
     * 
     * @param value the green value between 0-255.
     */
    public void setGreen(int value) {
        argb = (argb & 0xFFFF00FF) | ((value & 0xff) << 8);
    }

    /**
     * Return the blue value of the color (the value of the 0-7 bits).
     * 
     * @return the integer blue value.
     */
    public int getBlue() {
        return argb & 0xff;
    }

    /**
     * Set the blue value of the OMColor.
     * 
     * @param value the blue value between 0-255.
     */
    public void setBlue(int value) {
        argb = (argb & 0xFFFFFF00) | (value & 0xff);
    }

    /**
     * Return the transparency value of the color (the value of the
     * 24-31 bits).
     * 
     * @return the integer transparency value.
     */
    public int getAlpha() {
        return argb >>> 24;
    }

    /**
     * Set the transparency value of the OMColor.
     * 
     * @param value the transparency value between 0-255.
     */
    public void setAlpha(int value) {
        argb = (argb & 0x00FFFFFF) | (value << 24);
    }

    /**
     * Return a color integer that has the transparency alpha value
     * set to a value between 0-255. It cleans out the old alpha
     * setting, and inserts the new one.
     * 
     * @param colorValue the ARGB value of a color to be changed.
     * @param transValue the integer (0-255) representing the
     *        opaqueness of the return value. 0 is transparent, 255 is
     *        opaque.
     * @return the integer color value with the transparency value.
     */
    public static int setTransparentValue(int colorValue, int transValue) {
        return ((0x00FFFFFF & colorValue) | (transValue << 24));
    }

    /**
     * Return a color value that has the transparency alpha value set
     * to a percentage value between 0.0 and 1.0.
     * 
     * @param colorValue the RGB value of a color to be changed.
     * @param transValue the percentage of opaqueness (0-1) of the
     *        return value. 0 is transparent, 1 is opaque.
     * @return the integer color value with the transparency value.
     */
    public static int setTransparentValue(int colorValue, float transValue) {
        return ((0x00FFFFFF & colorValue) | (((int) (transValue * 255)) << 24));
    }

    /**
     * Checks if the color is transparent. The alpha bits (31-24) of
     * the color determine this.
     * 
     * @param value Color to be checked
     * @return true if Color is null or transparent
     */
    public static boolean isClear(Color value) {
        return (value == null) || ((value.getRGB() >>> 24) == 0);
    }

    /**
     * Computes the hash code for this color.
     * 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return getRGB();
    }

    /**
     * Determines whether another object is equal to this color.
     * <p>
     * The result is <code>true</code> if and only if the argument
     * is not <code>null</code> and is a <code>Color</code> object
     * that has the same alpha, red, green, and blue values as this
     * object.
     * 
     * @param obj the Color to compare.
     * @return true if the objects are the same, false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return (((Color) obj).getRGB() == this.getRGB());
    }

    /**
     * Returns a string representation of this color. This method is
     * intended to be used only for debugging purposes, and the
     * content and format of the returned string may vary between
     * implementations. The returned string may be empty but may not
     * be <code>null</code>.
     * 
     * @return a string representation of this color.
     */
    public String toString() {
        return "{" + super.toString() + " a=" + getAlpha() + "}";
    }

}