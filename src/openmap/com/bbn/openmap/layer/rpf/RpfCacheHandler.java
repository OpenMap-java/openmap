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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCacheHandler.java,v $
// $RCSfile: RpfCacheHandler.java,v $
// $Revision: 1.12 $
// $Date: 2006/10/04 14:46:13 $
// $Author: dietrick $
//
// **********************************************************************

/**
 *  Modifications :
 *
 *  1. Changed getSubframeFromOtherTOC(): changed offsets and prevent caching
 *     from other TOCs
 */

/*
 * Some of the ideas for this code is based on source code provided by
 * The MITRE Corporation, through the browse application source code.
 * Many thanks to Nancy Markuson who provided BBN with the software,
 * and Theron Tock, who wrote the software, and to Daniel Scholten,
 * who revised it - (c) 1994 The MITRE Corporation for those parts,
 * and used/distributed with permission.  Namely, the subframe caching
 * mechanism is the part that has been modified.
 */
package com.bbn.openmap.layer.rpf;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The RpfCacheHandler handles everything to do with the decompressed subframes,
 * which is what gets put up on the screen. It interfaces with the
 * RpfFrameProvider to get the information about the data. It also is usually
 * handled by the RpfCacheManager.
 */
public class RpfCacheHandler {
    /* Lowered from 128 - used too much memory */
    public final static int SUBFRAME_CACHE_SIZE = 20;
    /* # CHUM descriptor string */
    public final static int MAX_NUM_DESC = 20;
    /* # CHUM descriptor string */
    public final static int MAX_DESC_LEN = 512;
    public static final int DEFAULT_SUBFRAMEBUFFER = 5;

    /* DKS fix chum description problem : */
    /** subframe status constant. */
    public final static int NOT_CACHED = -1;
    /** subframe status constant. */
    public final static int NOT_PRESENT = -2;

    /** Subframe scaling for map scales that don't match chart scale. */
    protected int scalingHeight = RpfSubframe.PIXEL_EDGE_SIZE;
    /** Subframe scaling for map scales that don't match chart scale. */
    protected int scalingWidth = RpfSubframe.PIXEL_EDGE_SIZE;
    /** The subframe cache. */
    protected SubframeCache cache;
    /**
     * The current description of the TOC entry that currently applies.
     */
    protected Vector<RpfCoverageBox> coverageBoxes;
    /**
     * The array of indexes for subframes contained in the RpfTocEntry.
     */
    protected byte[][] subframeIndex;
    /**
     * The array of version markers for subframes contained in the RpfTocEntry.
     */
    protected byte[][] subframeVersion;

    /** The size of the subframe cache. */
    protected int subframeCacheSize = SUBFRAME_CACHE_SIZE;
    /**
     * Description of how the frames should be constructed and displayed.
     */
    protected RpfViewAttributes viewAttributes;
    /** The place to look for for image data. */
    protected RpfFrameProvider frameProvider;
    /** The upper left subframe index on screen. */
    protected Point start = new Point();
    /** The lower right subframe index on screen. */
    protected Point end = new Point();
    /**
     * A flag to let the cache manager know that the subframes needed for the
     * map make sense.
     */
    protected boolean goodData = false;
    /**
     * The subframe cache is mapped by a 2D matrix based on the number of
     * subframes that will fit in the RpfCoverageBox area. The subframeBuffer is
     * used to put additional subframe entries in the matrix, so that subframes
     * retrieved outside of the RpfCoverageBox area can still be cached. The
     * subframeBuffer refers to the number of subframes added on each side of
     * the matrix.
     */
    protected int subframeBuffer = DEFAULT_SUBFRAMEBUFFER;

    /**
     * Used in setCache to see if new coverage is needed with a projection
     * change.
     */
    // private float lastScaleDifference = -1f;

    protected boolean DEBUG_RPF = false;
    protected boolean DEBUG_RPFDETAIL = false;

    /** The entire subframe cache */
    static public class SubframeCache {
        RpfSubframe[] subframe;
        int LRU_head, LRU_tail;

        public SubframeCache(int numSubframes) {
            subframe = new RpfSubframe[numSubframes];
        }
    }

