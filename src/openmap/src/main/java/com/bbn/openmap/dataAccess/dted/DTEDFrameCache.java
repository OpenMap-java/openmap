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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDFrameCache.java,v $
// $RCSfile: DTEDFrameCache.java,v $
// $Revision: 1.8 $
// $Date: 2007/02/26 16:41:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.PropertyConsumer;
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
 * size of the cache is determined by startup settings.
 * <P>
 * 
 * The DTEDFrameCache can be placed in the MapHandler, where other objects can
 * share it in order to all use the same DTED data. It can be configured with
 * properties:
 * <P>
 * 
 * <pre>
 * 
 * 
 *         frameCache.cacheSize=40
 *         frameCache.directoryHandlers=dteddir1 dteddir2
 *         frameCache.dteddir1.translator=com.bbn.openmap.dataAccess.dted.StandardDTEDNameTranslator
 *         frameCache.dteddir1.path=/data/dted
 *         frameCache.dteddir2.translator=com.bbn.openmap.dataAccess.dted.StandardDTEDNameTranslator
 *         frameCache.dteddir2.path=/data/dted
 * 
 * 
 * </pre>
 * 
 * A DTEDDirectoryHandler needs to be specified for each DTED directory you want
 * to use. If a translator isn't specified in the properties for a directory
 * handler, the StandardDTEDNameTranslator will be used. If you have DTED data
 * that doesn't conform to the naming conventions specified in the Military
 * Standard, you can use a different DTEDNameTranslator instead for your
 * particular directory handler.
 */
public class DTEDFrameCache extends CacheHandler implements PropertyConsumer {
    /**
     * The elevation value returned if there is no data at a lat/lon (-32767).
     */
    public final static int NO_DATA = -32767;

    public static final String DTEDDirectoryHandlerProperty = "directoryHandlers";
    public static final String DTEDFrameCacheSizeProperty = "cacheSize";

    public int DEFAULT_CACHE_SIZE = 20;

    protected String propertyPrefix = null;

    protected Vector directories = new Vector();

    protected int highestResLevel = 2;

    public DTEDFrameCache() {
        super();
    }

    /**
     * Create the cache with paths to search for frames, and the maximum number
     * of frames to keep on hand. Assumes the paths given are for level 0 and 1
     * data.
     * 
     * @param max_size max number of frames to keep in the cache..
     */
    public DTEDFrameCache(int max_size) {
        super(max_size);
    }

    /**
     * Add a DTED DirectoryHandler to be used for the DTEDFrameCache.
     */
    public void addDTEDDirectoryHandler(DTEDDirectoryHandler handler) {
        directories.add(handler);
    }

    /**
     * Remove a DTED DirectoryHandler from the list used for the DTEDFrameCache.
     */
    public void removeDTEDDirectoryHandler(DTEDDirectoryHandler handler) {
        directories.remove(handler);
    }

    /**
     * Get the Vector of DTEDDirectoryHandlers used by the DTEDFrameCache.
     */
    public Vector getDTEDDirectoryHandlers() {
        return directories;
    }

    /**
     * Set the Vector of DTEDDirectoryHandlers used by the DTEDFrameCache. You
     * might want to use this to set the order of directories that are searched
     * for a DTED frame.
     */
    public void setDTEDDirectoryHandlers(Vector handlers) {
        directories = handlers;
    }

    /**
     * A utility to find the path to a dted file, given a lat, lon and a dted
     * level. Assumes that paths have been given to the cache. Lat/lons in
     * decimal degrees.
     * 
     * @param lat latitude of point
     * @param lon longitude of point
     * @param level the dted level wanted (0, 1)
     * @return complete path to file with lat/lon.
     */
    public String findFileName(double lat, double lon, int level) {

        if (directories != null) {
            for (Iterator it = directories.iterator(); it.hasNext();) {
                DTEDDirectoryHandler ddh = (DTEDDirectoryHandler) it.next();
                DTEDNameTranslator dnt = ddh.getTranslator();
                dnt.set(lat, lon, level);
                String dtedFileName = dnt.getName();
                if (Debug.debugging("dtedfile")) {
                    Debug.output("DTEDFrameCache translator returns "
                            + dtedFileName + " for " + lat + ", " + lon
                            + ", level " + level);
                }
                if (BinaryFile.exists(dtedFileName)) {
                    return dtedFileName;
                }
            }
        }

        return null;
    }

