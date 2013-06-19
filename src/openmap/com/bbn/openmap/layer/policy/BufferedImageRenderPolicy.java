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
public class BufferedImageRenderPolicy extends RenderingHintsRenderPolicy {

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

    /**
     * Called from the OMGraphicHandlerLayer's doPrepare() method. This method
     * updates the current image buffer so it can be re-projected for the
     * current projection before the layer worker goes off to do more work. In
     * case of rapid projection changes, the layer should be able to display the
     * current buffer in the right place, at least.
     */
    public void prePrepare() {
        // Instead of setting the buffer to null here, we want to re-project an
        // OMRaster that is positioning the buffer instead, and then prepare it
        // to be painted in the new location. When the prepare() method
        // returns, we create a new OMRaster buffer at the map location.

        OMRaster buffer = getBuffer();
        Projection proj = layer.getProjection();
        if (proj instanceof Cylindrical && buffer != null) {
            buffer.setNeedToRegenerate(true);
            buffer.generate(proj);
        } else {
            setBuffer(null);
        }
    }

    public OMGraphicList prepare() {
        if (layer != null) {
            // Grab a copy of the projection that the list is being made for, so
            // the buffer gets placed for the right location. It's really
            // important
            // in case the projection height and width don't quite match up with
            // the coordinate coverage area. Doesn't sound right, but that
            // happens
            // occasionally and the images appear stretched out if you don't
            // match
            // up the image to the projection.
            Projection proj = layer.getProjection();
            OMGraphicList list = layer.prepare();
            try {
                setBuffer(createAndPaintImageBuffer(list, proj));
            } catch (NullPointerException npe) {
                logger.fine("Caught NPE creating the image buffer for layer: " + layer.getName());
                if (logger.isLoggable(Level.FINE)) {
                    npe.printStackTrace();
                }
                setBuffer(null);
            }

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

        if (layer.isProjectionOK(proj)) {

            if (bufferedImage == null && list != null) {

                bufferedImage = createAndPaintImageBuffer(list, proj);
                setBuffer(bufferedImage);
                setCompositeOnGraphics(g2);

                if (bufferedImage != null) {
                    // Check one last time before rendering, is the image
                    // projection
                    // current for the layer? Scroll wheel sometimes screws this
                    // up.
                    Object imageProj = bufferedImage.getAttribute(CURRENT_PROJECTION);
                    Projection newProj = layer.getProjection();
                    if (!newProj.equals(imageProj)) {
                        bufferedImage.generate(newProj);
                        bufferedImage.putAttribute(CURRENT_PROJECTION, newProj);
                    }

                    bufferedImage.render(g2);
                } else {
                    // Not sure why we'd get here...
                    super.setRenderingHints(g2);
                    list.render(g2);
                }

            } else if (bufferedImage != null) {

                // Check one last time before rendering, is the image projection
                // current for the layer? Scroll wheel sometimes screws this up.
                Object imageProj = bufferedImage.getAttribute(CURRENT_PROJECTION);
                Projection newProj = layer.getProjection();
                if (!newProj.equals(imageProj)) {
                    bufferedImage.generate(newProj);
                    bufferedImage.putAttribute(CURRENT_PROJECTION, newProj);
                }
                setCompositeOnGraphics(g2);
                bufferedImage.render(g2);

            }
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + ".paint(): "
                    + (list == null ? "NULL list, skipping..." : " skipping due to projection."));
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

    /*
     * Used to keep the projection of the image with the image as an attribute.
     */
    protected String CURRENT_PROJECTION = "currentProjection";

    protected OMRaster createAndPaintImageBuffer(OMGraphicList list, Projection proj) {

        OMRaster omr = null;

        if (proj != null && list != null && layer != null) {
            int w = proj.getWidth();
            int h = proj.getHeight();

            Point2D llp1 = proj.getUpperLeft();
            // The lower right point is w-1, h-1, the actual pixel index,
            // starting at 0. The size of the image is one pixel more. Using
            // getLowerRight() leaves one pixel on the bottom and right blank in
            // the resulting image.
            Point2D llp2 = proj.inverse(w, h);

            // We're running into a problem here for the OMScalingRaster where
            // the
            // projection is providing a bad coordinate situation for
            // OMScalingRasters when zoomed way out. The left pixel coordinate
            // of
            // the map, at some point in the OMScalingRaster calculations, is
            // being
            // placed to the right of the right pixel coordinate of the map.
            // It's a
            // 360 degree precision thing, and ever so slight. We're going to
            // test
            // for that here, and if that x test fails, we're just going to use
            // a
            // standard OMRaster for the Buffer.

            // The OMScalingRaster is cool because it can respond to the
            // projection
            // change immediately, and display what was there before while the
            // layers are working.

            Point2D pnt1 = proj.forward(llp1);
            Point2D pnt2 = proj.forward(llp2);

            if (pnt1.getX() < pnt2.getX() && proj instanceof Cylindrical) {

                // Now make sure the projected area of the image is actually the
                // entire
                // image - otherwise, the image gets shrunk down and doesn't
                // line up
                // with the projection.

                // Need the offset for rendering the top of the drawing
                // OMGraphics
                // at the top of the projected space of the image.
                double yOffset = 0;
                double pnt1y = Math.round(pnt1.getY());
                double pnt2y = Math.round(pnt2.getY());
                if (pnt1y > 0 || pnt2y < (h - 1)) {
                    // There are cases where pnt2y = pnt1y causing h = 0
                    // and this causes problems below where we have to bail!

                    h = (int) Math.floor(pnt2y - pnt1y);
                    yOffset = pnt1y;
                }

                if (h > 0 && w > 0) {
                    BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
                    super.setRenderingHints(g2d);
                    if (yOffset != 0) {
                        g2d.setTransform(AffineTransform.getTranslateInstance(0, -yOffset));
                    }
                    list.generate(proj);
                    list.render(g2d);

                    omr = new OMScalingRaster(llp1.getY(), llp1.getX(), llp2.getY(), llp2.getX(), bufferedImage);
                }
            } else {
                // For anything other than a cylindrical image, we just want to
                // paint what we would have drawn in an image that covers the
                // map.
                BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
                super.setRenderingHints(g2d);
                list.render(g2d);
                omr = new OMRaster((int) 0, (int) 0, bufferedImage);

            }

            if (omr != null) {
                omr.generate(proj);
                // Save projection for later, so we can check it right before we
                // paint. With mouse wheel scrolling, sometimes the changes
                // happen
                // at just the right rate where we were painting images from old
                // projections, instead of reprojection the image that was
                // ready.
                omr.putAttribute(CURRENT_PROJECTION, proj);
            }
        }

        return omr;
    }
}