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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadLayer.java,v $
// $RCSfile: RoadLayer.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import com.bbn.openmap.proj.Projection;

/**
 * Services provided by RoadFinder for helper classes like
 * Intersection
 */
public interface RoadLayer {

    Projection getProjection();

    Road createRoad(Intersection inter);

    boolean isEditing();
}