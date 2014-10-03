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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameCache.java,v $
// $RCSfile: DTEDFrameCache.java,v $
// $Revision: 1.9 $
// $Date: 2005/12/09 21:09:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.dted.DTEDFrameUtil;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The DTEDFrameCache is an object that retrieves DTED paths, frames or
 * elevation values, given a latitude, longitude and dted level. It maintains a
 * collection of the frames it has already used for quicker access later. The
 * size of the cache is user-determined, and the cache also relies on user
 * provided paths to the dted directory, which is the top level directory in a
 * dted file structure. The paths are provided in a String array, so you can
 * have data in different places on a system, including a CDROM drive. 
 */
public class DTEDFrameCache
        extends CacheHandler
        implements PropertyConsumer {
    /** The elevation value returned if there is no data at a lat/lon. */
    public final static int NO_DATA = -500;
    /**
     * The maximum DTED level to check for data, given a lat/lon and a path to a
     * dted data directory.
     */
    public final static int MAX_DTED_LEVEL = 1;
    /** An array of strings representing data directory paths. */
    protected String[] dtedDirPaths;
    /**
     * Number of subframes used by each frame. Calculated by the
     * DTEDCacheHandler when it is given a projection.
     */
    protected int numXSubframes; // per frame, at the current scale
    /**
     * Number of subframes used by each frame. Calculated by the
     * DTEDCacheHandler when it is given a projection.
     */
    protected int numYSubframes;

    public static final String DTEDPathsProperty = "paths";
    public static final String DTEDFrameCacheSizeProperty = "cacheSize";

    protected String propertyPrefix = null;

    public DTEDFrameCache() {
        super();
    }

    /**
     * Create the cache with paths to search for frames, and the maximum number
     * of frames to keep on hand. Assumes the paths given are for level 0 and 1
     * data.
     * 
     * @param dtedPaths path to the level 0 and level 1 dted directories
     * @param max_size max number of frames to keep in the cache..
     */
    public DTEDFrameCache(String[] dtedPaths, int max_size) {
        super(max_size);
        dtedDirPaths = dtedPaths;
    }

    /**
     * Set the data paths.
     * 
     * @param paths paths to the dted level 0 and 1 directories.
     */
    public void setDtedDirPaths(String[] paths) {
        dtedDirPaths = paths;
    }

    /**
     * A utility to find the path to a dted file, given a lat, lon and a dted
     * level. Assumes that paths have been given to the cache. Lat/lons in
     * decimal degrees.
     * 
     * @return complete path to file with lat/lon.
     * @param lat latitude of point
     * @param lon longitude of point
     * @param level the dted level wanted (0, 1, 2, 3)
     */
    public String findFileName(double lat, double lon, int level) {

        String lonString = DTEDFrameUtil.lonToFileString((float) lon);
        String latString = DTEDFrameUtil.latToFileString((float) lat, level);

        String partialFile = "/" + lonString + "/" + latString;
        String ret = findFileName(dtedDirPaths, partialFile);

        return ret;
    }

    /**
     * Method to check the searchPaths for a file.
     * 
     * @param searchPaths an array of dted root directories
     * @param partialFile the relative pathname of a dted frame file from the
     *        dted root.
     * @return the name of the file, or null if not found.
     */
    protected String findFileName(String[] searchPaths, String partialFile) {
        if (searchPaths == null || searchPaths.length == 0) {
            return null;
        }

        for (int i = 0; i < searchPaths.length; i++) {
            String dtedFileName = searchPaths[i] + partialFile;
            if (BinaryFile.exists(dtedFileName)) {
                return dtedFileName;
            }
        }
        return null;
    }

    /**
     * Return The Dted Frame, Given A Lat, Lon And Dted Level.
     * 
     * @return Dted frame.
     * @param lat latitude of point
     * @param lon longitude of point
     * @param level the dted level wanted (0, 1, 2)
     */
    public DTEDSubframedFrame get(double lat, double lon, int level) {
        String name = findFileName(lat, lon, level);
        if (name != null) {
            return (DTEDSubframedFrame) get(name);
        }
        return null;
    }

    /**
     * A private class that makes sure that cached frames get disposed properly.
     */
    private static class DTEDCacheObject
            extends CacheObject {
        /**
         * Construct a DTEDCacheObject, just calls superclass constructor
         * 
         * @param id passed to superclass
         * @param obj passed to superclass
         */
        public DTEDCacheObject(String id, DTEDSubframedFrame obj) {
            super(id, obj);
        }

        /**
         * Calls dispose() on the contained frame, to make it eligible for
         * garbage collection.
         */
        protected void finalize() {
            ((DTEDSubframedFrame) obj).dispose();
        }
    }

    /**
     * Load a dted frame into the cache, based on the path of the frame as a
     * key.
     * 
     * @param key complete path to the frame, as a string.
     * @return DTED frame, hidden in a CacheObject.
     */
    public CacheObject load(Object key) {
        if (key != null) {
            String dtedFramePath = key.toString();
            // If it's a DTED level 0 frame, read it all in,
            // otherwise, read just what you need.
            DTEDSubframedFrame frame = new DTEDSubframedFrame(dtedFramePath, dtedFramePath.endsWith("dt0"));
            frame.initSubframes(numXSubframes, numYSubframes);

            if (frame.frame_is_valid) {
                return new DTEDCacheObject(dtedFramePath, frame);
            }
        }
        return null;
    }

    /**
     * This version of resizeCache is for projection changes, where the
     * post/pixel spacing of the images has changed, and the images need to be
     * rebuilt. The cache size will change based on scale, because more frames
     * are needed for smaller scales. If the number of subframes in either
     * direction is zero, then the resize becomes non-destructive, which means
     * that the frames will not delete their subframes. If the scale of the map
     * changes, then the frame subframe sizes need to be recalculated, and a
     * destructive resizing is necessary.
     * 
     * @param max_size the maximum number of frames in the cache.
     * @param num_x_subframes the number of horizontal subframes in each frame.
     * @param num_y_subframes the number of vertical subframes in each frame.
     */
    public void resizeCache(int max_size, int num_x_subframes, int num_y_subframes) {

        boolean destructive = false;

        if (num_x_subframes > 0 && num_y_subframes > 0) {
            numXSubframes = num_x_subframes;
            numYSubframes = num_y_subframes;
            destructive = true;
            Debug.message("dted", "DTEDFrameCache: destructive resizing");
        } else {
            Debug.message("dted", "DTEDFrameCache: passive resizing");
        }

        CacheObject[] oldObjs = objs;

        if (max_size != objs.length && max_size > 0) {
            objs = new CacheObject[max_size];
        }

        for (int i = 0; i < objs.length; i++) {

            if (i >= oldObjs.length) {
                break;
            }

            DTEDCacheObject dco = (DTEDCacheObject) oldObjs[i];

            if (dco == null) {
                // We load from the front to the back 0 -> length - 1;
                // Once you hit a null, the rest should be null, too.
                objs[i] = null;
                continue;
            }

            DTEDSubframedFrame frame = (DTEDSubframedFrame) (dco.obj);
            dco.cachedTime = 0;

            if (frame == null) {
                Debug.output("DTEDFrameCache: No Frame for key!");
                continue;
            }

            if (destructive) {
                frame.initSubframes(num_x_subframes, num_y_subframes);
            } else {
                // If it's not destructive, and the size didn't
                // change, then just continue through the loop
                // resetting the cachedTime for the CacheObjects.
                if (oldObjs == objs) {
                    continue;
                }
            }

            if (i < oldObjs.length) {
                objs[i] = oldObjs[i];
            } else {
                objs[i] = null;
            }
        }

        oldObjs = null;
    }

    /**
     * This version of resizeCache is for screen size changes, where the number
     * of frames kept on hand in the cache must change, but the images
     * themselves don't have to because the pixel/posting spacing hasn't changed
     * in the projection. The frames already in the cache are re-added to the
     * new cache, if the cache size is increasing. If the cache size is
     * shrinking, then as many as will fit are added to the new cache.
     * 
     * @param max_size the new size of the cache.
     */
    public void resizeCache(int max_size) {
        resizeCache(max_size, 0, 0);
    }

    /**
     * Return the elevation of a lat/lon point, in meters.
     * 
     * @return elevation in meters.
     * @param lat in decimal degrees.
     * @param lon in decimal degrees.
     */
    public int getElevation(float lat, float lon) {
        for (int i = /* dted level */2; i >= /* dted level */0; i--) {
            DTEDSubframedFrame frame = null;
            String dtedFileName = findFileName((double) lat, (double) lon, i);

            if (dtedFileName != null)
                frame = (DTEDSubframedFrame) get(dtedFileName);

            if (frame != null)
                return (int) frame.elevationAt(lat, lon);
        }
        return NO_DATA;
    }

    /**
     * Return the two-dimensional matrix of elevation posts (heights)
     * representing coverage of a given geographical rectangle. The matrix
     * represents coverage in an Equal Arc projection, and that's why the
     * rectangle is defined by the projection parameters.
     * 
     * @param proj the projection describing the wanted area
     * @param dtedLevel the DTED level (0, 1, 2) to be used, which describes the
     *        geographicsal spacing between the posts.
     * @return array of elevations, in meters. Spacing depends on the DTED
     *         level.
     */
    public short[][] getElevations(EqualArc proj, int dtedLevel) {
        Point2D ul = proj.getUpperLeft();
        Point2D lr = proj.getLowerRight();

        return getElevations((float) ul.getY(), (float) ul.getX(), (float) lr.getY(), (float) lr.getX(), dtedLevel);
    }

    /**
     * Return the two-dimensional matrix of elevation posts (heights)
     * representing coverage of a given geographical rectangle. The matrix
     * represents coverage in an Equal Arc projection. Doesn't handle
     * projections which cross the dateline - You must handle that yourself by
     * making two inquiries.
     * 
     * @param ullat upper latitude, in decimal degrees
     * @param ullon left longitude, in decimal degrees
     * @param lrlat lower latitude, in decimal degrees
     * @param lrlon right longitude, in decimal degrees
     * @param dtedLevel the DTED level (0, 1, 2) to be used, which describes the
     *        geographicsal spacing between the posts.
     */
    public short[][] getElevations(float ullat, float ullon, float lrlat, float lrlon, int dtedLevel) {
        return getElevations(ullat, ullon, lrlat, lrlon, dtedLevel, null);
    }

    /**
     * Return the two-dimensional matrix of elevation posts (heights)
     * representing coverage of a given geographical rectangle. The matrix
     * represents coverage in an Equal Arc projection. Doesn't handle
     * projections which cross the dateline - You must handle that yourself by
     * making two inquiries.
     * <P>
     * This method is slightly different that the one above, because it includes
     * a input variable DTEDFrame. There is an inherent problem in the algorithm
     * if some of the DTED frames are missing. It's too difficult to calculate
     * the size of the return array if you don't know that any frames are
     * available. So, you should always use the method above, which calls this
     * method with a null refFrame. If some of the DTED frames are missing, then
     * this method is called recursively, with a frame to use for calculating
     * post spacings at the right time.
     * 
     * @param ullat upper latitude, in decimal degrees
     * @param ullon left longitude, in decimal degrees
     * @param lrlat lower latitude, in decimal degrees
     * @param lrlon right longitude, in decimal degrees
     * @param dtedLevel the DTED level (0, 1, 2) to be used, which describes the
     *        geographicsal spacing between the posts.
     * @param refFrame DTEDFrame used to calculate measurements.
     * @return array of elevations, in meters. Spacing depends on the DTED
     *         level.
     */
    protected short[][] getElevations(float ullat, float ullon, float lrlat, float lrlon, int dtedLevel, DTEDSubframedFrame refFrame) {

        float upper = ullat;
        float lower = lrlat;
        float right = lrlon;
        float left = ullon;

        // Since matrix indexes depend on these being in the right
        // order, we'll double check and flip values, just to make
        // sure lower is lower, and higher is higher.
        if (ullon > lrlon) {
            if (ullon > 0 && lrlon < 0) {
                Debug.error("DTEDFrameCache: getElevations: Stradling dateline not handled!");
                return null;
            }
            right = ullon;
            left = lrlon;
        }

        if (lrlat > ullat) {
            upper = lrlat;
            lower = ullat;
        }

        // These are the limits of the lat/lons per frame searched
        float upperlat = 0;
        float upperlon = 0;
        float lowerlat = 0;
        float lowerlon = 0;

        int xSize = (int) (Math.ceil(right) - Math.floor(left));
        int ySize = (int) (Math.ceil(upper) - Math.floor(lower));

        // System.out.println("Going with size = " + xSize + "x" +
        // ySize);

        int[] xLengths = new int[xSize];
        int[] yLengths = new int[ySize];

        short[][][][] es = new short[xSize][ySize][][];
        int x, y;
        DTEDSubframedFrame frame = null;
        boolean needCalc = false;
        // Let's march through the frames, bottom to top, left to
        // right.
        for (x = 0; x < xSize; x++) {

            if (x == 0)
                lowerlon = left;
            else
                lowerlon = (float) Math.floor(left) + (float) x;

            if (x == xSize - 1)
                upperlon = right;
            else
                upperlon = (float) Math.floor(left) + (float) (x + 1);

            for (y = 0; y < ySize; y++) {

                if (y == 0)
                    lowerlat = lower;
                else
                    lowerlat = (float) Math.floor(lower) + (float) y;

                if (y == ySize - 1)
                    upperlat = upper;
                else
                    upperlat = (float) Math.floor(lower) + (float) (y + 1);

                DTEDSubframedFrame thisFrame = get(lowerlat, lowerlon, dtedLevel);

                if (thisFrame != null) {
                    // System.out.println("Getting elev for " +
                    // upperlat + ", " +
                    // lowerlon + ", " +
                    // lowerlat+ ", " + upperlon);
                    es[x][y] = thisFrame.getElevations(upperlat, lowerlon, lowerlat, upperlon);
                    xLengths[x] = es[x][y].length;
                    yLengths[y] = es[x][y][0].length;
                    frame = thisFrame;
                } else {
                    if (refFrame != null) {
                        Debug.output("DTEDFrameCache: Missing frames, going to use reference frame");
                        // calculate these lengths, since the refFrame
                        // was set...
                        int[] indexes = refFrame.getIndexesFromLatLons(upperlat, lowerlon, lowerlat, upperlon);
                        xLengths[x] = indexes[2] - indexes[0] + 1;
                        yLengths[y] = indexes[3] - indexes[1] + 1;

                    } else {
                        if (frame != null) {
                            // Well, we have a frame to do
                            // calculations on, and we know we need
                            // to do at least one calculation, so
                            // might as well go and do this right...
                            return getElevations(ullat, ullon, lrlat, lrlon, dtedLevel, frame);
                        } else {
                            needCalc = true;
                        }
                    }
                }
            }
        }

        // refFrame == null, and all the empty frames were found
        // before the good ones...
        if (needCalc == true && frame != null)
            return getElevations(ullat, ullon, lrlat, lrlon, dtedLevel, frame);

        int xLength = 0;
        int yLength = 0;

        // Need to figure out how big the returned matrix is! This
        // only works if all the frames come back....
        for (x = 0; x < xLengths.length; x++)
            xLength += xLengths[x];
        for (y = 0; y < yLengths.length; y++)
            yLength += yLengths[y];

        // System.out.println("Creating a matrix: " + xLength + "x" +
        // yLength);
        short[][] matrix = new short[xLength][yLength];

        // Now copy all the little matrixes into the big matrix
        int xspacer = 0;
        // Through each little matrix in the x direction
        for (x = 0; x < es.length; x++) {
            int yspacer = 0;
            // Through each little matrix in the y direction
            for (y = 0; y < es[x].length; y++) {

                // Make sure the frame exists and is found...
                if (es[x][y] != null) {
                    // Through each lon row in each little matrix
                    for (int i = 0; i < es[x][y].length; i++) {
                        System.arraycopy(es[x][y][i], 0, matrix[i + xspacer], yspacer, es[x][y][i].length);
                    }
                    // On the last one lon column, increase the spacer
                    // for the
                    // next little matrix above this one.
                    yspacer += yLengths[y];
                } else
                    yspacer += xLengths[y];
            }
            // On the last little matrix in the column, increase the
            // xspacer for the little matrixes in the next column.
            xspacer += xLengths[x];
        }

        return matrix;
    }

    /**
     * PropertyConsumer method.
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * PropertyConsumer method.
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * PropertyConsumer method.
     */
    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    /**
     * PropertyConsumer method.
     */
    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);

        prefix = PropUtils.getScopedPropertyPrefix(this);

        String[] paths = PropUtils.initPathsFromProperties(props, prefix + DTEDPathsProperty);

        setDtedDirPaths(paths);

        resetCache((int) PropUtils.intFromProperties(props, prefix + DTEDFrameCacheSizeProperty, DTEDCacheHandler.FRAME_CACHE_SIZE));

    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + DTEDFrameCacheSizeProperty, Integer.toString(getCacheSize()));

        // find out paths...
        String[] p;
        String prop = null;

        StringBuffer pathString = new StringBuffer();
        if (dtedDirPaths != null && dtedDirPaths.length > 0) {
            for (String path : dtedDirPaths) {
                if (pathString.length() > 0) {
                    pathString.append(';');
                }
                
                pathString.append(path);
            }
        }
        props.put(prefix + prop, pathString.toString());

        return props;
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        props.put(DTEDPathsProperty, "Paths to the DTED directories");
        props.put(DTEDFrameCacheSizeProperty, "Size of the frame cache");
        return props;
    }

    public static void main(String[] args) {
        Debug.init();
        if (args.length < 1) {
            Debug.output("DTEDFrameCache:  Need a path/filename");
            System.exit(0);
        }

        Debug.output("DTEDFrameCache: " + args[0]);
        DTEDFrameCache dfc = new DTEDFrameCache(args, 10);

        // 35.965065 -121.198715
        // 35.998 36.002 lon -121.002 -120.998
        float ullat = 37.002f;
        float ullon = -121.002f;
        float lrlat = 35.998f;
        float lrlon = -119.998f;

        // System.out.println("Getting elevations for " +
        // ullat + ", " + ullon + ", " +
        // lrlat + ", " + lrlon);
        short[][] e = dfc.getElevations(ullat, ullon, lrlat, lrlon, 0);
        if (e != null) {
            for (int i = e[0].length - 1; i >= 0; i--) {
                int col = 0;
                System.out.print("r" + i + "-");
                for (int j = 0; j < e.length; j++) {
                    System.out.print(e[j][i] + " ");
                    col++;
                }
                System.out.println(" - " + col);
            }
        }

    }
}
