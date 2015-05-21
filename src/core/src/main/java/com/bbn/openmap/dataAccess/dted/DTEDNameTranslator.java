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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDNameTranslator.java,v $
// $RCSfile: DTEDNameTranslator.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/11 20:39:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import com.bbn.openmap.io.FormatException;

/**
 * An interface that defines a object that knows how to define the
 * name of a DTED file from a set of coordinates and dted level, and
 * also knows how to translate the name of a DTED file to it's
 * coverage coordinates and level. Keeps track of the file name, the
 * sub-directory path from the top level dted directory to the file,
 * and the location of the dted directory if it is specified. It's
 * intended that a single DTEDNameTranslator be used for a single DTED
 * directory containing a set of directories and files. For objects
 * that want to manage a set of DTED directories, a set of
 * DTEDNameTranslators are needed. This is required to allow the
 * translator the ability to take a lat/lon and provide a String
 * representation of what a file would be called if it were stored
 * under the specified DTED directory. The translator makes no
 * guarantees on whether the file actually exists.
 */
public interface DTEDNameTranslator {

    /**
     * Set the internal parameters based on the given file path.
     * 
     * @param filePath a path to a dted frame file.
     */
    public void set(String filePath) throws FormatException;

    /**
     * Sets the internal parameters given a parent DTED directory,
     * latitude, longitude and level. getName can be called to fetch
     * a file name for these parameters.
     * 
     * @param dtedDir the path to the top DTED directory.
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public void set(String dtedDir, double lat, double lon, int level);

    /**
     * Assuming that the DTED directory is set, set the other
     * parameters to reveal a path to a file name, which would be
     * retrieved by calling getName().
     * 
     * @param lat the desired latitude, in decimal degrees.
     * @param lon the desired longitude, in decimal degrees.
     * @param level the DTED level (0, 1, 2).
     */
    public void set(double lat, double lon, int level);

    /**
     * Set the latitude in the translator.
     * 
     * @param lat the desired latitude, in decimal degrees.
     */
    public void setLat(double lat);

    /**
     * Get the latitude.
     */
    public double getLat();

    /**
     * Set the longitude in the translator.
     * 
     * @param lon the desired longitude, in decimal degrees.
     */
    public void setLon(double lon);

    /**
     * Get the longitude.
     */
    public double getLon();

    /**
     * Set the DTED level in the translator.
     * 
     * @param level the DTED level (0, 1, 2).
     */
    public void setLevel(int level);

    /**
     * Get the DTED level set in the translator.
     */
    public int getLevel();

    /**
     * Get the complete path to the DTED frame file based on the
     * current internal settings.
     */
    public String getName();

    /**
     * Configure the internal settings based on a path to a DTED frame
     * file.
     */
    public void setName(String fileName) throws FormatException;

    /**
     * Get the relative part of the path to the DTED frame file, from
     * just under the top-level dted directory to just above the frame
     * file.
     */
    public String getSubDirs();

    /**
     * Get the file name.
     */
    public String getFileName();

    /**
     * Set the top-level DTED directory.
     */
    public void setDTEDDir(String dtedDirectory);

    /**
     * Get the path to the top-level DTED directory.
     */
    public String getDTEDDir();
}