    /**
     * Constructor for a main cache, with the full size cache.
     */
    public RpfCacheHandler(RpfFrameProvider provider, RpfViewAttributes rva) {
        this(provider, rva, SUBFRAME_CACHE_SIZE);
    }

    /**
     * Constructor for an auxiliary cache, with a settable cache size.
     */
    public RpfCacheHandler(RpfFrameProvider provider, RpfViewAttributes rva, int subframe_cache_size) {
        DEBUG_RPF = Debug.debugging("rpf");
        DEBUG_RPFDETAIL = Debug.debugging("rpfdetail");

        frameProvider = provider;
        viewAttributes = rva;
        updateViewAttributes();

        if (subframe_cache_size > Byte.MAX_VALUE) {
            subframeCacheSize = Byte.MAX_VALUE;
        } else if (subframe_cache_size >= 0) {
            subframeCacheSize = subframe_cache_size;
        }

        initCache(true); // subframe cache, and it's new

        if (DEBUG_RPF) {
            Debug.output("RpfCacheHandler: Created with cache size of " + subframeCacheSize);
        }

    }

    // public void finalize() {
    // Debug.message("gc", "RpfCacheHandler: getting GC'd");
    // }

    /**
     * Set the view attributes for the layer. The frame provider view attributes
     * are updated, and the cache is cleared.
     * 
     * @param rva the RpfViewAttributes used for the layer.
     */
    public void setViewAttributes(RpfViewAttributes rva) {
        viewAttributes = rva;
        updateViewAttributes();
        clearCache();
    }

    /**
     * Get the view attributes or the layer.
     * 
     * @return RpfViewAttributes.
     */
    public RpfViewAttributes getViewAttributes() {
        if (viewAttributes == null) {
            viewAttributes = new RpfViewAttributes();
        }
        return viewAttributes;
    }

    /**
     * Set the RpfFrameProvider for the layer. Clears out the cache, and the
     * frame provider gets the RpfViewAttributes held by the layer.
     * 
     * @param fp the frame provider.
     */
    public void setFrameProvider(RpfFrameProvider fp) {
        frameProvider = fp;
        if (frameProvider != null) {
            frameProvider.setViewAttributes(getViewAttributes());
        }
        clearCache();
    }

    /**
     * Return RpfFrameProvider used by the layer.
     */
    public RpfFrameProvider getFrameProvider() {
        return frameProvider;
    }

    /**
     * This only needs to be called if the frame provider is not local. In that
     * case, updates to the view attributes object will not be reflected on the
     * server side. This will update the parameters.
     */
    public void updateViewAttributes() {
        if (frameProvider != null) {
            frameProvider.setViewAttributes(getViewAttributes());
        }
    }

    /**
     * Returns the Vector containing RpfCoverageBoxes that was returned from the
     * RpfFrameProvider as a result of the last setCache call. These provide
     * rudimentary knowledge about what is being displayed.
     * 
     * @return Vector of RpfCoverageBoxes.
     */
    public Vector<RpfCoverageBox> getCoverageBoxes() {
        return coverageBoxes;
    }

