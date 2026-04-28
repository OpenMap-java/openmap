/* ***********************************************************************
 * This is used by the MysqlGeometryLayer.
 * This program is distributed freely and in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Copyright 2003 by the Author
 *
 * Author name: Uwe Baier uwe.baier@gmx.net
 * Version 1.0
 * ***********************************************************************
 */
package com.bbn.openmap.layer.mysql;

import java.util.Vector;

/**
 * This class represents a Node used to construct a tree while parsing
 * the WKT (text) representation of the Geometry.
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class WKTNode {

    private boolean root = false;
    private boolean leaf = false;

    private Vector children = new Vector();
    private WKTNode parent;
    private String geoWKT = "";

    /**
     * Returns the geoWKT.
     * 
     * @return String
     */
    public String getGeoWKT() {
        return geoWKT;
    }

    /**
     * Returns the leaf.
     * 
     * @return boolean
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * Returns the parent.
     * 
     * @return WKTNode
     */

    public WKTNode getParent() {
        return parent;
    }

    /**
     * Returns the root.
     * 
     * @return boolean
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Sets the geoWKT.
     * 
     * @param c The geoWKT to set
     */
    public void adToGeoWKT(char[] c) {
        this.geoWKT = geoWKT.concat(new String(c));
    }

    /**
     * Sets the leaf.
     * 
     * @param leaf The leaf to set
     */
    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    /**
     * Sets the parent.
     * 
     * @param parent The parent to set
     */
    public void setParent(WKTNode parent) {
        this.parent = parent;
    }

    /**
     * Sets the root.
     * 
     * @param root The root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    public int countChildren() {
        return this.children.size();
    }

    public void adChild(WKTNode n) {
        this.children.add(n);
        n.setParent(this);
    }

    public WKTNode getChildByNumber(int i) {
        return (WKTNode) this.children.elementAt(i);
    }
}

