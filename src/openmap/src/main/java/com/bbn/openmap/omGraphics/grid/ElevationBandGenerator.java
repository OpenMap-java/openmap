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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/ElevationBandGenerator.java,v $
// $RCSfile: ElevationBandGenerator.java,v $
// $Revision: 1.2 $
// $Date: 2005/12/22 18:46:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.proj.Length;

/**
 * The ElevationBandGenerator is an OMGridGenerator that creates an OMRaster out
 * of OMGrid data. The OMgrid data is assumed to be meter elevation points, and
 * the colors given to the pixels in the raster reflect the values as dictated
 * by the Length units. The colortable given to the generator determines the
 * colors used, and the band height sets the elevation range for each color.
 */
public class ElevationBandGenerator extends SimpleColorGenerator {

    public static final int DEFAULT_BANDHEIGHT = 100;
    public static final int DEFAULT_ADJUST = 3;

    /**
     * The colors to use. The colors[0] is assumed to be the 0 elevation color,
     * and by default, is a light blue color.
     */
    protected int[] colors;
    /**
     * A number between 1-5 to adjust the contrast a little between the colors.
     */
    protected int adjust = DEFAULT_ADJUST;
    /**
     * The elevation difference between the edges of a color - or how much the
     * elevation must change before a pixel gets the next color.
     */
    protected int bandHeight = DEFAULT_BANDHEIGHT; // meters

    protected Length dataUnits = Length.METER;

    protected Length displayUnits = Length.METER;

    public ElevationBandGenerator() {
        setColortable(createGreyscaleColors(216, 255));
    }

    /**
     * Takes the value assigned to a pixel, as determined by it's location in
     * the grid, and gives it a color to be painted by. In this case, the pixel
     * is colored according to the elevation value.
     * 
     * @param source a grid point value assigned to the raster pixel.
     * @return the ARGB to color the pixel.
     */
    public int calibratePointValue(int source) {
        if (source < -500) {
            return 0; // clear, nothing is that low...
        }

        if (source == 0) {
            return colors[0]; // water blue, assumed.
        }

        if (displayUnits != dataUnits) {
            source = (int) displayUnits.fromRadians(dataUnits.toRadians(source));
        }
        
        // I'm not really sure how all this works out - I wrote it a
        // while ago, and it works, so I'm leaving well enough alone.
        // Some notes from before:

        // Start at the darkest color, and then go up through the
        // colormap for each band height, the start back at the
        // darkest when you get to the last color. To make this
        // more useful, I limit the number of colors (10) used - if
        // there isn;t enough contrast between the colors, you can't
        // see the bands. The contrast adjustment in 24-bit color
        // mode(216 colors) lets you add a few colors.
        int assignment = (int) (((source / bandHeight)
                % (10 - 2 * (3 - adjust)) * (colors.length / (10 - 2 * (3 - adjust)))) + 6);

        return colors[assignment];
    }

    public void setColortable(int[] colors) {
        this.colors = colors;
    }

    public int[] getColortable() {
        return colors;
    }
    
    public void setColors(ElevationColors ec) {
        setColortable(ec.getColortable());
    }

    public void setBandHeight(int height) {
        if (height <= 0)
            height = DEFAULT_BANDHEIGHT;
        bandHeight = height;
    }

    public int getBandHeight() {
        return bandHeight;
    }

    public void setAdjust(int value) {
        if (value <= 0 || value > 5)
            value = DEFAULT_ADJUST;
        adjust = value;
    }

    public int getAdjust() {
        return adjust;
    }

    public Length getDataUnits() {
        return dataUnits;
    }

    public void setDataUnits(Length units) {
        this.dataUnits = units;
    }

    public Length getDisplayUnits() {
        return displayUnits;
    }

    public void setDisplayUnits(Length displayUnits) {
        this.displayUnits = displayUnits;
    }
}
