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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
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

    /** The default line color.  */
    public final static String defaultCGColorString = "AC4853"; 
    /** The default line color.  */
    public final static String defaultTLMColorString = "CE4F3F";
    /** The default line color.  */
    public final static String defaultJOGColorString = "AC7D74";
    /** The default line color.  */
    public final static String defaultTPCColorString = "ACCD10";
    /** The default line color.  */
    public final static String defaultONCColorString = "FCCDE5";
    /** The default line color.  */
    public final static String defaultJNCColorString = "7386E5";
    /** The default line color.  */
    public final static String defaultGNCColorString = "55866B";
    /** The default line color.  */
    public final static String defaultCIB10ColorString = "07516B";
    /** The default line color.  */
    public final static String defaultCIB5ColorString = "071CE0";
    /** The default line color.  */
    public final static String defaultMISCColorString = "F2C921";

    /** The color to outline the shapes. */
    protected Color CGColor = new Color(Integer.parseInt(defaultCGColorString, 16));
    /** The color to outline the shapes. */
    protected Color TLMColor = new Color(Integer.parseInt(defaultTLMColorString, 16));
    /** The color to outline the shapes. */
    protected Color JOGColor = new Color(Integer.parseInt(defaultJOGColorString, 16));
    /** The color to outline the shapes. */
    protected Color TPCColor = new Color(Integer.parseInt(defaultTPCColorString, 16));
    /** The color to outline the shapes. */
    protected Color ONCColor = new Color(Integer.parseInt(defaultONCColorString, 16));
    /** The color to outline the shapes. */
    protected Color JNCColor = new Color(Integer.parseInt(defaultJNCColorString, 16));
    /** The color to outline the shapes. */
    protected Color GNCColor = new Color(Integer.parseInt(defaultGNCColorString, 16));
    /** The color to outline the shapes. */
    protected Color CIB10Color = new Color(Integer.parseInt(defaultCIB10ColorString, 16));
    /** The color to outline the shapes. */
    protected Color CIB5Color = new Color(Integer.parseInt(defaultCIB5ColorString, 16));
    /** The color to outline the shapes. */
    protected Color MISCColor = new Color(Integer.parseInt(defaultMISCColorString, 16));

    /** A setting for how transparent to make the images.  The default
     * is 255, which is totally opaque.  Not used right now.
     * */
    protected int opaqueness = 255;
    /** Flag to fill the coverage rectangles. */
    protected boolean fillRects = true;

    /** Graphic lists of coverage rectangles. */
    protected Vector omGraphics = null;

    /** The place to get the coverage information, */
    protected RpfFrameProvider frameProvider;

    /** Don't use this. */
    public RpfCoverageManager(RpfFrameProvider rfp){
	frameProvider = rfp;
	omGraphics = new Vector();
    }

    /** 
     * Method that sets all the colorvariables to the non-default
     * values. The colors array is assumed to be length 10, one for
     * each chart type.
     * 
     * @param colors array of colors for different chart types, in
     * this order: CGColor, TLMColor, JOGColor, TPCColor, ONCColor,
     * JNCColor, GNCColor, CIB10Color, CIB5Color, MISCColor.
     * @param opaque how transparent the frames should be if they are filled.
     * @param fillRectangles whether to fill the rectangles with color.  
     */
    public void setColors(Color[] colors, int opaque, boolean fillRectangles){
	if (colors.length == 10){
	    CGColor = colors[0];
	    TLMColor = colors[1];
	    JOGColor = colors[2];
	    TPCColor = colors[3];
	    ONCColor = colors[4];
	    JNCColor = colors[5];
	    GNCColor = colors[6];
	    CIB10Color = colors[7];
	    CIB5Color = colors[8];
	    MISCColor = colors[9];
	}

	opaqueness = opaque;
	fillRects = fillRectangles;
    }

    /** 
     * Looks at the paths for the A.TOC files and gets all the
     * coverage rectangles from them.  Sets the entries to a big list
     * of rectangles from all the A.TOC files.
     *
     * @return entries from within the A.TOC files.
     */
    protected Vector getCatalogCoverage(float ullat, float ullon, 
					float lrlat, float lrlon,
					Projection proj, String chartSeries){

	Debug.message("rpfcov", 
		      "RpfCoverageManager: Getting catalog coverage from RpfFrameProvider");
	if (proj == null || frameProvider == null) {
	    return new Vector();
	}

	CADRG cadrg;
	if (proj instanceof CADRG){
	    cadrg = (CADRG)proj;
	} else {
	    cadrg = new CADRG(proj.getCenter(), proj.getScale(), 
			      proj.getWidth(), proj.getHeight());
	}

	Vector[] hemisphereData;

	if (ullon > 0 && lrlon < 0 || (Math.abs(ullon - lrlon) < .001)){
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
// 	if (proj instanceof Cylindrical) {
// 	    currentLineType = OMGraphic.LINETYPE_STRAIGHT;
// 	}
	
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

	for (int j = 0; j < hemisphereData.length; j++){
	    if (hemisphereData[j] == null){
		Debug.message("rpfcov", "RpfCoverageManager. vector " + j + " is null");
		continue;
	    }

	    int size = hemisphereData[j].size();
	    for (int i = 0; i < size; i++){
		RpfCoverageBox box = (RpfCoverageBox) hemisphereData[j].elementAt(i);

		rect = new OMRect((float) box.nw_lat, (float) box.nw_lon, 
				  (float) box.se_lat, (float) box.se_lon, 
				  currentLineType);
	    
		float scale = RpfProductInfo.get(box.chartCode).scale;

		if (scale < 15000f){
		    rect.setLinePaint(CGColor);
		    if (fillRects) rect.setFillPaint(CGColor);
		    cgs.add(rect);
		} else if (scale == 50000f){
		    rect.setLinePaint(TLMColor);
		    if (fillRects) rect.setFillPaint(TLMColor);
		    tlms.add(rect);
		} else if (scale == 250000f){
		    rect.setLinePaint(JOGColor);
		    if (fillRects) rect.setFillPaint(JOGColor);
		    jogs.add(rect);
		} else if (scale == 500000f){
		    rect.setLinePaint(TPCColor);
		    if (fillRects) rect.setFillPaint(TPCColor);
		    tpcs.add(rect);
		} else if (scale == 1000000f){
		    rect.setLinePaint(ONCColor);
		    if (fillRects) rect.setFillPaint(ONCColor);
		    oncs.add(rect);
		} else if (scale == 2000000f){
		    rect.setLinePaint(JNCColor);
		    if (fillRects) rect.setFillPaint(JNCColor);
		    jncs.add(rect);
		} else if (scale == 5000000f){
		    rect.setLinePaint(GNCColor);
		    if (fillRects) rect.setFillPaint(GNCColor);
		    gncs.add(rect);
		} else if (scale == 66666f){
		    rect.setLinePaint(CIB10Color);
		    if (fillRects) rect.setFillPaint(CIB10Color);
		    cib10s.add(rect);
		} else if (scale == 33333f){
		    rect.setLinePaint(CIB5Color);
		    if (fillRects) rect.setFillPaint(CIB5Color);
		    cib5s.add(rect);
		} else if (scale == RpfConstants.Various){
		    // Don't show it, because we don't know how to
		    // display it anyway.  Don't bother projecting it.
		    continue;
		} else {
		    rect.setLinePaint(MISCColor);
		    if (fillRects) rect.setFillPaint(MISCColor);
		    miscs.add(rect);
		}
		rect.generate(proj);
	    }
	}
	return omGraphics;
    }
}
