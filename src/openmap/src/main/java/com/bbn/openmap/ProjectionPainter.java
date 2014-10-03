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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/ProjectionPainter.java,v $
// $RCSfile: ProjectionPainter.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.Graphics;

import com.bbn.openmap.proj.Projection;

/**
 * The ProjectionPainter interface is intended for objects that can
 * manage graphics and draw them into a Java Graphics object. The idea
 * is that the ProjectionPainter, all within one thread, gathers the
 * graphics that reside within the projection, and render them into
 * the Graphics that has been set up for that projection. The height
 * and width of the projection should match the Graphics height and
 * width, in case some of the graphics from the ProjectionPainter
 * depend on them to place themselves within the Graphics.
 * <P>
 * This is different from the usual paradigm of OpenMap components.
 * Since OpenMap components are Swing components, they usually ready
 * themselves, call repaint() on themselves, and then wait for the
 * Swing thread to call paint and supply a Graphics object. This leads
 * to uncertainty as to when the painting is actually completed, which
 * can be an issue if you are trying to create an image, or something
 * like that.
 * <P>
 * Some layers kick off a SwingWorker thread to do the work. If a
 * layer is modified to implement this interface, the layer should do
 * all the graphics collection work and rendering in the calling
 * thread, so the caller knows that the contribution to the map from
 * this ProjectionPainter is complete.
 */
public interface ProjectionPainter {

    /**
     * Given a projection and Graphics, paint graphic objects inside
     * the Graphics. When this function returns, everything should be
     * all set.
     * 
     * @param proj a com.bbn.openmap.proj.Projection that describes a
     *        map.
     * @param g a java.awt.Graphics to draw into.
     */
    public void renderDataForProjection(Projection proj, Graphics g);

}