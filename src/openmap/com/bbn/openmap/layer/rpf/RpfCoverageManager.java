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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverageManager.java,v $
// $RCSfile: RpfCoverageManager.java,v $
// $Revision: 1.3 $
// $Date: 2004/05/11 23:21:21 $
// $Author: dietrick $
// 
// **********************************************************************




package com.bbn.openmap.layer.rpf;


/*  Java Core  */
import java.awt.Point;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.io.*;
import java.net.URL;
import javax.swing.JCheckBox;
import javax.swing.Box;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.io.*;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;

/**
 * This is an object that provides coverage information on the Rpf
 * data.  It is supposed to be a simple tool that lets you see the
 * general location of data, to guide you to the right place and scale
 * of coverage. The layer really uses the properties passed in to it
 * to determine which RPF/A.TOC should be scanned for the data.  There
 * is a palette for this layer, that lets you turn off the coverage
 * for different levels of Rpf.  Right now, only City Graphics, TLM,
 * JOG, TPC, ONC, JNC, GNC and 5/10 meter CIB scales are are handled.
 * All other scales are tossed together under the misc setting.  The
 * City Graphics setting shows all charts for scales greater than than
 * 1:15k.
 */
public class RpfCoverageManager {

    /** The last line type of the edge of the rectangles.  Used to
     * determine whether the line needs to be re-projected based on a
     * projection change. */
    protected int currentLineType;

    /** Graphic lists of coverage rectangles. */
    protected Vector omGraphics = null;

    /** The place to get the coverage information, */
    protected RpfFrameProvider frameProvider;

    /** Don't use this. */
    public RpfCoverageManager(RpfFrameProvider rfp) {
        frameProvider = rfp;
        omGraphics = new Vector();
    }

