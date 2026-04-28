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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/FeatureCacheGraphicList.java,v $
// $RCSfile: FeatureCacheGraphicList.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 19:29:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Paint;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * The FeatureCacheGraphicList is an extended OMGraphicList that knows what
 * types of VPF features it holds. This allows it to be able to use a
 * VPFFeatureGraphicWarehouse to set the proper DrawingAttributes on its
 * contents.
 */
public abstract class FeatureCacheGraphicList
      extends OMGraphicList
      implements Cloneable {

   private static final long serialVersionUID = 1L;
   /**
    * The identifying code for the features held in this list.
    */
   protected String featureName = null;

   public FeatureCacheGraphicList() {
   }

   public FeatureCacheGraphicList(int initSize) {
      super(initSize);
   }

   public void setFeatureName(String name) {
      featureName = name;
   }

   public String getFeatureName() {
      return featureName;
   }

   public synchronized void setTo(DrawingAttributes da) {
      if (da != null) {
         for (OMGraphic omg : this) {
            da.setTo(omg);
         }
      }
   }

   /**
    * A factory method for creating the proper FeatureCacheGraphicList for a
    * particular feature type, VPFUtil.Edge, VPFUtil.Area, VPFUtil.Text, and/or
    * VPFUtil.Point, with each list subclass tuned to help properly color
    * features when they are set based on layer requirements. If featureType is
    * null or isn't one of the types listed above, the DEFAULT version will be
    * returned.
    */
   public static FeatureCacheGraphicList createForType(String featureType) {

      if (featureType != null) {

         int lastCharIndex = featureType.length() - 1;
         if (lastCharIndex >= 0) {
            // char lastLetter = featureType.charAt(lastCharIndex);

            if (VPFUtil.Edge.equals(featureType)) {
               return new FeatureCacheGraphicList.EDGE();
            }

            if (VPFUtil.Area.equals(featureType)) {
               return new FeatureCacheGraphicList.AREA();
            }

            if (VPFUtil.Text.equals(featureType)) {
               return new FeatureCacheGraphicList.TEXT();
            }

            if (VPFUtil.EPoint.equals(featureType) || VPFUtil.CPoint.equals(featureType)) {
               return new FeatureCacheGraphicList.POINT();
            }
         }
      }

      return new FeatureCacheGraphicList.DEFAULT();
   }

   /**
    * @return a duplicate list full of shallow copies of each of the OMGraphics
    *         contained on the list.
    */
   public synchronized Object clone() {
      try {
         FeatureCacheGraphicList omgl = getClass().newInstance();
         omgl.setFeatureName(getFeatureName());

         for (OMGraphic omg : this) {
            if (omg instanceof OMGraphicList) {
               omgl.add((OMGraphic) ((OMGraphicList) omg).clone());
            } else {
               omgl.graphics.add(omg);
            }
         }

         return omgl;
      } catch (InstantiationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return new DEFAULT();
   }

   /**
    * Different implementations depending on type.
    */
   public abstract void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw);

   public static class AREA
         extends FeatureCacheGraphicList {

      private static final long serialVersionUID = 1L;

      public AREA() {
         super();
      }

      public AREA(int size) {
         super(size);
      }

      public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
         DrawingAttributes da = vfgw.getAttributesForFeature(featureName);
         Paint fillPaint = da.getFillPaint();
         da.setLinePaint(fillPaint);
         da.setSelectPaint(fillPaint);
         setTo(da);
      }
   }

   public static class EDGE
         extends FeatureCacheGraphicList {

      private static final long serialVersionUID = 1L;

      public EDGE() {
         super();
      }

      public EDGE(int size) {
         super(size);
      }

      public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
         DrawingAttributes da = vfgw.getAttributesForFeature(featureName);
         da.setFillPaint(OMColor.clear);
         setTo(da);
      }
   }

   public static class DEFAULT
         extends FeatureCacheGraphicList {

      private static final long serialVersionUID = 1L;

      public DEFAULT() {
         super();
      }

      public DEFAULT(int size) {
         super(size);
      }

      public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
         setTo(vfgw.getAttributesForFeature(featureName));
      }
   }

   public static class TEXT
         extends DEFAULT {

      private static final long serialVersionUID = 1L;

      public TEXT() {
         super();
      }

      public TEXT(int size) {
         super(size);
      }
   }

   public static class POINT
         extends DEFAULT {

      private static final long serialVersionUID = 1L;

      public POINT() {
         super();
      }

      public POINT(int size) {
         super(size);
      }
   }
}