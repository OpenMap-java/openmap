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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFFeatureCache.java,v $
// $RCSfile: VPFFeatureCache.java,v $
// $Revision: 1.1 $
// $Date: 2004/02/01 21:21:59 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.layer.util.cacheHandler.CacheHandler;
import com.bbn.openmap.layer.util.cacheHandler.CacheObject;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

public class VPFFeatureCache extends CacheHandler {

    public VPFFeatureCache() {
        super();
    }

    public VPFFeatureCache(int maxSize) {
        super(maxSize);
    }

    protected void addToCachedList(OMGraphic omg, String featureType, PrimitiveTable pt) {
        String key = createTableCacheKey(featureType, pt.getTileDirectory().getPath());
        OMGraphicList omgl = (OMGraphicList) get(key);
        omgl.add(omg);
    }

    public static String createTableCacheKey(String featureType, String tilePath) {
        return featureType + "-" + tilePath;
    }

    /**
     * Returns true if the features from a tile (as described by the
     * key) existed and was added to the warehouse graphics list.
     * Returns false if the list needs to be created and the contents
     * read in from data files.
     */
    public synchronized boolean loadCachedGraphicList(String featureType, 
                                                      String tilePath, 
                                                      OMGraphicList requestor) {

        String key = createTableCacheKey(featureType, tilePath);
        boolean exists = (searchCache(key) != null);

        // Will retrieve the old list if it exists, create a new one
        // if it doesn't.
        FeatureCacheGraphicList fcgl = (FeatureCacheGraphicList)get(key);
        // Setting the featureType in the OMGraphicList app object so
        // the feature's drawing attributes can be set for the caller
        // later in getGraphics().
        fcgl.setFeatureName(featureType);
        requestor.add(fcgl);

        // Might want to set the current attributes for the existing
        // contents of the list in case they were changed by the user.

        return exists;
    }

    public boolean needToFetchTileContents(String currentFeature, 
                                           TileDirectory currentTile, 
                                           OMGraphicList requestor) {
        if (loadCachedGraphicList(currentFeature, currentTile.getPath(), requestor)) {
            if (Debug.debugging("vpf.cache")) {
                Debug.output("VPFFeatureCache: Loaded Cached List: " + 
                             createTableCacheKey(currentFeature, currentTile.getPath()));
            }
            return false;
        }
        return true;
    }

    public CacheObject load(String key) {
        if (key != null) {
            return new VPFListCacheObject(key, new FeatureCacheGraphicList());
        }
        return null;
    }

    public static class VPFListCacheObject extends CacheObject {
        /**
         * Construct a VPFListCacheObject, just calls superclass constructor
         * @param id passed to superclass
         * @param obj passed to superclass
         */
        public VPFListCacheObject(String id, OMGraphicList obj) {
            super(id, obj);
        }

        /**
         */
        public void finalize() {
            ((OMGraphicList)obj).clear();
        }
    }

}
