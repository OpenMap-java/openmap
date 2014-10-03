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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGraphic.java,v
// $
// $RCSfile: LinkGraphic.java,v $
// $Revision: 1.5 $
// $Date: 2006/10/10 22:05:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMBitmap;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMEllipse;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

public class LinkGraphic implements LinkConstants, LinkPropertiesConstants {

    public static void write(OMGraphic omGraphic, Link link) throws IOException {
        write(omGraphic, link, null);
    }

    public static void write(OMGraphic omGraphic, Link link,
                             LinkProperties props) throws IOException {

        // This is so not object-oriented. I know, I know.

        if (props == null) {
            Object obj = omGraphic.getAppObject();
            if (obj instanceof LinkProperties) {
                props = (LinkProperties) obj;
            } else {
                props = new LinkProperties();
            }
        }

        if (omGraphic instanceof OMGraphicList) {
            Iterator iterator = ((OMGraphicList) omGraphic).getTargets()
                    .iterator();
            while (iterator.hasNext()) {
                write((OMGraphic) iterator.next(), link);
            }
            return;
        }

        props.setProperty(LPC_LINEWIDTH,
                Integer.toString((int) ((BasicStroke) omGraphic.getStroke()).getLineWidth()));

        Paint paint = omGraphic.getLinePaint();
        if (paint instanceof Color) {
            props.setProperty(LPC_LINECOLOR,
                    ColorFactory.getHexColorString((Color) paint));
        }

        paint = omGraphic.getFillPaint();
        if (paint instanceof Color) {
            props.setProperty(LPC_FILLCOLOR,
                    ColorFactory.getHexColorString((Color) paint));
        }

        paint = omGraphic.getSelectPaint();
        if (paint instanceof Color) {
            props.setProperty(LPC_HIGHLIGHTCOLOR,
                    ColorFactory.getHexColorString((Color) paint));
        }

        if (omGraphic instanceof OMBitmap) {
            LinkBitmap.write((OMBitmap) omGraphic, link, props);
        } else if (omGraphic instanceof OMCircle) {
            LinkCircle.write((OMCircle) omGraphic, link, props);
        } else if (omGraphic instanceof OMEllipse) {
            LinkEllipse.write((OMEllipse) omGraphic, link, props);
        } else if (omGraphic instanceof OMGrid) {
            LinkGrid.write((OMGrid) omGraphic, link, props);
        } else if (omGraphic instanceof OMLine) {
            LinkLine.write((OMLine) omGraphic, link, props);
        } else if (omGraphic instanceof OMPoint) {
            LinkPoint.write((OMPoint) omGraphic, link, props);
        } else if (omGraphic instanceof OMRect) {
            LinkRectangle.write((OMRect) omGraphic, link, props);
        } else if (omGraphic instanceof OMRaster) {
            LinkRaster.write((OMRaster) omGraphic, link, props);
        } else if (omGraphic instanceof OMText) {
            LinkText.write((OMText) omGraphic, link, props);
        } else if (omGraphic instanceof OMPoly) {
            LinkPoly.write((OMPoly) omGraphic, link, props);
        } else {
            Debug.error("LinkGraphic.write: OMGraphic Type not handled by LinkProtocol");
        }

    }
}