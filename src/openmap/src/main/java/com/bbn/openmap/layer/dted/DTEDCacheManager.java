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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCacheManager.java,v $
// $RCSfile: DTEDCacheManager.java,v $
// $Revision: 1.8 $
// $Date: 2005/12/09 21:09:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The DTEDCacheManager is the object you need if you want to retrieve DTED
 * files en masse for display. You set it up with the paths to the files, and
 * then ask it for an OMGraphicList that contains the frames for a geographical
 * area.
 * <P>
 * The DTEDCacheManager also manages objects called DTEDCacheHandlers. Cache
 * handlers have their own set of frames, figure out which frames are needed to
 * fill the screen, and ask each frame for the rasters (subframes). The
 * situation gets pretty tricky when the screen has the equator and/or the
 * dateline on it, and a different cache handler is needed for each quadrant of
 * the earth. This situation is relatively rare, though, and the
 * DTEDCacheManager automatically checks for these situations and creates the
 * cache handlers needed.
 * <P>
 * There are two calls to the Cache that you need to use. The constructor sets
 * up the cache with the location of the data on your system. The getRectangle()
 * call returns an OMGraphicList of objects to draw, that cover the area asked
 * for.
 */
public class DTEDCacheManager {
    /**
     * The size of the smaller caches, when more cachehandlers are needed to
     * cover the equator and the dateline.
     */
    public final static int SMALL_CACHE_SIZE = 20;
    /** A box is a earth quadrant. */
    public final static int MAX_NUM_BOXES = 4;

    /**
     * The cache handlers needed to cover the screen. Need one for each earth
     * quadrant, and for each DTED directory, in case coverage is spread out
     * over different sources.
     */
    protected DTEDCacheHandler[] caches = new DTEDCacheHandler[MAX_NUM_BOXES];

    protected DTEDFrameSubframeInfo subframeInfo;
    protected String[] dtedDirPaths;

    // for cache constructors
    protected int numColors;
    protected int opaqueness = DTEDFrameColorTable.DEFAULT_OPAQUENESS;
    protected int cacheSize = DTEDCacheHandler.FRAME_CACHE_SIZE;

    /**
     * Constructor that uses the default paths and the default number of colors.
     */
    public DTEDCacheManager() {
        this(null, DTEDFrameColorTable.DTED_COLORS, DTEDFrameColorTable.DEFAULT_OPAQUENESS);
    }

    /**
     * Constructor that lets you set the paths of the DTED directories, where
     * the data is located.
     * 
     * @param DTEDPaths pathnames to the DTED directories.
     */
    public DTEDCacheManager(String[] DTEDPaths) {
        this(DTEDPaths, DTEDFrameColorTable.DTED_COLORS, DTEDFrameColorTable.DEFAULT_OPAQUENESS);
    }

    /**
     * Constructor that lets you set the paths of the DTED directories, where
     * the data is located, as well as the number of colors you want used in the
     * graphics.
     * 
     * @param DTEDPaths pathnames to the DTED level 0 and 1 directories.
     * @param num_colors number of colors to be used in the graphics.
     * @param opaque the opaqueness of the dted images, 0 - 255 (0 is clear)
     */
    public DTEDCacheManager(String[] DTEDPaths, int num_colors, int opaque) {
        dtedDirPaths = DTEDPaths;
        numColors = num_colors;
        opaqueness = opaque;
    }

    /**
     * Used to set the DTED directory paths.
     * 
     * @param paths DTED Level 0 and 1 directory paths.
     */
    public void setDtedDirPaths(String[] paths) {
        dtedDirPaths = paths;
        resetCaches();
    }

    /**
     * Reset the DTEDCacheHandler array so the handlers will get created on the
     * next frame request.
     */
    public void resetCaches() {
        caches = new DTEDCacheHandler[MAX_NUM_BOXES];
    }

    /**
     * Set the number of frames that the caches should contain. If negative or
     * zero, the cache calculates the cache size based on the projection.
     */
    public void setCacheSize(int size) {
        cacheSize = size;
    }

    /**
     * Get the number of frames that the caches should contain. If negative or
     * zero, the cache is calculating the cache size based on the projection.
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Get an elevation at a point. Always uses the main cache to load the frame
     * and get the data.
     * 
     * @param lat latitude of point in decimal degrees.
     * @param lon longitude of point in decimal degrees.
     */
    public int getElevation(float lat, float lon) {
        if (caches[0] == null) {
            caches[0] = new DTEDCacheHandler(dtedDirPaths, numColors, opaqueness);
            caches[0].setSubframeInfo(subframeInfo);
        }
        return caches[0].getElevation(lat, lon);
    }

    public void setSubframeInfo(DTEDFrameSubframeInfo dfsi) {
        subframeInfo = dfsi;
        for (int i = 0; i < MAX_NUM_BOXES; i++) {
            if (caches[i] != null)
                caches[i].setSubframeInfo(dfsi);
        }
    }

    public DTEDFrameSubframeInfo getSubframeInfo() {
        return subframeInfo;
    }

