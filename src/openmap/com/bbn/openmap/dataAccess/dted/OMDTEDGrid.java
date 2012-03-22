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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/OMDTEDGrid.java,v $
// $RCSfile: OMDTEDGrid.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.GridData;

/**
 * The OMDTEDGrid is an extended OMGrid with information about the
 * lat/lons of the corners of the grids, so the generators can use
 * OMScalingRasters for precise alignment.
 */
public class OMDTEDGrid extends OMGrid {

    /**
     * The starting latitude point of the grid. Only relevant when the
     * data points are laid out in a lat/lon grid, or when an x/y grid
     * is anchored to a lat/lon location. DOES NOT follow the OpenMap
     * convention where area object locations are defined by the upper
     * left location - the location of the grid is noted by the lower
     * left corner, because grid data is usually defined by the lower
     * left location. Makes it easier to deal with overlap rows and
     * columns, and to calculate the locations of the rows and
     * columns.
     */
    protected double latitude1;
    /**
     * The starting longitude point of the grid. Only relevant when
     * the data points are laid out in a lat/lon grid, or when an x/y
     * grid is anchored to a lat/lon location. DOES NOT follow the
     * OpenMap convention where area object locations are defined by
     * the upper left location - the location of the grid is noted by
     * the lower left corner, because grid data is usually defined by
     * the lower left location. Makes it easier to deal with overlap
     * rows and columns, and to calculate the locations of the rows
     * and columns.
     */
    protected double longitude1;

    public OMDTEDGrid(double lllat, double lllon, double urlat, double urlon,
                      double vResolution, double hResolution, GridData.Short data) {
        super(lllat, lllon, vResolution, hResolution, data);
        latitude1 = urlat;
        longitude1 = urlon;
    }

    /**
     * Change the upper latitude attribute.
     * 
     * @param value latitude in decimal degrees.
     */
    public void setLowerLat(double value) {
        setLatitude(value);
    }

    /**
     * Get the upper latitude.
     * 
     * @return the latitude in decimal degrees.
     */
    public double getLowerLat() {
        return getLatitude();
    }

    /**
     * Change the western longitude attribute.
     * 
     * @param value the longitude in decimal degrees.
     */
    public void setLeftLon(double value) {
        setLongitude(value);
    }

    /**
     * Get the western longitude.
     * 
     * @return longitude in decimal degrees.
     */
    public double getLeftLon() {
        return getLongitude();
    }

    /**
     * Change the southern latitude attribute.
     * 
     * @param value latitude in decimal degrees.
     */
    public void setUpperLat(double value) {
        if (latitude1 == value)
            return;
        latitude1 = value;
        setNeedToRegenerate(true);
    }

    /**
     * Get the southern latitude.
     * 
     * @return the latitude in decimal degrees.
     */
    public double getUpperLat() {
        return latitude1;
    }

    /**
     * Change the eastern longitude attribute.
     * 
     * @param value the longitude in decimal degrees.
     */
    public void setRightLon(double value) {
        if (longitude1 == value)
            return;
        longitude1 = value;
        setNeedToRegenerate(true);
    }

    /**
     * Get the eastern longitude.
     * 
     * @return longitude in decimal degrees.
     */
    public double getRightLon() {
        return longitude1;
    }

}