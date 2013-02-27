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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTreeNode.java,v $
// $RCSfile: QuadTreeNode.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.quadtree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import com.bbn.openmap.MoreMath;

/**
 * The QuadTreeNode is the part of the QuadTree that either holds children
 * nodes, or objects as leaves. Currently, the nodes that have children do not
 * hold items that span across children boundaries, since this was designed to
 * handle point data.
 */

public class QuadTreeNode implements Serializable {

    static final long serialVersionUID = -6111633198469889444L;

    public final static int NORTHWEST = 0;
    public final static int NORTHEAST = 1;
    public final static int SOUTHEAST = 2;
    public final static int SOUTHWEST = 3;
    public final static float NO_MIN_SIZE = -1;
    public final static float DEFAULT_MIN_SIZE = 5;

    protected Vector<QuadTreeLeaf> items;
    protected QuadTreeNode[] children;
    protected int maxItems;
    protected float minSize;
    public QuadTreeRect bounds;
    /**
     * Added to avoid problems when a node is completely filled with a single
     * point value.
     */
    protected boolean allTheSamePoint;
    protected float firstLat;
    protected float firstLon;

    /**
     * Constructor to use if you are going to store the objects in lat/lon
     * space, and there is really no smallest node size.
     * 
     * @param north northern border of node coverage.
     * @param west western border of node coverage.
     * @param south southern border of node coverage.
     * @param east eastern border of node coverage.
     * @param maximumItems number of items to hold in a node before splitting
     *        itself into four children and redispensing the items into them.
     */
    public QuadTreeNode(float north, float west, float south, float east, int maximumItems) {
        this(north, west, south, east, maximumItems, NO_MIN_SIZE);
    }

    /**
     * Constructor to use if you are going to store the objects in x/y space,
     * and there is a smallest node size because you don't want the nodes to be
     * smaller than a group of pixels.
     * 
     * @param north northern border of node coverage.
     * @param west western border of node coverage.
     * @param south southern border of node coverage.
     * @param east eastern border of node coverage.
     * @param maximumItems number of items to hold in a node before splitting
     *        itself into four children and redispensing the items into them.
     * @param minimumSize the minimum difference between the boundaries of the
     *        node.
     */
    public QuadTreeNode(float north, float west, float south, float east, int maximumItems,
            float minimumSize) {
        bounds = new QuadTreeRect(north, west, south, east);
        maxItems = maximumItems;
        minSize = minimumSize;
        items = new Vector<QuadTreeLeaf>();
    }

    /** Return true if the node has children. */
    public boolean hasChildren() {
        return (children != null);
    }

    /**
     * This method splits the node into four children, and disperses the items
     * into the children. The split only happens if the boundary size of the
     * node is larger than the minimum size (if we care). The items in this node
     * are cleared after they are put into the children.
     */
    protected void split() {
        // Make sure we're bigger than the minimum, if we care,
        if (minSize != NO_MIN_SIZE) {
            if (MoreMath.approximately_equal(bounds.north, bounds.south, minSize)
                    && MoreMath.approximately_equal(bounds.east, bounds.west, minSize))
                return;
        }

        float nsHalf = (float) (bounds.north - (bounds.north - bounds.south) / 2.0);
        float ewHalf = (float) (bounds.east - (bounds.east - bounds.west) / 2.0);
        children = new QuadTreeNode[4];

        children[NORTHWEST] = new QuadTreeNode(bounds.north, bounds.west, nsHalf, ewHalf, maxItems);
        children[NORTHEAST] = new QuadTreeNode(bounds.north, ewHalf, nsHalf, bounds.east, maxItems);
        children[SOUTHEAST] = new QuadTreeNode(nsHalf, ewHalf, bounds.south, bounds.east, maxItems);
        children[SOUTHWEST] = new QuadTreeNode(nsHalf, bounds.west, bounds.south, ewHalf, maxItems);
        Vector<QuadTreeLeaf> temp = (Vector<QuadTreeLeaf>) items.clone();
        items.removeAllElements();
        Enumeration<QuadTreeLeaf> things = temp.elements();
        while (things.hasMoreElements()) {
            put(things.nextElement());
        }
        // items.removeAllElements();
    }

    /**
     * Get the node that covers a certain lat/lon pair.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return node if child covers the point, null if the point is out of
     *         range.
     */
    protected QuadTreeNode getChild(float lat, float lon) {
        if (bounds.pointWithinBounds(lat, lon)) {
            if (children != null) {
                for (QuadTreeNode child : children) {
                    if (child.bounds.pointWithinBounds(lat, lon))
                        return child.getChild(lat, lon);
                }
            } else
                return this; // no children, lat, lon here...
        }
        return null;
    }

    /**
     * Add a object into the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj object to add to the tree.
     * @return true if the pution worked.
     */
    public boolean put(float lat, float lon, Object obj) {
        return put(new QuadTreeLeaf(lat, lon, obj));
    }

