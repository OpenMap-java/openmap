//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: ShapeRenderer.java,v $
//$Revision: 1.1 $
//$Date: 2009/01/21 01:24:42 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics2D;
import java.awt.Shape;

public interface ShapeRenderer {
    
    /**
     * Render the Shape into the Graphics2D object.
     */
    public void render(Graphics2D g, Shape shape);

    /**
     * Render the Shape into the Graphics2D object.
     * 
     * @param g java.awt.Graphics2D object to render into
     * @param shape java.awt.Shape to draw
     * @param replaceColorWithGradient flag to specify replacement of fill and
     *        edge colors with a GradientPaint to give a light to dark look.
     */
    public void render(Graphics2D g, Shape shape,
                       boolean replaceColorWithGradient);
}
