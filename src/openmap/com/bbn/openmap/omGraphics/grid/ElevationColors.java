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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/ElevationColors.java,v $
// $RCSfile: ElevationColors.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/24 03:38:44 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.grid;

import java.awt.Color;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.Debug;

/**
 * An interface that the SlopeGenerator uses to get colors for
 * different elevations and slopes.
 */
public interface ElevationColors { 

    /**
     * Get the Color for the given elevation, with the provided units.
     * The slope of the land, from the northwest to the southeast, is
     * provided.
     */
    public Color getColor(int elevation, Length units, double slope);

    /**
     * Get the int argb value for a given elevation, with the provided
     * units.  The slope of the land, from the northwest to the
     * southeast, is provided in case that should matter.
     */
    public int getARGB(int elevation, Length units, double slope);

}
