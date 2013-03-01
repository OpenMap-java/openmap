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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFFeatureGraphicWarehouse.java,v $
// $RCSfile: VPFFeatureGraphicWarehouse.java,v $
// $Revision: 1.9 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Implement a graphic factory that builds OMGraphics. It's different in that it
 * expects that the feature type has already been checked at the CoverageTable
 * level, and should just build whatever graphic is sent to it. Called from
 * within CoverageTable.drawFeatures().
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic
 */
public class VPFFeatureGraphicWarehouse extends VPFLayerGraphicWarehouse implements
        VPFFeatureWarehouse {

    public final static String DEFAULT = "DEFAULT";
    protected Hashtable<String, DrawingAttributes> featureDrawingAttributes;

    /**
     *  
     */
    public VPFFeatureGraphicWarehouse() {
        super();
    }

    /**
     * Called from super class constructor.
     * 
     */
    protected void initDrawingAttributes() {
        drawingAttributes = new FeatureDrawingAttributes();
    }

    /**
     * Set properties of the warehouse.
     * 
     * @param prefix the prefix to use for looking up properties.
     * @param props the properties file to look at.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        createFeatureDrawingAttributes(prefix, props, getFeatures());
    }

    /**
     * From the initial properties, create the hashtable that holds the
     * DrawingAttributes object for each feature type. The feature name is the
     * key to the drawing attributes for that feature (roadl).
     * 
     * @param prefix the prefix used for the properties
     * @param props the properties object
     * @param features a List of Strings, each representing a feature type that
     *        when appended to the prefix, will serve as a prefix for the
     *        drawing attributes settings for that feature. With a layer prefix
     *        of vmapRoads, and a feature type of roadl, the line color
     *        attribute property looked for will be vmapRoads.roadl.lineColor.
     */
    public void createFeatureDrawingAttributes(String prefix, Properties props,
                                               List<String> features) {

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        featureDrawingAttributes = new Hashtable<String, DrawingAttributes>();
        if (drawingAttributes != null) {
            featureDrawingAttributes.put(DEFAULT, drawingAttributes);
        } else {
            drawingAttributes = new FeatureDrawingAttributes();
        }

        for (String feature : features) {
            FeatureDrawingAttributes da = (FeatureDrawingAttributes) drawingAttributes.clone();
            da.setStroke(drawingAttributes.cloneBasicStroke());
            da.setProperties(realPrefix + feature, props);
            featureDrawingAttributes.put(feature, da);
        }
    }

    /**
     * Don't do this lightly, or everything will be colored the default value.
     * The keys to the Hashtable should be the feature type names, and the
     * values should be the DrawingAttributes for that feature.
     */
    public void setFeatureDrawingAttributes(Hashtable<String, DrawingAttributes> attributes) {
        featureDrawingAttributes = attributes;
    }

    /**
     * Get the Hashtable used for the feature DrawingAttributes lookup.
     */
    public Hashtable<String, DrawingAttributes> getFeatureDrawingAttributes() {
        return featureDrawingAttributes;
    }

    /**
     * Return the GUI for certain warehouse attributes. By default, return the
     * GUI for the DrawingAttributes object being used for rendering attributes
     * of the graphics.
     * 
     * @param lst LibrarySelectionTable to use to get information about the
     *        data, if needed.
     */
    public Component getGUI(LibrarySelectionTable lst) {

        JTabbedPane jtp = new JTabbedPane();

        jtp.addTab(DEFAULT, null, drawingAttributes.getGUI(), "General Attributes");

        for (String currentFeature : getFeatures()) {
            DrawingAttributes da = getAttributesForFeature(currentFeature);

            if (da != null) {
                String desc = null;
                try {
                    desc = lst.getDescription(currentFeature);
                } catch (FormatException fe) {
                }

                if (desc == null) {
                    desc = "Feature Description Unavailable";
                }
                JPanel featurePanel = new JPanel();
                featurePanel.add(da.getGUI());

                jtp.addTab(currentFeature, null, featurePanel, desc);
            }
        }
        return jtp;
    }

    /**
     * Given a feature type, get the DrawingAttributes for that feature. Should
     * be very unlikely to get a null value back.
     */
    public FeatureDrawingAttributes getAttributesForFeature(String featureType) {
        if (featureType != null) {
            FeatureDrawingAttributes ret;

            if (featureDrawingAttributes != null) {
                ret = (FeatureDrawingAttributes) featureDrawingAttributes.get(featureType);
                if (ret == null) {
                    ret = (FeatureDrawingAttributes) drawingAttributes;
                }

            } else {
                ret = (FeatureDrawingAttributes) drawingAttributes;
            }

            return ret;

        } else {
            return (FeatureDrawingAttributes) drawingAttributes;
        }
    }

    /**
     *  
     */
    public OMGraphic createArea(CoverageTable covtable, AreaTable areatable, List<Object> facevec,
                                LatLonPoint ll1, LatLonPoint ll2, double dpplat, double dpplon,
                                String featureType, int primID) {

        List<CoordFloatString> ipts = new ArrayList<CoordFloatString>();

        int totalSize = 0;
        try {
            totalSize = areatable.computeEdgePoints(facevec, ipts);
        } catch (FormatException f) {
            Debug.output("FormatException in computeEdgePoints: " + f);
            return null;
        }
        if (totalSize == 0) {
            return null;
        }

        OMPoly py = createAreaOMPoly(ipts, totalSize, ll1, ll2, dpplat, dpplon, covtable.doAntarcticaWorkaround);

        // getAttributesForFeature(featureType).setTo(py);
        //int id = ((Integer) facevec.get(0)).intValue();
        setAttributesForFeature(py, covtable, featureType, primID);
        // HACK to get tile boundaries to not show up for areas.
        // py.setLinePaint(py.getFillPaint());
        // py.setSelectPaint(py.getFillPaint());
        py.setLinePaint(OMColor.clear);
        py.setSelectPaint(OMColor.clear);

        addArea(py);
        return py;
    }

    protected String info = null;

    /**
     *  
     */
    public OMGraphic createEdge(CoverageTable c, EdgeTable edgetable, List<Object> edgevec,
                                LatLonPoint ll1, LatLonPoint ll2, double dpplat, double dpplon,
                                CoordFloatString coords, String featureType, int primID) {

        //int id = ((Integer) edgevec.get(0)).intValue();

        OMPoly py = createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
        setAttributesForFeature(py, c, featureType, primID);
        py.setFillPaint(OMColor.clear);
        py.setIsPolygon(false);
        addEdge(py);
        return py;
    }

    /**
     * @param omg The OMGraphic owning the attributes.
     * @param c the CoverageTable for the feature.
     * @param featureType the type of Feature.
     * @param id ID of the OMGraphic.
     */
    protected void setAttributesForFeature(OMGraphic omg, CoverageTable c, String featureType,
                                           int id) {
        FeatureDrawingAttributes fda = getAttributesForFeature(featureType);

        if (fda.getFci() == null) {
            fda.setFci(c.getFeatureClassInfo(featureType));
        }

        fda.setTo(omg, id);
    }

    /**
     *  
     */
    public OMGraphic createText(CoverageTable c, TextTable texttable, List<Object> textvec,
                                double latitude, double longitude, String text, String featureType,
                                int primID) {

        OMText txt = createOMText(text, latitude, longitude);
        //int id = ((Integer) textvec.get(0)).intValue();
        setAttributesForFeature(txt, c, featureType, primID);
        // getAttributesForFeature(featureType).setTo(txt);
        addText(txt);
        return txt;
    }

    /**
     * Method called by the VPF reader code to construct a node feature.
     */
    public OMGraphic createNode(CoverageTable c, NodeTable t, List<Object> nodeprim,
                                double latitude, double longitude, boolean isEntityNode,
                                String featureType, int primID) {
        OMPoint pt = createOMPoint(latitude, longitude);
        // int id = ((Integer) nodeprim.get(0)).intValue();
        setAttributesForFeature(pt, c, featureType, primID);
        // getAttributesForFeature(featureType).setTo(pt);
        addPoint(pt);
        return pt;
    }

    public boolean needToFetchTileContents(String libraryName, String currentFeature,
                                           TileDirectory currentTile) {
        return true;
    }

    public static void main(String argv[]) {
        new VPFFeatureGraphicWarehouse();
    }
}