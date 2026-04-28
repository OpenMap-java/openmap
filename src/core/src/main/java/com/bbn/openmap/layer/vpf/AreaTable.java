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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/AreaTable.java,v $
// $Revision: 1.5 $ $Date: 2005/12/09 21:08:58 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Read VPF format edge, face, and ring tables to generate filled polygon
 * graphics for OpenMap.
 */
public class AreaTable extends PrimitiveTable {

    /** the ring table for the tile we are working on */
    final private DcwRecordFile rings;

    /** the edge table for the tile we are working on */
    final private EdgeTable edges;

    /** if the edgetable is private or shared */
    final private boolean privateEdgeTable;

    /** the column number of our ring ID column */
    final private int ringIDColumn;

    /** the column number of our face ID column */
    final private int faceIDColumn;

    /** the column number of our start_edge column */
    final private int ringStartColumn;

    /** TilingAdapters used to retrieve edge table information */
    final private TilingAdapter edgeRightFaceAdapter, edgeLeftFaceAdapter, edgeRightEdgeAdapter,
            edgeLeftEdgeAdapter;

    /**
     * Construct an AreaTable for a tile.
     * 
     * @param cov the coverage table that is our "parent"
     * @param edg the edge table for the same tile as us (can be null)
     * @param tile the tile to parse
     * @exception FormatException if something goes wrong reading the area
     */
    public AreaTable(CoverageTable cov, EdgeTable edg, TileDirectory tile) throws FormatException {
        super(cov, tile, Constants.faceTableName);

        ringIDColumn = whatColumn(Constants.FAC_RINGPTR);
        privateEdgeTable = (edg == null);
        edges = privateEdgeTable ? new EdgeTable(cov, tile) : edg;

        edgeRightFaceAdapter = edges.getRightFaceTilingAdapter();
        edgeLeftFaceAdapter = edges.getLeftFaceTilingAdapter();
        edgeRightEdgeAdapter = edges.getRightEdgeTilingAdapter();
        edgeLeftEdgeAdapter = edges.getLeftEdgeTilingAdapter();

        if (edges.topologyLevel() != 3) {
            throw new FormatException("AreaTable: need level 3 topology: " + edges.topologyLevel());
        }

        rings = new DcwRecordFile(cov.getDataPath() + tile.getPath() + Constants.ringTableName
                + (cov.appendDot ? "." : ""));

        if ((ringStartColumn = rings.whatColumn(Constants.RNG_STARTEDGE)) == -1) {
            throw new FormatException("ring has no start edge: " + rings.filename);
        }

        if ((faceIDColumn = rings.whatColumn(Constants.RNG_FACEID)) == -1) {
            throw new FormatException("ring has no face_id: " + rings.filename);
        }
    }

    /**
     * Close the files associated with this tile. If an edgetable was passed to
     * the constructor, that table is NOT closed. If this instance created its
     * own edgetable, it IS closed.
     */
    public void close() {
        if (privateEdgeTable) {
            edges.close();
        }
        rings.close();
        super.close();
    }

    /**
     * Computes the full set of points that determine the edge of the area.
     * 
     * @param facevec a row from the VPF face table for this area
     * @param allLLPoints a List that gets modified to contain CoordFloatString
     *        objects defining the area. CoordFloatString objects with a
     *        negative element count (e.g. -3) contain the absolute value of the
     *        count (e.g. 3), but must be traversed in reverse order.
     * @return the total number of points that define the polygon
     * @exception FormatException may throw FormatExceptions
     */
    public int computeEdgePoints(List<Object> facevec, List<CoordFloatString> allLLPoints)
            throws FormatException {
        int ring_ptr = ((Number) facevec.get(ringIDColumn)).intValue();
        List<Object> ring1 = new ArrayList<Object>(rings.getColumnCount());
        rings.getRow(ring1, ring_ptr);
        int fac_id = ((Number) ring1.get(faceIDColumn)).intValue();

        int startedgeid = ((Number) ring1.get(ringStartColumn)).intValue();
        if (startedgeid <= 0) {
            return 0;
        }
        int nextedgeid = startedgeid;
        boolean firsttime = true;
        allLLPoints.clear();
        int polySize = 0;
        int prev_node = -1;
        final List<Object> edge = new ArrayList<Object>(edges.getColumnCount());

        do {
            edges.getRow(edge, nextedgeid);
            int start_node = edges.getStartNode(edge);
            int end_node = edges.getEndNode(edge);
            int rht_face = edgeRightFaceAdapter.getPrimId(edge);
            int lft_face = edgeLeftFaceAdapter.getPrimId(edge);
            int right_edge = edgeRightEdgeAdapter.getPrimId(edge);
            int left_edge = edgeLeftEdgeAdapter.getPrimId(edge);
            if (firsttime) {
                prev_node = start_node;
                firsttime = false;
            }

            // Debug.message("dcwSpecialist",
            // "edge: " + nextedgeid + " start->end: "
            // + start_node + "->" + end_node);
            CoordFloatString cfs = edges.getCoordinates(edge);

            if ((fac_id == rht_face) && (fac_id == lft_face)) {
                if (start_node == prev_node) {
                    nextedgeid = right_edge;
                    prev_node = end_node;
                } else if (end_node == prev_node) {
                    nextedgeid = left_edge;
                    prev_node = start_node;
                } else {
                    throw new FormatException(" node matching assertion failed ");
                }
            } else if (fac_id == rht_face) {
                nextedgeid = right_edge;
                prev_node = end_node;
                polySize += cfs.tcount;
                allLLPoints.add(cfs);
            } else if (fac_id == lft_face) { // reverse direction
                nextedgeid = left_edge;
                prev_node = start_node;
                polySize += cfs.tcount;
                cfs.tcount *= -1;// flag reverse
                allLLPoints.add(cfs);
            } else {
                throw new FormatException("Node Assertion failed");
            }
        } while (nextedgeid != startedgeid);
        return polySize;
    }

    /**
     * Parse the area records for this tile, calling warehouse.createArea once
     * for each record.
     * 
     * @param warehouse the warehouse used for createArea calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @see VPFGraphicWarehouse#createArea
     */
    public void drawTile(VPFGraphicWarehouse warehouse, double dpplat, double dpplon,
                         LatLonPoint ll1, LatLonPoint ll2) {
        try {
            for (List<Object> area = new ArrayList<Object>(getColumnCount()); parseRow(area);) {
                warehouse.createArea(covtable, this, area, ll1, ll2, dpplat, dpplon);
            }
        } catch (FormatException f) {
            System.out.println("Exception: " + f.getClass() + " " + f.getMessage());
        }
    }

    /**
     * Use the warehouse to create a graphic from a feature in the AreaTable.
     * 
     * @param warehouse the warehouse used for createArea calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinning (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param area a List containing the AreaTable row contents.
     * @param featureType the string representing the feature type, in case the
     *        warehouse wants to do some intelligent rendering.
     * @param primID the primitive ID of the feature, in order to gather attributes if
     *        necessary.
     * @see VPFGraphicWarehouse#createEdge
     */
    public OMGraphic drawFeature(VPFFeatureWarehouse warehouse, double dpplat, double dpplon,
                                 LatLonPoint ll1, LatLonPoint ll2, List<Object> area,
                                 String featureType, int primID) {

        if (warehouse == null) {
            return null;
        }

        return warehouse.createArea(covtable, this, area, ll1, ll2, dpplat, dpplon, featureType, primID);
    }

}