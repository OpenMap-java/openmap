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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/IconPart.java,v $
// $RCSfile: IconPart.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.Graphics;
import java.awt.Shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * An IconPart is an object that makes up a piece of what's rendered
 * on an Icon. It has a java.awt.Shape object that specifies what gets
 * rendered, and it has a clipping shape that specifies how much of
 * the shape gets rendered. The rendering attributes specify how the
 * shape is colored, drawn, etc.
 * 
 * The coordinates of the Shape objects used for the clip area and
 * geometry are in relative coordinates, and should be thought of as
 * percentage coordinates, on a scale from 1 to 100. The size of the
 * Graphics object provided in the render method will dictate how
 * large the part will actually be on the screen. If coordinates of
 * the IconPart are greater than 100 or less than zero, that part will
 * not appear on the icon. That may help with defining certain shapes
 * to appear on the icon, however.
 */
public interface IconPart {

    /**
     * Have the IconPart render itself into the Graphic object for a
     * given height and width.
     */
    public void render(Graphics g, int width, int height);

    /**
     * Have the IconPart render itself into the Graphic object for a
     * given height and width. The DrawingAttributes will dictate how
     * the geometries can be drawn, of the IconPart may react to a
     * System setting or last-minute attributes. The IconPart may
     * decide to ignore this provided DrawingAttributes and just use
     * what it has. appDA may be null, in which case the internal
     * DrawingAttributes will be used.
     */
    public void render(Graphics g, int width, int height,
                       DrawingAttributes appDA);

    /**
     * Set a clip area for the IconPart to draw only certain parts of
     * the geometry.
     */
    public void setClip(Shape clipArea);

    /**
     * Get a clip area for the IconPart.
     */
    public Shape getClip();

    /**
     * Set the geometry for this IconPart.
     */
    public void setGeometry(Shape shape);

    /**
     * Get the geometry for this IconPart.
     */
    public Shape getGeometry();

    /**
     * Set the rendering attributes for this IconPart.
     */
    public void setRenderingAttributes(DrawingAttributes da);

    /**
     * Get the rendering attributes for this IconPart.
     */
    public DrawingAttributes getRenderingAttributes();
    
    /**
     * @return copy of itself, IconParts need to be Cloneable
     */
    public Object clone();

}