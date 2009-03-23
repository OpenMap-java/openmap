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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/rpf/corba/CRFPServer.java,v $
// $RCSfile: CRFPServer.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/11 19:30:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf.corba;

import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Timer;

import com.bbn.openmap.image.JPEGHelper;
import com.bbn.openmap.layer.rpf.RpfCacheHandler;
import com.bbn.openmap.layer.rpf.RpfColortable;
import com.bbn.openmap.layer.rpf.RpfCoverageBox;
import com.bbn.openmap.layer.rpf.RpfFrameCacheHandler;
import com.bbn.openmap.layer.rpf.RpfIndexedImageData;
import com.bbn.openmap.layer.rpf.RpfSubframe;
import com.bbn.openmap.layer.rpf.RpfTocHandler;
import com.bbn.openmap.layer.rpf.RpfViewAttributes;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPCADRGProjection;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPCoverageBox;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPViewAttributes;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.RawImage;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.ServerPOA;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.XYPoint;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.corba.CORBASupport;

/**
 * The CRFPServer is a server implementation of the
 * CorbaRpfFrameProvider.idl. It realy implements most of the fuctions
 * of the RpfFrameProvider, but is not one. The CRFPClient is the
 * RpfFrameProvider.
 * 
 * <P>
 * This server requires the com.sun.image.codec.jpeg package.
 */
public class CRFPServer extends ServerPOA implements ActionListener {

    protected static String iorfile = null;
    protected static String naming = null;
    /** A cache for every client. */
    Hashtable caches;
    /** View Attributes for every client. */
    Hashtable viewAttributeLists;
    /** The cache for the current client. */
    protected RpfFrameCacheHandler currentCache;
    /** The view attrbutes for the current client. */
    protected RpfViewAttributes currentViewAttributes;
    /** The paths to the RPF directories. */
    protected String[] rpfpaths;
    /** The Rpf Table of Contents handlers for the data. */
    protected RpfTocHandler[] tocs;
    /** Hashtable to keep track of how old certain caches are. */
    Hashtable timestamps;
    /**
     * Timer for clearing out caches from sloppy clients. It's only
     * enabled when the -timewindow flag is used.
     */
    Timer timer;
    /** 10, or the default number of active caches kept. */
    public final static int DEFAULT_MAX_USERS = 10;
    /** The number of caches kept by the server. */
    protected int maxUsers = DEFAULT_MAX_USERS;
    /** 5 minutes. The default timer cycle. */
    public final static int DEFAULT_TIME_WINDOW = 1000 * 60 * 5; // 5
                                                                 // minutes
    /**
     * The amount of time (milliseconds) reflecting how long an
     * inactive cache is kept
     */
    protected long timeWindow = DEFAULT_TIME_WINDOW;

    /**
     * Default Constructor.
     */
    public CRFPServer() {
        this("Default");
    }

    /**
     * The constructor that you should use.
     * 
     * @param name the identifying name for persistance.
     */
    public CRFPServer(String name) {
        super();
        caches = new Hashtable();
        viewAttributeLists = new Hashtable();
        timestamps = new Hashtable();
    }

    /**
     * Get the current cache given a unique ID. If a cache is not
     * here, create it.
     * 
     * @param uniqueID a unique identifier.
     */
    protected RpfFrameCacheHandler getCurrentCache(String uniqueID) {
        RpfFrameCacheHandler cache = (RpfFrameCacheHandler) caches.get(uniqueID);
        if (cache == null && tocs != null) {
            Debug.message("crfp", "CRFPServer: Creating cache for new client");
            cache = new RpfFrameCacheHandler(tocs);
            caches.put(uniqueID, cache);
        }

        timestamps.put(uniqueID, new Long(System.currentTimeMillis()));
        return cache;
    }

