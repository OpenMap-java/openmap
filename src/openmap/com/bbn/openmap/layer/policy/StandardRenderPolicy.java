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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/StandardRenderPolicy.java,v $
// $RCSfile: StandardRenderPolicy.java,v $
// $Revision: 1.9 $
// $Date: 2005/10/26 15:47:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The StandardRenderPolicy is a RenderPolicy that simply paints the current
 * graphic list. No conditions or deviations are considered.
 */
public class StandardRenderPolicy
      extends OMComponent
      implements RenderPolicy {

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.policy.RenderPolicy");

   /**
    * Don't let this be null, nothing will happen. At all.
    */
   protected OMGraphicHandlerLayer layer;

   protected Composite composite;

   public StandardRenderPolicy() {
   }

   /**
    * Don't pass in a null layer.
    */
   public StandardRenderPolicy(OMGraphicHandlerLayer layer) {
      this();
      setLayer(layer);
   }

   public void setLayer(OMGraphicHandlerLayer l) {
      layer = l;
   }

   public OMGraphicHandlerLayer getLayer() {
      return layer;
   }

   public Composite getComposite() {
      return composite;
   }

   /**
    * Can be used to set Composite objects (like AlphaComposite) on Graphics2D
    * objects before the layer is painted.
    * 
    * @param composite
    */
   public void setComposite(Composite composite) {
      this.composite = composite;
   }

   /**
    * Call made by the policy from the paint(g) method in order to set the
    * composite on the Graphics2D object. This method is meant to be overridden
    * if needed.
    * 
    * @param g Graphics2D that the Composite will be set on.
    */
   protected void setCompositeOnGraphics(Graphics2D g) {
      if (composite != null) {
         g.setComposite(composite);
      }
   }

   /**
    * The StandardRenderPolicy doesn't need to do anything before prepare()
    * returns.
    */
   public void prePrepare() {
      // NOOP
   }

   public OMGraphicList prepare() {
      if (layer != null) {
         return layer.prepare();
      } else {
         return null;
      }
   }

   /**
    * Assumes that the OMGraphicList to be rendered is set on the
    * OMGraphicHandlerLayer, available via setList().
    */
   public void paint(Graphics g) {
      if (layer != null) {
         OMGraphicList list = layer.getList();
         Projection proj = layer.getProjection();
         if (list != null && layer.isProjectionOK(proj)) {
            if (proj != null) {
               g.setClip(0, 0, proj.getWidth(), proj.getHeight());
            }

            setCompositeOnGraphics((Graphics2D) g);

            list.render(g);
         } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + ".paint(): " + (list == null ? "NULL list, skipping..." : " skipping due to projection."));
         }
      } else {
         Debug.error("RenderPolicy.paint():  NULL layer, skipping...");
      }
   }
}