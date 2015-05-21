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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/EdgeTable.java,v $
// $Revision: 1.7 $ $Date: 2005/12/09 21:08:57 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Read VPF format edge tables to generate polyline graphics for OpenMap.
 */
public class EdgeTable extends PrimitiveTable {

    /** The set of columns that we need */
    private static final String[] edgcolumns = { Constants.ID, Constants.EDG_START_NODE,
            Constants.EDG_END_NODE, Constants.EDG_RIGHT_FACE, Constants.EDG_LEFT_FACE,
            Constants.EDG_RIGHT_EDGE, Constants.EDG_LEFT_EDGE, Constants.EDG_COORDINATES };

    /**
     * Construct an EdgeTable object for a tile of a coverage.
     * 
     * @param cov the coverage table that this tile is part of
     * @param tile the tile to parse
     * @throws FormatException a problem was encountered initializing this tile
     */
    public EdgeTable(CoverageTable cov, TileDirectory tile) throws FormatException {

        super(cov, tile, "edg");
        if (Debug.debugging("vpf")) {
            Debug.output("EdgeTable(): " + filename);
        }
        if (cov.cachedLineSchema == null) {
            cov.cachedLineSchema = lookupSchema(edgcolumns, false);
        }
    }

    /**
     * Get the value of the ID column
     * 
     * @param l the list to retrieve the value from
     */
    public final int getID(List<Number> l) {
        return l.get(covtable.cachedLineSchema[0]).intValue();
    }

    /**
     * Get the value of the start_node column
     * 
     * @param l the list to retrieve the value from
     */
    public final int getStartNode(List<Object> l) {
        return ((Number) l.get(covtable.cachedLineSchema[1])).intValue();
    }

    /**
     * Get the value of the end_node column
     * 
     * @param l the list to retrieve the value from
     */
    public final int getEndNode(List<Object> l) {
        return ((Number) l.get(covtable.cachedLineSchema[2])).intValue();
    }

    /**
     * Get the TilingAdapter for the right_face column
     */
    public final TilingAdapter getRightFaceTilingAdapter() {
        return getTilingAdapter(Constants.EDG_RIGHT_FACE);
    }

    /**
     * Get the TilingAdapter for the left_face column
     */
    public final TilingAdapter getLeftFaceTilingAdapter() {
        return getTilingAdapter(Constants.EDG_LEFT_FACE);
    }

    /**
     * Get the TilingAdapter for the right_edge column
     */
    public final TilingAdapter getRightEdgeTilingAdapter() {
        return getTilingAdapter(Constants.EDG_RIGHT_EDGE);
    }

    /**
     * Get the TilingAdapter for the left_edge column
     */
    public final TilingAdapter getLeftEdgeTilingAdapter() {
        return getTilingAdapter(Constants.EDG_LEFT_EDGE);
    }

    /**
     * Get the value of the coordinates column
     * 
     * @param l the list to retrieve the value from
     */
    public final CoordFloatString getCoordinates(List<Object> l) {
        return (CoordFloatString) l.get(covtable.cachedLineSchema[7]);
    }

    /**
     * get the topology level of the edge table
     * 
     * @return the vpf topology level
     */
    public int topologyLevel() {
        if (covtable.cachedLineSchema[1] == -1) {// no start_node,
            // topology level 0
            return 0;
        }
        if (covtable.cachedLineSchema[3] == -1) {// no right_face,
            // level 1 or 2
            return 2;
        }
        return 3;
    }

    /**
     * get the coverage table that this edge is in
     */
    public CoverageTable getCoverageTable() {
        return covtable;
    }

    /**
     * Parse the edge records for this tile, calling warehouse.createEdge once
     * for each record.
     * 
     * @param warehouse the warehouse used for createEdge calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @see VPFGraphicWarehouse#createEdge
     */
    public void drawTile(VPFGraphicWarehouse warehouse, double dpplat, double dpplon,
                         LatLonPoint ll1, LatLonPoint ll2) {

        if (warehouse == null) {
            return;
        }

        try {
            seekToRow(1);
            for (List<Object> edge = new ArrayList<Object>(); parseRow(edge);) {
                warehouse.createEdge(covtable, this, edge, ll1, ll2, dpplat, dpplon, getCoordinates(edge));
            }
        } catch (FormatException f) {
            System.out.println("Exception: " + f.getClass() + " " + f.getMessage());
        }
    }

    /**
     * Use the warehouse to create a graphic from the edge feature, if you
     * already have the line from the edgetable.
     * 
     * @param warehouse the warehouse used for createEdge calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param edge list containing the EdgeTable row contents.
     * @param featureType the name of the feature. The warehouse may want to do
     *        some intelligent rendering.
     * @param primID the primitive ID of the feature, in order to gather attributes if
     *        necessary.
     * @see VPFGraphicWarehouse#createEdge
     */
    public OMGraphic drawFeature(VPFFeatureWarehouse warehouse, double dpplat, double dpplon,
                                 LatLonPoint ll1, LatLonPoint ll2, List<Object> edge,
                                 String featureType, int primID) {
        if (warehouse != null) {
            return warehouse.createEdge(covtable, this, edge, ll1, ll2, dpplat, dpplon, getCoordinates(edge), featureType, primID);
        }
        return null;
    }

}
