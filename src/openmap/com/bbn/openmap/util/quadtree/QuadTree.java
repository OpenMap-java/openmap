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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTree.java,v $
// $RCSfile: QuadTree.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.quadtree;

import java.io.Serializable;
import java.util.Vector;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.DataOrganizer;

/**
 * The QuadTree lets you organize objects in a grid, that redefines itself and
 * focuses more gridding when more objects appear in a certain area.
 */
public class QuadTree implements DataOrganizer, Serializable {

    static final long serialVersionUID = -7707825592455579873L;

    protected QuadTreeNode top;

    public QuadTree() {
        this(90.0f, -180.0f, -90.0f, 180.0f, 20, QuadTreeNode.NO_MIN_SIZE);
    }

    public QuadTree(float north, float west, float south, float east,
            int maxItems) {
        this(north, west, south, east, maxItems, QuadTreeNode.NO_MIN_SIZE);
    }

    public QuadTree(int up, int left, int down, int right, int maxItems) {
        this((float) up,
             (float) left,
             (float) down,
             (float) right,
             maxItems,
             QuadTreeNode.DEFAULT_MIN_SIZE);
    }

    public QuadTree(float north, float west, float south, float east,
            int maxItems, float minSize) {
        top = new QuadTreeNode(north, west, south, east, maxItems, minSize);
    }

    /**
     * Add a object into the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return true if the insertion worked.
     */
    public boolean put(float lat, float lon, Object obj) {
        return top.put(lat, lon, obj);
    }

    /**
     * Remove a object out of the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object removed, null if the object not found.
     */
    public Object remove(float lat, float lon, Object obj) {
        return top.remove(lat, lon, obj);
    }

    /** Clear the tree. */
    public void clear() {
        top.clear();
    }

    /**
     * Get an object closest to a lat/lon.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that was found.
     */
    public Object get(float lat, float lon) {
        return top.get(lat, lon);
    }

    /**
     * Get an object closest to a lat/lon, within a maximum distance.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance the maximum distance to get a hit, in decimal
     *        degrees.
     * @return the object that was found, null if nothing is within the maximum
     *         distance.
     */
    public Object get(float lat, float lon, double withinDistance) {
        return top.get(lat, lon, withinDistance);
    }

    /**
     * Get all the objects within a bounding box.
     * 
     * @param north top location in QuadTree Grid (latitude, y)
     * @param west left location in QuadTree Grid (longitude, x)
     * @param south lower location in QuadTree Grid (latitude, y)
     * @param east right location in QuadTree Grid (longitude, x)
     * @return Vector of objects.
     */
    public Vector get(float north, float west, float south, float east) {
        return get(north, west, south, east, new Vector());
    }

    /**
     * Get all the objects within a bounding box, and return the objects within
     * a given Vector.
     * 
     * @param north top location in QuadTree Grid (latitude, y)
     * @param west left location in QuadTree Grid (longitude, x)
     * @param south lower location in QuadTree Grid (latitude, y)
     * @param east right location in QuadTree Grid (longitude, x)
     * @param vector a vector to add objects to.
     * @return Vector of objects.
     */
    public Vector get(float north, float west, float south, float east,
                      Vector vector) {

        if (vector == null) {
            vector = new Vector<Object>();
        }
        // crossing the dateline, right?? Or at least containing the
        // entire earth. Might be trouble for VERY LARGE scales. The
        // last check is for micro-errors that happen to lon points
        // where there might be a smudge overlap for very small
        // scales.
        if (west > east || MoreMath.approximately_equal(west, east, .001)) {
            return top.get(north, west, south, 180, top.get(north,
                    -180,
                    south,
                    east,
                    vector));
        } else
            return top.get(north, west, south, east, vector);
    }
}