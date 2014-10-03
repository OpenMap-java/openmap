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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/LayerView.java,v $
// $RCSfile: LayerView.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.util.List;

import com.bbn.openmap.proj.Projection;

/**
 * Services the layer provides to the road layer helper.
 */
public interface LayerView {

    /**
     * Needed to find the height and width of the displayed routes,
     * etc.
     */
    Projection getProjection();

    /**
     * Should return a flattened list of road OMGraphics. No embedded
     * OMGraphicLists.
     */
    List getGraphicList();

    /**
     * Draw extra OMGraphics (intersections, to debug the road layer),
     * a callback method to the LayerView to let it know what the
     * RoadFinder is doing.
     */
    void setExtraGraphics(List toDraw);
}