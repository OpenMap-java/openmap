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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LineType.java,v $
// $RCSfile: LineType.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

/**
 * LineType constants.
 */
public final class LineType {

    // cannot construct
    private LineType() {}

    /**
     * Straight lines are the easiest and fastest to draw. Use them if
     * you're concerned about speed of projecting.
     */
    final public static transient int Straight = 1;

    /**
     * Rhumb lines follow a constant bearing.
     */
    final public static transient int Rhumb = 2;

    /**
     * Great circle lines follow the shortest path between two points
     * on a sphere.
     */
    final public static transient int GreatCircle = 3;
}