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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/TileDirectory.java,v $
// $RCSfile: TileDirectory.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

/**
 * This class maps latitudes and longitudes to a particular tile
 * directory.
 */
public class TileDirectory {

    /** the name of the subdirectory */
    final private String tilename;
    /** the boundaries */
    final private float westlon, southlat, eastlon, northlat;
    /** the tile ID of this object */
    final private int tileID;

    /**
     * Construct a TileDirectory with a path and boundaries.
     * 
     * @param path the directory path
     * @param n the northern boundary
     * @param s the southern boundary
     * @param e the eastern boundary
     * @param w the western boundary
     * @param tileID our tile identifier
     */
    public TileDirectory(String path, int tileID, float n, float s, float e,
            float w) {
        StringBuffer strbuf = new StringBuffer(path.toLowerCase());
        //      strbuf.append(File.separator);
        strbuf.append("/"); // in Java land with the BinaryFile.
        tilename = strbuf.toString().intern();
        this.tileID = tileID;
        if (e < w) {
            e += 360.0;
        }
        westlon = w;
        eastlon = e;
        northlat = n;
        southlat = s;
    }

    /**
     * Construct an untiled TileDirectory. Since this object does not
     * have valid boundaries, it is an error to call inRegion on it
     * 
     * @see #inRegion(float, float, float, float)
     */
    public TileDirectory() {
        tilename = "";
        this.tileID = -1;
        westlon = eastlon = northlat = southlat = Float.NaN;
    }

    /**
     * Return the path for this tile
     * 
     * @return a string path
     */
    public String getPath() {
        return tilename;
    }

    /**
     * Return the tile identifier for this tile
     * 
     * @return the tile id (-1 for untiled coverage)
     */
    public int getTileID() {
        return tileID;
    }

    /**
     * Return a string describing ourselves
     * 
     * @return a string usable as a directory path component
     */
    public String toString() {
        return (tilename);
    }

    /**
     * Figure out if our region overlaps the passed in region
     * 
     * @return <code>true</code> if the regions overlap
     * @param n the northern boundary
     * @param s the southern boundary
     * @param e the eastern boundary
     * @param w the western boundary
     */
    public boolean inRegion(float n, float s, float e, float w) {
        // take care of the easy case first...
        if ((s > northlat) || (n < southlat)) {
            return false;
        }
        if (e < w) {
            e += 360.0f;
        }
        if ((w > eastlon) || (e < westlon)) {
            return false;
        }
        return true;
    }
}