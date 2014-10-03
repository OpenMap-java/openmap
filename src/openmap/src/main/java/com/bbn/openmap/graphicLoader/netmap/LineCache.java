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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/LineCache.java,v $
// $RCSfile: LineCache.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The holder for Lines on the map. This cache is smart enough to
 * add/delete/modify Lines as needed based on line labels and nodes.
 */
public class LineCache {
    private Hashtable lineTable = null;

    public LineCache() {
        this.lineTable = new Hashtable();
    }

    public LineCache(int initialCapacity) {
        this.lineTable = new Hashtable(initialCapacity);
    }

    public void flush() {
        for (Enumeration list = elements(); list.hasMoreElements();)
            delete((Line) list.nextElement());
    }

    public Enumeration elements() {
        return lineTable.elements();
    }

    public Line add(String label, int index, int shape, int color, Node node1,
                    Node node2) {
        del(index);

        Line line = new Line(label, index, shape, color, node1, node2);
        lineTable.put(label, (Object) line);

        return line;
    }

    public void move(Node atNode) {
        Line[] lines = get(atNode);
        if (lines == null)
            return;

        for (int i = 0; i < lines.length; i++)
            lines[i].setPos(atNode);
    }

    public void move(Node atNode1, Node atNode2) {
        move(atNode1);
        move(atNode2);
    }

    public Line[] get(Node atNode) {
        int count = 0;
        Line[] lines = null;

        Enumeration list = lineTable.elements();
        if (list == null)
            return null;

        while (list.hasMoreElements()) {
            Line line = (Line) list.nextElement();

            if ((line.getNode1() == atNode) || (line.getNode2() == atNode)) {
                if (lines == null)
                    lines = new Line[1];
                else {
                    Line[] newLines = new Line[lines.length + 1];

                    System.arraycopy(lines, 0, newLines, 0, lines.length);
                    lines = newLines;
                }

                lines[count++] = line;
            }
        }

        return lines;
    }

    public Line get(Node atNode1, Node atNode2) {
        Enumeration list = lineTable.elements();
        if (list == null)
            return null;

        while (list.hasMoreElements()) {
            Line line = (Line) list.nextElement();

            if (((line.getNode1() == atNode1) || (line.getNode2() == atNode1))
                    && ((line.getNode1() == atNode2) || (line.getNode2() == atNode2))) {
                return line;
            }
        }

        return null;
    }

    public Line get(String label) {
        return ((Line) lineTable.get(label));
    }

    public Line get(int index) {
        Enumeration list = lineTable.elements();
        if (list == null)
            return null;

        while (list.hasMoreElements()) {
            Line line = (Line) list.nextElement();

            if (line.getIndex() == index)
                return line;
        }

        return null;
    }

    public void del(Node atNode) {
        Line[] lines = get(atNode);
        if (lines == null)
            return;

        for (int i = 0; i < lines.length; i++)
            delete(lines[i]);
    }

    public void del(Line[] lines) {
        for (int i = 0; i < lines.length; i++)
            delete(lines[i]);
    }

    public void del(Node atNode1, Node atNode2) {
        delete(get(atNode1, atNode2));
    }

    public void del(int index) {
        delete(get(index));
    }

    public void del(String label) {
        delete(get(label));
    }

    private void delete(Line line) {
        if (line == null)
            return;

        lineTable.remove(line.getLabel());
    }
}