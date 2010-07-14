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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/BufferedImageRenderPolicy.java,v $
// $RCSfile: BufferedImageRenderPolicy.java,v $
// $Revision: 1.8 $
// $Date: 2005/10/26 15:47:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.Projection;

/**
 * The BufferedImageRenderPolicy is a RenderPolicy that creates and uses an
 * image buffer based on the painting times for the layer. If the time to paint
 * exceeds the bufferTiggerDelay, an image buffer for the layer is used for
 * paints as long as the projection doesn't change. A new buffer is used for a
 * projection change because we need the image buffer to be transparent for
 * parts of the map that are not used by the layer.
 */
public class BufferedImageRenderPolicy
      extends RenderingHintsRenderPolicy {

   protected OMRaster buffer = null;

   /**
    * Set the layer at some point before use.
    */
   public BufferedImageRenderPolicy() {
      super();
   }

   /**
    * Don't pass in a null layer.
    */
   public BufferedImageRenderPolicy(OMGraphicHandlerLayer layer) {
      super(layer);
   }

   public OMGraphicList prepare() {
      if (layer != null) {

         // Instead of setting the buffer to null here, we want to re-project an
         // OMRaster that is positioning the buffer instead, and then prepare it
         // to be painted in the new location. When the prepare() method
         // returns, we create a new OMRaster buffer at the map location.

         OMRaster buffer = getBuffer();
         Projection proj = layer.getProjection();
         if (proj instanceof Cylindrical && buffer != null) {
            buffer.generate(layer.getProjection());
         } else {
            setBuffer(null);
         }

         OMGraphicList list = layer.prepare();
         setBuffer(createAndPaintImageBuffer(list));

         return list;
      } else {
         logger.warning("NULL layer, can't do anything.");
      }
      return null;
   }

   public void paint(Graphics g) {
      if (layer == null) {
         logger.warning("NULL layer, skipping...");
         return;
      }

      OMGraphicList list = layer.getList();
      Projection proj = layer.getProjection();
      Graphics2D g2 = (Graphics2D) g.create();
      OMRaster bufferedImage = getBuffer();

      if (bufferedImage == null && list != null && layer.isProjectionOK(proj)) {

         bufferedImage = createAndPaintImageBuffer(list);
         setBuffer(bufferedImage);
         setCompositeOnGraphics(g2);

         if (bufferedImage != null) {

            if (logger.isLoggable(Level.FINE)) {
               logger.fine(layer.getName() + ": rendering buffer in paint after creating it from list");
            }

            // g2.drawRenderedImage((BufferedImage) bufferedImage, new
            // AffineTransform());
            bufferedImage.render(g2);

         } else {
            // Not sure why we'd get here...
            super.setRenderingHints(g2);
            list.render(g2);
         }
      } else if (bufferedImage != null && layer.isProjectionOK(proj)) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + ": rendering buffer in paint........");
         }
         setCompositeOnGraphics(g2);
         bufferedImage.render(g2);
      } else if (logger.isLoggable(Level.FINE)) {
         logger.fine(layer.getName() + ".paint(): " + (list == null ? "NULL list, skipping..." : " skipping due to projection."));
      }

      g2.dispose();

   }

   /** Get the BufferedImage for the layer. */
   protected OMRaster getBuffer() {
      return buffer;
   }

   /** Set the BufferedImage for the layer. */
   protected void setBuffer(OMRaster bi) {
      buffer = bi;
   }

   protected OMRaster createAndPaintImageBuffer(OMGraphicList list) {

      OMRaster omr = null;

      if (list != null && layer != null) {
         Projection proj = layer.getProjection();
         int w = proj.getWidth();
         int h = proj.getHeight();

         Point2D llp1 = proj.getUpperLeft();
         Point2D llp2 = proj.getLowerRight();

         // Make sure the projected area of the image is actually the entire
         // image - otherwise, the image gets shrunk down and doesn't line up
         // with the projection.

         Point2D pnt1 = proj.forward(llp1);
         Point2D pnt2 = proj.forward(llp2);

         // Need the offset for rendering the top of the drawing OMGraphics at
         // the top of the projected space of the image.
         double offset = 0;
         if (pnt1.getY() > 0 || pnt2.getY() < h) {
            h = (int) Math.floor(pnt2.getY() - pnt1.getY());
            offset = pnt1.getY();
         }

         BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
         super.setRenderingHints(g2d);
         if (offset > 0) {
            g2d.setTransform(AffineTransform.getTranslateInstance(0, -offset));
         }
         list.render(g2d);
         if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + ": $$$$$$$$$$ rendering list into buffer");
         }

         if (proj instanceof Cylindrical) {
            omr = new OMScalingRaster(llp1.getY(), llp1.getX(), llp2.getY(), llp2.getX(), bufferedImage);
         } else {
            omr = new OMRaster((int) 0, (int) 0, bufferedImage);
         }
         omr.generate(proj);
      }

      return omr;
   }
}