//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: OMLabeler.java,v $
//$Revision: 1.1 $
//$Date: 2005/01/10 16:58:33 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * An OMLabeler is used by OMGraphics to handle their labels. It can
 * draw the text on the map, and tell how far away an x,y pixel
 * location is away from where it's drawing itself on the map.
 * 
 * @author dietrick
 */
public interface OMLabeler {

    /**
     * The OMLabeler should render the text label onto the Graphics
     * object.
     * 
     * @param g
     */
    public void render(Graphics g);

    /**
     * The OMLabler should return the pixel distance from any part of
     * the label to the given x, y pixel distance.
     * 
     * @param x pixel distance from left side of map window.
     * @param y pixel distance from top of map window.
     * @return distance in pixels from label text to given point.
     */
    public float distance(double x, double y);

    /**
     * The OMLabeler should be able to determine its pixel location
     * based on the GeneralPath of the OMGraphic. This will generally
     * cause the label to be placed in the center of the bounds of the
     * shape.
     * 
     * @param gp
     */
    public void setLocation(GeneralPath gp);

    /**
     * The OMLabeler should be able to determine its pixel location
     * based on a set of x and y pixel coordinates common in
     * OMGraphics.
     * 
     * @param xpoints
     * @param ypoints
     */
    public void setLocation(int[] xpoints, int[] ypoints);

    /**
     * The OMLabeler should be able to set its pixel location
     * directly.
     * 
     * @param p
     */
    public void setLocation(Point2D p);

}