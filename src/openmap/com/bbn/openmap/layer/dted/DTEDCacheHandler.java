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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCacheHandler.java,v $
// $RCSfile: DTEDCacheHandler.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:53 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.util.Debug;

/**
 * The DTEDCacheHandler controls the real cache of DTED frames. It is
 * managed by the DTEDCacheManager, and the manager asks it for
 * frames. The DTEDCacheHandler goes to its cache for the images, but
 * it also manages the configuration of the frames, and figures out
 * what frames are needed, given a projection.
 */
public class DTEDCacheHandler {
    /** Default frame cache size. */
    public final static int FRAME_CACHE_SIZE = 20;
    /** Subframe pixel height and width. */
    public final static int SF_PIXEL_HW = 200;
    /** The DTED level 0, 1 directory paths. */
    protected String[] paths;
    /** The DTED level 2 directory paths. */
    protected String[] paths2;
    /** The real frame cache. */
    protected DTEDFrameCache frameCache;
    protected int frameCacheSize = -1; // No limit.
    /** The colors used by the frames to create the images. */
    protected DTEDFrameColorTable colortable; // numColors, reduced
                                              // colortable

    // Setting up the screen...
    protected LatLonPoint ulCoords, lrCoords;
    protected double frameUp, frameDown, frameLeft, frameRight;
    protected double xPixInterval, yPixInterval; // degrees/pixel
    protected int numXSubframes, numYSubframes;
    protected int lastSubframeWidth, lastSubframeHeight;
    protected int currentFrameCacheSize = -10; // guarantees that it
                                               // will changed first
                                               // time.

    // Returning the images...
    protected boolean firstImageReturned = true;
    protected double frameLon = 0.0;
    protected double frameLat = 0.0;
    protected int subx = 0;
    protected int suby = 0;
    protected boolean newframe = false;
    protected DTEDFrame frame = null;

    /** A description of the drawing attributes of the images. */
    protected DTEDFrameSubframeInfo dfsi = new DTEDFrameSubframeInfo(DTEDFrameSubframe.NOSHADING, DTEDFrameSubframe.DEFAULT_BANDHEIGHT, DTEDFrameSubframe.LEVEL_0, DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST);

    public DTEDCacheHandler() {
        this(null,
             DTEDFrameColorTable.DTED_COLORS,
             DTEDFrameColorTable.DEFAULT_OPAQUENESS,
             FRAME_CACHE_SIZE);
    }

    public DTEDCacheHandler(String[] dataPaths, int numColors, int opaque) {
        this(dataPaths, null, numColors, opaque, -1);
    }

    public DTEDCacheHandler(String[] dataPaths, String[] data2Paths,
            int numColors, int opaque) {
        this(dataPaths, data2Paths, numColors, opaque, -1);
    }

    public DTEDCacheHandler(String[] dataPaths, int numColors, int opaqueness,
            int subframe_cache_size) {
        this(dataPaths, null, numColors, opaqueness, subframe_cache_size);
    }

    public DTEDCacheHandler(String[] dataPaths, String[] data2Paths,
            int numColors, int opaqueness, int subframe_cache_size) {

        colortable = new DTEDFrameColorTable(numColors, opaqueness, true);

        setFrameCacheSize(subframe_cache_size);

        paths = dataPaths;
        paths2 = data2Paths;
        frameCache = new DTEDFrameCache(dataPaths, data2Paths, frameCacheSize);

        if (Debug.debugging("dted")) {
            Debug.output("DTEDCacheHandler: Created with cache size of "
                    + frameCacheSize);
        }
    }

    /**
     * Normally, the cache grows and shrinks as appropriate according
     * to the number of frames needed to cover the screen. If you want
     * to limit the size it can grow, set the size. If it's negative,
     * then there will be no limit.
     */
    public void setFrameCacheSize(int size) {
        if (size <= 0) {
            frameCacheSize = FRAME_CACHE_SIZE;
        } else {
            frameCacheSize = size;
        }
    }

    /**
     * Get the limit imposed on the number of frames used in the
     * cache.
     */
    public int getFrameCacheSize() {
        return frameCacheSize;
    }

    /**
     * Get an elevation at a point. Always uses the cache to load the
     * frame and get the data.
     */
    public int getElevation(float lat, float lon) {
        return frameCache.getElevation(lat, lon);
    }

