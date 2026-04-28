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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFFeatureWarehouse.java,v $
// $RCSfile: VPFFeatureWarehouse.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Define an interface for a Graphic Factory for graphics read from VPF.
 */
public interface VPFFeatureWarehouse extends VPFWarehouse {

    public boolean needToFetchTileContents(String libraryName, String currentFeature,
                                           TileDirectory currentTile);

    /**
     *  
     */
    public OMGraphic createArea(CoverageTable covtable, AreaTable areatable, List<Object> facevec,
                                LatLonPoint ll1, LatLonPoint ll2, double dpplat, double dpplon,
                                String featureType, int primID);

    /**
     *  
     */
    public OMGraphic createEdge(CoverageTable c, EdgeTable edgetable, List<Object> edgevec,
                                LatLonPoint ll1, LatLonPoint ll2, double dpplat, double dpplon,
                                CoordFloatString coords, String featureType, int primID);

    /**
     *  
     */
    public OMGraphic createText(CoverageTable c, TextTable texttable, List<Object> textvec,
                                double latitude, double longitude, String text, String featureType,
                                int primID);

    /**
     * Method called by the VPF reader code to construct a node feature.
     */
    public OMGraphic createNode(CoverageTable c, NodeTable t, List<Object> nodeprim,
                                double latitude, double longitude, boolean isEntityNode,
                                String featureType, int primID);
}