    /**
     * Get rid of any cache that is older than the time window.
     */
    protected void cleanCache(long timeWindow) {
        // OK, we need to get rid of one.
        long currentTime = System.currentTimeMillis();
        Enumeration keys = timestamps.keys();

        while (keys.hasMoreElements()) {
            Object tester = keys.nextElement();
            Long time = (Long) timestamps.get(tester);

            if ((currentTime - time.longValue()) >= timeWindow) {
                caches.remove(tester);
                timestamps.remove(tester);
                viewAttributeLists.remove(tester);

                if (Debug.debugging("crfp")) {
                    Debug.output("Expired cache, removing, have "
                            + caches.size() + " caches left.");
                }
            }
        }
    }

    /**
     * Create a spot in the cache for a new entry. If something is
     * removed from the cache, it is returned here.
     */
    protected RpfCacheHandler sweepCaches() {
        if (caches.size() < maxUsers) {
            return null;
        }

        // OK, we need to get rid of one.
        long diff = Long.MAX_VALUE;
        Enumeration keys = timestamps.keys();
        Object getRid = null;

        while (keys.hasMoreElements()) {
            Object tester = keys.nextElement();
            Long time = (Long) timestamps.get(tester);

            if (time.longValue() < diff) {
                getRid = tester;
                diff = time.longValue();
            }
        }
        boolean DEBUG = false;
        if (getRid != null) {
            if (Debug.debugging("crfp")) {
                DEBUG = true;
            }
            if (DEBUG)
                Debug.output("Removing cache for new user, was "
                        + caches.size());

            caches.remove(getRid);
            timestamps.remove(getRid);
            viewAttributeLists.remove(getRid);

            if (DEBUG)
                Debug.output("  now " + caches.size());
        }

        if (caches.size() >= maxUsers) {
            return sweepCaches();
        } else {
            return (RpfCacheHandler) getRid;
        }
    }

    /**
     * Get the current view attributes given a unique ID. If view
     * attributes are not here, create them.
     * 
     * @param uniqueID a client-unique identifier.
     */
    protected RpfViewAttributes getCurrentViewAttributes(String uniqueID) {
        RpfViewAttributes va = (RpfViewAttributes) viewAttributeLists.get(uniqueID);
        if (va == null) {
            Debug.message("crfp",
                    "CRFPServer: Creating attributes for new client");
            va = new RpfViewAttributes();
            viewAttributeLists.put(uniqueID, va);
        }
        return va;
    }

    /**
     * Set the view attributtes for the current client.
     * 
     * @param va the view attribute settings.
     * @param uniqueID a client-unique identifier.
     */
    public void setViewAttributes(CRFPViewAttributes va, String uniqueID) {

        currentViewAttributes = getCurrentViewAttributes(uniqueID);
        currentViewAttributes.numberOfColors = (int) va.numberOfColors;
        currentViewAttributes.opaqueness = (int) va.opaqueness;
        currentViewAttributes.scaleImages = va.scaleImages;
        currentViewAttributes.imageScaleFactor = va.imageScaleFactor;
        currentViewAttributes.chartSeries = va.chartSeries;

        if (Debug.debugging("crfp")) {
            Debug.output("CRFPServer: Setting attributes for client:\n    "
                    + currentViewAttributes);
        }
    }

    /**
     * Get the Coverage Boxes that fit the geographical area given.
     * 
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     * @param uniqueID a client-unique identifier.
     */
    public CRFPCoverageBox[] getCoverage(float ullat, float ullon, float lrlat,
                                         float lrlon, CRFPCADRGProjection p,
                                         String uniqueID) {

        Debug.message("crfp",
                "CRFPServer: Handling coverage request for client");

        currentCache = getCurrentCache(uniqueID);
        currentViewAttributes = getCurrentViewAttributes(uniqueID);
        currentCache.setViewAttributes(currentViewAttributes);

        LatLonPoint llpoint = new LatLonPoint.Double(p.center.lat, p.center.lon);
        CADRG proj = new CADRG(llpoint, p.scale, p.width, p.height);

        Vector vector = currentCache.getCoverage(ullat,
                ullon,
                lrlat,
                lrlon,
                proj);

        return vectorToCRFPCoverageBoxes(vector);
    }

