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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapBeanRepaintPolicy.java,v $
// $RCSfile: MapBeanRepaintPolicy.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.Graphics;

/**
 * A MapBeanRepaintPolicy is a policy object that makes decisions on
 * how a MapBean handles repaint requests from a layer. It can forward
 * them on to the Swing thread by calling MapBean.repaint(), or ignore
 * them until conditions that it considers valuable are met.
 */
public interface MapBeanRepaintPolicy extends Cloneable {

    /**
     * Set the MapBean to call repaint on when a layer requests it.
     */
    public void setMap(MapBean mb);

    /**
     * Take some action based on a repaint request from this
     * particular layer.
     */
    public void repaint(Layer layer);

    /**
     * A hook for the RepaintPolicy to make any adjustments to the
     * java.awt.Graphics object before sending the Graphics object to
     * the layers for painting. Gives the policy a chance to make
     * rendering hint changes on Graphic2D objects, setting
     * anti-aliasing configurations, etc.
     */
    public Graphics modifyGraphicsForPainting(Graphics graphics);

    /**
     * Provide a configured copy (except for the MapBean).
     */
    public Object clone();
}