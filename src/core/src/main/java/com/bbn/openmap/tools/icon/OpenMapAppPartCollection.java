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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/OpenMapAppPartCollection.java,v
// $
// $RCSfile: OpenMapAppPartCollection.java,v $
// $Revision: 1.5 $
// $Date: 2006/08/29 23:07:53 $
// $Author: dietrick $
// 
// **********************************************************************
package com.bbn.openmap.tools.icon;

public class OpenMapAppPartCollection extends IconPartCollection {

	static OpenMapAppPartCollection omparts;

	protected OpenMapAppPartCollection() {
		super("OpenMap", "Common parts used in OpenMap Application Icons");
		init();
	}

	public static OpenMapAppPartCollection getInstance() {
		if (omparts == null) {
			omparts = new OpenMapAppPartCollection();
		}
		return omparts;
	}

	protected void init() {
		add(BIG_BOX);
		add(SMALL_BOX);
		add(FILL_BOX);
		add(UL_TRI);
		add(LR_TRI);
		add(LL_UR_LINE);
		add(UL_LR_LINE);
		add(BIG_ARROW);
		add(MED_ARROW);
		add(SMALL_ARROW);
		add(CORNER_TRI);
		add(OPP_CORNER_TRI);
		add(CIRCLE);
		add(DOT);
		add(PLUS);
		add(ADD_PLUS);
		add(MINUS);
		add(CIRCLE);
		add(MAP_PIN_HEAD);
		add(MAP_PIN_BOTTOM);
		add(TRIANGLE);
		add(SQUAT_TRIANGLE);
	}

	public final static OpenMapAppPart BIG_BOX = new OpenMapAppPart("BIG_BOX", "BIG_BOX", OMIconPart.BIG_BOX);
	public final static OpenMapAppPart SMALL_BOX = new OpenMapAppPart("SMALL_BOX", "SMALL_BOX", OMIconPart.SMALL_BOX);
	public final static OpenMapAppPart FILL_BOX = new OpenMapAppPart("FILL_BOX", "FILL_BOX", OMIconPart.FILL_BOX);
	public final static OpenMapAppPart UL_TRI = new OpenMapAppPart("UL_TRI", "UL_TRI", OMIconPart.UL_TRI);
	public final static OpenMapAppPart LR_TRI = new OpenMapAppPart("LR_TRI", "LR_TRI", OMIconPart.LR_TRI);
	public final static OpenMapAppPart LL_UR_LINE = new OpenMapAppPart("LL_UR_LINE", "LL_UR_LINE",
			OMIconPart.LL_UR_LINE);
	public final static OpenMapAppPart UL_LR_LINE = new OpenMapAppPart("UL_LR_LINE", "UL_LR_LINE",
			OMIconPart.UL_LR_LINE);
	public final static OpenMapAppPart BIG_ARROW = new OpenMapAppPart("BIG_ARROW", "BIG_ARROW", OMIconPart.BIG_ARROW);
	public final static OpenMapAppPart MED_ARROW = new OpenMapAppPart("MED_ARROW", "MED_ARROW", OMIconPart.MED_ARROW);
	public final static OpenMapAppPart SMALL_ARROW = new OpenMapAppPart("SMALL_ARROW", "SMALL_ARROW",
			OMIconPart.SMALL_ARROW);
	public final static OpenMapAppPart CORNER_TRI = new OpenMapAppPart("CORNER_TRI", "CORNER_TRI",
			OMIconPart.CORNER_TRI);
	public final static OpenMapAppPart OPP_CORNER_TRI = new OpenMapAppPart("OPP_CORNER_TRI", "OPP_CORNER_TRI",
			OMIconPart.OPP_CORNER_TRI);
	public final static OpenMapAppPart CIRCLE = new OpenMapAppPart("CIRCLE", "CIRCLE", OMIconPart.CIRCLE);
	public final static OpenMapAppPart DOT = new OpenMapAppPart("DOT", "DOT", OMIconPart.DOT);
	public final static OpenMapAppPart PLUS = new OpenMapAppPart("PLUS", "PLUS", OMIconPart.PLUS);
	public final static OpenMapAppPart ADD_PLUS = new OpenMapAppPart("ADD_PLUS", "ADD_PLUS", OMIconPart.ADD_PLUS);
	public final static OpenMapAppPart MINUS = new OpenMapAppPart("MINUS", "MINUS", OMIconPart.MINUS);
	public final static OpenMapAppPart MAP_PIN_HEAD = new OpenMapAppPart("MAP_PIN_HEAD", "MAP_PIN_HEAD",
			OMIconPart.MAP_PIN_HEAD);
	public final static OpenMapAppPart MAP_PIN_BOTTOM = new OpenMapAppPart("MAP_PIN_BOTTOM", "MAP_PIN_BOTTOM",
			OMIconPart.MAP_PIN_BOTTOM);
	public final static OpenMapAppPart TRIANGLE = new OpenMapAppPart("TRIANGLE", "TRIANGLE", OMIconPart.TRIANGLE);
	public final static OpenMapAppPart SQUAT_TRIANGLE = new OpenMapAppPart("SQUAT_TRIANGLE", "SQUAT_TRIANGLE",
			OMIconPart.SQUAT_TRIANGLE);

	public static class OpenMapAppPart extends IconPartCollectionEntry {

		public OpenMapAppPart(String n, String d, OMIconPart iconPart) {
			super(n, d, iconPart.getIconPart());
		}
	}

}
