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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDSlopeGenerator.java,v $
// $RCSfile: DTEDSlopeGenerator.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.grid.ElevationColors;
import com.bbn.openmap.omGraphics.grid.SlopeGenerator;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;

/**
 * The DTEDSlopeGenerator is an extension to the SlopeGenerator that
 * uses OMScalingRasters for images created from the DTED data. It
 * caches the image and will reuse it if the projection parameters
 * allow it. It's needed because it assumes that the OMGrid used is a
 * OMDTEDGrid, which has the lat/lons of the lower left and upper
 * right corners of the DTED frame.
 */
public class DTEDSlopeGenerator extends SlopeGenerator {

    public DTEDSlopeGenerator() {}

    public DTEDSlopeGenerator(ElevationColors elevColors) {
        super(elevColors);
    }

    /**
     * Called from within generate to create an OMRaster object for
     * the OMGrid. This method exists to make it easier to extend this
     * class to create an OMRaster as needed.
     */
    protected OMRaster getRaster(OMGrid grid) {
        if (grid instanceof OMDTEDGrid) {
            OMDTEDGrid dGrid = (OMDTEDGrid) grid;

            // This is probably too expensive for our tastes, since it
            // could be keeping two images around. One for the
            // created image, and one for the scaled image.
            //             raster = new OMScalingRaster(dGrid.getUpperLat(),
            // dGrid.getLeftLon(),
            //                                          dGrid.getLowerLat(), dGrid.getRightLon(),
            //                                          dGrid.width, dGrid.height,
            //                                          new int[dGrid.width*dGrid.height]);

            raster = new OMRaster(dGrid.getUpperLat(), dGrid.getLeftLon(), dGrid.width, dGrid.height, new int[dGrid.width
                    * dGrid.height]);
            return raster;
        } else {
            raster = null;
            return super.getRaster(grid);
        }
    }

    /**
     * The cached OMScalingRaster, which can be reused instead of
     * regenerated in some projection circumstances.
     */
    protected OMRaster raster;

    /**
     * The scale at which the cached raster was generated.
     */
    protected float generatedScale = Float.MAX_VALUE;

    /**
     * Called from the OMGrid.generate() method to tell the generator
     * to create something to represent the grid contents.
     */
    public OMGraphic generate(OMGrid grid, Projection proj) {
        // Make a decision based on the last projection to see if we
        // should generate a new raster from the data with better
        // detail.

        // We used to keep the same OMScalingRaster here if the scale
        // number was larger, but I think that might be too much
        // memory usage, more so if the scales are pretty close and
        // you have two images pretty much the same size for each dted
        // frame. So we reuse if the scales match.
        if (raster != null && proj instanceof EqualArc
                && proj.getScale() == generatedScale && !isIncompleteImage()) {
            raster.generate(proj);
            return raster;
        } else {
            generatedScale = proj.getScale();
            return super.generate(grid, proj);
        }
    }
}