    /**
     * Return The DTED Frame, Given A Lat, Lon And DTED Level.
     * 
     * @param lat latitude of point
     * @param lon longitude of point
     * @param level the dted level wanted (0, 1, 2)
     * @return DTED frame.
     */
    public DTEDFrame get(double lat, double lon, int level) {
        String name = findFileName(lat, lon, level);
        if (name != null) {
            if (Debug.debugging("dtedfile")) {
                Debug.output("DTEDFrameCache: returning " + name + " for "
                        + lat + ", " + lon + ", level " + level);
            }
            return (DTEDFrame) get(name);
        } else {
            if (Debug.debugging("dtedfile")) {
                Debug.output("DTEDFrameCache: couldn't find frame for " + lat
                        + ", " + lon + ", level " + level);
            }
        }
        return null;
    }

    /**
     * A private class that makes sure that cached frames get disposed properly.
     */
    private static class DTEDCacheObject extends CacheObject {
        /**
         * Construct a DTEDCacheObject, just calls superclass constructor
         * 
         * @param id passed to superclass
         * @param obj passed to superclass
         */
        public DTEDCacheObject(String id, DTEDFrame obj) {
            super(id, obj);
        }

        /**
         * Calls dispose() on the contained frame, to make it eligible for
         * garbage collection.
         */
        protected void finalize() {
            ((DTEDFrame) obj).dispose();
        }
    }

    /**
     * Load a dted frame into the cache, based on the path of the frame as a
     * key.
     * 
     * @param key complete path to the frame, String.
     * @return DTED frame, hidden as a CacheObject.
     */
    public CacheObject load(Object key) {
        if (key != null) {
            String dtedFramePath = key.toString();
            DTEDFrame frame = new DTEDFrame(dtedFramePath, true);
            if (frame.frame_is_valid) {
                return new DTEDCacheObject(dtedFramePath, frame);
            }
        }
        return null;
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

            DTEDFrame frame = (DTEDFrame) (dco.obj);
            dco.cachedTime = 0;

            if (frame == null) {
                Debug.output("DTEDFrameCache: No Frame for key!");
                continue;
            }

            if (oldObjs == objs) {
                continue;
            }

            if (i < oldObjs.length) {
                objs[i] = oldObjs[i];
            } else {
                objs[i] = null;
            }
        }

