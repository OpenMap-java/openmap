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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/Attic/ContourLineGenerator.java,v $
// $RCSfile: ContourLineGenerator.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The SimpleColorGenerator is an OMGridGenerator that creates an
 * OMRaster out of OMGrid data.
 */
public class ContourLineGenerator extends ElevationMBandGenerator {

    /** The color to use for the contour lines. */
    protected int lineColor = 0xFFFF0000;
    /** The color to use for the background. */
    protected int backColor = 0x00000000;

    /**
     * Going to return an OMRaster.
     * 
     * @param grid the grid to create a raster for.
     * @param proj description of the map.
     */
    public OMGraphic generate(OMGrid grid, Projection proj) {

        Debug.message("grid",
                "ContourLineGenerator: generating OMRaster from grid");

        OMRaster raster = new OMRaster(grid.point1.x, grid.point1.y, grid.width, grid.height, new int[grid.width
                * grid.height]);

        if (grid.height == 0 || grid.width == 0) {
            Debug.message("grid",
                    "ContourLineGenerator: grid height/width ZERO!");
            return raster;
        }

        GridData gd = grid.getData();
        if (!(gd instanceof GridData.Int)) {
            Debug.message("grid",
                    "SimpleColorGenerator: grid doesn't contain integer data.");
            return SinkGraphic.getSharedInstance();
        }

        int rows = grid.getRows();
        int columns = grid.getColumns();
        int[][] data = ((GridData.Int) gd).getData();
        boolean major = grid.getMajor();

        /** lat and lon_intervals are grid point/pixel.. */
        double y_interval = (double) rows / (double) grid.height;
        double x_interval = (double) columns / (double) grid.width;

        Debug.message("grid", "ContourLineGenerator: y_point_interval = "
                + y_interval + ", x_point_interval = " + x_interval);

        /**
         * Right now, if the rendertype of the grid is
         * RENDERTYPE_LATLON, we limit rendering to the CADRG
         * projection.
         */

        int post_x, post_y, value;

        int[][] values = new int[grid.width][grid.height];

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

                    value = calibratePointValue(data[post_x][post_y]);

                } else {
                    if (post_y >= columns)
                        post_y = columns - 1;
                    if (post_x >= rows)
                        post_x = rows - 1;

                    value = calibratePointValue(data[post_y][post_x]);
                }

                values[x][y] = value;

                if (value != 0)
                    raster.setPixel(x, y, checkData(values, x, y));
                else
                    raster.setPixel(x, y, backColor);
            }
        }
        raster.generate(proj);

        return raster;
    }

    /**
     * Takes the value assigned to a pixel, as determined by it's
     * location in the grid, and gives it a color to be painted by. In
     * this case, the pixel is colored according to the elevation
     * value.
     * 
     * @param source a grid point value assigned to the raster pixel.
     * @return the ARGB to color the pixel.
     */
    public int calibratePointValue(int source) {
        return super.calibratePointValue((int) ((float) source * 3.2f));
    }

    protected int checkData(int[][] pastValues, int x, int y) {
        int ret = backColor;
        int checkx, checky, check;

        for (int offx = -1; offx < 1; offx++) {
            for (int offy = -1; offy < 1; offy++) {
                if (offx != 0 && offy != 0) {

                    checkx = x + offx;
                    checky = y + offy;

                    if (!(checkx < 0 || checky < 0)) {

                        check = pastValues[checkx][checky];

                        if (check != 0 && check != pastValues[x][y]) {
                            ret = lineColor;
                            return ret;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * We at least need one generate for XY and OFFSET grids. We will
     * need a generate every time the projection changes for LATLON
     * rendertype grids. So return true for everything.
     */
    public boolean needGenerateToRender() {
        return true;
    }
}

