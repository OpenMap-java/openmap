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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/DataOrganizer.java,v $
// $RCSfile: DataOrganizer.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.util.Vector;

/**
 * The DataOrganizer lets you organize objects in a way that will let
 * you retrieve them based on a geographic query. It's an interface
 * that lets you decide, with an implementation, the best way to
 * manage and retrieve your data.
 */

public interface DataOrganizer {

    /**
     * Add a object into the organizer at a location.
     * 
     * @param lat up-down location (latitude, y)
     * @param lon left-right location (longitude, x)
     * @return true if the insertion worked.
     */
    public boolean put(float lat, float lon, Object obj);

    /**
     * Remove a object out of the organizer at a location.
     * 
     * @param lat up-down location (latitude, y)
     * @param lon left-right location (longitude, x)
     * @return the object removed, null if the object not found.
     */
    public Object remove(float lat, float lon, Object obj);

    /** Clear the organizer. */
    public void clear();

    /**
     * Find an object closest to a lat/lon.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that is closest to the lat/lon.
     */
    public Object get(float lat, float lon);

    /**
     * Find an object closest to a lat/lon, within a given maximum.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance maximum distance to have a hit.
     * @return the object that is closest to the lat/lon, within the
     *         given distance.
     */
    public Object get(float lat, float lon, double withinDistance);

    /**
     * Find all the objects within a bounding box.
     * 
     * @param north top location in QuadTree Grid (latitude, y)
     * @param west left location in QuadTree Grid (longitude, x)
     * @param south lower location in QuadTree Grid (latitude, y)
     * @param east right location in QuadTree Grid (longitude, x)
     * @return Vector of objects.
     */
    public Vector get(float north, float west, float south, float east);

}