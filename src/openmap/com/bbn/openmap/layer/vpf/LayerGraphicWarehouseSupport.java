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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LayerGraphicWarehouseSupport.java,v $
// $Revision: 1.13 $ $Date: 2009/01/21 01:24:41 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Component;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FanCompress;
import com.bbn.openmap.util.PropUtils;

/**
 * Implement a graphic factory that builds OMGraphics.
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic
 */
public abstract class LayerGraphicWarehouseSupport
      implements VPFGraphicWarehouse {

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.vpf.VPFGraphicWarehouse");

   protected DrawingAttributes drawingAttributes;

   /** HACK around antarctica display problem. */
   final transient protected static float antarcticaThreshold = ProjMath.degToRad(-89.9f);

   /** hang on to the graphics that we build */
   protected OMGraphicList graphics;

   /** remember if we draw edge features */
   private boolean drawEdgeFeatures;
   /** remember if we draw text features */
   private boolean drawTextFeatures;
   /** remember if we draw area features */
   private boolean drawAreaFeatures;
   /** remember if we draw entity point features */
   private boolean drawEPointFeatures;
   /** remember if we draw connected point features */
   private boolean drawCPointFeatures;

   /**
    * thinning variables. note that thinning is meant to be done offline, so
    * this is not optimized...
    */
   private static boolean doThinning = false;
   private static double fan_eps = 0.01f;

   /**
    * Construct an object, initializes graphiclist
    */
   public LayerGraphicWarehouseSupport() {
      initDrawingAttributes();
      graphics = new OMGraphicList();
      graphics.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);
   }

   /**
    * Called from super class constructor.
    * 
    */
   protected void initDrawingAttributes() {
      drawingAttributes = new DrawingAttributes();
   }

   /**
    * Get the current graphics list.
    * 
    * @return the OMGraphicList.
    */
   public synchronized OMGraphicList getGraphics() {
      return getGraphics(graphics);
   }

   /**
    * Add the area, edge, text and point sublists to the provided list.
    */
   protected synchronized OMGraphicList getGraphics(OMGraphicList addToList) {
      if (areaSubList != null) {
         addToList.add(areaSubList);
      }
      if (edgeSubList != null) {
         addToList.add(edgeSubList);
      }
      if (pointSubList != null) {
         addToList.add(pointSubList);
      }
      if (textSubList != null) {
         addToList.add(textSubList);
      }

      return addToList;
   }

   /**
    * Get the DrawingAttributes used for the coverage type.
    */
   public DrawingAttributes getDrawingAttributes() {
      return drawingAttributes;
   }

   /**
    * Set the drawing attributes for the coverage type.
    */
   public void setDrawingAttributes(DrawingAttributes da) {
      drawingAttributes = da;
   }

   /**
    * Lets the warehouse know that a different CoverageAttributeTable will be
    * using it. Default action is to do nothing.
    */
   public void resetForCAT() {
   }

   /**
    * Set which library to use. If null, all applicable libraries in database
    * will be searched.
    */
   private List<String> useLibrary = null;

   /**
    * Set the VPF library names to use. If null, all libraries will be searched.
    * Null is default.
    */
   public void setUseLibraries(List<String> lib) {
      useLibrary = lib;
   }

   /**
    * Get the VPF library names to use.
    */
   public List<String> getUseLibraries() {
      return useLibrary;
   }

   /**
    * Utility method to check if the specified library name has been set by the
    * configuration as one to use.
    * 
    * @param libName the library name to test
    * @return true if the useLibrary list has not been set, is empty, or if the
    *         provided name starts with a useList entry on it (good for sets of
    *         libraries).
    */
   public boolean checkLibraryForUsage(String libName) {
      boolean useLibrary = true;
      List<String> libraryNames = getUseLibraries();
      if (libraryNames != null && !libraryNames.isEmpty()) {
         useLibrary = false;
         for (String libraryName : libraryNames) {
            if (libName.startsWith(libraryName)) {
               useLibrary = true;
               break;
            }
         }
      }
      return useLibrary;
   }

   /**
    * Return the GUI for certain warehouse attributes. By default, return the
    * GUI for the DrawingAttributes object being used for rendering attributes
    * of the graphics.
    * 
    * @param lst LibrarySelectionTable to use to get information about the data,
    *        if needed. Not needed here.
    */
   public Component getGUI(LibrarySelectionTable lst) {
      if (drawingAttributes != null) {
         return drawingAttributes.getGUI();
      } else {
         return null;
      }
   }

   protected OMGraphicList areaSubList;
   protected OMGraphicList edgeSubList;
   protected OMGraphicList textSubList;
   protected OMGraphicList pointSubList;

   /**
    * Clears the contained list of graphics.
    */
   public void clear() {
      graphics.clear();
      if (areaSubList != null) {
         areaSubList.clear();
         areaSubList = null;
      }
      if (edgeSubList != null) {
         edgeSubList.clear();
         edgeSubList = null;
      }
      if (textSubList != null) {
         textSubList.clear();
         textSubList = null;
      }
      if (pointSubList != null) {
         pointSubList.clear();
         pointSubList = null;
      }
   }

   protected void addArea(OMGraphic area) {
      if (areaSubList == null) {
         areaSubList = new OMGraphicList();
      }
      areaSubList.add(area);
   }

   protected void addEdge(OMGraphic edge) {
      if (edgeSubList == null) {
         edgeSubList = new OMGraphicList();
      }
      edgeSubList.add(edge);
   }

   protected void addText(OMGraphic text) {
      if (textSubList == null) {
         textSubList = new OMGraphicList();
      }
      textSubList.add(text);
   }

   protected void addPoint(OMGraphic point) {
      if (pointSubList == null) {
         pointSubList = new OMGraphicList();
      }
      pointSubList.add(point);
   }

   /**
    * set if we draw edge features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setEdgeFeatures(boolean newvalue) {
      drawEdgeFeatures = newvalue;
   }

   /**
    * Return true if we may draw some edge features.
    */
   public boolean drawEdgeFeatures() {
      return drawEdgeFeatures;
   }

   /**
    * set if we draw text features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setTextFeatures(boolean newvalue) {
      drawTextFeatures = newvalue;
   }

   /**
    * Return true if we may draw some text features.
    */
   public boolean drawTextFeatures() {
      return drawTextFeatures;
   }

   /**
    * set if we draw area features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setAreaFeatures(boolean newvalue) {
      drawAreaFeatures = newvalue;
   }

   /**
    * Return true if we may draw some area features.
    */
   public boolean drawAreaFeatures() {
      return drawAreaFeatures;
   }

   /**
    * set if we draw entity point features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setEPointFeatures(boolean newvalue) {
      drawEPointFeatures = newvalue;
   }

   /**
    * Return true if we may draw some entity point features.
    */
   public boolean drawEPointFeatures() {
      return drawEPointFeatures;
   }

   /**
    * set if we draw connected point features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setCPointFeatures(boolean newvalue) {
      drawCPointFeatures = newvalue;
   }

   /**
    * Return true if we may draw some connected point features.
    */
   public boolean drawCPointFeatures() {
      return drawCPointFeatures;
   }

   /**
    * Sets the features (lines, areas, text, points) that get displayed
    * 
    * @param features a whitespace-separated list of features to display
    */
   public void setFeatures(String features) {
      // If someone gives us a list of features, we need to make
      // sure thats
      // what we use.
      setAreaFeatures(false);
      setEdgeFeatures(false);
      setTextFeatures(false);
      setEPointFeatures(false);
      setCPointFeatures(false);
      StringTokenizer t = new StringTokenizer(features);
      while (t.hasMoreTokens()) {
         String token = t.nextToken();
         if (token.equalsIgnoreCase(VPFUtil.Area)) {
            setAreaFeatures(true);
         } else if (token.equalsIgnoreCase(VPFUtil.Edge)) {
            setEdgeFeatures(true);
         } else if (token.equalsIgnoreCase(VPFUtil.EPoint)) {
            setEPointFeatures(true);
         } else if (token.equalsIgnoreCase(VPFUtil.CPoint)) {
            setCPointFeatures(true);
         } else if (token.equalsIgnoreCase(VPFUtil.Text)) {
            setTextFeatures(true);
         } else {
            Debug.output("LayerGraphicsWarehouseSupport: ignoring feature: " + token);
         }
      }

   }

   public String getFeatureString() {
      StringBuffer features = new StringBuffer();

      if (drawAreaFeatures)
         features.append(VPFUtil.Area.toLowerCase()).append(" ");
      if (drawEdgeFeatures)
         features.append(VPFUtil.Edge.toLowerCase()).append(" ");
      if (drawEPointFeatures)
         features.append(VPFUtil.EPoint.toLowerCase()).append(" ");
      if (drawCPointFeatures)
         features.append(VPFUtil.CPoint.toLowerCase()).append(" ");
      if (drawTextFeatures)
         features.append(VPFUtil.Text.toLowerCase()).append(" ");
      return features.toString();
   }

   /**
    * set drawing attribute properties
    * 
    * @param prefix the prefix for our properties
    * @param props the Properties object we use to look up values
    */
   public void setProperties(String prefix, Properties props) {
      String features;

      String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

      features = props.getProperty(realPrefix + VPFLayer.featureTypesProperty);
      drawingAttributes.setProperties(prefix, props);

      if (features != null) {
         setFeatures(features);
      }
   }

   /**
    * set drawing attribute properties
    * 
    * @param props the Properties object.
    */
   public Properties getProperties(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      String realPrefix = PropUtils.getScopedPropertyPrefix(drawingAttributes);

      props.put(realPrefix + VPFLayer.featureTypesProperty, getFeatureString());
      drawingAttributes.getProperties(props);
      return props;
   }

   /**
    * create a filled polygon
    * 
    * @param ipts a list of CoordFloatString objects
    * @param totalSize the total number of points
    * @param dpplat threshold for latitude thinning (passed to warehouse)
    * @param dpplon threshold for longitude thinngin (passed to warehouse)
    * @param ll1 upperleft of selection region (passed to warehouse)
    * @param ll2 lowerright of selection region (passed to warehouse)
    * @param doAntarcticaWorkaround hack for funny DCW antarctica data (passed
    *        to warehouse)
    */
   public static OMPoly createAreaOMPoly(List<CoordFloatString> ipts, int totalSize, LatLonPoint ll1, LatLonPoint ll2,
                                         double dpplat, double dpplon, boolean doAntarcticaWorkaround) {

      // thin the data
      // if (doThinning) {
      // totalSize = doThinning(ipts);
      // }

      // *2 for pairs
      double[] llpts = new double[totalSize * 2];

      // only do it if we're in the vicinity
      if (doAntarcticaWorkaround) {
         doAntarcticaWorkaround = (ll2.getLatitude() < -62f);
      }

      int npts = 0;
      for (CoordFloatString cfs : ipts) {
         int cfscnt = cfs.tcount;
         int cfssz = cfs.tsize;
         double cfsvals[] = cfs.vals;
         if (cfscnt > 0) { // normal
            for (int i = 0; i < cfscnt; i++) {
               llpts[npts++] = ProjMath.degToRad(cfsvals[i * cfssz + 1]);// lat
               llpts[npts++] = ProjMath.degToRad(cfsvals[i * cfssz]);// lon
            }
         } else { // reverse
            cfscnt *= -1;
            for (int i = cfscnt - 1; i >= 0; i--) {
               llpts[npts++] = ProjMath.degToRad(cfsvals[i * cfssz + 1]);// lat
               llpts[npts++] = ProjMath.degToRad(cfsvals[i * cfssz]);// lon
            }
         }
      }

      // HACK: we will rewrite the data for the Antarctica polygon
      // so that
      // it will display "correctly" in the cylindrical projections.
      // only check if bottom edge of screen below a certain
      // latitude
      if (doAntarcticaWorkaround) {
         double[] newllpts = new double[llpts.length];
         for (int i = 0; i < newllpts.length; i += 2) {
            newllpts[i] = llpts[i];
            newllpts[i + 1] = llpts[i + 1];

            if (newllpts[i] < antarcticaThreshold) {
               Debug.message("vpf", "AreaTable.generateOMPoly(): Antarctica!");
               // HACK: we're assuming data is going from west to
               // east,
               // so we wrap the other way
               newllpts[i++] = ProjMath.degToRad(-89.99f);
               newllpts[i++] = ProjMath.degToRad(179.99f);
               newllpts[i++] = ProjMath.degToRad(-89.99f);
               newllpts[i++] = ProjMath.degToRad(90f);
               newllpts[i++] = ProjMath.degToRad(-89.99f);
               newllpts[i++] = ProjMath.degToRad(0f);
               newllpts[i++] = ProjMath.degToRad(-89.99f);
               newllpts[i++] = ProjMath.degToRad(-90f);
               newllpts[i++] = ProjMath.degToRad(-89.99f);
               newllpts[i++] = ProjMath.degToRad(-179.99f);
               // HACK: advance to western hemisphere where we
               // pick up the real data again
               while (llpts[i + 1] > 0) {// lon
                  newllpts[i++] = ProjMath.degToRad(-89.99f);
                  newllpts[i++] = ProjMath.degToRad(-179.99f);
               }
               i -= 2;
            }
         }
         llpts = newllpts;
      }

      // create polygon - change to OMPoly for jdk 1.1.x compliance.
      OMPoly py = new OMPoly(llpts, OMGraphic.RADIANS, OMGraphic.LINETYPE_STRAIGHT);

      return py;
   }

   /**
    * Create an OMPoly corresponding to a VPF edge feature
    * 
    * @param coords the coordinates to use for the poly
    * @param ll1 upper left, used for clipping
    * @param ll2 lower right, used for clipping
    * @param dpplat used for latitude thinning
    * @param dpplon used for longitude thinning
    */
   public static OMPoly createEdgeOMPoly(CoordFloatString coords, LatLonPoint ll1, LatLonPoint ll2, double dpplat, double dpplon) {
      // thin the data
      // if (doThinning) {
      // List ipts = new ArrayList(1);
      // ipts.add(coords);
      // doThinning(ipts);
      // }

      double[] llpts = coords.vals; // NOTE: lon,lat order!

      // handle larger tuples (do extra O(n) loop to extract only
      // lon/lats.
      if (coords.tsize > 2) {// assume 3
         /*
          * if (Debug.debugging("vpf")) {
          * Debug.output("EdgeTable.drawTile: big tuple size: " + coords.tsize);
          * }
          */
         double[] newllpts = new double[coords.tcount * 2];// *2 for
                                                           // pairs
         int len = newllpts.length;
         for (int i = 0, j = 0; i < len; i += 2, j += 3) {
            newllpts[i] = ProjMath.degToRad(llpts[j + 1]);// lat
            newllpts[i + 1] = ProjMath.degToRad(llpts[j]);// lon
         }
         llpts = newllpts;
      } else {
         double lon;
         int len = llpts.length;
         for (int i = 0; i < len; i += 2) {
            lon = ProjMath.degToRad(llpts[i]);
            llpts[i] = ProjMath.degToRad(llpts[i + 1]);// lat
            llpts[i + 1] = lon;// lon
         }
      }

      // create polyline - change to OMPoly for jdk 1.1.x
      // compliance.
      OMPoly py = new OMPoly(llpts, OMGraphic.RADIANS, OMGraphic.LINETYPE_STRAIGHT);
      return py;
   }

   /**
    * Set doThinning.
    * 
    * @param value boolean
    */
   public static void setDoThinning(boolean value) {
      doThinning = value;
   }

   /**
    * Are we thinning?.
    * 
    * @return boolean
    */
   public static boolean isDoThinning() {
      return doThinning;
   }

   /**
    * Set fan compression epsilon.
    * 
    * @param value double
    */
   public static void setFanEpsilon(double value) {
      fan_eps = value;
   }

   /**
    * Get fan compression epsilon.
    * 
    * @return double
    */
   public static double getFanEpsilon() {
      return fan_eps;
   }

   /**
    * do fan compression of raw edge points
    */
   protected static int doThinning(List<Object> ipts) {
      int size = ipts.size();
      int totalSize = 0;
      for (int j = 0; j < size; j++) {

         // get poly
         CoordFloatString cfs = (CoordFloatString) ipts.get(j);
         int cfscnt = cfs.tcount;
         int cfssz = cfs.tsize;
         double[] cfsvals = cfs.vals;
         int npts = 0;

         // handle reverse
         boolean rev = (cfscnt < 0);
         if (rev) {
            cfscnt = -cfscnt;
         }

         // copy points
         double[] llpts = new double[cfscnt << 1];
         for (int i = 0; i < cfscnt; i++) {
            llpts[npts++] = cfsvals[i * cfssz];// lon
            llpts[npts++] = cfsvals[i * cfssz + 1];// lat
         }

         // thin points
         FanCompress.FloatCompress fan = new FanCompress.FloatCompress(llpts);
         FanCompress.fan_compress(fan, fan_eps);

         // install new points
         cfs.vals = fan.getArray(); // install thinned 2-tuple
                                    // array
         cfs.tcount = cfs.vals.length >>> 1;// num pairs
         cfs.tsize = 2;// HACK lossy...
         totalSize += cfs.tcount;
         if (rev) {
            cfs.tcount *= -1;
         }
      }
      return totalSize;
   }

   /**
    * Create an OMText object corresponding to a VPF text feature
    * 
    * @param text the text
    * @param latitude the latitude of where to place the text
    * @param longitude the longitude of where to place the text
    */
   public static OMText createOMText(String text, double latitude, double longitude) {

      OMText txt = new OMText(latitude, longitude, text, OMText.JUSTIFY_LEFT);
      return txt;
   }

   /**
    * Create an OMPoint object corresponding to a VPF node feature
    * 
    * @param latitude the latitude of where to place the text
    * @param longitude the longitude of where to place the text
    */
   public static OMPoint createOMPoint(double latitude, double longitude) {

      return new OMPoint(latitude, longitude);
   }
}