    /**
     * Called to prepare the cache for subframes that will fit into the next
     * request. The subframe entry from the TOC is known and tracked, and if it
     * changes, the frame cache gets tossed and recreated via
     * setScreenSubframes.
     * 
     * @param ullat NW latitude.
     * @param ullon NW longitude.
     * @param lrlat SE latitude.
     * @param lrlon SE longitude
     * @param proj projection to use for zone decisions.
     */
    public synchronized void setCache(float ullat, float ullon, float lrlat, float lrlon,
                                      Projection proj) {

        if (DEBUG_RPF) {
            Debug.output("RpfCacheHandler: Need new Coverage.");
        }

        if (frameProvider != null) {
            coverageBoxes = frameProvider.getCoverage(ullat, ullon, lrlat, lrlon, proj);
        } else {
            coverageBoxes = null;
        }

        // See if anything came back...
        if (coverageBoxes == null || coverageBoxes.isEmpty()) {
            // Guess not.
            goodData = false;
            return;
        }

        // The percent coverage should be greater than zero here.
        // That should be checked by the RpfTocHandler.

        // Base the cache off the coverage in the first box. It's
        // supposed to have the best coverage.
        RpfCoverageBox currentBox = coverageBoxes.elementAt(0);

        resetSubframeIndex(currentBox.verticalSubframes(), currentBox.horizontalSubframes());
        initCache(false);

        start = currentBox.startIndexes;
        end = currentBox.endIndexes;
        goodData = true;

        // Set the backup indexes, just in case.
        for (int i = 1; i < coverageBoxes.size(); i++) {
            ((RpfCoverageBox) coverageBoxes.elementAt(i)).setPercentCoverage(ullat, ullon, lrlat, lrlon);
        }

        if (DEBUG_RPF) {
            Debug.output("RpfCachehandler: ####################");
            Debug.output("" + currentBox);
            Debug.output(" Starting point " + start);
            Debug.output(" Ending point " + end);
        }

        // Figure out how much to scale the cached images. This would
        // be one of the big problems if we were going to merge
        // different data types.
        if (getViewAttributes().scaleImages) {
            // Do the work for a great scaling factor here...

            // Need to figure how much this will change for this scale
            // chart at this screen scale
            // Reference at 0, 0
            Point2D refllpt = (Point2D) proj.getUpperLeft();
            refllpt.setLocation(refllpt.getX() + (float) currentBox.subframeLonInterval, refllpt.getY()
                    - (float) currentBox.subframeLatInterval);

            Point refpt = (Point) proj.forward(refllpt, new Point());

            scalingWidth = refpt.x;
            scalingHeight = refpt.y;
        } else {
            scalingWidth = RpfSubframe.PIXEL_EDGE_SIZE;
            scalingHeight = RpfSubframe.PIXEL_EDGE_SIZE;
        }

    }

    protected OMGraphicList getSubframes(float ullat, float ullon, float lrlat, float lrlon,
                                         Projection proj, OMGraphicList omGraphics) {
        setCache(ullat, ullon, lrlat, lrlon, proj);

        int subframeRunningCount = 0;

        if (omGraphics == null) {
            omGraphics = new OMGraphicList();
        }

        for (int subx = start.x; subx <= end.x; subx++) {
            for (int suby = start.y; suby <= end.y; suby++) {

                // /////
                RpfSubframe subframe = getCached(subx, suby, subframeRunningCount);
                // /////

                if (subframe == null) {
                    if (Debug.debugging("rpf")) {
                        Debug.output("RpfCacheManager: checking other TOCs for subframe.");
                    }
                    subframe = getSubframeFromOtherTOC(subx, suby, subframeRunningCount);
                }

                if (subframe != null) {
                    OMGraphic image = subframe.getImage(proj);
                    if (image != null) {
                        image.setSelected(viewAttributes.showInfo);
                        omGraphics.add(image);

                        if (Debug.debugging("rpf")) {
                            Debug.output("RpfCacheManager: Adding subframe " + subx + ", " + suby);
                        }
                    }
                } else {
                    if (Debug.debugging("rpf")) {
                        Debug.output("RpfCacheManager: subframe " + subx + ", " + suby + " empty");
                    }
                }

                subframeRunningCount++;
            }
        }

        return omGraphics;
    }

    /**
     * Resets the indicators in the subframe cache, so that none of the current
     * contents will be used - they'll have to be loaded with data first. The
     * cache management is also set for the current main RpfCoverageBox.
     */
    protected void resetSubframeIndex(int vertFrames, int horizFrames) {

        if (subframeCacheSize > 0) {
            /* Allocate the indices into the subframe cache */
            int matrixheight = (vertFrames * 6) + (subframeBuffer * 2);
            int matrixwidth = (horizFrames * 6) + (subframeBuffer * 2);

            subframeIndex = new byte[matrixheight][matrixwidth];
            subframeVersion = new byte[matrixheight][matrixwidth];
            clearCache();
        } else {
            subframeIndex = null;
            subframeVersion = null;
        }
    }