    /** Setting the subframe attributes. */
    public void setSubframeInfo(DTEDFrameSubframeInfo new_dfsi) {
        dfsi = new_dfsi;
        if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING)
            colortable.setGreyScale(false);
        else
            colortable.setGreyScale(true);
    }

    /**
     * The method to call to let the cache handler know what the
     * projection looks like so it can figure out which frames (and
     * subframes) will be needed.
     * 
     * @param proj the EqualArc projection of the screen.
     */
    public void setProjection(EqualArc proj) {
        setProjection(proj,
                proj.getUpperLeft().getLatitude(),
                proj.getUpperLeft().getLongitude(),
                proj.getLowerRight().getLatitude(),
                proj.getLowerRight().getLongitude());
    }

    /**
     * The method to call to let the cache handler know what the
     * projection looks like so it can figure out which frames (and
     * subframes) will be needed. Should be called when the
     * CacheHandler is dealing with just a part of the map, such as
     * when the map covers the dateline or equator.
     * 
     * @param proj the EqualArc projection of the screen.
     * @param lat1 latitude of the upper left corner of the window, in
     *        decimal degrees.
     * @param lon1 longitude of the upper left corner of the window,
     *        in decimal degrees.
     * @param lat2 latitude of the lower right corner of the window,
     *        in decimal degrees.
     * @param lon2 longitude of the lower right corner of the window,
     *        in decimal degrees.
     */
    public void setProjection(EqualArc proj, float lat1, float lon1,
                              float lat2, float lon2) {

        ulCoords = new LatLonPoint(lat1, lon1);
        lrCoords = new LatLonPoint(lat2, lon2);
        double xpi = 360 / proj.getXPixConstant();
        double ypi = 90 / proj.getYPixConstant();

        int numFramesNeeded;

        firstImageReturned = true;

        // upper lat of top frame of the screen
        // lower lat of bottom frame of the screen
        // left lon of left frame of the screen
        // upper lon of right frame of the screen
        frameUp = Math.floor((double) lat1);
        frameDown = Math.floor((double) lat2);
        frameLeft = Math.floor((double) lon1);
        frameRight = Math.ceil((double) lon2);

        if (Debug.debugging("dted"))
            Debug.output("frameUp = " + frameUp + ", frameDown = " + frameDown
                    + ", frameLeft = " + frameLeft + ", frameRight = "
                    + frameRight);

        // Limit the size of the cache, if desired.
        if (frameCacheSize > 0) {
            numFramesNeeded = frameCacheSize;
            if (Debug.debugging("dteddetail")) {
                Debug.output("DTEDCacheHandler: frameCacheSize remains at: "
                        + numFramesNeeded);
            }
        } else {
            // calculate how many frames should be in the cache...
            numFramesNeeded = (int) (Math.abs(frameUp - frameDown)
                    * Math.abs(frameRight - frameLeft) * 2);
        }

        if (xPixInterval != xpi || yPixInterval != ypi) {

            // Screen attributes changed!!!!
            xPixInterval = xpi;
            yPixInterval = ypi;

            // While it changed...
            dfsi.xPixInterval = xPixInterval;
            dfsi.yPixInterval = yPixInterval;

            // Trap for funky values...
            if (xpi == 0 || ypi == 0) {
                numXSubframes = 0;
                numYSubframes = 0;
                return;
            }

            int frame_width = (int) Math.ceil(1.0 / xpi);
            int frame_height = (int) Math.ceil(1.0 / ypi);

            // Even number of subframes in frame
            numXSubframes = frame_width / SF_PIXEL_HW;
            lastSubframeWidth = SF_PIXEL_HW;
            numYSubframes = frame_height / SF_PIXEL_HW;
            lastSubframeHeight = SF_PIXEL_HW;

            if (frame_width % SF_PIXEL_HW != 0) {
                lastSubframeWidth = frame_width - (numXSubframes * SF_PIXEL_HW);
                numXSubframes++;
            }
            if (frame_height % SF_PIXEL_HW != 0) {
                lastSubframeHeight = frame_height
                        - (numYSubframes * SF_PIXEL_HW);
                numYSubframes++;
            }

            currentFrameCacheSize = numFramesNeeded;

            frameCache.resizeCache(numFramesNeeded,
                    numXSubframes,
                    numYSubframes);
            if (Debug.debugging("dteddetail")) {
                Debug.output("DTEDCacheHandler: frameCacheSize set to: "
                        + numFramesNeeded);
            }

            if (Debug.debugging("dted"))
                Debug.output("***** Screen Parameters Changed! \n"
                        + " Frame width (pix) = " + frame_width + "\n"
                        + " Frame height (pix) = " + frame_height + "\n"
                        + " Num x subframes = " + numXSubframes + "\n"
                        + " Num y subframes = " + numYSubframes + "\n"
                        + " last sf width = " + lastSubframeWidth + "\n"
                        + " last sf height = " + lastSubframeHeight + "\n"
                        + " X pix interval = " + xpi + "\n"
                        + " Y pix interval = " + ypi + "\n");

        } else if (Math.abs(numFramesNeeded - currentFrameCacheSize) > numFramesNeeded / 2) {
            currentFrameCacheSize = numFramesNeeded;
            frameCache.resizeCache(numFramesNeeded);
            if (Debug.debugging("dteddetail")) {
                Debug.output("DTEDCacheHandler: frameCacheSize set to: "
                        + numFramesNeeded);
            }
        }
    }

    /**
     * Returns the next OMRaster image. When setProjection() is
     * called, the cache sets the projection parameters it needs, and
     * also resets this popping mechanism. When this mechanism is
     * reset, you can keep calling this method to get another subframe
     * image. When it returns a null value, it is done. It will
     * automatically skip over window frames it doesn't have, and
     * return the next one it does have. It traverses from the top
     * left to right frames, and top to bottom for each column of
     * frames. It handles all the subframes for a frame at one time.
     * 
     * @return OMRaster image.
     */
    public OMRaster getNextImage() {

        OMRaster subframe = null;

        // Subframe coordinates and height and width
        // upper left, lower right
        double sf_ullat, sf_ullon, sf_lrlat, sf_lrlon;
        long sf_width = SF_PIXEL_HW;
        long sf_height = SF_PIXEL_HW;

        if (Debug.debugging("dted"))
            Debug.output("--- DTEDCacheHandler: getNextImage:");

        while (true) {

            if (firstImageReturned == true) {
                frameLon = frameLeft;
                frameLat = frameDown;
                subx = 0;
                suby = 0;
                newframe = true;
                firstImageReturned = false;
            } else {
                if (frame != null && subx < numXSubframes) {
                    // update statics to look for next subframe
                    if (suby < numYSubframes - 1)
                        suby++;
                    else {
                        suby = 0;
                        subx++;
                    }
                } else if (frameLon < frameRight) {
                    // update statics to look for next frame
                    subx = 0;
                    suby = 0;
                    if (frameLat < frameUp)
                        frameLat++;
                    else {
                        frameLat = frameDown;
                        frameLon++;
                    }
                    newframe = true;
                } else { // bounds exceeded, all done
                    return (OMRaster) null;
                }
            }

            if (newframe && frameLon < frameRight) {
                if (Debug.debugging("dted"))
                    Debug.output(" gni: Getting new frame Lat = " + frameLat
                            + " Lon = " + frameLon);

                frame = frameCache.get(frameLat, frameLon, dfsi.dtedLevel);
            }

            //  Figure out subframe lat/lon and height/width
            if (frame != null) {

                newframe = false;
                if (subx == (numXSubframes - 1))
                    sf_width = lastSubframeWidth;
                if (suby == (numYSubframes - 1))
                    sf_height = lastSubframeHeight;
                // width/height degrees are spacers - degrees to
                // subframe within the frame. sf_height/width_degrees
                // are the lat/lon of the frame corner.
                double sf_width_degrees = (double) sf_width * xPixInterval;
                double sf_height_degrees = (double) sf_height * yPixInterval;
                double width_degrees = (double) SF_PIXEL_HW * xPixInterval;
                double height_degrees = (double) SF_PIXEL_HW * yPixInterval;

                sf_ullat = (double) (frameLat + 1.0)
                        - ((double) suby * height_degrees);
                sf_ullon = (double) frameLon + ((double) subx * width_degrees);
                sf_lrlat = (double) (frameLat + 1.0)
                        - ((double) suby * height_degrees) - sf_height_degrees;
                sf_lrlon = (double) frameLon + ((double) subx * width_degrees)
                        + sf_width_degrees;

                if ((ulCoords.getLatitude() > sf_lrlat && lrCoords.getLatitude() < sf_ullat)
                        &&

                        (ulCoords.getLongitude() < sf_lrlon && lrCoords.getLongitude() > sf_ullon)
                        &&

                        subx < numXSubframes) {

                    dfsi.height = (int) sf_height;
                    dfsi.width = (int) sf_width;
                    dfsi.lon = (float) sf_ullon;
                    dfsi.lat = (float) sf_ullat;
                    dfsi.subx = subx;
                    dfsi.suby = suby;

                    if (Debug.debugging("dteddetail")) {
                        Debug.output(" gni: Looking for Subframe " + subx
                                + ", " + suby);
                    }

                    subframe = frame.getSubframeOMRaster(dfsi, colortable);

                    if (subframe != null) {
                        if (Debug.debugging("dted")) {
                            Debug.output(" gni: Subframe " + subx + ", " + suby
                                    + " found :)");
                        }
                        return subframe;
                    }
                } else if (Debug.debugging("dteddetail")) {
                    Debug.output(" gni: Subframe " + subx + ", " + suby
                            + " didn't meet screen criteria");
                }
            }
            sf_width = SF_PIXEL_HW;
            sf_height = SF_PIXEL_HW;
        }
    }

}

