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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/GrabPoint.java,v $
// $RCSfile: GrabPoint.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.Color;

/**
 * A GrabPoint is used by editable graphics to show a location that
 * can be adjusted.  Some GrabPoints are limited in the direction that
 * they are able to move.
 */
public class GrabPoint extends OMPoint {

    public final static int DEFAULT_RADIUS = 3;

    public GrabPoint(int x, int y) {
	this(x, y, DEFAULT_RADIUS);
	setLinePaint(Color.black);
	setFillPaint(Color.white);
	setVisible(false);
    }

    public GrabPoint(int x, int y, int radius) {
	super(x, y, radius);
	setLinePaint(Color.black);
	setFillPaint(Color.white);
	setVisible(false);
    }
}