    /**
     * Clear the subframes in the cache, marking them as NOT_CACHED.
     */
    public void clearCache() {
        if (subframeIndex != null && subframeVersion != null) {
            /* Initialize the subframe indices */
            for (int i = 0; i < subframeIndex.length; i++) {
                for (int j = 0; j < subframeIndex[0].length; j++) {
                    subframeIndex[i][j] = NOT_CACHED;
                    subframeVersion[i][j] = -1;
                }
            }
        }

        // TODO see if this makes a difference
        initCache(false);
    }

    /**
     * Return true if the cache handler knows about good data in the current
     * situation.
     */
    public boolean getGoodData() {
        return goodData;
    }

    // /////////////////////////////////////////////////////////
    // SUBFRAME CACHE HANDLING
    // /////////////////////////////////////////////////////////

    protected void initCache(boolean newCache) {
        int i;

        // Don't have a cache.
        if (subframeCacheSize <= 0) {
            cache = null;
            return;
        }

        if (newCache || cache == null) {
            cache = new SubframeCache(subframeCacheSize);
        }

        cache.LRU_head = 0;
        cache.LRU_tail = subframeCacheSize - 1;

        for (i = 0; i < subframeCacheSize; i++) {
            if (newCache) {
                try {
                    cache.subframe[i] = new RpfSubframe();
                } catch (java.lang.OutOfMemoryError oome) {
                    Debug.error("RpfCacheHandler: \n\tRan out of memory allocating the image cache.\tConsider increasing the java memory heap using the -Xmx option.");

                    cache = null;

                    subframeCacheSize = i;
                    if (DEBUG_RPF) {
                        Debug.output("RpfCacheHandler: resetting cache size to "
                                + subframeCacheSize);
                    }
                    initCache(true);
                    return;
                }
            }

            RpfSubframe subframe = cache.subframe[i];
            subframe.version = 0;

            // Here's where I messed up - forgot to hook up the ends
            // of the chain...
            if (i < subframeCacheSize - 1) {
                subframe.nextSubframe = i + 1;
            } else {
                subframe.nextSubframe = 0;
            }
            if (i > 0) {
                subframe.prevSubframe = i - 1;
            } else {
                subframe.prevSubframe = subframeCacheSize - 1;
            }
        }
    }

    /**
     * Get the index of the least recently used entry from the subframe cache.
     */
    protected int getLRU() {
        if (cache != null) {
            return cache.LRU_tail;
        } else {
            return NOT_CACHED;
        }
    }

    protected void freeCache(int index) {

        if (cache == null) {
            return;
        }

        if (index == cache.LRU_tail) {
            return;
        } else if (index == cache.LRU_head) {
            cache.LRU_head = cache.subframe[cache.LRU_head].nextSubframe;
        } else {
            int next = cache.subframe[index].nextSubframe;
            int prev = cache.subframe[index].prevSubframe;

            cache.subframe[next].prevSubframe = prev;
            cache.subframe[prev].nextSubframe = next;
        }

        cache.subframe[cache.LRU_tail].nextSubframe = index;
        cache.subframe[index].prevSubframe = cache.LRU_tail;
        cache.LRU_tail = index;
    }

    /**
     * Mark a cache entry as being recently used.
     */
    protected void referenceCache(int index) {
        if (cache == null) {
            return;
        }

        /* First unlink the cache entry from the list */
        if (index == cache.LRU_head) {
            return;
        } else if (index == cache.LRU_tail) {
            cache.LRU_tail = cache.subframe[cache.LRU_tail].prevSubframe;
        } else {
            int next = cache.subframe[index].nextSubframe;
            int prev = cache.subframe[index].prevSubframe;

            cache.subframe[next].prevSubframe = prev;
            cache.subframe[prev].nextSubframe = next;
        }

        /* Now add the entry as the most recently referenced */
        cache.subframe[cache.LRU_head].prevSubframe = index;
        cache.subframe[index].nextSubframe = cache.LRU_head;
        cache.LRU_head = index;
    }

    /**
     * Find out the size of the subframe cache. From the start and end indexes,
     * you can figure out the number of subframes the map needs. If that number
     * is bigger than this cache size, you'll need to use the getCached that
     * lets you supply the subframe number that you are requesting, so that the
     * RpfCacheHandler knows when to stop caching subframes during a retrival..
     * Otherwise, the cache will overwrite data and subframes will not show up
     * on the map.
     */
    public int getCacheSize() {
        return subframeCacheSize;
    }

