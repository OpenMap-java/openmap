// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/Attic/ElevationFBandGenerator.java,v $
// $RCSfile: ElevationFBandGenerator.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import java.awt.Graphics;

/** 
 * The ElevationFBandGenerator is an OMGridGenerator that creates an
 * OMRaster out of OMGrid data.  The OMgrid data is assumed to be
 * meter elevation points, and the colors given to the pixels in the
 * raster reflect the feet values.  The colortable given to the
 * generator determines the colors used, and the band height sets the
 * elevation range for each color.  
 */
public class ElevationFBandGenerator extends ElevationMBandGenerator {

    /** 
     * Takes the value assigned to a pixel, as determined by it's
     * location in the grid, and gives it a color to be painted by.
     * In this case, the pixel is colored according to the elevation
     * value.
     *
     * @param source a grid point value assigned to the raster pixel.
     * @return the ARGB to color the pixel.  
     */
    public int calibratePointValue(int source){
        return super.calibratePointValue((int)((float)source*3.2f));
    }
}

