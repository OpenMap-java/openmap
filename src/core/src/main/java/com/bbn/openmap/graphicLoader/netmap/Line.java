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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/Line.java,v $
// $RCSfile: Line.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Color;
import java.awt.event.FocusEvent;

import com.bbn.openmap.omGraphics.OMLine;

/**
 * Object that represents a NetMap link on the map.
 */
public class Line extends OMLine {

    protected String label = null;
    protected Color color = null;
    protected Node node1 = null;
    protected Node node2 = null;
    protected int shape = 0;
    protected int index = 0;
    protected double[] ll = null;

    public Line(String label, int index, int shape, int color, Node node1,
            Node node2) {
        super();

        this.index = index;
        this.shape = shape;
        this.node1 = node1;
        this.node2 = node2;
        this.label = label;
        this.color = NodeColor.colorOf(color);

        this.ll = new double[4];

        setPos(node1);
        setPos(node2);

        initOM();
    }

    private void initOM() {
        setRenderType(RENDERTYPE_LATLON);
        setLineType(LINETYPE_GREATCIRCLE);
        setLinePaint(this.color);
    }

    public String getName() {
        return "";
    }

    public String getLabel() {
        return label;
    }

    public Node getNode1() {
        return this.node1;
    }

    public Node getNode2() {
        return this.node2;
    }

    public int getIndex() {
        return this.index;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(int color) {
        setStatus(color);
    }

    public void setStatus(int color) {
        this.color = NodeColor.colorOf(color);
        setLinePaint(this.color);
    }

    public int getStatus() {
        return NodeColor.valueOf(this.color);
    }

    public void setPos(double lat1, double lon1, double lat2, double lon2) {
        ll[0] = lat1;
        ll[1] = lon1;
        ll[2] = lat2;
        ll[3] = lon2;

        setLL(ll);
    }

    public void setPos(Node atNode) {
        if (this.node1 == atNode) {
            ll[0] = atNode.getLat();
            ll[1] = atNode.getLon();
        } else {
            ll[2] = atNode.getLat();
            ll[3] = atNode.getLon();
        }

        setLL(ll);
    }

    public void setPos() {
        setPos(node1);
        setPos(node2);
    }

    public double[] getPos() {
        return ll;
    }

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {}
}