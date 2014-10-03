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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadServices.java,v $
// $RCSfile: RoadServices.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/12 21:47:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Point;
import java.util.List;

import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * getPathOnRoad interface - any layer that implements this interface
 * can return a route between two points
 */
public interface RoadServices {

    /**
     * Returns the best Route between a start point and end point.
     * <p>
     * 
     * This method works by finding the closest intersection to start and end
     * points, and then finding a path from start intersection to end intersection.
     * The method works on screen coordinates.
     * 
     * @param start - Start point on the map in screen coordinates.
     * @param end - End point on the map in screen coordinates.
     * @param segments is populated by road segments, each segment is
     *        a list of points in screen coordinates.
     * @return a list of points between start and end points in screen coordinates.
     */
    List getPathOnRoad(Point start, Point end, List segments);
    
    /**
     * Returns the best Route between a start point and end point.
     * <p>
     * 
     * This method works by finding the closest intersection to start and end
     * points, and then finding a path from start intersection to end intersection.
     * The method works in latitude/longitude coordinates.
     * 
     * @param start - Start point in latitude/longitude coordinates.
     * @param end - End point in latitude/longitude coordinates.
     * @return the best route to travel by Road from start to end
     */
    Route getPathOnRoad(LatLonPoint start, LatLonPoint end);
    
    /**
     * Displays a Route between two points on the map.
     * <p>
     * 
     * @param start start from start point on map
     * @param end to end point on map
     * @param route the Route to travel from start to end
     * @param segments as side effect, populated with PathSegments
     *        between returned WayPoints
     * @return List of WayPoints
     */    
    List displayPathOnRoad(Point start, Point end, Route route,
        List segments);
}