        oldObjs = null;
    }

    public int getHighestResLevel() {
        return highestResLevel;
    }

    public void setHighestResLevel(int highestResLevel) {
        this.highestResLevel = highestResLevel;
    }

    /**
     * Return the elevation of a lat/lon point, in meters. Will look for frames
     * starting at the highest resolution specified in this DTEDFrameCache, and
     * work up to level 0, searching for a frame to provide an answer. Will
     * return NO_DATA if a frame is not found, or if there is no data in the
     * frame file that is found. The default highest resolution DTED level is 2.
     * 
     * @return elevation in meters.
     * @param lat in decimal degrees.
     * @param lon in decimal degrees.
     */
    public int getElevation(float lat, float lon) {
        for (int i = /* dted level */highestResLevel; i >= /* dted level */0; i--) {
            String dtedFileName = findFileName((double) lat, (double) lon, i);

            if (dtedFileName != null) {
                DTEDFrame frame = (DTEDFrame) get(dtedFileName);

                if (frame != null) {
                    return (int) frame.elevationAt(lat, lon);
                }
            }
        }

        return NO_DATA;
    }

    /**
     * Return the elevation of a lat/lon point, in meters.
     * 
     * @return elevation in meters.
     * @param lat in decimal degrees.
     * @param lon in decimal degrees.
     * @param level the dted level.
     */
    public int getElevation(float lat, float lon, int level) {
        String dtedFileName = findFileName((double) lat, (double) lon, level);

        if (dtedFileName != null) {
            DTEDFrame frame = (DTEDFrame) get(dtedFileName);

            if (frame != null) {
                return (int) frame.elevationAt(lat, lon);
            }
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

        return getElevations((float) ul.getY(),
                (float) ul.getX(),
                (float) lr.getY(),
                (float) lr.getX(),
                dtedLevel);
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
    public short[][] getElevations(float ullat, float ullon, float lrlat,
                                   float lrlon, int dtedLevel) {
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
    protected short[][] getElevations(float ullat, float ullon, float lrlat,
                                      float lrlon, int dtedLevel,
                                      DTEDFrame refFrame) {

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
        DTEDFrame frame = null;
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

                DTEDFrame thisFrame = get(lowerlat, lowerlon, dtedLevel);

                if (thisFrame != null) {
                    // System.out.println("Getting elev for " +
                    // upperlat + ", " +
                    // lowerlon + ", " +
                    // lowerlat+ ", " + upperlon);
                    es[x][y] = thisFrame.getElevations(upperlat,
                            lowerlon,
                            lowerlat,
                            upperlon);
                    xLengths[x] = es[x][y].length;
                    yLengths[y] = es[x][y][0].length;
                    frame = thisFrame;
                } else {
                    if (refFrame != null) {
                        Debug.output("DTEDFrameCache: Missing frames, going to use reference frame");
                        // calculate these lengths, since the refFrame
                        // was set...
                        int[] indexes = refFrame.getIndexesFromLatLons(upperlat,
                                lowerlon,
                                lowerlat,
                                upperlon);
                        xLengths[x] = indexes[2] - indexes[0] + 1;
                        yLengths[y] = indexes[3] - indexes[1] + 1;

                    } else {
                        if (frame != null) {
                            // Well, we have a frame to do
                            // calculations on, and we know we need
                            // to do at least one calculation, so
                            // might as well go and do this right...
                            return getElevations(ullat,
                                    ullon,
                                    lrlat,
                                    lrlon,
                                    dtedLevel,
                                    frame);
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
                        System.arraycopy(es[x][y][i],
                                0,
                                matrix[i + xspacer],
                                yspacer,
                                es[x][y][i].length);
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

        // Space-separated list of marker names for different
        // DTEDDirectoryHandlers
        Vector directoryHandlerList = PropUtils.parseSpacedMarkers(props.getProperty(prefix
                + DTEDDirectoryHandlerProperty));

        for (Iterator it = directoryHandlerList.iterator(); it.hasNext();) {
            String handlerPrefix = (String) it.next();
            DTEDDirectoryHandler handler = new DTEDDirectoryHandler();
            handler.setProperties(prefix + handlerPrefix, props);
            addDTEDDirectoryHandler(handler);
        }

        resetCache(PropUtils.intFromProperties(props, prefix
                + DTEDFrameCacheSizeProperty, DEFAULT_CACHE_SIZE));
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + DTEDFrameCacheSizeProperty,
                Integer.toString(getCacheSize()));

        // Directory handler properties...
        if (directories != null) {
            StringBuffer dhPrefixes = new StringBuffer();

            for (Iterator it = directories.iterator(); it.hasNext();) {
                DTEDDirectoryHandler ddh = (DTEDDirectoryHandler) it.next();
                String dhPrefix = ddh.getPropertyPrefix();
                if (dhPrefix != null) {
                    int index = dhPrefix.indexOf(prefix);
                    if (index != -1) {
                        dhPrefixes.append(dhPrefix.substring(index
                                + prefix.length())).append(" ");
                    }
                    ddh.getProperties(props);
                }
            }
            props.put(prefix + DTEDDirectoryHandlerProperty,
                    dhPrefixes.toString());
        }

        return props;
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        props.put(DTEDFrameCacheSizeProperty, "Size of the frame cache");

        // Not sure how to handle setting up a DTEDDirectoryHandler
        // yet.

        return props;
    }

    public static void main(String[] args) {
        Debug.init();
        if (args.length < 1) {
            Debug.output("DTEDFrameCache:  Need a path/filename");
            System.exit(0);
        }

        Debug.output("DTEDFrameCache: " + args[0]);
        DTEDFrameCache dfc = new DTEDFrameCache(10);

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