    /**
     * Add a QuadTreeLeaf into the tree at a location.
     * 
     * @param leaf object-location composite
     * @return true if the pution worked.
     */
    public boolean put(QuadTreeLeaf leaf) {
        if (children == null) {
            this.items.addElement(leaf);
            if (this.items.size() == 1) {
                this.allTheSamePoint = true;
                this.firstLat = leaf.latitude;
                this.firstLon = leaf.longitude;
            } else {
                if (this.firstLat != leaf.latitude || this.firstLon != leaf.longitude) {
                    this.allTheSamePoint = false;
                }
            }

            if (this.items.size() > maxItems && !this.allTheSamePoint)
                split();
            return true;
        } else {
            QuadTreeNode node = getChild(leaf.latitude, leaf.longitude);
            if (node != null) {
                return node.put(leaf);
            }
        }
        return false;
    }

    /**
     * Remove a object out of the tree at a location.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object removed, null if the object not found.
     */
    public Object remove(float lat, float lon, Object obj) {
        return remove(new QuadTreeLeaf(lat, lon, obj));
    }

    /**
     * Remove a QuadTreeLeaf out of the tree at a location.
     * 
     * @param leaf object-location composite
     * @return the object removed, null if the object not found.
     */
    public Object remove(QuadTreeLeaf leaf) {
        if (children == null) {
            // This must be the node that has it...
            for (int i = 0; i < items.size(); i++) {
                QuadTreeLeaf qtl = (QuadTreeLeaf) items.elementAt(i);
                if (leaf.object == qtl.object) {
                    items.removeElementAt(i);
                    return qtl.object;
                }
            }
        } else {
            QuadTreeNode node = getChild(leaf.latitude, leaf.longitude);
            if (node != null) {
                return node.remove(leaf);
            }
        }
        return null;
    }

    /** Clear the tree below this node. */
    public void clear() {
        this.items.removeAllElements();
        if (children != null) {
            for (QuadTreeNode child : children) {
                child.clear();
            }
            children = null;
        }
    }

    /**
     * Get an object closest to a lat/lon.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that matches the best distance, null if no object was
     *         found.
     */
    public Object get(float lat, float lon) {
        return get(lat, lon, Double.POSITIVE_INFINITY);
    }

    /**
     * Get an object closest to a lat/lon. If there are children at this node,
     * then the children are searched. The children are checked first, to see if
     * they are closer than the best distance already found. If a closer object
     * is found, bestDistance will be updated with a new Double object that has
     * the new distance.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance maximum get distance.
     * @return the object that matches the best distance, null if no closer
     *         object was found.
     */
    public Object get(float lat, float lon, double withinDistance) {
        return get(lat, lon, new MutableDistance(withinDistance));
    }

    /**
     * Get an object closest to a lat/lon. If there are children at this node,
     * then the children are searched. The children are checked first, to see if
     * they are closer than the best distance already found. If a closer object
     * is found, bestDistance will be updated with a new Double object that has
     * the new distance.
     * 
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param bestDistance the closest distance of the object found so far.
     * @return the object that matches the best distance, null if no closer
     *         object was found.
     */
    public Object get(double lat, double lon, MutableDistance bestDistance) {
        Object closest = null;
        if (children == null) // This must be the node that has it...
        {
            for (QuadTreeLeaf qtl : items) {
                double dx = lon - qtl.longitude;
                double dy = lat - qtl.latitude;
                double distanceSqr = dx * dx + dy * dy;

                if (distanceSqr < bestDistance.value) {
                    bestDistance.value = distanceSqr;
                    closest = qtl.object;
                }
            }
            return closest;
        } else {
            // Check the distance of the bounds of the children,
            // versus the bestDistance. If there is a boundary that
            // is closer, then it is possible that another node has an
            // object that is closer.
            for (QuadTreeNode child : children) {
                double childDistance = child.bounds.borderDistanceSqr(lat, lon);
                if (childDistance < bestDistance.value) {
                    Object test = child.get(lat, lon, bestDistance);
                    if (test != null)
                        closest = test;
                }
            }
        }
        return closest;
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
    public Vector<?> get(float north, float west, float south, float east) {
        return get(new QuadTreeRect(north, west, south, east), new Vector<Object>());
    }

    /**
     * Get all the objects within a bounding box.
     * 
     * @param north top location in QuadTree Grid (latitude, y)
     * @param west left location in QuadTree Grid (longitude, x)
     * @param south lower location in QuadTree Grid (latitude, y)
     * @param east right location in QuadTree Grid (longitude, x)
     * @param vector current vector of objects.
     * @return Vector of objects.
     */
    public Vector get(float north, float west, float south, float east, Vector vector) {
        return get(new QuadTreeRect(north, west, south, east), vector);
    }

    /**
     * Get all the objects within a bounding box.
     * 
     * @param rect boundary of area to fill.
     * @param vector current vector of objects.
     * @return updated Vector of objects.
     */
    public Vector get(QuadTreeRect rect, Vector vector) {
        if (children == null) {
            for (QuadTreeLeaf qtl : this.items) {
                if (rect.pointWithinBounds(qtl.latitude, qtl.longitude)) {
                    vector.add(qtl.object);
                }
            }
        } else {
            for (QuadTreeNode child : children) {
                if (child.bounds.within(rect)) {
                    child.get(rect, vector);
                }
            }
        }
        return vector;
    }
}