    /**
     * Get a subframe from one of the other RpfCoverageBoxes. Keep going through
     * them until there is a subframe returned, or if there's nothing. Use this
     * method when you are sure that the subframe cache is big enough to handle
     * all the subframes on the map.
     * 
     * @param x the x index of subframe in the FIRST RpfCoverageBox space -
     *        translation needed.
     * @param y the y index of subframe in the FIRST RpfCoverageBox space -
     *        translation needed.
     */
    protected RpfSubframe getSubframeFromOtherTOC(int x, int y) {
        return getSubframeFromOtherTOC(x, y, -1);
    }

    /**
     * Get a subframe from one of the other RpfCoverageBoxes. Keep going through
     * them until there is a subframe returned, or if there nothing. If you are
     * not sure that the number of subframes that go on the map is less than or
     * equal to the size of the subframe cache, then use this method to provide
     * a running count of how many subframes you've already called for to use in
     * the current map. If this number gets bigger than the cache size, then the
     * RpfCacheHandler will keep fetching data without storing the extra
     * subframes in the cache. Otherwise, the previous images in the cache would
     * be replaced before they were painted, and they would not appear on the
     * map. If subframeCount is less than subframe size, then the latest
     * retrieved subframe will be stored in the cache.
     * 
     * @param x the x index of subframe in the FIRST RpfCoverageBox space -
     *        translation needed.
     * @param y the y index of subframe in the FIRST RpfCoverageBox space -
     *        translation needed.
     * @param subframeCount a running count of the number of subframes retrieved
     *        so far for the current map. Should be used it there is concern
     *        that the number of subframes needed for the map is greater than
     *        the size of the subframe.
     */
    protected RpfSubframe getSubframeFromOtherTOC(int x, int y, int subframeCount) {
        int size = coverageBoxes.size();
        RpfCoverageBox currentBox = null;

        // Decision to never cache if it's coming from another TOC.
        // Problems arose in areas that had 3 coverage boxes
        // converging.
        // They kept writing over each others' cache.
        boolean cacheIt = false;

        RpfSubframe ret = null;
        int index = 0;

        // There isn't anything else to check.
        if (size < 2) {
            return null;
        } else {

            /* If beyond the cache boundary, don't cache it. */
            if (subframeIndex == null || y < 0 || x < 0 || y >= subframeIndex.length
                    || x >= subframeIndex[0].length || subframeCount >= subframeCacheSize) {
                cacheIt = false;
            }

            for (int i = 1; i < size; i++) {

                try {
                    currentBox = (RpfCoverageBox) coverageBoxes.elementAt(i);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    return null;
                }
                // Changed offsets because they were
                // incorrect, and this was preventing other RCBs from
                // finding the box
                int offsetX = x - start.x;
                int offsetY = y - start.y;
                // previous values were:
                // int offsetX = start.x - x;
                // int offsetY = start.y - y;

                int newX = currentBox.startIndexes.x + offsetX;
                int newY = currentBox.startIndexes.y + offsetY;

                if (cacheIt) {
                    /*
                     * Subframe isn't cached; allocate new entry and decompress
                     * it
                     */
                    index = getLRU();
                    if (index < 0 || index >= subframeCacheSize
                            || subframeCount >= subframeCacheSize) {
                        ret = null;
                    } else {
                        referenceCache(index);
                        RpfSubframe subframe = cache.subframe[index];
                        // DFD - not sure we need to do this. It seems like it's
                        // more important to make sure the version changes to
                        // some other value that matches what's in the subframe,
                        // rather than keep it stuck on MAX_VALUE.
                        // if (subframe.version < Byte.MAX_VALUE) {
                        // subframe.version++;
                        // }
                        subframeIndex[y][x] = (byte) index;
                        subframeVersion[y][x] = subframe.version;
                        ret = cache.subframe[index];
                    }
                }

                if (ret == null) {
                    try {
                        ret = new RpfSubframe();
                    } catch (java.lang.OutOfMemoryError oome) {
                        Debug.error("RpfCacheHandler: Out of memory!  No subframe for you!  Next up!");
                        return null;
                    }
                }
                if (loadSubframe(ret, currentBox, newX, newY)) {
                    return ret;
                } else if (cacheIt) {
                    freeCache(index);
                    subframeIndex[y][x] = NOT_PRESENT;
                }

            }
        }
        return null;
    }

