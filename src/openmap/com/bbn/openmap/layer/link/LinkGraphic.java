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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGraphic.java,v $
// $RCSfile: LinkGraphic.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/22 23:52:34 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

import java.awt.*;
import java.io.*;
import java.util.Iterator;

public class LinkGraphic implements LinkConstants, LinkPropertiesConstants {

    public static void write(OMGraphic omGraphic, Link link) 
	throws IOException {
	write(omGraphic, link, null);
    }

    public static void write(OMGraphic omGraphic, Link link, 
			     LinkProperties props) throws IOException {

	// This is so not object-oriented.  I know, I know.

	if (props == null) {
	    Object obj = omGraphic.getAppObject();
	    if (obj != null && obj instanceof LinkProperties) {
		props = (LinkProperties) obj;
	    } else {
		props = new LinkProperties();
	    }
	}

	if (omGraphic instanceof OMGraphicList) {
	    Iterator iterator = ((OMGraphicList)omGraphic).getTargets().iterator();
	    while (iterator.hasNext()) {
		write((OMGraphic)iterator.next(), link);
	    }
	    return;
	}

	props.setProperty(LPC_LINEWIDTH, Integer.toString((int)((BasicStroke)omGraphic.getStroke()).getLineWidth()));

	Paint paint = omGraphic.getLinePaint();
	if (paint instanceof Color) {
	    props.setProperty(LPC_LINECOLOR, 
			      ColorFactory.getHexColorString((Color)paint));
	}

	paint = omGraphic.getFillPaint();
	if (paint instanceof Color) {
	    props.setProperty(LPC_FILLCOLOR,
			      ColorFactory.getHexColorString((Color)paint));
	}

	paint = omGraphic.getSelectPaint();
	if (paint instanceof Color) {
	    props.setProperty(LPC_HIGHLIGHTCOLOR,
			      ColorFactory.getHexColorString((Color)paint));
	}

	if (omGraphic instanceof OMBitmap) {
	    LinkBitmap.write((OMBitmap)omGraphic, link, props);
	} else if (omGraphic instanceof OMCircle) {
	    LinkCircle.write((OMCircle)omGraphic, link, props);
	} else if (omGraphic instanceof OMGrid) {
	    LinkGrid.write((OMGrid)omGraphic, link, props);
	} else if (omGraphic instanceof OMLine) {
	    LinkLine.write((OMLine)omGraphic, link, props);
	} else if (omGraphic instanceof OMPoint) {
	    LinkPoint.write((OMPoint)omGraphic, link, props);
	} else if (omGraphic instanceof OMRect) {
	    LinkRectangle.write((OMRect)omGraphic, link, props);
	} else if (omGraphic instanceof OMRaster) {
	    LinkRaster.write((OMRaster)omGraphic, link, props);
	} else if (omGraphic instanceof OMText) {
	    LinkText.write((OMText)omGraphic, link, props);
	} else if (omGraphic instanceof OMPoly) {
	    LinkPoly.write((OMPoly)omGraphic, link, props);
	} else {
	    Debug.error("LinkGraphic.write: OMGraphic Type not handled by LinkProtocol");
	}


    }
}
