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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFCachedFeatureGraphicWarehouse.java,v $
// $RCSfile: VPFCachedFeatureGraphicWarehouse.java,v $
// $Revision: 1.1 $
// $Date: 2004/02/01 21:21:59 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.util.cacheHandler.CacheHandler;
import com.bbn.openmap.layer.util.cacheHandler.CacheObject;
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
    public void createArea(CoverageTable covtable, AreaTable areatable,
                           List facevec,
                           LatLonPoint ll1,
                           LatLonPoint ll2,
                           float dpplat,
                           float dpplon,
                           String featureType) {

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
        // HACK to get tile boundaries to not show up for areas.
        py.setLinePaint(py.getFillPaint());
        py.setSelectPaint(py.getFillPaint());
        addToCachedList(py, featureType, areatable);
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
                           String featureType) {

        OMPoly py = createEdgeOMPoly(coords, ll1, ll2, dpplat, dpplon);
        getAttributesForFeature(featureType).setTo(py);
        py.setIsPolygon(false);

        addToCachedList(py, featureType, edgetable);
    }

    /**
     *
     */
    public void createText(CoverageTable c, TextTable texttable,
                           List textvec,
                           float latitude,
                           float longitude,
                           String text,
                           String featureType) {

        OMText txt = createOMText(text, latitude, longitude);
        getAttributesForFeature(featureType).setTo(txt);
        addToCachedList(txt, featureType, texttable);
    }

    /**
     * Method called by the VPF reader code to construct a node feature.
     */
    public void createNode(CoverageTable c, NodeTable t, List nodeprim,
                           float latitude, float longitude,
                           boolean isEntityNode, String featureType) {
        OMPoint pt = createOMPoint(latitude, longitude);
        getAttributesForFeature(featureType).setTo(pt);
        addToCachedList(pt, featureType, t);
    }

    protected void addToCachedList(OMGraphic omg, String featureType, PrimitiveTable pt) {
        if (featureCache != null) {
            String key = featureCache.createTableCacheKey(featureType, pt.getTileDirectory().getPath());
            OMGraphicList omgl = (OMGraphicList)featureCache.get(key);
            omgl.add(omg);
        } else {
            // Main OMGraphicList stored in super class
            graphics.add(omg);
        }
    }

    public boolean needToFetchTileContents(String currentFeature, 
                                           TileDirectory currentTile) {
        if (featureCache != null) {
            // The cached graphics list will be added to the graphics list provided.
            return featureCache.needToFetchTileContents(currentFeature, 
                                                        currentTile, 
                                                        graphics);
        } else {
            return super.needToFetchTileContents(currentFeature, currentTile);
        }
    }

    public OMGraphicList getGraphics() {
        // Clone from the cache...
        if (featureCache != null) {
            // The main graphics object is made up of
            // FeatureCacheGraphicLists for features for applicable
            // tiles.
            OMGraphicList ret = new OMGraphicList();
            for (Iterator it = graphics.iterator(); it.hasNext();) {
                OMGraphic omg = (OMGraphic)it.next();
                if (omg instanceof FeatureCacheGraphicList) {
                    FeatureCacheGraphicList fcgl = 
                        (FeatureCacheGraphicList)((FeatureCacheGraphicList)omg).clone();
                    fcgl.setDrawingAttributes(this);
                    ret.add(fcgl);
                } else {
                    ret.add(omg);
                }
            }

            return ret;
        } else {
            return graphics;
        }
    }
}