    /**
     * Method that provides all the coverage boxes that could provide
     * coverage over the given area.
     * 
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     * @param uniqueID a client-unique identifier.
     */
    public CRFPCoverageBox[] getCatalogCoverage(float ullat, float ullon,
                                                float lrlat, float lrlon,
                                                CRFPCADRGProjection p,
                                                String chartSeriesCode,
                                                String uniqueID) {

        Debug.message("crfp", "CRFPServer: handling catalog request for client");
        currentCache = getCurrentCache(uniqueID);
        currentViewAttributes = getCurrentViewAttributes(uniqueID);
        currentCache.setViewAttributes(currentViewAttributes);

        LatLonPoint llpoint = new LatLonPoint.Float(p.center.lat, p.center.lon);
        CADRG proj = new CADRG(llpoint, p.scale, p.width, p.height);
        Vector vector = currentCache.getCatalogCoverage(ullat,
                ullon,
                lrlat,
                lrlon,
                proj,
                chartSeriesCode);
        return vectorToCRFPCoverageBoxes(vector);
    }

    /**
     * Convert a Vector of RpfCoverageBox to a CRFPCoverageBox array.
     * 
     * @param vector vector of RpfCoverageBox.
     * @return array of CRFPCoverageBox.
     */
    protected CRFPCoverageBox[] vectorToCRFPCoverageBoxes(Vector vector) {
        int size = vector.size();
        CRFPCoverageBox[] rets = new CRFPCoverageBox[size];

        for (int i = 0; i < size; i++) {
            RpfCoverageBox box = (RpfCoverageBox) vector.elementAt(i);
            if (box != null) {
                rets[i] = new CRFPCoverageBox((float) box.nw_lat, (float) box.nw_lon, (float) box.se_lat, (float) box.se_lon, box.subframeLatInterval, box.subframeLonInterval, box.chartCode, (short) box.zone, new XYPoint((short) box.startIndexes.x, (short) box.startIndexes.y), new XYPoint((short) box.endIndexes.x, (short) box.endIndexes.y), (short) box.tocNumber, (short) box.entryNumber, box.scale, box.percentCoverage);
            }
        }
        return rets;
    }

    /**
     * Retrieve the subframe data from the frame cache, decompress it,
     * and convert it to a JPEG image.
     * 
     * @param tocNumber the number of the RpfTocHandler for the
     *        currentCache to use.
     * @param entryNumber the coverage box index that contains the
     *        subframe.
     * @param x the horizontal location of the subframe. The
     *        RpfCacheHandler figures this out.
     * @param y the vertical location of the subframe. The
     *        RpfCacheHandler figures this out.
     * @param jpegQuality the compression parameter for the image.
     * @param uniqueID a client-unique identifier.
     * @return byte[] of jpeg image
     */
    public byte[] getSubframeData(short tocNumber, short entryNumber, short x,
                                  short y, float jpegQuality, String uniqueID) {

        Debug.message("crfpdetail",
                "CRFPServer: handling subframe request for client");

        try {
            currentCache = getCurrentCache(uniqueID);

            int[] pixels = currentCache.getSubframeData((int) tocNumber,
                    (int) entryNumber,
                    (int) x,
                    (int) y);
            if (pixels != null) {
                byte[] compressed = null;
                try {
                    compressed = JPEGHelper.encodeJPEG(RpfSubframe.PIXEL_EDGE_SIZE,
                            RpfSubframe.PIXEL_EDGE_SIZE,
                            pixels,
                            jpegQuality);
                } catch (Exception e) {
                    Debug.error("CRFPServer: JPEG Compression error: " + e);
                    compressed = new byte[0];
                }
                if (Debug.debugging("crfpdetail")) {
                    Debug.output("CRFPServer: subframe is " + compressed.length
                            + " bytes");
                }
                return compressed;
            }
        } catch (OutOfMemoryError oome) {
            handleMemoryShortage();
        }

        return new byte[0];
    }

