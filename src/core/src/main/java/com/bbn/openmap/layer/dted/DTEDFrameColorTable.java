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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameColorTable.java,v $
// $RCSfile: DTEDFrameColorTable.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import java.awt.Color;

import com.bbn.openmap.util.Debug;

/**
 * Creates the colors used for displaying the DTED images. The default
 * mode is to use greyscale colors.
 */
public class DTEDFrameColorTable {

    public final static int DTED_COLORS = 216;
    public final static int NUM_ELEVATION_COLORS = 16;
    public final static int DEFAULT_OPAQUENESS = 255;
    /** the colors in use right now. */
    public Color[] colors;
    /** The colored colortable. */
    protected Color[] ccolors = null;
    /** The greyscale colortable. */
    protected Color[] gcolors = null;
    /** opaqueness should be a value between 0 (clear) and 255 (opaque) */
    protected int opaqueness = 255;
    /** Flag to indicate which colortable to use - color or greys. */
    protected boolean greyScale = true;
    /** adjustment is set up for values between 1-5. */
    public int adjustment = 3;

    int[] elevation_color_cutoff = { 0, 50, 100, 200, 400, 800, 1200, 1600,
            2000, 3000, 4000, 6000, 8000, 10000, 12000, 33000 };

    //////////////////////////////////////////
    ///// OLD settings, for a different feel.
    //     int[] elevation_color_cutoff = {0, 0, 50, 100, 200, 400, 800,
    // 1200, 1600,
    //                                  2000, 3000, 4000, 6000, 8000, 10000, 33000};
    // Bright colors - green - yellow- red
    //     int[] reds[] = { 191, 10, 56, 96, 112, 128, 160, 208,
    //                    224, 225, 255, 240, 240, 240, 225, 208};
    //     int[] greens[] = {239, 154, 166, 192, 208, 224, 224, 208,
    //                    224, 236, 186, 176, 144, 112, 96, 80};
    //     int[] blues[] = { 255, 0, 0, 0, 0, 0, 0, 0,
    //                      0, 116, 102, 0, 0, 0, 0, 0};
    //////////////////////////////////////////

    // Natural kinda colors green-light green-green grey-tan-to white
    int[] reds = { 191, 20, 40, 60, 110, 140, 190, 225, 179, 159, 163, 178,
            185, 215, 217, 243 };
    int[] greens = { 239, 95, 102, 128, 153, 175, 200, 200, 158, 142, 152, 165,
            165, 205, 217, 243 };
    int[] blues = { 250, 70, 80, 100, 130, 150, 150, 155, 77, 51, 51, 77, 112,
            140, 217, 230 };

    /** Default setting is 216 colors, and greyscale. */
    public DTEDFrameColorTable() {
        this(216, 255, true);
    }

    public DTEDFrameColorTable(int num_colors) {
        this(num_colors, 255, true);
    }

    public DTEDFrameColorTable(int num_colors, int opaque, boolean greyscale) {
        opaqueness = opaque;
        ccolors = createColors(num_colors, adjustment);
        gcolors = createGreyScaleColors(num_colors);
        greyScale = greyscale;

        if (greyScale)
            colors = gcolors;
        else
            colors = ccolors;
    }

    public DTEDFrameColorTable(DTEDFrameColorTable cTable) {
        opaqueness = cTable.getOpaqueness();
        ccolors = cTable.getCColors();
        gcolors = cTable.getGColors();
        setGreyScale(cTable.getGreyScale());

        for (int i = 0; i < cTable.colors.length; i++)
            colors[i] = cTable.colors[i];
    }

    public int getOpaqueness() {
        return opaqueness;
    }

    public void setOpaqueness(int opaque) {
        int i;
        opaqueness = opaque;
        Color tc; // tmp color
        if (ccolors != null) {
            for (i = 0; i < ccolors.length; i++) {
                tc = ccolors[i];
                ccolors[i] = new Color(tc.getRed(), tc.getGreen(), tc.getBlue(), opaqueness);
            }
        }
        if (gcolors != null) {
            for (i = 0; i < gcolors.length; i++) {
                tc = gcolors[i];
                gcolors[i] = new Color(tc.getRed(), tc.getGreen(), tc.getBlue(), opaqueness);
            }
        }
    }

    public Color[] getCColors() {
        return ccolors;
    }

    public Color[] getGColors() {
        return gcolors;
    }

    public void setGreyScale(boolean greyscale) {
        greyScale = greyscale;
        if (greyScale) {
            colors = gcolors;
        } else {
            colors = ccolors;
        }
    }

    public boolean getGreyScale() {
        return greyScale;
    }

    public int numColors() {
        return colors.length;
    }

    public int colorValue(int color_index) {
        if ((color_index > 0) && (color_index < colors.length))
            return colors[color_index].getRGB();
        else
            return -1;
    }

