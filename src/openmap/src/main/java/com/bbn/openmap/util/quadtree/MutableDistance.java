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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/MutableDistance.java,v $
// $RCSfile: MutableDistance.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.quadtree;

/**
 * A *really* simple class used as a changable double.
 */
public class MutableDistance {
    public double value = 0;

    public MutableDistance(double distance) {
        value = distance;
    }
}