    /** 
     * Looks at the paths for the A.TOC files and gets all the
     * coverage rectangles from them.  Sets the entries to a big list
     * of rectangles from all the A.TOC files.
     * @param colors looks for an array of 10 colors.
     * @param fillRects whether to fill the rectangles with the color,
     * or just do outlines.
     * @return entries from within the A.TOC files.
     */
    protected Vector getCatalogCoverage(float ullat, float ullon, 
                                        float lrlat, float lrlon,
                                        Projection proj, String chartSeries, 
                                        Color[] colors, boolean fillRects) {

        Debug.message("rpfcov", 
                      "RpfCoverageManager: Getting catalog coverage from RpfFrameProvider");
        if (proj == null || frameProvider == null) {
            return new Vector();
        }

        CADRG cadrg;
        if (proj instanceof CADRG) {
            cadrg = (CADRG)proj;
        } else {
            cadrg = new CADRG(proj.getCenter(), proj.getScale(), 
                              proj.getWidth(), proj.getHeight());
        }

        Vector[] hemisphereData;

        if ((ullon > lrlon) || MoreMath.approximately_equal(ullon, lrlon, .001f)) {

            hemisphereData = new Vector[2];
            hemisphereData[0] = frameProvider.getCatalogCoverage(ullat, ullon, 
                                                                 lrlat, 180f, cadrg, 
                                                                 chartSeries);
            hemisphereData[1] = frameProvider.getCatalogCoverage(ullat, -180f, 
                                                                 lrlat, lrlon, cadrg, 
                                                                 chartSeries);
        } else {
            hemisphereData = new Vector[1];
            hemisphereData[0] = frameProvider.getCatalogCoverage(ullat, ullon, 
                                                                 lrlat, lrlon, cadrg, 
                                                                 chartSeries);
        }

        omGraphics.removeAllElements();
                
        currentLineType = OMGraphic.LINETYPE_RHUMB;
//      if (proj instanceof Cylindrical) {
//          currentLineType = OMGraphic.LINETYPE_STRAIGHT;
//      }
        
        OMGraphicList cgs = new OMGraphicList();
        OMGraphicList tlms = new OMGraphicList();
        OMGraphicList jogs = new OMGraphicList();
        OMGraphicList tpcs = new OMGraphicList();
        OMGraphicList oncs = new OMGraphicList();
        OMGraphicList jncs = new OMGraphicList();
        OMGraphicList gncs = new OMGraphicList();
        OMGraphicList cib10s = new OMGraphicList();
        OMGraphicList cib5s = new OMGraphicList();
        OMGraphicList miscs = new OMGraphicList();

        omGraphics.addElement(cgs);
        omGraphics.addElement(cib5s);
        omGraphics.addElement(tlms);
        omGraphics.addElement(cib10s);
        omGraphics.addElement(jogs);
        omGraphics.addElement(miscs);
        omGraphics.addElement(tpcs);
        omGraphics.addElement(oncs);
        omGraphics.addElement(jncs);
        omGraphics.addElement(gncs);

        OMRect rect;

        for (int j = 0; j < hemisphereData.length; j++) {
            if (hemisphereData[j] == null) {
                Debug.message("rpfcov", "RpfCoverageManager. vector " + j + " is null");
                continue;
            }

            int size = hemisphereData[j].size();
            for (int i = 0; i < size; i++) {
                RpfCoverageBox box = (RpfCoverageBox) hemisphereData[j].elementAt(i);

                rect = new OMRect((float) box.nw_lat, (float) box.nw_lon, 
                                  (float) box.se_lat, (float) box.se_lon, 
                                  currentLineType);
            
                float scale = RpfProductInfo.get(box.chartCode).scale;

                if (scale < 15000f) {
                    if (colors != null && colors.length >= 1) {
                        rect.setLinePaint(colors[0]);
                        if (fillRects) rect.setFillPaint(colors[0]);
                    }
                    cgs.add(rect);
                } else if (scale == 50000f) {
                    if (colors != null && colors.length >= 2) {
                        rect.setLinePaint(colors[1]);
                        if (fillRects) rect.setFillPaint(colors[1]);
                    }
                    tlms.add(rect);
                } else if (scale == 250000f) {
                    if (colors != null && colors.length >= 3) {
                        rect.setLinePaint(colors[2]);
                        if (fillRects) rect.setFillPaint(colors[2]);
                    }
                    jogs.add(rect);
                } else if (scale == 500000f) {
                    if (colors != null && colors.length >= 4) {
                        rect.setLinePaint(colors[3]);
                        if (fillRects) rect.setFillPaint(colors[3]);
                    }
                    tpcs.add(rect);
                } else if (scale == 1000000f) {
                    if (colors != null && colors.length >= 5) {
                        rect.setLinePaint(colors[4]);
                        if (fillRects) rect.setFillPaint(colors[4]);
                    }
                    oncs.add(rect);
                } else if (scale == 2000000f) {
                    if (colors != null && colors.length >= 6) {
                        rect.setLinePaint(colors[5]);
                        if (fillRects) rect.setFillPaint(colors[5]); 
                    }
                    jncs.add(rect);
                } else if (scale == 5000000f) {
                    if (colors != null && colors.length >= 7) {
                        rect.setLinePaint(colors[6]);
                        if (fillRects) rect.setFillPaint(colors[6]);
                    }
                    gncs.add(rect);
                } else if (scale == 66666f) {
                    if (colors != null && colors.length >= 8) {
                        rect.setLinePaint(colors[7]);
                        if (fillRects) rect.setFillPaint(colors[7]);
                    }
                    cib10s.add(rect);
                } else if (scale == 33333f) {
                    if (colors != null && colors.length >= 9) {
                        rect.setLinePaint(colors[8]);
                        if (fillRects) rect.setFillPaint(colors[8]);
                    }
                    cib5s.add(rect);
                } else if (scale == RpfConstants.Various) {
                    // Don't show it, because we don't know how to
                    // display it anyway.  Don't bother projecting it.
                    continue;
                } else {
                    if (colors != null && colors.length >= 10) {
                        rect.setLinePaint(colors[9]);
                        if (fillRects) rect.setFillPaint(colors[9]);
                    }
                    miscs.add(rect);
                }
                rect.generate(proj);
            }
        }
        return omGraphics;
    }
}
