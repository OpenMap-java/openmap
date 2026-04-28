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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NodeCache.java,v $
// $RCSfile: NodeCache.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A holder for all the nodes on the map. Cache is smart enough to
 * modify or add a node based on a description.
 */
public class NodeCache {

    private Hashtable nodeTable = null;

    NodeCache() {
        nodeTable = new Hashtable();
    }

    NodeCache(int initialCapacity) {
        nodeTable = new Hashtable(initialCapacity);
    }

    public void flush() {
        for (Enumeration list = elements(); list.hasMoreElements();)
            delete((Node) list.nextElement());
    }

    public Enumeration elements() {
        return nodeTable.elements();
    }

    public Node add(String label, int index, int shape, int menu, int color) {
        Node node = null;

        if ((node = get(label)) != null)
            return node;

        node = new Node(label, index, shape, menu, color);
        nodeTable.put(label, (Object) node);

        return node;
    }

    public Node get(int index) {
        Enumeration list = nodeTable.elements();

        if (list == null)
            return null;
        while (list.hasMoreElements()) {
            Node node = (Node) list.nextElement();

            if (node.getIndex() == index) {
                return (node);
            }
        }

        return null;
    }

    public Node get(String label) {
        return (Node) nodeTable.get((Object) label);
    }

    public void del(String label) {
        delete(get(label));
    }

    public void del(int index) {
        delete(get(index));
    }

    public void del(Node node) {
        delete(node);
    }

    // Delete a node from the internal cache
    //
    private void delete(Node node) {
        if (node == null)
            return;

        nodeTable.remove(node.getLabel());
    }
}