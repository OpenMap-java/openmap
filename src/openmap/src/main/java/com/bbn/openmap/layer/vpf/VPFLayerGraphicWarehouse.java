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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFLayerGraphicWarehouse.java,v $
// $RCSfile: VPFLayerGraphicWarehouse.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Implement a graphic factory that builds OMGraphics.
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic
 */
public class VPFLayerGraphicWarehouse extends LayerGraphicWarehouseSupport {

    /**
     * the properties file string that tells us what area features to
     * draw
     */
    String areaFeatures = null;
    /** one of these columns must be non-null to draw area features */
    int areaSkipFeatures[] = null;
    /**
     * the properties file string that tells us what edge features to
     * draw
     */
    String edgeFeatures = null;
    /** one of these columns must be non-null to draw edge features */
    int edgeSkipFeatures[] = null;
    /**
     * the properties file string that tells us what text features to
     * draw
     */
    String textFeatures = null;
    /** one of these columns must be non-null to draw text features */
    int textSkipFeatures[] = null;
    /**
     * the properties file string that tells us what entity point
     * features to draw
     */
    String epointFeatures = null;
    /**
     * one of these columns must be non-null to draw entity point
     * features
     */
    int epointSkipFeatures[] = null;
    /**
     * the properties file string that tells us what connected point
     * features to draw
     */
    String cpointFeatures = null;
    /**
     * one of these columns must be non-null to draw connected point
     * features
     */
    int cpointSkipFeatures[] = null;

    /**
     *  
     */
    public VPFLayerGraphicWarehouse() {
        super();
    }

    /**
     * Set properties of the warehouse.
     * 
     * @param prefix the prefix to use for looking up properties.
     * @param props the properties file to look at.
     */
    public void setProperties(String prefix, java.util.Properties props) {
        super.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        areaFeatures = props.getProperty(realPrefix + "area");
        if (areaFeatures == null) {
            areaSkipFeatures = new int[0];
        } else {
            areaSkipFeatures = null;
        }

        textFeatures = props.getProperty(realPrefix + "text");
        if (textFeatures == null) {
            textSkipFeatures = new int[0];
        } else {
            textSkipFeatures = null;
        }

        edgeFeatures = props.getProperty(realPrefix + "edge");
        if (edgeFeatures == null) {
            edgeSkipFeatures = new int[0];
        } else {
            edgeSkipFeatures = null;
        }

        epointFeatures = props.getProperty(realPrefix + "epoint");
        if (epointFeatures == null) {
            epointSkipFeatures = new int[0];
        } else {
            epointSkipFeatures = null;
        }

        cpointFeatures = props.getProperty(realPrefix + "cpoint");
        if (cpointFeatures == null) {
            cpointSkipFeatures = new int[0];
        } else {
            cpointSkipFeatures = null;
        }

    }

    /**
     * Get a List of Strings listing all the feature types wanted.
     * Returned with the area features first, then text features, then
     * line features, then point features.
     */
    public List<String> getFeatures() {
        List<String> retval = new ArrayList<String>();

        StringTokenizer t;
        if (areaFeatures != null) {
            t = new StringTokenizer(areaFeatures);
            while (t.hasMoreTokens()) {
                retval.add(t.nextToken());
            }
        }
        if (textFeatures != null) {
            t = new StringTokenizer(textFeatures);
            while (t.hasMoreTokens()) {
                retval.add(t.nextToken());
            }
        }
        if (edgeFeatures != null) {
            t = new StringTokenizer(edgeFeatures);
            while (t.hasMoreTokens()) {
                retval.add(t.nextToken());
            }
        }
        if (epointFeatures != null) {
            t = new StringTokenizer(epointFeatures);
            while (t.hasMoreTokens()) {
                retval.add(t.nextToken());
            }
        }
        if (cpointFeatures != null) {
            t = new StringTokenizer(cpointFeatures);
            while (t.hasMoreTokens()) {
                retval.add(t.nextToken());
            }
        }

        return retval;
    }

    /**
     * Build an array that lists the columns we require the record to
     * have.
     * 
     * @param featureString the (space-separated) list of required
     *        columns
     * @param table the table we use to find the column numbers
     * @param colAppend the (possibly null) string we append to the
     *        entries in featureString to build the real column name
     */
    protected int[] getSkipArray(String featureString, DcwRecordFile table,
                                 String colAppend) {
        List<Integer> tmpvec = new ArrayList<Integer>();
        if (featureString != null) {
            StringTokenizer t = new StringTokenizer(featureString);
            while (t.hasMoreTokens()) {
                String colname = t.nextToken();
                if (colAppend != null) {
                    colname += colAppend;
                }
                int colnum = table.whatColumn(colname);
                if (colnum != -1) {
                    tmpvec.add(new Integer(colnum));
                }
            }
        }

        int[] retval = new int[tmpvec.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = tmpvec.get(i).intValue();
        }
        return retval;
    }

    /**
     * Lets the warehouse know that a different CoverageAttributeTable
     * will be using it. The skip arrays need to be reset.
     */
    public void resetForCAT() {
        areaSkipFeatures = null;
        textSkipFeatures = null;
        edgeSkipFeatures = null;
        epointSkipFeatures = null;
        cpointSkipFeatures = null;
    }

