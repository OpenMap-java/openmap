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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/FeatureCacheGraphicList.java,v $
// $RCSfile: FeatureCacheGraphicList.java,v $
// $Revision: 1.3 $
// $Date: 2004/03/31 21:17:58 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.DrawingAttributes;

import java.util.Iterator;

/**
 * The FeatureCacheGraphicList is an extended OMGraphicList that knows
 * what types of VPF features it holds.  This allows it to be able to
 * use a VPFFeatureGraphicWarehouse to set the proper
 * DrawingAttributes on its contents.
 */
public abstract class FeatureCacheGraphicList extends OMGraphicList 
    implements Cloneable {

    /**
     * The identifying code for the features held in this list.
     */
    protected String featureName = null;

    public FeatureCacheGraphicList() {}

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
            for (Iterator it = iterator(); it.hasNext();) {
                da.setTo((OMGraphic)it.next());
            }
        }
    }

    /**
     * A factory method for creating the proper
     * FeatureCacheGraphicList for a particular feature type,
     * VPFUtil.Edge, VPFUtil.Area, VPFUtil.Text, and/or VPFUtil.Point,
     * with each list subclass tuned to help properly color features
     * when they are set based on layer requirements.  If featureType
     * is null or isn't one of the types listed above, the DEFAULT
     * version will be returned.
     */
    public static FeatureCacheGraphicList createForType(String featureType) {

        int lastCharIndex = featureType.length() - 1;
        if (lastCharIndex >= 0) {
            char lastLetter = featureType.charAt(lastCharIndex);

            if (featureType == VPFUtil.Edge) {
                return new FeatureCacheGraphicList.EDGE();
            }

            if (featureType == VPFUtil.Area) {
                return new FeatureCacheGraphicList.AREA();
            }

            if (featureType == VPFUtil.Text) {
                return new FeatureCacheGraphicList.TEXT();
            }

            if (featureType == VPFUtil.EPoint || featureType == VPFUtil.CPoint) {
                return new FeatureCacheGraphicList.POINT();
            }
        }

        return new FeatureCacheGraphicList.DEFAULT();
    }

    /**
     * Different implementations depending on type.
     */
    public abstract void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw);

    public static class AREA extends FeatureCacheGraphicList {
        public AREA() {
            super();
        }

        public AREA(int size) {
            super(size);
        }

        public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
            DrawingAttributes da = vfgw.getAttributesForFeature(featureName);
            da.setLinePaint(com.bbn.openmap.omGraphics.OMColor.clear);
            da.setSelectPaint(com.bbn.openmap.omGraphics.OMColor.clear);
            setTo(da);
        }
    }

    public static class EDGE extends FeatureCacheGraphicList {
        public EDGE() {
            super();
        }

        public EDGE(int size) {
            super(size);
        }

        public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
            DrawingAttributes da = vfgw.getAttributesForFeature(featureName);
            da.setFillPaint(com.bbn.openmap.omGraphics.OMColor.clear);
            setTo(da);
        }
    }

    public static class DEFAULT extends FeatureCacheGraphicList {
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

    public static class TEXT extends DEFAULT {
        public TEXT() {
            super();
        }

        public TEXT(int size) {
            super(size);
        }
    }

    public static class POINT extends DEFAULT {
        public POINT() {
            super();
        }

        public POINT(int size) {
            super(size);
        }
    }
}