    /**
     * Get a subframe from the cache if possible, otherwise allocate a new cache
     * entry and decompress it. Each cache entry has a version number that is
     * incremented whenever it is replaced by a new subframe. This ensures the
     * replacement is detected. Use this method when you are sure that the
     * subframe cache is big enough to handle all the subframes on the map.
     * 
     * @param cbx the x index of subframe in the rcbIndex A.TOC space.
     * @param cby the y index of subframe in the rcbIndex A.TOC space.
     */
    protected RpfSubframe getCached(int cbx, int cby) {
        return getCached(cbx, cby, -1);
    }

    /**
     * Get a subframe from the cache if possible, otherwise allocate a new cache
     * entry and decompress it. Each cache entry has a version number that is
     * incremented whenever it is replaced by a new subframe. This ensures the
     * replacement is detected. If you are not sure that the number of subframes
     * that go on the map is less than or equal to the size of the subframe
     * cache, then use this method to provide a running count of how many
     * subframes you've already called for to use in the current map. If this
     * number gets bigger than the cache size, then the RpfCacheHandler will
     * keep fetching data without storing the extra subframes in the cache.
     * Otherwise, the previous images in the cache would be replaced before they
     * were painted, and they would not appear on the map. If subframeCount is
     * less than subframe size, then the latest retrieved subframe will be
     * stored in the cache.
     * 
     * @param cbx the x index of subframe in the rcbIndex A.TOC space.
     * @param cby the y index of subframe in the rcbIndex A.TOC space.
     * @param subframeCount a running count of the number of subframes retrieved
     *        so far for the current map. Should be used if there is concern
     *        that the number of subframes needed for the map is greater than
     *        the size of the cache.
     */
    protected RpfSubframe getCached(int cbx, int cby, int subframeCount) {

        RpfSubframe ret;
        RpfCoverageBox currentBox = null;
        RpfViewAttributes viewAttributes = getViewAttributes();
        // x, y are the subframe indexes in the cache matrix
        int x = cbx + subframeBuffer;
        int y = cby + subframeBuffer;

        /* If beyond the image boundary, forget it */
        if (subframeIndex != null
                && (coverageBoxes == null || coverageBoxes.isEmpty() || y < 0 || x < 0
                        || y >= subframeIndex.length || x >= subframeIndex[0].length)) {
            return null;
        }

        try {
            currentBox = (RpfCoverageBox) coverageBoxes.elementAt(0);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }

        int index = NOT_CACHED;

        if (subframeIndex != null) {
            index = subframeIndex[y][x];
        }

        if (index == NOT_PRESENT) {
            return null;

        } else if (index != NOT_CACHED && cache != null
                && cache.subframe[index].version == subframeVersion[y][x]
                && subframeCount < subframeCacheSize) {
            /* We found it and it's ours; return the cached image */
            referenceCache(index);
            ret = cache.subframe[index];

            if (DEBUG_RPF) {
                Debug.output("RpfCacheHandler: found subframe " + x + ", " + y + " in cache.");
            }

            ret.setTransparent(viewAttributes.opaqueness);

            // Check to see if the attribute text has even been
            // retrieved from the RpfFrameProvider. If it hasn't, and
            // needs to be, get it.
            if (frameProvider != null && viewAttributes.showInfo
                    && (ret.getAttributeText() == null || ret.getAttributeText().length() == 0)) {

                // It's needed but not here.
                ret.setAttributeText(frameProvider.getSubframeAttributes(currentBox.tocNumber, currentBox.entryNumber, x, y));
            }

            return ret;

        } else {

            /*
             * Subframe isn't cached; allocate new entry and decompress it
             */
            index = getLRU();
            // Meet the requirements for not caching...
            if (index < 0 || index >= subframeCacheSize || subframeCount >= subframeCacheSize) {
                try {
                    ret = new RpfSubframe();
                    if (DEBUG_RPF) {
                        Debug.output("RpfCacheHandler: using uncached subframe.");
                    }
                } catch (java.lang.OutOfMemoryError oome) {
                    Debug.error("RpfCacheHandler: Out of memory!  No subframe for you!  Next up!");
                    return null;
                }
            } else { // or set the cache for the new subframe
                referenceCache(index);
                // DFD - I'm not sure it ever gets to the point where version
                // gets bigger than a byte can hold, and we don't
                // want the byte overrunning, do we? Then again, does it matter,
                // if the frame version number matches the array setting for it,
                // even
                // if it's negative?
                cache.subframe[index].version++;
                subframeIndex[y][x] = (byte) index;
                subframeVersion[y][x] = cache.subframe[index].version;
                ret = cache.subframe[index];
            }

            if (loadSubframe(ret, currentBox, cbx, cby)) {
                return ret;
            } else {
                freeCache(index);
                if (subframeIndex != null) {
                    subframeIndex[y][x] = NOT_PRESENT;
                }
            }
        }
        return null;
    }