    /**
     * Determine if this primitive should be drawn or skipped.
     * 
     * @param prim the list for the primitive feature object.
     * @param skipArray a list of columns.
     * @return true if any of the columns listed in skipArray is
     *         non-null.
     */
    protected boolean createFeature(List<Object> prim, int[] skipArray) {
        //length==0 --> user wants everything
        if (skipArray.length == 0) {
            return true;
        }
        for (int i = 0; i < skipArray.length; i++) {
            int val = VPFUtil.objectToInt(prim.get(skipArray[i]));
            if (val != Integer.MIN_VALUE) {
                return true;
            }
        }
        return false;
    }

    final transient static java.awt.Color aaronscolor = new java.awt.Color(0xBDDE83);

    /**
     *  
     */
    public void createArea(CoverageTable covtable, AreaTable areatable,
                           List<Object> facevec, LatLonPoint ll1, LatLonPoint ll2,
                           double dpplat, double dpplon) {
        if (areaSkipFeatures == null) {
            areaSkipFeatures = getSkipArray(areaFeatures, areatable, ".aft_id");
        }

        if (!createFeature(facevec, areaSkipFeatures)) {
            return;
        }

        List<CoordFloatString> ipts = new ArrayList<CoordFloatString>();

        int totalSize = 0;
        try {
            totalSize = areatable.computeEdgePoints(facevec, ipts);
        } catch (FormatException f) {
            Debug.output("FormatException in computeEdgePoints: " + f);
            return;
        }
        if (totalSize == 0) {
            //System.out.println("No edged: " + descript);
            return;
        }

        OMPoly py = createAreaOMPoly(ipts,
                totalSize,
                ll1,
                ll2,
                dpplat,
                dpplon,
                covtable.doAntarcticaWorkaround);

        //      final MutableInt areatype = new MutableInt(-1);
        //      String descript = covtable.getAreaDescription(facevec,
        // areatype);
        //      if (areatype.value == -1) {
        //          areatype.value = 0;
        //      }

        drawingAttributes.setTo(py);

        // HACK to get tile boundaries to not show up for areas.
        //         py.setLinePaint(py.getFillPaint());
        //         py.setSelectPaint(py.getFillPaint());
        py.setLinePaint(OMColor.clear);
        py.setSelectPaint(OMColor.clear);

        addArea(py);
    }

    /**
     *  
     */
    public void createEdge(CoverageTable c, EdgeTable edgetable, List<Object> edgevec,
                           LatLonPoint ll1, LatLonPoint ll2, double dpplat,
                           double dpplon, CoordFloatString coords) {

        if (edgeSkipFeatures == null) {
            if (Debug.debugging("vpf")) {
                Debug.output("Warehouse.createEdge(): edgeFeatures = "
                        + edgeFeatures);

                final MutableInt lineType = new MutableInt(-1);
                Debug.output("Warehouse: "
                        + c.getLineDescription(edgevec, lineType));

            }

            String columnName = ".lft_id";
            edgeSkipFeatures = getSkipArray(edgeFeatures, edgetable, columnName);
        }

        // HACK remove crufty dateline. This HACK may require
        // additional hackage in FeatureClassInfo.java In particular,
        // you may need to initialize the class during construction.
        /*
         * FeatureClassInfo[] lineinfo = c.lineinfo; int len =
         * lineinfo.length; for (int i=0; i <len; i++) { String ftname =
         * lineinfo[i].getTableName(); ftname.trim(); if
         * (ftname.equals("polbndl.lft")) { int col =
         * edgetable.whatColumn("polbndl.lft_id"); int row =
         * ((Integer)edgevec.get(col)).intValue(); if (row ==
         * Integer.MIN_VALUE) { continue; } List fvec=null; try { fvec =
         * lineinfo[i].getRow(row); } catch (FormatException f) {
         * f.printStackTrace(); continue; } String str =
         * (String)fvec.get(lineinfo[i].whatColumn("f_code"));
         * str.trim(); if (str.equals("FA110")) {
         * System.out.println("ignoring dateline"); return; } } }
         */

        if (!createFeature(edgevec, edgeSkipFeatures)) {
            return;
        }

        OMPoly py = createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
        drawingAttributes.setTo(py);
        py.setFillPaint(OMColor.clear);
        py.setIsPolygon(false);
        addEdge(py);
    }

    /**
     *  
     */
    public void createText(CoverageTable c, TextTable texttable, List<Object> textvec,
                           double latitude, double longitude, String text) {

        if (textSkipFeatures == null) {
            textSkipFeatures = getSkipArray(textFeatures, texttable, ".tft_id");
        }

        if (!createFeature(textvec, textSkipFeatures)) {
            return;
        }

        OMText txt = createOMText(text, latitude, longitude);

        drawingAttributes.setTo(txt);
        addText(txt);
    }

    public void createNode(CoverageTable c, NodeTable nt, List<Object> nodeprim,
                           double latitude, double longitude, boolean isEntityNode) {
        int[] skipFeatures = null;
        if (isEntityNode) {
            if (epointSkipFeatures == null) {
                epointSkipFeatures = getSkipArray(epointFeatures, nt, ".pft_id");
            }
            skipFeatures = epointSkipFeatures;
        } else {
            if (cpointSkipFeatures == null) {
                cpointSkipFeatures = getSkipArray(cpointFeatures, nt, ".pft_id");
            }
            skipFeatures = cpointSkipFeatures;
        }

        if (!createFeature(nodeprim, skipFeatures)) {
            return;
        }

        OMPoint pt = createOMPoint(latitude, longitude);

        drawingAttributes.setTo(pt);
        addPoint(pt);
    }

    public static void main(String argv[]) {
        new VPFLayerGraphicWarehouse();
    }
}