    public RawImage getRawSubframeData(short tocNumber, short entryNumber,
                                       short x, short y, String uniqueID) {

        Debug.message("crfpdetail",
                "CRFPServer: handling raw subframe request for client");

        RawImage ri = new RawImage();
        RpfIndexedImageData riid = null;

        try {
            currentCache = getCurrentCache(uniqueID);

            riid = currentCache.getRawSubframeData((int) tocNumber,
                    (int) entryNumber,
                    (int) x,
                    (int) y);
        } catch (OutOfMemoryError oome) {
            handleMemoryShortage();
            riid = null;
        }

        if (riid == null || riid.imageData == null) {
            Debug.message("crfpdetail", "CRFPServer: null image data");
            ri.imagedata = new byte[0];
            ri.colortable = new int[0];
        } else {
            ri.imagedata = riid.imageData;

            RpfColortable colortable = currentCache.getColortable();

            ri.colortable = new int[colortable.colors.length];
            for (int i = 0; i < colortable.colors.length; i++) {
                ri.colortable[i] = colortable.colors[i].getRGB();
            }
            Debug.message("crfpdetail", "CRFPServer: GOOD image data");
        }
        return ri;
    }

    /**
     * Get the subframe attributes for the identified subframe.
     * Provided as a single string, with newline characters separating
     * features.
     * 
     * @param tocNumber the number of the RpfTocHandler for the
     *        currentCache to use.
     * @param entryNumber the coverage box index that contains the
     *        subframe.
     * @param x the horizontal location of the subframe. The
     *        RpfCacheHandler figures this out.
     * @param y the vertical location of the subframe. The
     *        RpfCacheHandler figures this out.
     * @param uniqueID a client-unique identifier.
     * @return String with the subframe attributes.
     */
    public String getSubframeAttributes(short tocNumber, short entryNumber,
                                        short x, short y, String uniqueID) {
        Debug.message("crfpdetail",
                "CRFPServer: handling subframe attribute request for client");

        try {
            currentCache = getCurrentCache(uniqueID);
            return currentCache.getSubframeAttributes((int) tocNumber,
                    (int) entryNumber,
                    (int) x,
                    (int) y);
        } catch (OutOfMemoryError oome) {
            handleMemoryShortage();
        }
        return new String();
    }

    /**
     * The signoff function lets the server know that a client is
     * checking out.
     * 
     * @param uniqueID a client-unique identifier.
     */
    public void signoff(String uniqueID) {
        Debug.message("crfp", "CRFPServer: Client" + uniqueID + " signing off!");
        caches.remove(uniqueID);
        viewAttributeLists.remove(uniqueID);
        timestamps.remove(uniqueID);
    }

    protected void handleMemoryShortage() {
        Debug.error("CRFPServer out of memory! Dumping all caches!");
        caches.clear();
        viewAttributeLists.clear();
        timestamps.clear();
    }

    /**
     * Start the server.
     * 
     * @param args command line arguments.
     */
    public void start(String[] args) {
        CORBASupport cs = new CORBASupport();

        if (args != null) {
            parseArgs(args);
        }

        cs.start(this, args, iorfile, naming);
    }

    /**
     * Set the maximum number of caches to given number, represented
     * in a string. If the string isn't a good number,
     * DEFAULT_MAX_USERS will be used.
     */
    public void setMaxUsers(String number) {
        try {
            setMaxUsers(Integer.parseInt(number));
        } catch (NumberFormatException nfe) {
            setMaxUsers(DEFAULT_MAX_USERS);
        }
    }

    /**
     * Set the maximum number of caches to given number. If the number
     * isn't a good, DEFAULT_MAX_USERS will be used.
     */
    public void setMaxUsers(int number) {
        if (number >= 1) {
            maxUsers = number;
        } else {
            Debug.output("Max users of " + number + " not supported, set to "
                    + DEFAULT_MAX_USERS);
            maxUsers = DEFAULT_MAX_USERS;
        }
    }

