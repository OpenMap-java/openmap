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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFFeatureGraphicWarehouse.java,v $
// $RCSfile: VPFFeatureGraphicWarehouse.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.io.FormatException;
import java.awt.Color;
import java.awt.Component;
import java.util.*;
import javax.swing.JTabbedPane;

/**
 * Implement a graphic factory that builds OMGraphics.  It's different
 * in that it expects that the feature type has already been checked
 * at the CoverageTable level, and should just build whatever graphic
 * is sent to it.  Called from within CoverageTable.drawFeatures().
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic 
 */
public class VPFFeatureGraphicWarehouse 
    extends VPFLayerGraphicWarehouse implements VPFFeatureWarehouse {

    public final static String DEFAULT = "DEFAULT";
    Hashtable featureDrawingAttributes;
    
    /**
     *
     */
    public VPFFeatureGraphicWarehouse() {
        super();
    }

    /**
     * Set properties of the warehouse.
     * @param prefix the prefix to use for looking up properties.
     * @param props the properties file to look at.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
	createFeatureDrawingAttributes(prefix, props, getFeatures());
    }

    /**
     * From the initial properties, create the hashtable that holds
     * the DrawingAttributes object for each feature type.  The
     * feature name is the key to the drawing attributes for that
     * feature (roadl).
     *
     * @param prefix the prefix used for the properties
     * @param props the properties object
     * @param features a List of Strings, each representing a
     * feature type that when appended to the prefix, will serve as a
     * prefix for the drawing attributes settings for that feature.
     * With a layer prefix of vmapRoads, and a feature type of roadl,
     * the line color attribute property looked for will be
     * vmapRoads.roadl.lineColor.
     */
    public void createFeatureDrawingAttributes(String prefix, 
					       Properties props, 
					       List features) {

	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

	featureDrawingAttributes = new Hashtable();
	if (drawingAttributes != null) {
	    featureDrawingAttributes.put(DEFAULT, drawingAttributes);
	} else {
	    drawingAttributes = DrawingAttributes.getDefaultClone();
	}

	for(Iterator fiter = features.iterator(); fiter.hasNext();) {
	    String feature = ((String)fiter.next()).intern();
	    DrawingAttributes da = (DrawingAttributes)drawingAttributes.clone();
	    da.setProperties(realPrefix + feature, props);
	    // If they are equal, don't save a copy.
	    if (da.equals(drawingAttributes)) {
		da = drawingAttributes;
	    }
	    featureDrawingAttributes.put(feature, da);
	}
    }

    /**
     * Don't do this lightly, or everything will be colored the
     * default value.  The keys to the Hashtable should be the feature
     * type names, and the values should be the DrawingAttributes for
     * that feature.
     */
    public void setFeatureDrawingAttributes(Hashtable attributes) {
	featureDrawingAttributes = attributes;
    }

    /**
     * Get the Hashtable used for the feature DrawingAttributes
     * lookup.
     */
    public Hashtable getFeatureDrawingAttributes() {
	return featureDrawingAttributes;
    }

    /**
     * Return the GUI for certain warehouse attributes.  By default,
     * return the GUI for the DrawingAttributes object being used for
     * rendering attributes of the graphics.  
     *
     * @param lst LibrarySelectionTable to use to get information
     * about the data, if needed.  
     */
    public Component getGUI(LibrarySelectionTable lst) {
	JTabbedPane jtp = new JTabbedPane();

	jtp.addTab(DEFAULT, null, drawingAttributes.getGUI(), "General Attributes");
	List features = getFeatures();
	int size = features.size();
	for (int i = 0; i < size; i++) {
	    String currentFeature = (String) features.get(i);
	    DrawingAttributes da = getAttributesForFeature(currentFeature);
	    if (da != null && !da.equals(drawingAttributes)) {
		String desc = null;
		try {
		    desc = lst.getDescription(currentFeature);
		} catch (FormatException fe){}

		if (desc == null) {
		    desc = "Feature Description Unavailable";
		}

		jtp.addTab(currentFeature, null, da.getGUI(), desc);
	    }
	}
	return jtp;
    }

    /**
     * Given a feature type, get the DrawingAttributes for that
     * feature.  Should be very unlikely to get a null value back.
     */
    public DrawingAttributes getAttributesForFeature(String featureType) {
	DrawingAttributes ret;

	if (featureDrawingAttributes != null) {
	    ret = (DrawingAttributes)featureDrawingAttributes.get(featureType);
	    if (ret == null) {
		ret = drawingAttributes;
	    }
	} else {
	    ret = drawingAttributes;
	}
	return ret;
    }

    /**
     *
     */
    public void createArea(CoverageTable covtable, AreaTable areatable,
			   List facevec,
			   LatLonPoint ll1,
			   LatLonPoint ll2,
			   float dpplat,
			   float dpplon,
			   String featureType)
    {

	List ipts = new ArrayList();

	int totalSize = 0;
        try {
	    totalSize = areatable.computeEdgePoints(facevec, ipts);
	} catch (FormatException f) {
 	    Debug.output("FormatException in computeEdgePoints: " + f);
	    return;
	}
	if (totalSize == 0) {
	    return;
	}

	OMPoly py = createAreaOMPoly(ipts, totalSize, ll1, ll2, 
				     dpplat, dpplon,
				     covtable.doAntarcticaWorkaround);

	getAttributesForFeature(featureType).setTo(py);
//  	drawingAttributes.setTo(py);

	// HACK to get tile boundaries to not show up for areas.
	py.setLinePaint(py.getFillPaint());
	py.setSelectPaint(py.getFillPaint());

	graphics.add(py);
    }

    /**
     *
     */
    public void createEdge(CoverageTable c, EdgeTable edgetable,
			   List edgevec,
			   LatLonPoint ll1,
			   LatLonPoint ll2,
			   float dpplat,
			   float dpplon,
			   CoordFloatString coords,
			   String featureType)
    {

	OMPoly py = createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
	getAttributesForFeature(featureType).setTo(py);
//  	drawingAttributes.setTo(py);
	py.setIsPolygon(false);
	graphics.add(py);
    }

    /**
     *
     */
    public void createText(CoverageTable c, TextTable texttable,
			   List textvec,
			   float latitude,
			   float longitude,
			   String text,
			   String featureType)
    {

	OMText txt = createOMText(text, latitude, longitude);
	getAttributesForFeature(featureType).setTo(txt);
//  	drawingAttributes.setTo(txt);
	graphics.add(txt);
    }

    /**
     * Method called by the VPF reader code to construct a node feature.
     */
    public void createNode(CoverageTable c, NodeTable t, List nodeprim,
			   float latitude, float longitude,
			   boolean isEntityNode, String featureType) {
	OMPoint pt = createOMPoint(latitude, longitude);
	getAttributesForFeature(featureType).setTo(pt);
	graphics.add(pt);
    }

    public static void main(String argv[]) {
	new VPFFeatureGraphicWarehouse();
    }
}
