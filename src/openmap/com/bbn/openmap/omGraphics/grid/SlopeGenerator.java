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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/SlopeGenerator.java,v $
// $RCSfile: SlopeGenerator.java,v $
// $Revision: 1.8 $
// $Date: 2005/12/22 18:46:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.awt.Point;
import java.awt.Shape;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The SlopeGenerator is an OMGridGenerator that looks at elevation
 * data and creates shading images from it. It currently works for
 * short data only, since that what DTED elevation data is. Making it
 * work with different data types is on the to-do list.
 */
public class SlopeGenerator implements OMGridGenerator {

    protected int contrast = 5;
    protected ElevationColors colors = new ColoredShadingColors();

    public SlopeGenerator() {}

    public SlopeGenerator(ElevationColors elevColors) {
        setColors(elevColors);
    }

    public void setColors(ElevationColors elevColors) {
        colors = elevColors;
    }

    public ElevationColors getColors() {
        return colors;
    }

    public void setContrast(int val) {
        if (val > 10 || val < 1) {
            val = 5;
        }
        contrast = val;
    }

    public int getContrast() {
        return contrast;
    }

    /**
     * Called from within generate to create an OMRaster object for
     * the OMGrid. This method exists to make it easier to extend this
     * class to create an OMRaster as needed.
     */
    protected OMRaster getRaster(OMGrid grid) {
        return new OMRaster(grid.point1.x, grid.point1.y, grid.width, grid.height, new int[grid.width
                * grid.height]);
    }

    protected boolean incomplete = false;

    /**
     * A method to check if the last image created was a complete one.
     * If it was incomplete, then that means some pixels of the image
     * were not set because they were outside of the projection.
     */
    public boolean isIncompleteImage() {
        return incomplete;
    }

    /**
     * A more defining API method to get what this SlopeGenerator can
     * create. An OMGridGenerator generates an OMGraphic, this method
     * lets you specifically ask for an OMRaster if that's what you
     * want. If SlopeGenerator is extended so that it doesn't
     * necessarily return an OMRaster from generate, this method
     * checks for that and will return null if generate returns
     * something other than an OMRaster.
     * 
     * @param grid
     * @param proj
     * @return OMRaster that reflects slope information.
     */
    public OMRaster generateRasterForProjection(OMGrid grid, Projection proj) {
        OMGraphic omg = generate(grid, proj);
        if (omg instanceof OMRaster) {
            return (OMRaster) omg;
        } else {
            return null;
        }
    }

