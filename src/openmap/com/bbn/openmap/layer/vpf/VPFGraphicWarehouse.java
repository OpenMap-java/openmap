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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFGraphicWarehouse.java,v $
// $RCSfile: VPFGraphicWarehouse.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.util.*;
import com.bbn.openmap.LatLonPoint;

/**
 * Define an interface for a Graphic Factory for graphics read from VPF.
 */
public interface VPFGraphicWarehouse extends VPFWarehouse {
    /**
     * Method called by the VPF reader code to construct an area feature.
     * @param c the coverage table for this area
     * @param a the areatable being parsed
     * @param l the record read from the area table
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     */
    public void createArea(CoverageTable c, AreaTable a, List l,
                           LatLonPoint ll1,
                           LatLonPoint ll2,
                           float dpplat,
                           float dpplon);

    /**
     * Method called by the VPF reader code to construct an edge feature.
     * @param c the coverage table for this edge
     * @param e the edgetable being parsed
     * @param l the record read from the edge table
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     */
     public void createEdge(CoverageTable c, EdgeTable e, List l,
                            LatLonPoint ll1,
                            LatLonPoint ll2,
                            float dpplat,
                            float dpplon,
                            CoordFloatString coords);

    /**
     * Method called by the VPF reader code to construct a text feature.
     * @param c the coverage table for this text
     * @param t the texttable being parsed
     * @param textprim the record read from the text table
     * @param latitude the latitude of the text
     * @param longitude the longitude of the text
     * @param text the text string
     */
    public void createText(CoverageTable c, TextTable t, List textprim,
                           float latitude, float longitude,
                           String text);

    /**
     * Method called by the VPF reader code to construct a node feature.
     * @param c the coverage table for this node
     * @param t the nodetable being parsed
     * @param nodeprim the record read from the node table
     * @param latitude the latitude of the node
     * @param longitude the longitude of the node
     * @param isEntityNode true if we are reading entity notes, false
     *   if we are reading connected nodes
     */
    public void createNode(CoverageTable c, NodeTable t, List nodeprim,
                           float latitude, float longitude,
                           boolean isEntityNode);
}