    /**
     * The call to the cache that lets you choose what kind of information is
     * returned. This function also figures out what part of the earth is
     * covered on the screen, and creates auxillary cache handlers as needed.
     * 
     * @param proj The projection of the screen (CADRG).
     * @return List of rasters to display.
     */
    public OMGraphicList getRectangle(Projection proj) {

        float[] lat = new float[3];
        float[] lon = new float[3];

        // This next bit of mumbo jumbo is to handle the equator and
        // dateline: Worst case, crossing both, treat each area
        // separately, so it is the same as handling four requests for
        // data - above and below the equator, and left and right of
        // the dateline. Normal case, there is only one box. Two
        // boxes if crossing only one of the boundaries.

        int xa = 2;
        int ya = 2;
        int lat_minus = 2;
        int lon_minus = 2;
        // Set up checks for equator and dateline
        Point2D ll1 = proj.getUpperLeft();
        Point2D ll2 = proj.getLowerRight();

        lat[0] = (float) ll1.getY();
        lon[0] = (float) ll1.getX();
        lat[1] = (float) ll2.getY();
        lon[1] = (float) ll2.getX();
        lat[2] = (float) ll2.getY();
        lon[2] = (float) ll2.getX();

        if (lon[0] > 0 && lon[2] < 0) {
            lon[1] = -179.999f; // put a little breather on the
            // dateline
            lon_minus = 1;
        }
        if (lat[0] > 0 && lat[2] < 0) {
            lat[1] = -0.0001f; // put a little breather on the equator
            lat_minus = 1;
        }

        if (Debug.debugging("dteddetail")) {
            Debug.output("For :");
            Debug.output("lat[0] " + lat[0]);
            Debug.output("lon[0] " + lon[0]);
            Debug.output("lat[1] " + lat[1]);
            Debug.output("lon[1] " + lon[1]);
            Debug.output("lat[2] " + lat[2]);
            Debug.output("lon[2] " + lon[2]);
            Debug.output("lat_minus = " + lat_minus);
            Debug.output("lon_minus = " + lon_minus);
        }

        /*
         * Look at all the paths if needed. Worst case, there are four boxes on
         * the screen. Best case, there is one. The things that create boxes and
         * dictates how large they are are the equator and the dateline. When
         * the screen straddles one or both of these lat/lon lines, lon_minus
         * and lat_minus get adjusted, causing two or four different calls to
         * the tochandler to get the data above/below the equator, and
         * left/right of the dateline. Plus, each path gets checked until the
         * required boxes are filled.
         */

        if (caches[0] == null) {
            caches[0] = new DTEDCacheHandler(dtedDirPaths, numColors, opaqueness, cacheSize);
            caches[0].setSubframeInfo(subframeInfo);
        }

        caches[0].setProjection(proj, lat[ya - lat_minus], lon[xa - lon_minus], lat[ya], lon[xa]);

        // Dateline split
        if (lon_minus == 1) {
            if (caches[1] == null) {
                caches[1] = new DTEDCacheHandler(dtedDirPaths, numColors, opaqueness, cacheSize);
                caches[1].setSubframeInfo(subframeInfo);
            }

            caches[1].setProjection(proj, lat[ya - lat_minus], lon[0], lat[ya], -1f * lon[1]); // -1
                                                                                               // to
                                                                                               // make
                                                                                               // it
                                                                                               // 180
        } else
            caches[1] = null;

        // Equator Split
        if (lat_minus == 1) {
            if (caches[2] == null) {
                caches[2] = new DTEDCacheHandler(dtedDirPaths, numColors, opaqueness, cacheSize);
                caches[2].setSubframeInfo(subframeInfo);
            }

            caches[2].setProjection(proj, lat[0], lon[xa - lon_minus], -1f * lat[1], // flip
                                                                                     // breather
                                    lon[xa]);
        } else
            caches[2] = null;

        // Both!!
        if (lon_minus == 1 && lat_minus == 1) {
            if (caches[3] == null) {
                caches[3] = new DTEDCacheHandler(dtedDirPaths, numColors, opaqueness, cacheSize);
                caches[3].setSubframeInfo(subframeInfo);
            }

            caches[3].setProjection(proj, lat[0], lon[0], -1f * lat[1],// flip
                                    // breather
                                    -1f * lon[1]);// -1 to make it 180, not -180
        } else
            caches[3] = null;

        OMGraphicList graphics = new OMGraphicList();

        if (Debug.debugging("dted"))
            Debug.output("--- DTEDCacheManager: getting images: ---");

        for (int nbox = 0; nbox < MAX_NUM_BOXES; nbox++) {
            if (caches[nbox] != null) {
                OMGraphic image = caches[nbox].getNextImage(proj);
                while (image != null) {
                    graphics.add(image);
                    image = caches[nbox].getNextImage(proj);
                }
            }
        }
        return graphics;
    }

    public void setNumColors(int numberOfColors) {
        numColors = numberOfColors;
    }

    public int getNumColors() {
        return numColors;
    }

    public void setOpaqueness(int setting) {
        opaqueness = setting;
    }

    public int getOpaqueness() {
        return opaqueness;
    }

    /**
     * The DTEDCacheManager uses four DTEDCacheHandlers to create frame from
     * DTED files. The one at postion 0 is the main one. The one at index 1 is
     * used when the map cross the equator. The other two are used when the map
     * crosses the dateline.
     * 
     * @return gets the array of DTEDCacheHandlers.
     */
    public DTEDCacheHandler[] getCaches() {
        return caches;
    }
}
