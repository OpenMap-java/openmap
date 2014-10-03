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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/cacheHandler/CacheHandler.java,v
// $
// $RCSfile: CacheHandler.java,v $
// $Revision: 1.2 $
// $Date: 2006/12/15 18:39:53 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.cacheHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A base cache support object. Based on the
 * com.bbn.openmap.layer.util.cacheHandler package components, this CacheHandler
 * uses Objects instead of Strings and will be replacing the earlier version.
 * 
 * @author dietrick
 */
public abstract class CacheHandler {
    protected CacheObject[] objs;
    protected int logicalClock;

    public static Logger logger = Logger.getLogger("com.bbn.openmap.util.cacheHandler.CacheHandler");

    public static int DEFAULT_MAX_CACHE_SIZE = 25;

    /**
     * Standard default constructor
     */
    public CacheHandler() {
        initCache(DEFAULT_MAX_CACHE_SIZE);
    }

    /**
     * Constructor used when you know the limits
     */
    public CacheHandler(int max_size) {
        initCache(max_size);
    }

    /**
     * Set the size, reset the logical clock
     */
    private void initCache(int max_size) {
        if (objs != null && objs.length == max_size) {
            clear();
        } else {
            objs = new CacheObject[max_size];
        }
        logicalClock = 0;
    }

    /**
     * Remove all the objects from the cache.
     */
    public void clear() {
        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                objs[i] = null;
            }
        }
    }

    /**
     * Need to clear memory, get gc moving, and ready for new objects
     */
    public void resetCache() {
        initCache(objs.length);
    }

    /**
     * Need to clear memory, get gc moving, and ready for new objects. Delete
     * the current Hashtable and create a new one with the new capacity.
     * 
     * @param max_size the capacity of the Hashtable.
     */
    public void resetCache(int max_size) {
        initCache(max_size);
    }

    /**
     * Get the current size of the cache.
     */
    public int getCacheSize() {
        return objs.length;
    }

    /**
     * The main call to retrieve something from the cache
     */
    public Object get(Object key) {
        CacheObject ret = searchCache(key);
        if (ret != null)
            return ret.obj;

        ret = load(key);
        if (ret == null)
            return null;

        replaceLeastUsed(ret);
        return ret.obj;
    }

    /**
     * Called from get if the key doesn't exist, to "load" the new object into
     * the cache before returning it. This function should define how a
     * CacheObject is created, or loaded from the file system, or whatever.
     */
    public abstract CacheObject load(Object key);

    /**
     * Search the cache for a match -return null if not found. The key search is
     * case insensitive.
     */
    public CacheObject searchCache(Object key) {
        for (int i = 0; i < objs.length; i++) {
            CacheObject co = objs[i];
            if (co == null) {
                // Since we load 0 -> length - 1, if we get a null
                // one, the rest are null, too.
                break;
            } else if (co.id.equals(key)) {
                return co;
            }
        }
        return null;
    }

    /**
     * If there is space in the cache, put the object in. If there isn't space,
     * find the least used object, and replace it.
     */
    protected void replaceLeastUsed(CacheObject newObj) {

        // If the cache has room...
        int i;
        for (i = objs.length - 1; i >= 0; i--) {
            if (objs[i] == null) {
                // Somewhere in a partially filled cache, keep looking
                // for the last taken place...
                if (i == 0) {
                    // there is nothing in the cache.
                    objs[0] = newObj;
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("was empty - added " + newObj.id);
                    }

                    return;
                } else {
                    continue;
                }
            } else if (i == objs.length - 1) {
                // We're at the end, and there is no empty space -
                // we'll need to look at the LRU clock.
                break;
            } else {
                // We are at the index of the last taken spot, and the
                // next place is available.
                objs[i + 1] = newObj;
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("had room - added " + newObj.id + " to the "
                            + i + " spot.");
                }
                return;
            }
        }

        // If we get here, we need to replace something in the cache.

        int minClock = logicalClock + 1;
        int LUIndex = -1;

        for (i = objs.length - 1; i >= 0; i--) {
            if (objs[i].older(minClock)) {
                LUIndex = i;
                minClock = objs[i].cachedTime;
            }
        }

        if (LUIndex != -1) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Tossing " + objs[LUIndex].id
                        + " from cache[" + LUIndex + "] to add " + newObj.id);
            }
            objs[LUIndex] = newObj;
            newObj.cachedTime = logicalClock++;
        }
    }

    /**
     * Return a ListIterator of the cache objects.
     */
    public java.util.ListIterator<CacheObject> listIterator() {
        return java.util.Arrays.asList(objs).listIterator();
    }
}