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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/Node.java,v $
// $RCSfile: Node.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 17:46:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoint;

/**
 * Object that represents a NetMap node on the map.
 */
public class Node extends OMPoint implements ActionListener, FocusListener {

    public static final int DEFAULT_LENGTH = 10;
    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_RADIUS = 5;

    public static Color STATUS_UP = Color.green;
    public static Color STATUS_DOWN = Color.red;
    public static Color STATUS_UNKNOWN = Color.yellow;

    protected String label;
    protected int index;
    protected Color color = null;

    protected int posX = 0;
    protected int posY = 0;
    protected String posLat = null;
    protected String posLon = null;
    protected double gpsTime = 0;

    protected int length = 0;
    protected int width = 0;

    protected int menu = 0;
    protected int shapeValue = 0;

    protected boolean localhost = false;

    public Node(String label, int index, int shapeval, int menu, int color) {
        super();

        this.label = label;
        this.index = index;

        this.shapeValue = shapeval;

        this.menu = menu;

        this.color = NodeColor.colorOf(color);

        initOM();
    }

    private void initOM() {
        setOval(true);
        setRadius(DEFAULT_RADIUS);
        setRenderType(RENDERTYPE_LATLON);
        setFillPaint(this.color);
    }

    public String getLabel() {
        return this.label;
    }

    public String getName() {
        return getLabel();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public Color getColor() {
        return this.color;
    }

    public void setStatus(int color) {
        this.color = NodeColor.colorOf(color);
        setFillPaint(this.color);
    }

    public int getStatus() {
        return NodeColor.valueOf(this.color);
    }

    public void setTime(double time) {
        this.gpsTime = time;
    }

    public double getTime() {
        return this.gpsTime;
    }

    public void setShape(int shapeval) {
        this.shapeValue = shapeval;
    }

    public void moveTo(int newX, int newY) {
        if (getRenderType() == OMGraphic.RENDERTYPE_XY) {
            setX(newX);
            setY(newY);
        }
    }

    public void moveTo(float newLat, float newLon) {
        if (getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            setLat(newLat);
            setLon(newLon);
        }
    }

    /**
     * Set this node as the one reflecting the localhost.
     */
    public void setLocalhost(boolean value) {
        localhost = value;
    }

    public boolean isLocalhost() {
        return localhost;
    }

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {}

    public void actionPerformed(ActionEvent ae) {}
}

