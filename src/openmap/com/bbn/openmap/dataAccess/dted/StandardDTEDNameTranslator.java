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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/StandardDTEDNameTranslator.java,v $
// $RCSfile: StandardDTEDNameTranslator.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/11 20:39:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/**
 * A class implementing the DTEDNameTranslator interface that handles
 * DTED file names as specified in the DTED specification. These file
 * names follow this naming convention:
 * <P>
 * 
 * <pre>
 * 
 * 
 *  dted/[e|w]xxx/[n|s]yy.dt[level]
 * 
 *  
 * </pre>
 */
public class StandardDTEDNameTranslator implements DTEDNameTranslator {
    protected boolean DEBUG = Debug.debugging("dtedfile");

    protected String filename = null;
    protected String subDirs = null;
    protected String dtedDir = "";
    protected double lat;
    protected double lon;
    protected int level;

    /**
     * Constructor for object to use for multiple translations.
     */
    public StandardDTEDNameTranslator() {}

    /**
     * Set the internal parameters given a parent DTED directory,
     * latitude, longitude and level. getName can be called to fetch
     * a file name for these parameters.
     * 
     * @param dtedDir the path to the top DTED directory.
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public StandardDTEDNameTranslator(String dtedDir, double lat, double lon,
            int level) {
        set(dtedDir, lat, lon, level);
    }

    /**
     * Assuming that the DTED directory will be set, set the other
     * parameters to reveal a path to a file name, which would be
     * retrieved by calling getName().
     * 
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public StandardDTEDNameTranslator(double lat, double lon, int level) {
        set(null, lat, lon, level);
    }

    /**
     * Set the internal parameters based on the given file path.
     * 
     * @param filePath a path to a dted frame file.
     */
    public void set(String filePath) throws FormatException {
        evaluate(filePath);
    }

    /**
     * Set the internal parameters given a parent DTED directory,
     * latitude, longitude and level. getName can be called to fetch
     * a file name for these parameters.
     * 
     * @param dtedDir the path to the top DTED directory.
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public void set(String dtedDir, double lat, double lon, int level) {
        setDTEDDir(dtedDir);
        filename = evaluate(lat, lon, level);
    }

    /**
     * Assuming that the DTED directory is set, set the other
     * parameters to reveal a path to a file name, which would be
     * retrieved by calling getName().
     * 
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public void set(double lat, double lon, int level) {
        set(getDTEDDir(), lat, lon, level);
    }

    /**
     * Set the latitude in the translator.
     * 
     * @param latitude the desired latitude, in decimal degrees.
     */
    public void setLat(double latitude) {
        filename = evaluate(latitude, getLon(), getLevel());
    }

    /**
     * Get the latitude.
     */
    public double getLat() {
        return lat;
    }

    /**
     * Set the longitude in the translator.
     * 
     * @param longitude the desired longitude, in decimal degrees.
     */
    public void setLon(double longitude) {
        filename = evaluate(getLat(), longitude, getLevel());
    }

    /**
     * Get the longitude.
     */
    public double getLon() {
        return lon;
    }

    /**
     * Set the DTED level in the translator.
     * 
     * @param level the DTED level (0, 1, 2).
     */
    public void setLevel(int level) {
        filename = evaluate(getLat(), getLon(), level);
    }

    /**
     * Get the DTED level set in the translator.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the relative part of the path to the DTED frame file, from
     * just under the top-level dted directory to just above the frame
     * file.
     */
    public String getSubDirs() {
        return subDirs;
    }

    /**
     * Get the file name.
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Get the complete path to the DTED frame file based on the
     * current internal settings.
     */
    public String getName() {
        return dtedDir + "/" + filename;
    }

    /**
     * Configure the internal settings based on a path to a DTED frame
     * file.
     */
    public void setName(String fileName) throws FormatException {
        evaluate(fileName);
    }

    /**
     * Set the top-level DTED directory.
     */
    public void setDTEDDir(String dtedDirectory) {
        if (dtedDirectory == null) {
            dtedDir = "";
        }
        dtedDir = dtedDirectory;
    }

    /**
     * Get the path to the top-level DTED directory.
     */
    public String getDTEDDir() {
        return dtedDir;
    }

    /**
     * Update all the other settings based on these.
     */
    protected String evaluate(double lat, double lon, int level) {
        this.lat = Math.floor(lat);
        this.lon = Math.floor(lon);
        this.level = level;
        return DTEDFrameUtil.lonToFileString((float) lon) + "/"
                + DTEDFrameUtil.latToFileString((float) lat, level);
    }

    /**
     * Update all the other settings based on these.
     */
    protected void evaluate(String filePath) throws FormatException {

        try {
            int latSlash = filePath.lastIndexOf("/");
            if (latSlash > 1) {
                if (DEBUG) {
                    Debug.output("Have lat index of " + latSlash);
                }
                String lonSearch = filePath.substring(0, latSlash);

                if (DEBUG) {
                    Debug.output("Searching for lon index in " + lonSearch);
                }
                int lonSlash = lonSearch.lastIndexOf("/");
                if (lonSlash > 1) {
                    filename = filePath.substring(latSlash + 1);
                    String latString = filename.toUpperCase();

                    if (DEBUG) {
                        Debug.output("have lat " + latString);
                    }

                    int dotIndex = latString.indexOf(".");
                    if (dotIndex > 0) {

                        lat = Double.parseDouble(latString.substring(1,
                                dotIndex));
                        if (latString.charAt(0) == 'S') {
                            lat *= -1;
                        }

                        subDirs = filePath.substring(lonSlash + 1, latSlash);
                        String dd = filePath.substring(0, lonSlash + 1);
                        if (dd.length() > 0) {
                            dtedDir = dd;
                        }

                        String lonString = subDirs.toUpperCase();

                        if (DEBUG) {
                            Debug.output("have lon " + lonString);
                        }

                        lon = Double.parseDouble(lonString.substring(1));
                        if (lonString.charAt(0) == 'W') {
                            lon *= -1;
                        }

                        level = (int) Integer.parseInt(filePath.substring(filePath.length() - 1));
                        if (DEBUG) {
                            Debug.output("have level " + level);
                        }
                        return;
                    }
                }
            }
        } catch (NumberFormatException nfe) {

        }

        throw new FormatException("StandardDTEDNameTranslator couldn't convert "
                + filePath + " to valid parameters");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("For file: ").append(filename).append("\n");
        sb.append("  lat = ").append(lat).append("\n");
        sb.append("  lon = ").append(lon).append("\n");
        sb.append("  level = ").append(level).append("\n");
        String dd = getDTEDDir();
        if (dd.length() > 0) {
            dd += "/";
        }
        sb.append("  path = ").append(dd).append(getSubDirs())
                .append("/").append(getName()).append("\n");
        return sb.toString();
    }

    public static void main(String[] argv) {
        Debug.init();
        if (argv.length == 0) {
            Debug.output("Usage:  StandardDTEDNameTranslator <dted file path>");
            System.exit(0);
        }

        StandardDTEDNameTranslator sdnt = new StandardDTEDNameTranslator();
        try {
            sdnt.set(argv[0]);
            Debug.output(sdnt.toString());
        } catch (FormatException fe) {
            Debug.output(fe.getMessage());
        }
    }

}