    /**
     * Contacts the frame provider to put the subframe image in the RpfSubframe.
     * 
     * @param subframe the RpfSubframe to load the image data into.
     * @param coverageBox that has toc and entry numbers to use.
     * @param x the coveragebox x index for the subframe.
     * @param y the coveragebox y index for the subframe.
     * @return true if successful.
     */
    protected boolean loadSubframe(RpfSubframe subframe, RpfCoverageBox coverageBox, int x, int y) {
        boolean good = false;
        int[] pixels = null;

        if (frameProvider == null) {
            Debug.message("rpf", "RpfCacheHandler.loadSubframes(): null frameProvider");
            return false;
        }

        RpfViewAttributes viewAttributes = getViewAttributes();

        subframe.opaqueness = viewAttributes.opaqueness;

        if (viewAttributes.colorModel == OMRasterObject.COLORMODEL_DIRECT) {
            pixels = frameProvider.getSubframeData(coverageBox.tocNumber, coverageBox.entryNumber, x, y);
            if (pixels != null) {
                subframe.setPixels(pixels);
                good = true;
            }
        } else if (viewAttributes.colorModel == OMRasterObject.COLORMODEL_INDEXED) {
            RpfIndexedImageData riid = frameProvider.getRawSubframeData(coverageBox.tocNumber, coverageBox.entryNumber, x, y);

            if (riid != null && riid.imageData != null && riid.colortable != null) {
                subframe.setBitsAndColors(riid.imageData, riid.colortable);
                subframe.setTransparent(viewAttributes.opaqueness);
                good = true;
            }

        } else {
            Debug.error("RpfCacheHandler: Frame Provider colormodel not handled.");
            return false;
        }

        if (good) {
            // LOAD UP the geographic stuff into
            // cache.subframe[index].image
            double lat, lon, lat2, lon2;
            double xlloffset, ylloffset;

            ylloffset = (double) (y * coverageBox.subframeLatInterval);
            xlloffset = (double) (x * coverageBox.subframeLonInterval);
            lat = coverageBox.nw_lat - ylloffset;
            lon = coverageBox.nw_lon + xlloffset;
            lat2 = lat - coverageBox.subframeLatInterval;
            lon2 = lon + coverageBox.subframeLonInterval;

            String data;
            if (viewAttributes != null
                    && (viewAttributes.autofetchAttributes || viewAttributes.showInfo)) {
                data = frameProvider.getSubframeAttributes(coverageBox.tocNumber, coverageBox.entryNumber, x, y);
            } else {
                data = "";
            }

            if (DEBUG_RPFDETAIL) {
                Debug.output("Attribute data for subframe " + x + ", " + y + ":\n" + data);
            }

            // fill in the information for the subframe.
            subframe.setLocation(lat, lon, lat2, lon2);
            subframe.setAttributeText(data);
            return true;

        } else {
            subframe.setAttributeText("");
        }

        return false;
    }

}
