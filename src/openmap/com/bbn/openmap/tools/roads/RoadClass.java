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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadClass.java,v $
// $RCSfile: RoadClass.java,v $
// $Revision: 1.1 $
// $Date: 2004/02/13 17:16:33 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.roads;

import java.awt.Color;

public class RoadClass {

    private Object name;
    private Color color;
    private int width;
    private float convoySpeed;

    public RoadClass(Object name, Color color, int width, float convoySpeed) {
	this.name = name;
	this.color = color;
	this.width = width;
	this.convoySpeed = convoySpeed;
    }

    public Object getName() {
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
