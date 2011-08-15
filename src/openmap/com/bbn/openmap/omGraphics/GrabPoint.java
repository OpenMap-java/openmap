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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/GrabPoint.java,v $
// $RCSfile: GrabPoint.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;

/**
 * A GrabPoint is used by editable graphics to show a location that can be
 * adjusted. Some GrabPoints are limited in the direction that they are able to
 * move.
 */
public class GrabPoint
        extends OMPoint {

    public final static int DEFAULT_RADIUS = 3;

    public GrabPoint(int x, int y) {
        this(x, y, DEFAULT_RADIUS);
    }

    public GrabPoint(int x, int y, int radius) {
        super(x, y);
        setDefaultDrawingAttributes(radius);
        setVisible(false);
    }

    public void set(double x, double y) {
        set((int) x, (int) y);
    }
    
    public void setDefaultDrawingAttributes(int pointRadius) {
        new DrawingAttributes.Builder().setLinePaint(Color.black).setFillPaint(Color.white).setPointRadius(pointRadius).build().setTo(this);
    }
}