    /**
     * Called from the OMGrid.generate() method to tell the generator
     * to create something to represent the grid contents.
     */
    public OMGraphic generate(OMGrid grid, Projection proj) {

        Shape gridShape = grid.getShape();

        // Don't generate the raster if the grid is off-map...
        if (gridShape == null
                || !gridShape.intersects(0,
                        0,
                        proj.getWidth(),
                        proj.getHeight())) {
            if (Debug.debugging("grid")) {
                Debug.output("SlopeGenerator: OMGrid does not overlap map, skipping generation.");
            }
            return SinkGraphic.getSharedInstance();
        }

        OMRaster raster = getRaster(grid);
        incomplete = false;

        if (grid.height == 0 || grid.width == 0) {
            Debug.message("grid", "SlopeGenerator: grid height/width ZERO!");
            return raster;
        }

        GridData gd = grid.getData();

        if (!(gd instanceof GridData.Short)) {
            Debug.message("grid",
                    "SlopeGenerator: grid doesn't contain short data.");
            return SinkGraphic.getSharedInstance();
        }

        int rows = grid.getRows();
        int columns = grid.getColumns();
        short[][] data = ((GridData.Short) gd).getData();
//        boolean major = grid.getMajor();

        double distance = getSlopeRun(grid, getContrast());

        // Used for projections of image coordinates. Reused in the
        // loops to save memory.
        LatLonPoint llp = new LatLonPoint.Double();
        Point point = new Point();
        ElevationColors colors = getColors();

        if (colors == null) {
            return SinkGraphic.getSharedInstance();
        }

        // x is the horizontal pixel being modified
        for (short x = 0; x < grid.width; x++) {
            // Check to make sure the pixels we're calculating are on
            // the map.
            int screenx = (int) grid.point1.getX() + x;
            if (screenx < 0 || screenx > proj.getWidth()) {
                incomplete = true;
                continue;
            }

            for (short y = 0; y < grid.height; y++) {

                // Check to make sure the pixels we're calculating are
                // on the map.
                int screeny = (int) grid.point1.getY() + y;
                if (screeny < 0 || screeny > proj.getHeight()) {
                    incomplete = true;
                    continue;
                }

                // OK, on the map.
                point.setLocation(screenx, screeny);
                llp = proj.inverse(point.x, point.y, new LatLonPoint.Double());

                int yc = (int) Math.round((llp.getLatitude() - grid.getLatitude())
                        / grid.getVerticalResolution());
                int xc = (int) Math.round((llp.getLongitude() - grid.getLongitude())
                        / grid.getHorizontalResolution());

                // If the calculated index is out of the data ranges,
                // push it to the end. Don't blow it off, it will
                // cause unfilled data pixel lines to appear. You
                // only want to blow off pixels that are not on the
                // map, or are never going to be on the map.
                if (yc < 0)
                    yc = 0;
                if (yc > rows - 1)
                    yc = rows - 1;
                if (xc < 0)
                    xc = 0;
                if (xc > columns - 1)
                    xc = columns - 1;

                int elevation = 0;

                // Otherwise, get the elevation for the data point.
                try {
                    elevation = (int) data[xc][yc];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    Debug.output("Error Accessing data array:\n\txc: " + xc
                            + ", yc: " + yc + " for x: " + x + ", y: " + y);
                }

                // Slope shading calculations, get the upper left and
                // lower right data points.

                int xnw = xc - 1;
                int xse = xc + 1;
                // trying to smooth out the edge of the frame
                if (xc == 0 || xnw < 0) {
                    xnw = xc;
                }
                if (xc == columns - 1 || xse > columns - 1) {
                    xse = columns - 1;
                }

                int yse = yc - 1;
                int ynw = yc + 1;
                //  trying to smooth out the edge of the frame by
                // handling the
                //  frame limits
                if (yse < 0) {
                    yse = 0;
                }
                if (yc == rows - 1 || ynw > rows - 1) {
                    ynw = rows - 1;
                }

                // Get the elevation points for the slope measurement
                // points.
                try {
                    short e2 = data[xse][yse]; // down & right
                    // elevation
                    short e1 = data[xnw][ynw]; // up and left
                    // elevation

                    // colormap value darker for negative slopes,
                    // brighter for
                    // positive slopes.
                    // slope relative to nw sun
                    double slope = (e2 - e1) / distance;

                    raster.setPixel(x, y, colors.getARGB(elevation,
                            grid.getUnits(),
                            slope));

                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    Debug.output("Error Accessing data array:\n\txse: " + xse
                            + ", yse: " + yse + "\n\txnw: " + xnw + ", ynw: "
                            + ynw + "\n\tfor x: " + x + ", y: " + y);
                }
            }
        }

        raster.generate(proj);
        if (Debug.debugging("grid"))
            Debug.output("SlopeGenerator: leaving generate");
        return raster;
    }

    public boolean needGenerateToRender() {
        return true;
    }

    /**
     * Method to calculate the run part of the slope (rise over run,
     * right?). The rise is calculated with the elevations of the
     * points to the northwest and southeast of the point of concern,
     * the run is some factor of the distance between those two posts.
     * 
     * @param grid the OMGrid that contains the data, need to get
     *        units.
     * @param contrastAdj contrast adjustment from 1-10, 5 being no
     *        adjustment. 10 is high contrast, 1 is low contrast.
     */
    protected double getSlopeRun(OMGrid grid, int contrastAdj) {
        double modifier = (double) .045;

        for (int h = 0; h < contrastAdj; h++) {
            modifier -= .005;
        }

        // Smaller modifiers result in more contrast. .025, by
        // testing and judgement, looks like a good average, and the
        // values have been set up to provide that with an adjustment
        // of 5. 0 is very high contrast.

        double vRes = grid.getVerticalResolution(); // Degrees per data
        // point
        double hRes = grid.getHorizontalResolution();

        double vResRad = Length.DECIMAL_DEGREE.toRadians(vRes);
        double hResRad = Length.DECIMAL_DEGREE.toRadians(hRes);

        Length units = grid.getUnits();
        double vDist = Math.pow(2.0 * units.fromRadians(vResRad), 2);
        double hDist = Math.pow(2.0 * units.fromRadians(hResRad), 2);

        return modifier * Math.sqrt(vDist + hDist);
    }

}

