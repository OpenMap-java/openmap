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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/OpenMapAppPartCollection.java,v $
// $RCSfile: OpenMapAppPartCollection.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/26 17:34:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.icon;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;

public class OpenMapAppPartCollection extends IconPartCollection {

    static OpenMapAppPartCollection omparts;

    protected OpenMapAppPartCollection() {
	super("OpenMap", "Common parts used in OpenMap Application Icons");
	init();
    }

    public synchronized static OpenMapAppPartCollection getInstance() {
	if (omparts == null) {
	    omparts = new OpenMapAppPartCollection();
	}
	return omparts;
    }
    
    protected void init() {
	add(BIG_BOX.getEntry());
	add(SMALL_BOX.getEntry());
	add(FILL_BOX.getEntry());
	add(UL_TRI.getEntry());
	add(LR_TRI.getEntry());
	add(LL_UR_LINE.getEntry());
	add(UL_LR_LINE.getEntry());
    }

    public final static OpenMapAppPart BIG_BOX = new OpenMapAppPart(
	"BIG_BOX", "BIG_BOX", new int[] {10, 10, 90, 90, 10}, new int[] {10, 90, 90, 10, 10});
    public final static OpenMapAppPart SMALL_BOX = new OpenMapAppPart(
	"SMALL_BOX", "SMALL_BOX", new int[] {30, 30, 70, 70, 30}, new int[] {30, 70, 70, 30, 30});
    public final static OpenMapAppPart FILL_BOX = new OpenMapAppPart(
	"FILL_BOX", "FILL_BOX", 
	new int[] {10, 10, 50, 50, 30, 30, 70, 70, 50, 50, 90, 90, 10},
	new int[] {10, 90, 90, 70, 70, 30, 30, 70, 70, 90, 90, 10, 10});
    public final static OpenMapAppPart UL_TRI = new OpenMapAppPart(
	"UL_TRI", "UL_TRI", new int[] {10, 10, 75, 10}, new int[] {10, 75, 10, 10});
    public final static OpenMapAppPart LR_TRI = new OpenMapAppPart(
	"LR_TRI", "LR_TRI", new int[] {25, 90, 90, 25}, new int[] {90, 90, 25, 90});
    public final static OpenMapAppPart LL_UR_LINE = new OpenMapAppPart(
	"LL_UR_LINE", "LL_UR_LINE", new int[] {10, 90}, new int[] {90, 10});
    public final static OpenMapAppPart UL_LR_LINE = new OpenMapAppPart(
	"UL_LR_LINE", "UL_LR_LINE", new int[] {10, 10}, new int[] {90, 90});

    public static class OpenMapAppPart {
	int[] xpoints;
	int[] ypoints;
	String name;
	String description;
	AffineTransform af;

	public OpenMapAppPart(String n, String d, int[] xp, int[] yp) {
	    this(n, d, xp, yp, null);
	}

	public OpenMapAppPart(String n, String d, int[] xp, int[] yp, AffineTransform affTrans) {
	    name = n;
	    description = d;
	    xpoints = xp;
	    ypoints = yp;
	    af = affTrans;
	}

	public IconPartCollectionEntry getEntry() {
	    return new IconPartCollectionEntry(
		name, description, new BasicIconPart(new Polygon(xpoints, ypoints, xpoints.length), af));
	}
    }
}
