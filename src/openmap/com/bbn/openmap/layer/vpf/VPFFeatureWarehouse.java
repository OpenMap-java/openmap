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
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.*;
import com.bbn.openmap.LatLonPoint;

/**
 * Define an interface for a Graphic Factory for graphics read from
 * VPF.
 */
public interface VPFFeatureWarehouse extends VPFWarehouse {

    public boolean needToFetchTileContents(String currentFeature,
                                           TileDirectory currentTile);

    /**
     *  
     */
    public void createArea(CoverageTable covtable, AreaTable areatable,
                           List facevec, LatLonPoint ll1, LatLonPoint ll2,
                           float dpplat, float dpplon, String featureType);

    /**
     *  
     */
    public void createEdge(CoverageTable c, EdgeTable edgetable, List edgevec,
                           LatLonPoint ll1, LatLonPoint ll2, float dpplat,
                           float dpplon, CoordFloatString coords,
                           String featureType);

    /**
     *  
     */
    public void createText(CoverageTable c, TextTable texttable, List textvec,
                           float latitude, float longitude, String text,
                           String featureType);

    /**
     * Method called by the VPF reader code to construct a node
     * feature.
     */
    public void createNode(CoverageTable c, NodeTable t, List nodeprim,
                           float latitude, float longitude,
                           boolean isEntityNode, String featureType);
}