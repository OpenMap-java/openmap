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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/GreyscaleSlopeColors.java,v $
// $RCSfile: GreyscaleSlopeColors.java,v $
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
 * Creates the colors used for displaying the DTED images. The default
 * mode is to use greyscale colors.
 */
public class GreyscaleSlopeColors implements ElevationColors {

    public final static int NUM_COLORS = 256;
    public final static int NUM_ELEVATION_COLORS = 25;
    public final static int DEFAULT_OPAQUENESS = 255;
    public Color waterColor = new OMColor(0x00bfefff);

    /** the colors in use right now, ARGB values. */
    public int[] colors;
    /** adjustment is set up for values between 1-5. */
    public int adjustment = 3;

    /** Default setting is 216 colors, and greyscale. */
    public GreyscaleSlopeColors() {
        getColors();
    }

    /**
     * Get a List of colors.
     */
    public int[] getColors() {
        if (colors == null) {
            colors = createGreyscaleColors(NUM_ELEVATION_COLORS,
                    DEFAULT_OPAQUENESS);
        }
        return colors;
    }

    /**
     * Set the List of colors.
     */
    public void setColors(int[] clrs) {
        colors = clrs;
    }

    public int[] getColortable() {
        return getColors();
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
     * Get the Color for the given elevation, with the provided units.
     * The slope of the land, from the northwest to the southeast, is
     * provided.
     */
    public Color getColor(int elevation, Length units, double slope) {
        return new Color(getARGB(elevation, units, slope));
    }

    /**
     * Get the int argb value for a given elevation, with the provided
     * units. The slope of the land, from the northwest to the
     * southeast, is provided in case that should matter.
     */
    public int getARGB(int elevation, Length units, double slope) {
        //         Debug.output("slope = " + slope + ", elevation = " +
        // elevation);

        float value = (float) (((colors.length - 1) / 2) + slope);

        // not water, but close in the colormap - max dark
        if (slope != 0 && value < 1)
            value = 1;
        if (elevation == 0)
            value = 0; // water?!?

        if (value > (colors.length - 1)) {
            value = colors.length - 1; // max bright
        }

        return colors[(int) value];
    }

    public int numColors() {
        return colors.length;
    }

    public int[] createGreyscaleColors(int num_colors, int opaqueness) {
        int[] tempColors = new int[num_colors];
        if (num_colors == 0) {
            num_colors = NUM_ELEVATION_COLORS;
        }

        int grey_interval = 256 / num_colors;

        for (int i = 0; i < num_colors; i++) {
            if (i == 0) {
                tempColors[i] = waterColor.getRGB();
            } else {
                int color = (i * grey_interval) + (grey_interval / 2);
                tempColors[i] = new Color(color, color, color, opaqueness).getRGB();
            }
        }
        return tempColors;
    }
}