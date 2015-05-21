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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Segment.java,v $
// $RCSfile: Segment.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Point;
import java.util.List;

/**
 * Represents one road segment.
 */
public class Segment {

    protected List allTravelPoints;

    public Segment(List points) {
        this.allTravelPoints = points;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < allTravelPoints.size(); i++) {
            str.append("=>");

            Point pt = (Point) allTravelPoints.get(i);
            str.append("{").append(pt.x).append(",").append(pt.y).append("}");
        }
        str.append("=>");

        return str.toString();
    }
}