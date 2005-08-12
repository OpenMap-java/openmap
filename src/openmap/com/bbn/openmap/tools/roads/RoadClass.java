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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadClass.java,v
// $
// $RCSfile: RoadClass.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/12 21:47:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Color;
import java.io.Serializable;

public class RoadClass implements Serializable {

    private Serializable name;
    private Color color;
    private int width;
    private float convoySpeed;

    public RoadClass(Serializable name, Color color, int width, float convoySpeed) {
        this.name = name;
        this.color = color;
        this.width = width;
        this.convoySpeed = convoySpeed;
    }

    public Serializable getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public float getConvoySpeed() {
        return convoySpeed;
    }
}