    /**
     * Get the maximum number of caches allowed in the server. One per
     * user. Get it?
     */
    public int getMaxUsers() {
        return maxUsers;
    }

    /**
     * Set how long a user's cache will be kept around.
     */
    public void setTimeWindow(String number) {
        try {
            setTimeWindow(Long.parseLong(number));
        } catch (NumberFormatException nfe) {
            setTimeWindow(DEFAULT_TIME_WINDOW);
        }
    }

    /**
     * Set how long a user's cache will be kept around.
     */
    public void setTimeWindow(long number) {
        if (timer == null) {
            timer = new javax.swing.Timer((int) number, (ActionListener) this);
        }

        if (number >= 1) {
            timeWindow = number;
            Debug.output("Timer enabled,  set to " + (number / 1000)
                    + " seconds");
        } else if (number == 0) {
            // stop timer
            timer.stop();
            return;
        } else {
            timeWindow = DEFAULT_TIME_WINDOW;
            Debug.output("Timer enabled,  set to "
                    + (DEFAULT_TIME_WINDOW / 1000) + " seconds");
        }

        timer.start();
    }

    /**
     * The the time window for how long users caches are kept around.
     */
    public long getTimeWindow() {
        return timeWindow;
    }

    /**
     * Handle an ActionEvent from the Timer.
     * 
     * @param ae action event from the timer.
     */
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        if (Debug.debugging("crfp")) {
            Debug.output("Ping! checking cache...");
        }
        cleanCache(getTimeWindow());
    }

    /**
     */
    public void parseArgs(String[] args) {
        rpfpaths = null;

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-ior")) {
                    iorfile = args[++i];
                } else if (args[i].equalsIgnoreCase("-name")) {
                    naming = args[++i];
                } else if (args[i].equalsIgnoreCase("-help")) {
                    printHelp();
                } else if (args[i].equalsIgnoreCase("-rpfpaths")) {
                    rpfpaths = getPaths(args[++i]);
                } else if (args[i].equalsIgnoreCase("-maxusers")) {
                    setMaxUsers(args[++i]);
                } else if (args[i].equalsIgnoreCase("-timewindow")) {
                    setTimeWindow(args[++i]);
                } else if (args[i].equalsIgnoreCase("-verbose")) {
                    Debug.put("crfp");
                } else if (args[i].equalsIgnoreCase("-h")) {
                    printHelp();
                }
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            printHelp();
        }

        // if you didn't specify an iorfile
        if (iorfile == null && naming == null) {
            Debug.error("CRFPServer: IOR file and name service name are null!  Use `-ior' or '-name' flag!");
            System.exit(-1);
        }

        if (rpfpaths == null) {
            Debug.error("CRFPServer: No RPF directory paths specified!  Use `-rpfpaths' flag!");
            System.exit(-1);
        } else {
            tocs = RpfFrameCacheHandler.createTocHandlers(rpfpaths);
            Debug.output("CRFPServer: CRFPServer!  Running with paths => ");
            for (int j = 0; j < rpfpaths.length; j++) {
                Debug.output("     " + rpfpaths[j]);
            }
        }
    }

    private String[] getPaths(String str) {
        StringTokenizer tok = new StringTokenizer(str, ";");
        int len = tok.countTokens();
        String[] paths = new String[len];
        for (int j = 0; j < len; j++) {
            paths[j] = tok.nextToken();
        }
        return paths;
    }

    /**
     * <b>printHelp </b> should print a usage statement which reflects
     * the command line needs of your specialist.
     */
    public void printHelp() {
        Debug.output("usage: java CRFPServer [-ior <file> || -name <NAME>] -rpfpaths \"<path to rpf dir>;<path to rpf dir>;<...>\" -maxusers <max number of users to cache> -timewindow <milliseconds for idle cache removal>");
        System.exit(1);
    }

    public static void main(String[] args) {
        Debug.init(System.getProperties());

        // Create the specialist server
        CRFPServer srv = new CRFPServer("CRFPServer");
        srv.start(args);
    }

}