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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/ColoredShadingColors.java,v $
// $RCSfile: ColoredShadingColors.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/22 18:46:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.awt.Color;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Length;

/**
 * A holder for handling colors for elevations. This ElevationColors object also
 * takes into account slope when providing pixel int values.
 */
public class ColoredShadingColors implements ElevationColors {

    protected ColorHelper[] colors;
    public Color waterColor = OMColor.clear;

    public final int[] DEFAULT_COLOR_CUTOFFS = { 0, 50, 100, 200, 400, 800,
            1200, 1600, 2000, 3000, 4000, 6000, 8000, 10000, 12000, 33000 };

    public final int[] DEFAULT_COLORS = { 0x00bfeffa, 0xff145f46, 0xff286650,
            0xff3c8064, 0xff6e9982, 0xff8caf96, 0xffbec896, 0xffe1c89b,
            0xffb39e4d, 0xff9f8e33, 0xffa39833, 0xffb2a54d, 0xffb9a570,
            0xffd7cd8c, 0xffd9d9d9, 0xfff3f3e6 };

    /**
     * Slope color adjustment, gets subtracted from the color for slopes of zerp
     * or less. Default is 0x0a0a0a.
     */
    protected int slopeColorAdjustment = 0x000a0a0a;

    /*
     * Default constructor, used default elevation cutoffs and color values.
     */
    public ColoredShadingColors() {
        getColors();
    }

    /**
     * Constructor to provide unique elevation cutoffs and colors.
     */
    public ColoredShadingColors(ColorHelper[] colors) {
        this.colors = colors;
    }

    /**
     * Set the color factor to subtract from the colors for level or negative
     * slopes. Should be the same for each byte, or the colors will change
     * drastically.
     */
    public void setSlopeColorAdjustment(int adj) {
        slopeColorAdjustment = adj;
    }

    public int getSlopeColorAdjustment() {
        return slopeColorAdjustment;
    }

    /**
     * Get an array of colors with elevation values.
     */
    public ColorHelper[] getColors() {
        if (colors == null) {
            colors = createDefaultColors();
        }
        return colors;
    }

    /**
     * Set the list of ColorHelpers. The elevations of the ColorHelpers should
     * be in order, from lowest to highest.
     */
    public void setColors(ColorHelper[] clrs) {
        colors = clrs;
    }

    public int[] getColortable() {
        ColorHelper[] helpers = getColors();
        int[] cs = new int[helpers.length];
        for (int i = 0; i < helpers.length; i++) {
            cs[i] = helpers[i].value;
        }
        return cs;
    }

    /**
     * Set the color to use for water/invalid data, zero elevation.
     */
    public void setWaterColor(Color water) {
        waterColor = water;
    }

    /**
     * Get the color to use for water/invalid data, zero elevation.
     */
    public Color getWaterColor() {
        return waterColor;
    }

    /**
     * Get the Color for the given elevation, with the provided units. The slope
     * of the land, from the northwest to the southeast, is provided.
     */
    public Color getColor(int elevation, Length units, double slope) {
        return new Color(getARGB(elevation, units, slope));
    }

    /**
     * Get the int argb value for a given elevation, with the provided units.
     * The slope of the land, from the northwest to the southeast, is provided
     * in case that should matter. It's not used in this version of the
     * algorithm.
     */
    public int getARGB(int elevation, Length units, double slope) {

        if (elevation < 0 || colors == null) {
            return waterColor.getRGB();
        }

        float elev = units.toRadians(elevation);
        float numCutoffs = colors.length;
        int i = 0;

        while (i < numCutoffs && colors[i] != null
                && colors[i].height < elev) {
            i++;
        }

        int value = colors[i].value;

        if (slope < 0) {
            value -= 2 * slopeColorAdjustment;
        } else if (slope == 0) {
            value -= slopeColorAdjustment;
        }

        return value;
    }

    public ColorHelper[] createDefaultColors() {
        int number = DEFAULT_COLOR_CUTOFFS.length;
        ColorHelper[] helpers = new ColorHelper[number];

        for (int i = 0; i < number; i++) {
            helpers[i] = new ColorHelper(Length.FEET.toRadians((float) DEFAULT_COLOR_CUTOFFS[i]), DEFAULT_COLORS[i]);
        }
        return helpers;
    }

    /**
     * A helper class that associates an int color value with an elevation.
     * Elevation has to be in radians.
     */
    public static class ColorHelper {
        /** height value in radians. */
        public double height = 0; // in radians
        /** Integer ARGB color value. */
        public int value = 0;

        /**
         * Create a ColorHelper.
         * 
         * @param radianHeight minimum elevation for color use, in radians.
         * @param val integer ARGB color value
         */
        public ColorHelper(double radianHeight, int val) {
            height = radianHeight;
            value = val;
        }
    }
}