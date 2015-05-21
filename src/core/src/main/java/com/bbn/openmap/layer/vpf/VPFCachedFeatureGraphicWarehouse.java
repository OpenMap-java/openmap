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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFCachedFeatureGraphicWarehouse.java,v $
// $RCSfile: VPFCachedFeatureGraphicWarehouse.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The VPFFeatureGraphicWarehouse extension that knows how to use a
 * VPFFeatureCache. The cached lists are cloned and the drawing attributes for
 * the clones are set based on the warehouse settings.
 */
public class VPFCachedFeatureGraphicWarehouse
      extends VPFFeatureGraphicWarehouse {

   protected VPFFeatureCache featureCache = null;

   /**
     */
   public VPFCachedFeatureGraphicWarehouse() {
      super();
   }

   /**
     */
   public VPFCachedFeatureGraphicWarehouse(VPFFeatureCache vfc) {
      this();
      setFeatureCache(vfc);
   }

   public void setFeatureCache(VPFFeatureCache vfc) {
      featureCache = vfc;
   }

   public VPFFeatureCache getFeatureCache() {
      return featureCache;
   }

   /**
     *  
     */
   public OMGraphic createArea(CoverageTable covtable, AreaTable areatable, List<Object> facevec, LatLonPoint ll1, LatLonPoint ll2,
                               double dpplat, double dpplon, String featureType) {

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

      DrawingAttributes da = getAttributesForFeature(featureType);
      // Must make sure that line paint equals fill paint, the
      // boundary for areas isn't always the sum of the areas.
      // da.setLinePaint(da.getFillPaint());
      // da.setSelectPaint(da.getFillPaint());
      da.setTo(py);
      py.setLinePaint(da.getFillPaint());
      py.setSelectPaint(da.getFillPaint());
      addToCachedList(py, featureType, areatable, VPFUtil.Area);
      return py;
   }

   /**
     *  
     */
   public OMGraphic createEdge(CoverageTable c, EdgeTable edgetable, List<Object> edgevec, LatLonPoint ll1, LatLonPoint ll2, double dpplat,
                               double dpplon, CoordFloatString coords, String featureType) {

      OMPoly py = createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
      DrawingAttributes da = getAttributesForFeature(featureType);
      // da.setFillPaint(OMColor.clear); // Just to make sure that
      // it is always set in the DA.
      da.setTo(py);
      py.setFillPaint(OMColor.clear);
      py.setIsPolygon(false);

      addToCachedList(py, featureType, edgetable, VPFUtil.Edge);
      return py;
   }

   /**
     *  
     */
   public OMGraphic createText(CoverageTable c, TextTable texttable, List<Object> textvec, double latitude, double longitude, String text,
                               String featureType) {

      OMText txt = createOMText(text, latitude, longitude);
      getAttributesForFeature(featureType).setTo(txt);
      addToCachedList(txt, featureType, texttable, VPFUtil.Text);
      return txt;
   }

   /**
    * Method called by the VPF reader code to construct a node feature.
    */
   public OMGraphic createNode(CoverageTable c, NodeTable t, List<Object> nodeprim, double latitude, double longitude,
                               boolean isEntityNode, String featureType) {
      OMPoint pt = createOMPoint(latitude, longitude);
      getAttributesForFeature(featureType).setTo(pt);
      addToCachedList(pt, featureType, t, isEntityNode ? VPFUtil.EPoint : VPFUtil.CPoint);
      return pt;
   }

   protected FeatureCacheGraphicList cacheList;

   /**
    * Calls addToCachedList on the feature cache if it's available.
    */
   protected synchronized void addToCachedList(OMGraphic omg, String featureType, PrimitiveTable pt, String type) {
      if (featureCache != null && cacheList != null) {

         // this might be causing problems in the cache, if the list has
         // already be replaced by another library or layer. We're going to
         // keep track of the list returned from the cache from before and
         // add it to the list here if it exists.
         cacheList.add(omg);

         // We don't do this anymore...
         // featureCache.addToCachedList(omg, featureType, pt, type);
      } else {
         // Main OMGraphicList stored in super class

         if (VPFUtil.Area.equals(type))
            addArea(omg);
         else if (VPFUtil.Edge.equals(type))
            addEdge(omg);
         else if (VPFUtil.Text.equals(type))
            addText(omg);
         else
            addPoint(omg);

         // Sorting by type, now.
         // graphics.add(omg);
      }
   }

   /**
    * Calls VPFFeatureCache.needToFetchTileContents().
    */
   public boolean needToFetchTileContents(String libraryName, String currentFeature, TileDirectory currentTile) {
      if (featureCache != null) {
         // The cached graphics list will be added to the graphics
         // list provided. If it was just created, it will also be passed
         // back from the needToFetchTileContents and should be loaded by the
         // CoverageTable. We're going to hold on to it inside the warehouse
         // and just add OMGraphics to it while it is set.

         cacheList = featureCache.needToFetchTileContents(libraryName, currentFeature, currentTile, graphics);

         return cacheList != null;

      } else {
         return super.needToFetchTileContents(libraryName, currentFeature, currentTile);
      }
   }

   /**
    * Overridden method of VPFFeatureGraphicWarehouse, clones cached
    * OMGraphicLst and sets the proper DrawingAttributes settings for the
    * particular features.
    */
   public synchronized OMGraphicList getGraphics() {
      // Clone from the cache...
      if (featureCache != null) {
         // The main graphics object is made up of
         // FeatureCacheGraphicLists for features for applicable
         // tiles. All of the other warehouses are filling up the
         // area, edge, text and point lists, but the cache is
         // filling up the main list with these feature cache
         // graphic lists. We need to sort them, reorganize and
         // then return the newly sorted list so the areas are on
         // the bottom. We're going to assume that the area, edge,
         // text, point sublists are null and empty, since the
         // cached stuff has been added directly to graphics.
         OMGraphicList ret = new OMGraphicList();
         ret.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

         logger.fine("checking for cached lists");
         for (OMGraphic omg :graphics) {
            if (omg instanceof FeatureCacheGraphicList) {
               FeatureCacheGraphicList fcgl = (FeatureCacheGraphicList) ((FeatureCacheGraphicList) omg).clone();
               fcgl.setDrawingAttributes(this);

               if (fcgl instanceof FeatureCacheGraphicList.AREA) {
                  addArea(fcgl);
               } else if (fcgl instanceof FeatureCacheGraphicList.EDGE) {
                  addEdge(fcgl);
               } else if (fcgl instanceof FeatureCacheGraphicList.TEXT) {
                  addText(fcgl);
               } else {
                  addPoint(fcgl);
               }

            } else {
               // Add on top
               addPoint(omg);
            }
         }

         getGraphics(ret);

         return ret;
      } else {
         return super.getGraphics();
      }
   }
}