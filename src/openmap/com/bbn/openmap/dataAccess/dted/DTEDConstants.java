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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDConstants.java,v $
// $RCSfile: DTEDConstants.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

/**
 * An interface that contains constants used by various classes in the
 * package.
 */
public interface DTEDConstants {
    // Types of slope shading
    /** Empty image. */
    public static final int NOSHADING = 0;
    /** Gray scale slope shading, sun from the Northwest. */
    public static final int SLOPESHADING = 1;
    /** Banded contour coloring, contour based on meters. */
    public static final int BANDSHADING = 2;
    /** Test markings, for the boundary of the subframe. */
    public static final int BOUNDARYSHADING = 4;
    /**
     * Colorized slope shading. Color bands are based on elevation,
     * and are accented by shaded indications.
     */
    public static final int COLOREDSHADING = 5;
    /** DTED LEVEL 0, 1km posts. */
    public static final int LEVEL_0 = 0;
    /** DTED LEVEL 1, 100m posts. */
    public static final int LEVEL_1 = 1;
    /** DTED LEVEL 2, 30m posts. */
    public static final int LEVEL_2 = 2;
    /** Default height between bands in band views. */
    public static final int DEFAULT_BANDHEIGHT = 25;
    /** Default contrast setting for slope shading. */
    public static final int DEFAULT_SLOPE_ADJUST = 3;

}