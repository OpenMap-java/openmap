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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/NodeTable.java,v $
// $Revision: 1.5 $ $Date: 2004/10/14 18:06:09 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.*;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.io.FormatException;

/**
 * Read VPF format node tables to generate point graphics for OpenMap.
 */
public class NodeTable extends PrimitiveTable {

    /** the column that our coordinates are in */
    final private int coordColumn;
    /** the column that the first_edge info is in */
    final private int firstEdgeColumn;
    /** the column that containing_face info is in */
    final private int containingFaceColumn;
    /* if true, parse 'end' file; false, parse 'cnd' file */
    final private boolean isEntityNode;

    /**
     * Construct a NodeTable for reading VPF text features.
     * 
     * @param cov the CoverageTable for the tile
     * @param tile the tile to parse
     * @param isEntityNode if true, parse end file; false, parse cnd
     *        file
     * @exception FormatException if something goes wrong reading the
     *            text
     */
    public NodeTable(CoverageTable cov, TileDirectory tile, boolean isEntityNode)
            throws FormatException {
        super(cov, tile, isEntityNode ? Constants.endTableName
                : Constants.cndTableName);
        this.isEntityNode = isEntityNode;
        if ((coordColumn = whatColumn(Constants.ND_COORDINATE)) == -1) {
            throw new FormatException("nodetable couldn't get "
                    + Constants.ND_COORDINATE + " column");
        }
        firstEdgeColumn = whatColumn(Constants.ND_FIRSTEDGE);
        containingFaceColumn = whatColumn(Constants.ND_CONTAININGFACE);
    }

    /**
     * Returns the column that contains first_edge. May return -1
     * indicating the column doesn't exist.
     */
    public int getFirstEdgeColumn() {
        return firstEdgeColumn;
    }

    /**
     * Returns the column that contains containing_face. May return -1
     * indicating the column doesn't exist.
     */
    public int getContainingFaceColumn() {
        return containingFaceColumn;
    }

    /**
     * Parse the node records for this tile, calling
     * warehouse.createNode once for each record in the selection
     * region.
     * 
     * @param warehouse the warehouse used for createNode calls (must
     *        not be null)
     * @param dpplat threshold for latitude thinning (passed to
     *        warehouse)
     * @param dpplon threshold for longitude thinning (passed to
     *        warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @see VPFGraphicWarehouse#createNode
     */
    public void drawTile(VPFGraphicWarehouse warehouse, float dpplat,
                         float dpplon, LatLonPoint ll1, LatLonPoint ll2) {

        float ll1lat = ll1.getLatitude();
        float ll1lon = ll1.getLongitude();
        float ll2lat = ll2.getLatitude();
        float ll2lon = ll2.getLongitude();

        try {
            for (List node = new ArrayList(); parseRow(node);) {
                CoordFloatString coords = (CoordFloatString) node.get(coordColumn);
                float lat = coords.getYasFloat(0);
                float lon = coords.getXasFloat(0);
                if ((lat > ll2lat) && (lat < ll1lat) && (lon > ll1lon)
                        && (lon < ll2lon)) {

                    warehouse.createNode(covtable,
                            this,
                            node,
                            lat,
                            lon,
                            isEntityNode);
                }
            }
        } catch (FormatException f) {
            System.out.println("Exception: " + f.getClass() + " "
                    + f.getMessage());
        }
    }

    /**
     * Use the warehouse to create a graphic from a feature in a
     * NodeTable.
     * 
     * @param warehouse the warehouse used for createNode calls (must
     *        not be null)
     * @param dpplat threshold for latitude thinning (passed to
     *        warehouse)
     * @param dpplon threshold for longitude thinngin (passed to
     *        warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param node a list with the NodeTable row contents.
     * @param featureType the string representing the feature type, in
     *        case the warehouse wants to do some intelligent
     *        rendering.
     * @see VPFGraphicWarehouse#createNode
     */
    public void drawFeature(VPFFeatureWarehouse warehouse, float dpplat,
                            float dpplon, LatLonPoint ll1, LatLonPoint ll2,
                            List node, String featureType) {

        if (warehouse == null) {
            return;
        }

        float ll1lat = ll1.getLatitude();
        float ll1lon = ll1.getLongitude();
        float ll2lat = ll2.getLatitude();
        float ll2lon = ll2.getLongitude();

        CoordFloatString coords = (CoordFloatString) node.get(coordColumn);
        float lat = coords.getYasFloat(0);
        float lon = coords.getXasFloat(0);
        if ((lat > ll2lat) && (lat < ll1lat) && (lon > ll1lon)
                && (lon < ll2lon)) {
            warehouse.createNode(covtable,
                    this,
                    node,
                    lat,
                    lon,
                    isEntityNode,
                    featureType);
        }
    }
}