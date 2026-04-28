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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/SimpleColorGenerator.java,v $
// $RCSfile: SimpleColorGenerator.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/22 18:46:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The SimpleColorGenerator is an OMGridGenerator that creates an OMRaster out
 * of OMGrid data. The OMgrid data is assumed to be color ARGB integer values.
 * Each pixel is colored according to the closest grid data point value.
 */
public class SimpleColorGenerator implements OMGridGenerator {

    /**
     * Going to return an OMRaster, sized to the current projection, and colored
     * according to the colortable. The grid values are indexes into the
     * colortable. The grid should already be generated when it gets here -
     * meaning that the bounds and parameters of the grid, as they apply to the
     * projection, are figured out.
     * 
     * @param grid the grid to create a raster for.
     * @param proj description of the map.
     */
    public OMGraphic generate(OMGrid grid, Projection proj) {

        Debug.message("grid",
                "SimpleColorGenerator: generating OMRaster from grid");

        if (grid.height <= 0 || grid.width <= 0) {
            Debug.message("grid",
                    "SimpleColorGenerator: grid height/width ZERO!");
            return SinkGraphic.getSharedInstance();
        }

        OMRaster raster = new OMRaster(grid.point1.x, grid.point1.y, grid.width, grid.height, new int[grid.width
                * grid.height]);

        int rows = grid.getRows();
        int columns = grid.getColumns();
        RasterHelper rasterHelper = getRasterHelper(grid.getData());
        boolean major = grid.getMajor();

        /** lat and lon_intervals are grid point/pixel.. */
        double y_interval = (double) rows / (double) grid.height;
        double x_interval = (double) columns / (double) grid.width;

        Debug.message("grid", "SimpleColorGenerator: y_point_interval = "
                + y_interval + ", x_point_interval = " + x_interval);

        /**
         * Right now, if the rendertype of the grid is RENDERTYPE_LATLON, we
         * limit sensible rendering to the CADRG/LLXY projections. No warping
         * done.
         */

        int post_x, post_y, value;
        /** Do this pixel by pixel. */
        for (int x = 0; x < grid.width; x++) {
            for (int y = 0; y < grid.height; y++) {

                post_x = (int) Math.round(x_interval * (double) x);
                if (grid.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
                    post_y = (int) Math.round(y_interval
                            * (grid.height - 1 - (double) y));
                } else {
                    post_y = (int) Math.round(y_interval * (double) y);
                }

                if (major == OMGrid.COLUMN_MAJOR) {
                    if (post_x >= columns)
                        post_x = columns - 1;
                    if (post_y >= rows)
                        post_y = rows - 1;

                    value = calibratePointValue(rasterHelper.get(post_x, post_y));
                } else {
                    if (post_y >= columns)
                        post_y = columns - 1;
                    if (post_x >= rows)
                        post_x = rows - 1;

                    value = calibratePointValue(rasterHelper.get(post_y, post_x));
                }

                raster.setPixel(x, y, value);
            }
        }
        raster.generate(proj);

        return raster;
    }

    /**
     * Takes the value assigned to a pixel, as determined by it's location in
     * the grid, and gives a color to paint the pixel. In this case, the grid
     * point value IS the color value.
     * 
     * @param source a grid point value assigned to the raster pixel.
     * @return the ARGB to color the pixel.
     */
    public int calibratePointValue(int source) {
        return source;
    }

    /**
     * We at least need one generate for XY and OFFSET grids. We will need a
     * generate every time the projection changes for LATLON rendertype grids.
     * So return true for everything.
     */
    public boolean needGenerateToRender() {
        return true;
    }

    /**
     * This method creates a set of greyscale colors. It isn't used in this
     * class (SimpleColorGenerator), but it probably will be used in extended
     * classes.
     * 
     * @param num_colors the number of greys needed.
     * @param opaqueness how clear the color should be (0-255, where 0 is
     *        clear).
     * @return an array of color ARGB integers.
     */
    public int[] createGreyscaleColors(int num_colors, int opaqueness) {
        int[] tempColors = new int[num_colors];
        if (num_colors == 0) {
            num_colors = 216;
        }

        int grey_interval = 256 / num_colors;

        for (int i = 0; i < num_colors; i++) {

            if (i == 0) {
                tempColors[i] = ((opaqueness & 0xFF) << 24)
                        | ((191 & 0xFF) << 16) | ((239 & 0xFF) << 8)
                        | ((255 & 0xFF));
            }

            else {
                int color = (i * grey_interval) + (grey_interval / 2);
                tempColors[i] = ((opaqueness & 0xFF) << 24)
                        | ((color & 0xFF) << 16) | ((color & 0xFF) << 8)
                        | ((color & 0xFF));
            }
        }
        return tempColors;
    }

    protected RasterHelper getRasterHelper(GridData gd) {
        if (gd instanceof GridData.Short) {
            return new ShortRasterHelper(((GridData.Short) gd).getData());
        } else if (gd instanceof GridData.Int) {
            return new IntRasterHelper(((GridData.Int) gd).getData());
        } else if (gd instanceof GridData.Float) {
            return new FloatRasterHelper(((GridData.Float) gd).getData());
        } else if (gd instanceof GridData.Byte) {
            return new ByteRasterHelper(((GridData.Byte) gd).getData());
        } else if (gd instanceof GridData.Char) {
            return new CharRasterHelper(((GridData.Char) gd).getData());
        }
        return new DoubleRasterHelper(((GridData.Double) gd).getData());
    }

    protected interface RasterHelper {
        public int get(int x, int y);
    }

    protected class IntRasterHelper implements RasterHelper {
        int[][] data;

        public IntRasterHelper(int[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return data[x][y];
        }
    }

    protected class ShortRasterHelper implements RasterHelper {
        short[][] data;

        public ShortRasterHelper(short[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return data[x][y];
        }
    }

    protected class ByteRasterHelper implements RasterHelper {
        byte[][] data;

        public ByteRasterHelper(byte[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return data[x][y];
        }
    }

    protected class FloatRasterHelper implements RasterHelper {
        float[][] data;

        public FloatRasterHelper(float[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return (int) data[x][y];
        }
    }

    protected class DoubleRasterHelper implements RasterHelper {
        double[][] data;

        public DoubleRasterHelper(double[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return (int) data[x][y];
        }
    }

    protected class CharRasterHelper implements RasterHelper {
        char[][] data;

        public CharRasterHelper(char[][] intData) {
            data = intData;
        }

        public int get(int x, int y) {
            return data[x][y];
        }
    }
}
