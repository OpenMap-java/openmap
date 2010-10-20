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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/VPFSpecialistGraphicWarehouse.java,v
// $
// $RCSfile: VPFSpecialistGraphicWarehouse.java,v $
// $Revision: 1.5 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.specialist.SPoly;
import com.bbn.openmap.layer.specialist.SText;
import com.bbn.openmap.layer.vpf.AreaTable;
import com.bbn.openmap.layer.vpf.CoordFloatString;
import com.bbn.openmap.layer.vpf.CoverageTable;
import com.bbn.openmap.layer.vpf.DcwRecordFile;
import com.bbn.openmap.layer.vpf.EdgeTable;
import com.bbn.openmap.layer.vpf.NodeTable;
import com.bbn.openmap.layer.vpf.TextTable;
import com.bbn.openmap.layer.vpf.VPFUtil;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class VPFSpecialistGraphicWarehouse
      extends GraphicWarehouseSupport {

   /**
    * the properties file string that tells us what area features to draw
    */
   String areaFeatures = null;
   /** one of these columns must be non-null to draw area features */
   int areaSkipFeatures[] = null;
   /**
    * the properties file string that tells us what edge features to draw
    */
   String edgeFeatures = null;
   /** one of these columns must be non-null to draw edge features */
   int edgeSkipFeatures[] = null;
   /**
    * the properties file string that tells us what text features to draw
    */
   String textFeatures = null;
   /** one of these columns must be non-null to draw text features */
   int textSkipFeatures[] = null;
   /**
    * the properties file string that tells us what entity point features to
    * draw
    */
   String epointFeatures = null;
   /**
    * one of these columns must be non-null to draw entity point features
    */
   int epointSkipFeatures[] = null;
   /**
    * the properties file string that tells us what connected point features to
    * draw
    */
   String cpointFeatures = null;
   /**
    * one of these columns must be non-null to draw connected point features
    */
   int cpointSkipFeatures[] = null;

   /**
    * Drawing attributes for the particular graphic. Values are set on a
    * request.
    */
   DrawingAttributes drawAtt = new DrawingAttributes();

   /**
     *  
     */
   public VPFSpecialistGraphicWarehouse() {
      super();
   }

   /**
    * Set properties of the warehouse
    * 
    * @param prefix the prefix to use for looking up properties
    * @param props the properties file to look it
    */
   public void setProperties(String prefix, java.util.Properties props) {

      String forceFeatureTypes = props.getProperty(prefix + ".draw");

      areaFeatures = props.getProperty(prefix + ".area");
      if (areaFeatures == null) {
         areaSkipFeatures = new int[0];
      } else {
         areaSkipFeatures = null;
         if (forceFeatureTypes != null) {
            setAreaFeatures(drawAreaFeatures() || (forceFeatureTypes.indexOf("area") != -1));
         }
      }

      textFeatures = props.getProperty(prefix + ".text");
      if (textFeatures == null) {
         textSkipFeatures = new int[0];
      } else {
         textSkipFeatures = null;
         if (forceFeatureTypes != null) {
            setTextFeatures(drawTextFeatures() || (forceFeatureTypes.indexOf("text") != -1));
         }
      }

      edgeFeatures = props.getProperty(prefix + ".edge");
      if (edgeFeatures == null) {
         edgeSkipFeatures = new int[0];
      } else {
         edgeSkipFeatures = null;
         if (forceFeatureTypes != null) {
            setEdgeFeatures(drawEdgeFeatures() || (forceFeatureTypes.indexOf("edge") != -1));
         }
      }

      drawAtt.setProperties(prefix, props);
   }

   /**
    * Build an array that lists the columns we require the record to have.
    * 
    * @param featureString the (space-separated) list of required columns
    * @param table the table we use to find the column numbers
    * @param colAppend the (possibly null) string we append to the entries in
    *        featureString to build the real column name
    */

   protected int[] getSkipArray(String featureString, DcwRecordFile table, String colAppend) {
      List tmpvec = new ArrayList();
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
         retval[i] = ((Integer) tmpvec.get(i)).intValue();
      }
      return retval;
   }

   /**
    * Determine if this primitive should be drawn or skipped.
    * 
    * @param primvec the vector for the primitive feature object
    * @param skipArray a list of columns.
    * @return true if any of the columns listed in skipArray is non-null
    */
   protected boolean createFeature(List primvec, int[] skipArray) {
      // length==0 --> user wants everything
      if (skipArray.length == 0) {
         return true;
      }
      for (int i = 0; i < skipArray.length; i++) {
         int val = VPFUtil.objectToInt(primvec.get(skipArray[i]));
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
   public void createArea(CoverageTable covtable, AreaTable areatable, List facevec, LatLonPoint ll1, LatLonPoint ll2,
                          double dpplat, double dpplon) {
      if (areaSkipFeatures == null) {
         areaSkipFeatures = getSkipArray(areaFeatures, areatable, ".aft_id");
      }

      if (!createFeature(facevec, areaSkipFeatures)) {
         return;
      }

      List ipts = new ArrayList();

      // final MutableInt areatype = new MutableInt(-1);
      // String descript = covtable.getAreaDescription(facevec,
      // areatype);

      // if (areatype.value == 0) {//topology artifact
      // return;
      // }

      // if (areatype.value == 2) {
      // // if (Debug.debugging("vpf")) {
      // // Debug.output("Skipping open ocean: " + descript);
      // // }
      // return;
      // }

      int totalSize = 0;
      try {
         totalSize = areatable.computeEdgePoints(facevec, ipts);
      } catch (FormatException f) {
         // Debug.output("FormatException in computeEdgePoints: " +
         // f);
         return;
      }
      if (totalSize == 0) {
         return;
      }

      SPoly py = createAreaSPoly(ipts, totalSize, ll1, ll2, (float) dpplat, (float) dpplon);
      if (py == null) {
         return;
      }

      // if (areatype.value == -1) {
      // areatype.value = 0;
      // }

      java.awt.Color fc = (java.awt.Color) drawAtt.getFillPaint();
      if (fc == null) {
         fc = java.awt.Color.black;
      }

      py.color(ns(fc));
      py.fillColor(ns(fc));

      // py.fillColor(getSColor(areatype.value));

      // py.object(new LineComp(descript));
      graphics.addSGraphic(py);
   }

   /**
     *  
     */
   public void createEdge(CoverageTable covtable, EdgeTable edgetable, List edgevec, LatLonPoint ll1, LatLonPoint ll2,
                          double dpplat, double dpplon, CoordFloatString coords) {
      if (edgeSkipFeatures == null) {
         edgeSkipFeatures = getSkipArray(edgeFeatures, edgetable, ".lft_id");
      }

      // HACK remove crufty dateline. This HACK may require
      // additional hackage in FeatureClassInfo.java In particular,
      // you may need to initialize the class during construction.
      /*
       * FeatureClassInfo[] lineinfo = c.lineinfo; int len = lineinfo.length;
       * for (int i=0; i <len; i++) { String ftname =
       * lineinfo[i].getTableName(); ftname.trim(); if
       * (ftname.equals("polbndl.lft")) { int col =
       * edgetable.whatColumn("polbndl.lft_id"); int row =
       * ((Integer)edgevec.elementAt(col)).intValue(); if (row ==
       * Integer.MIN_VALUE) continue; Vector fvec=null; try { fvec =
       * lineinfo[i].getRow(row); } catch (FormatException f) {
       * f.printStackTrace(); continue; } String str =
       * (String)fvec.elementAt(lineinfo[i].whatColumn("f_code")); str.trim();
       * if (str.equals("FA110")) { System.out.println("ignoring dateline");
       * return; } } }
       */

      if (!createFeature(edgevec, edgeSkipFeatures)) {
         return;
      }

      // MutableInt lineType = new MutableInt(-1);
      // String desc = covtable.getLineDescription(edgevec,
      // lineType);

      SPoly py = createEdgeSPoly(coords, ll1, ll2, (float) dpplat, (float) dpplon);
      if (py == null) {
         return;
      }

      // py.object(new LineComp(desc));

      java.awt.Color lc = (java.awt.Color) drawAtt.getLinePaint();
      if (lc == null)
         lc = java.awt.Color.black;

      py.color(ns(lc));
      py.lineWidth((short) ((java.awt.BasicStroke) drawAtt.getStroke()).getLineWidth());

      // if (lineType.value < 0) {
      // py.color(new SColor((short)30000,(short)30000,(short)0));
      // } else {
      // py.color(edgeColors[lineType.value % 5]);
      // }

      graphics.addSGraphic(py);
   }

   /**
     *  
     */
   public void createText(CoverageTable covtable, TextTable texttable, List textvec, double latitude, double longitude, String text) {
      if (textSkipFeatures == null) {
         textSkipFeatures = getSkipArray(textFeatures, texttable, ".tft_id");
      }

      if (!createFeature(textvec, textSkipFeatures)) {
         return;
      }

      // MutableInt textType = new MutableInt(-1);
      // String desc = covtable.getTextDescription(textvec,
      // textType);

      SText py = createTextSText(text, (float) latitude, (float) longitude);
      if (py == null) {
         return;
      }
      // py.object(new LineComp(desc));

      java.awt.Color tc = (java.awt.Color) drawAtt.getLinePaint();
      if (tc == null)
         tc = java.awt.Color.black;

      py.color(ns(tc));

      // if (textType.value < 0) {
      // py.color(textColors[5]);
      // } else {
      // py.color(textColors[textType.value % 5]);
      // }

      graphics.addSGraphic(py);
   }

   /**
    * Method called by the VPF reader code to construct a node feature.
    * 
    * @param c the coverage table for this node
    * @param t the nodetable being parsed
    * @param nodeprim the record read from the node table
    * @param latitude the latitude of the node
    * @param longitude the longitude of the node
    * @param isEntityNode true if we are reading entity notes, false if we are
    *        reading connected nodes
    */
   public void createNode(CoverageTable c, NodeTable t, List nodeprim, double latitude, double longitude, boolean isEntityNode) {

   }

}