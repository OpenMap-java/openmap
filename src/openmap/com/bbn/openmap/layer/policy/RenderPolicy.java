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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/RenderPolicy.java,v $
// $RCSfile: RenderPolicy.java,v $
// $Revision: 1.4 $
// $Date: 2005/10/26 15:47:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import java.awt.Composite;
import java.awt.Graphics;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A policy object that can be used by an OMGraphicHandlerLayer to figure out
 * the best way to paint on the map.
 */
public interface RenderPolicy {

   /**
    * A method to set the parent layer on the RenderPolicy.
    */
   public void setLayer(OMGraphicHandlerLayer layer);

   /**
    * A method to get the parent layer on the RenderPolicy.
    */
   public OMGraphicHandlerLayer getLayer();

   /**
    * Set a Composite object on a Graphics2D object before rendering. Set to
    * null (default) to not do anything.
    */
   public void setComposite(Composite composite);

   public Composite getComposite();

   /**
    * Be very careful with doing things in this method. It is called from the
    * doPrepare() method in the calling thread, in order to allow any buffering
    * to update itself outside of the LayerWorker thread. It allows the render
    * policy to keep up with rapid projection changes even if the LayerWorker
    * threads are getting backed up. Do not do a lot of work in this thread, it
    * will slow down projection change notifications being received from other
    * layers if you do.
    */
   void prePrepare();

   /**
    * Called when an OMGraphicHandlerLayer should begin preparing OMGraphics for
    * the map. This is a hook into the list to help RenderPolicy make decisions
    * or set up the list for faster rendering.
    */
   public OMGraphicList prepare();

   /**
    * Called from OMGraphicHandlerLayer.paint(Graphics), so the policy can
    * handle the painting for the layer. If you are going to change the Graphics
    * object in this method, you should make a copy of it first using the
    * Graphics.create() method so the changes won't affect other layers.
    */
   public void paint(Graphics g);

}