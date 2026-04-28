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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/VerticalGrabPoint.java,v $
// $RCSfile: VerticalGrabPoint.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

/**
 * A GrabPoint that can only move in the vertical direction.
 */
public class VerticalGrabPoint extends GrabPoint {

    public VerticalGrabPoint(int x, int y) {
        super(x, y);
    }

    /**
     * x is ignored, since it can't affect the horizontal movement of
     * a VerticalGrabPoint.
     */
    public void set(int x, int y) {
        setY(y);
    }

    /**
     * Move the point.
     * 
     * @param x the new x location
     * @param y the new y location
     * @param override true if the horizontal limitation should be
     *        ignored.
     */
    public void set(int x, int y, boolean override) {
        if (override) {
            super.set(x, y);
        } else {
            setY(y);
        }
    }

    /**
     * No action, because horizontal movement is limited.
     */
    public void setX(int x) {}
}