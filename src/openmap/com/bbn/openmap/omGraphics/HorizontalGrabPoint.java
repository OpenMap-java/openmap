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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/HorizontalGrabPoint.java,v $
// $RCSfile: HorizontalGrabPoint.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

/**
 * A GrabPoint that can only move in the horizontal direction.
 */
public class HorizontalGrabPoint extends GrabPoint {

    public HorizontalGrabPoint(int x, int y) {
        super(x, y);
    }

    /**
     * y is ignored, since it can't affect the vertical movement of a
     * HorizontalGrabPoint.
     */
    public void set(int x, int y) {
        setX(x);
    }

    /**
     * Move the point.
     * 
     * @param x the new x location
     * @param y the new y location
     * @param override true if the vertical limitation should be
     *        ignored.
     */
    public void set(int x, int y, boolean override) {
        if (override) {
            super.set(x, y);
        } else {
            setX(x);
        }
    }

    /**
     * No action, because vertical movement is limited.
     */
    public void setY(int y) {}

}