    protected Color[] createGreyScaleColors(int num_colors) {
        if (num_colors == 0) {
            num_colors = 216;
        }
        Color[] tempColors = new Color[num_colors];

        int grey_interval = 256 / num_colors;

        for (int i = 0; i < num_colors; i++) {

            if (i == 0)
                tempColors[i] = new Color(191, 239, 255, 0);

            else {
                int color = (i * grey_interval) + (grey_interval / 2);
                tempColors[i] = new Color(color, color, color, opaqueness);
            }
        }
        return tempColors;
    }

    protected Color[] createColors(int num_colors, int adjustment) {
        if (num_colors == 0) {
            num_colors = 216;
        }
        Color[] tempColors = new Color[num_colors];
        int ncolors = NUM_ELEVATION_COLORS;
        // How many versions of each color to make up, for sloping
        int num_loops = 1;
        int modifier = (5 - adjustment) * 4;
        int red, green, blue;
        // Re-adjust the number of colors to match the number of
        // colors
        // available.
        if (num_colors >= NUM_ELEVATION_COLORS * 3) {
            ncolors = NUM_ELEVATION_COLORS * 3;
            num_loops = 3;
        } else if (num_colors >= NUM_ELEVATION_COLORS * 2) {
            ncolors = NUM_ELEVATION_COLORS * 2;
            num_loops = 2;
        }

        tempColors = new Color[ncolors];

        if (Debug.debugging("dteddetail"))
            Debug.output("DTEDFrameColortable: Setting number of colors to "
                    + ncolors);

        for (int j = 0; j < num_loops; j++) {
            if (Debug.debugging("dteddetail"))
                Debug.output("dted_raster: Setting round " + j + " of colors.");
            // Color the 0 index (and the multiples) to be clear water
            tempColors[(NUM_ELEVATION_COLORS * j)] = new Color(191, 239, 255, 0);

            for (int i = 1; i < NUM_ELEVATION_COLORS; i++) {
                switch (j) {
                case 0:
                    red = reds[i] - (20 - modifier) / 2;
                    green = greens[i] - (20 - modifier) / 2;
                    blue = blues[i] - (20 - modifier) / 2;
                    tempColors[i] = new Color(red, green, blue, opaqueness);
                    break;
                case 1:
                    red = reds[i] - (20 - modifier);
                    green = greens[i] - (20 - modifier);
                    blue = blues[i] - (20 - modifier);
                    tempColors[i + NUM_ELEVATION_COLORS] = new Color(red, green, blue, opaqueness);
                    break;
                case 2:
                    red = reds[i];
                    green = greens[i];
                    blue = blues[i];
                    tempColors[i + (NUM_ELEVATION_COLORS * 2)] = new Color(red, green, blue, opaqueness);
                    break;

                //  These settings are the original ones, where flat
                // lands get
                //  the original color, and slopes are changed color.
                // In the
                //  above settings, the positive slope gets the
                // original
                //  color, the level gets a darker color, and the
                // negative
                //  slope gets a twice darker color.
                //       case 0:
                //      colors_[i].red = reds[i];
                //      colors_[i].green = greens[i];
                //      colors_[i].blue = blues[i];
                //      break;
                //       case 1:
                //      colors_[i + NUM_ELEVATION_COLORS].red =
                // reds[i]-(20-modifier);
                //      colors_[i + NUM_ELEVATION_COLORS].green =
                // greens[i]-(20-modifier);
                //      colors_[i + NUM_ELEVATION_COLORS].blue =
                // blues[i]-(20-modifier);
                //      break;
                //       case 2:
                //      colors_[i + (NUM_ELEVATION_COLORS*2)].red =
                // reds[i]+(5);//20-modifier);
                //      colors_[i + (NUM_ELEVATION_COLORS*2)].green =
                // greens[i]+(5);//20-modifier);
                //      colors_[i + (NUM_ELEVATION_COLORS*2)].blue =
                // blues[i]+(5);//20-modifier);
                //      break;

                default:
                    break;
                }
            }
        }
        return tempColors;
    }

    public static void main(String args[]) {
        Debug.init();
        if (args.length < 1) {
            System.out.println("DTEDFrameColorTable:  Need a number.");
            System.exit(0);
        }

        System.out.println("DTEDFrameColorTable: " + args[0]);
        int nColors = new Integer(args[0]).intValue();
        DTEDFrameColorTable ct = new DTEDFrameColorTable(nColors);
        System.out.println(ct);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("DTEDFrameColortable: \n");
        for (int i = 0; i < colors.length; i++) {
            s.append("OMColor ").append(i).append(": alpha = ").append(colors[i].getAlpha());
            s.append(", red = ").append(colors[i].getRed());
            s.append(", green = ").append(colors[i].getGreen());
            s.append(", blue = ").append(colors[i].getBlue()).append("\n");
        }
        return s.toString();
    }

}