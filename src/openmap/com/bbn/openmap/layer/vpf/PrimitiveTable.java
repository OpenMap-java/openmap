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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/PrimitiveTable.java,v $
// $RCSfile: PrimitiveTable.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:08:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Parent class for reading VPF primitive tables.
 */
public abstract class PrimitiveTable extends DcwRecordFile {

    /** the coverage table that we are working for */
    final protected CoverageTable covtable;

    /** the id column position */
    final protected int idColumn;

    /** the tile we are for */
    final protected TileDirectory tile;

    /**
     * Construct a PrimitiveTable.
     * 
     * @param cov the coverage table that is our "parent"
     * @param tile the tile for the table
     * @param tablename must be a 3 character VPF tablename
     * @exception FormatException if something goes wrong opening the file
     */
    public PrimitiveTable(CoverageTable cov, TileDirectory tile,
            String tablename) throws FormatException {
        super(cov.getDataPath() + tile.getPath() + tablename
                + (cov.appendDot ? "." : ""));
        this.idColumn = whatColumn(Constants.ID);
        this.covtable = cov;
        this.tile = tile;
    }

    /**
     * Get the coverage table that we are working on.
     */
    public CoverageTable getCoverageTable() {
        return covtable;
    }

    /**
     * Gets the id column value for the row.
     */
    public int getID(List<Number> l) {
        return l.get(idColumn).intValue();
    }

    /**
     * Gets the tile we are for.
     */
    public TileDirectory getTileDirectory() {
        return tile;
    }

    /**
     * Parse the records for this tile, calling warehouse.createXXXX once for
     * each record.
     * 
     * @param warehouse the warehouse used for createArea calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse) (passed
     *        to warehouse)
     * @see VPFGraphicWarehouse
     */
    abstract public void drawTile(VPFGraphicWarehouse warehouse, double dpplat,
                                  double dpplon, LatLonPoint ll1, LatLonPoint ll2);

    /**
     * Use the warehouse to create a graphic from a feature in the AreaTable.
     * 
     * @param warehouse the warehouse used for createXXXX calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param area a List containing the AreaTable row contents.
     * @param featureType the string representing the feature type, in case the
     *        warehouse wants to do some intelligent rendering.
     * @param primID the primitive ID of the feature, in order to gather attributes if necessary.
     * @see VPFGraphicWarehouse#createEdge
     */
    abstract public OMGraphic drawFeature(VPFFeatureWarehouse warehouse,
                                     double dpplat, double dpplon,
                                     LatLonPoint ll1, LatLonPoint ll2,
                                     List<Object> area, String featureType, int primID);
}