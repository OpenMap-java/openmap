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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadServices.java,v $
// $RCSfile: RoadServices.java,v $
// $Revision: 1.1 $
// $Date: 2004/02/13 17:16:33 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.roads;

import java.util.List;
import java.awt.Point;

/**
 * getPathOnRoad interface - any layer that implements this
 * interface can return a route between two points
 */
public interface RoadServices {
    /** 
     * @param start point
     * @param end point
     * @param segments is populated by road segments, each segment is a list of points
     * @return a list of points between start and end 
     */
    List getPathOnRoad(Point start, Point end, List segments);
}
