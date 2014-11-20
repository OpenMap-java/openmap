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

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
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

    protected final ImageBuffer imageBuffer = new ImageBuffer();

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
     * @return the imageBuffer
     */
    protected ImageBuffer getImageBuffer() {
        return imageBuffer;
    }

    /**
     * Called from the OMGraphicHandlerLayer's projectionChanged() method. This
     * method updates the current image buffer so it can be re-projected for the
     * current projection before the layer worker goes off to do more work. In
     * case of rapid projection changes, the layer should be able to display the
     * current buffer in the right place, at least. Ghah! Don't do a lot of work
     * in this thread.
     * 
     * @param newProj the newest projection known.
     */
    public void prePrepare(Projection newProj) {
        getImageBuffer().generate(newProj);
    }

    public OMGraphicList prepare() {
        if (layer != null) {
            /*
             * Grab a copy of the projection that the list is being made for, so
             * the buffer gets placed for the right location. It's really
             * important in case the projection height and width don't quite
             * match up with the coordinate coverage area. Doesn't sound right,
             * but that happens occasionally and the images appear stretched out
             * if you don't match up the image to the projection.
             */
            Projection proj = layer.getProjection();
            OMGraphicList list = layer.prepare();
            try {
                getImageBuffer().update(list, proj);
            } catch (NullPointerException npe) {
                logger.fine("Caught NPE creating the image buffer for layer: " + layer.getName());
                if (logger.isLoggable(Level.FINE)) {
                    npe.printStackTrace();
                }
                getImageBuffer().clear();
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

        Projection proj = layer.getProjection();

        if (layer.isProjectionOK(proj)) {

            Graphics2D g2 = (Graphics2D) g.create();
            ImageBuffer imageBuffer = getImageBuffer();
            setCompositeOnGraphics(g2);

            if (!imageBuffer.paint(g2, proj)) {
                OMGraphicList list = layer.getList();
                if (list != null) {
                    layer.getList().render(g2);
                }
            }

            g2.dispose();

        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + ".paint(): skipping due to projection.");
        }

    }

    protected class ImageBuffer {

        BufferedImage imageBuffer;
        OMScalingRaster imageRaster;
        Projection currentProjection;

        protected ImageBuffer() {
        }

        protected void generate(Projection proj) {
            OMScalingRaster lImageRaster = getImageRaster();
            if (lImageRaster != null) {
                if (proj instanceof Cylindrical) {
                    lImageRaster.setNeedToReposition(true);
                    lImageRaster.setNeedToRegenerate(true);
                    lImageRaster.generate(proj);
                } else {
                    setImageRaster(null);
                }
            }
        }

        /**
         * Return true if something was rendered.
         * 
         * @param g
         * @param proj
         * @return true if paint was successful for the OMScaling Raster
         */
        public boolean paint(Graphics2D g, Projection proj) {
            Projection currentProj = currentProjection;
            OMScalingRaster omr = getImageRaster();

            if (!proj.equals(currentProj)) {
                currentProjection = proj;
                if (omr != null) {
                    omr.generate(proj);
                    omr.render(g);
                    return true;
                }
            }

            /*
             * We used to have a could of calls here that painted the current
             * ImageBuffer if it wasn't null. Turns out, that's not a good idea.
             * It causes the layer to paint itself in its old location for a
             * flash before updating. So we're just going to let the
             * OMScalingRaster handling painting.
             */

            return false;
        }

        /**
         * Get the updated BufferedImage, with current OMGraphics rendered into
         * it. Called with the results of layer.prepare().
         * 
         * @param list OMGraphicList from layer's prepare method.
         * @param proj current projection that has buffer size information.
         */
        protected void update(OMGraphicList list, Projection proj) {
            BufferedImage currentImageBuffer = null;

            if (proj != null && layer != null) {

                int w = proj.getWidth();
                int h = proj.getHeight();

                currentImageBuffer = getImageBuffer();
                BufferedImage bufferedImage = scrubOrGetNewBufferedImage(currentImageBuffer, w, h);

                // Updated image for projection
                if (bufferedImage != null) {
                    if (currentImageBuffer != null) {
                        currentImageBuffer.flush();
                    }

                    currentImageBuffer = bufferedImage;
                }

                Graphics2D g2d = (Graphics2D) currentImageBuffer.getGraphics();
                setRenderingHints(g2d);

                if (list != null) {
                    list.render(g2d);
                }

                g2d.dispose();

                setImageRaster(updateRaster(currentImageBuffer, proj));
            }

            setImageBuffer(currentImageBuffer);
            currentProjection = proj;
        }

        protected OMScalingRaster updateRaster(BufferedImage imageBuffer, Projection proj) {

            if (proj instanceof Cylindrical) {

                int w = proj.getWidth();
                int h = proj.getHeight();

                // If a buffer wasn't created, bail
                if (imageBuffer == null) {
                    return null;
                }

                Point2D llp1 = proj.getUpperLeft();
                /*
                 * The lower right point is w-1, h-1, the actual pixel index,
                 * starting at 0. The size of the image is one pixel more. Using
                 * getLowerRight() leaves one pixel on the bottom and right
                 * blank in the resulting image.
                 */
                Point2D llp2 = proj.inverse(w, h);
                /*
                 * We're running into a problem here for the OMScalingRaster
                 * where the projection is providing a bad coordinate situation
                 * for OMScalingRasters when zoomed way out. The left pixel
                 * coordinate of the map, at some point in the OMScalingRaster
                 * calculations, is being placed to the right of the right pixel
                 * coordinate of the map. It's a 360 degree precision thing, and
                 * ever so slight. We're going to test for that here, and if
                 * that x test fails, we're just going to use a standard
                 * OMRaster for the Buffer.
                 */

                /*
                 * The OMScalingRaster is cool because it can respond to the
                 * projection change immediately, and display what was there
                 * before while the layers are working.
                 */
                Point2D pnt1 = proj.forward(llp1);
                Point2D pnt2 = proj.forward(llp2);

                if (pnt1.getX() < pnt2.getX() && h > 0 && w > 0) {
                    OMScalingRaster raster = getImageRaster();

                    if (raster == null) {
                        raster = new OMScalingRaster(llp1.getY(), llp1.getX(), llp2.getY(), llp2.getX(), imageBuffer);
                    } else {
                        raster.setImage(imageBuffer);
                        raster.setLat(llp1.getY());
                        raster.setLon(llp1.getX());
                        raster.setLRLat(llp2.getY());
                        raster.setLRLon(llp2.getX());
                    }

                    raster.generate(proj);
                    return raster;
                }
            }

            return null;
        }

        /**
         * Given the current image buffer, and the desired width and height of
         * the new projection, return a fresh/refreshed image buffer ready for
         * layer painting. Should be clear.
         * 
         * @param currentImage the current BufferedImage
         * @param width current projection width, pixels.
         * @param height current projection height, pixels.
         * @return new BufferedImage if one was needed, i.e. if size changed.
         */
        protected BufferedImage scrubOrGetNewBufferedImage(BufferedImage currentImage, int width,
                                                           int height) {
            int cWidth = -1;
            int cHeight = -1;

            if (currentImage != null) {
                cWidth = currentImage.getWidth();
                cHeight = currentImage.getHeight();
            }

            if (currentImage != null && cWidth == width && cHeight == height) {

                // scrub it, refresh the pixels.
                Graphics2D graphics = (Graphics2D) currentImage.getGraphics();
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, width, height);
                graphics.setComposite(AlphaComposite.SrcOver);
                return null;

            }

            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        protected void clear() {
            BufferedImage bImage = getImageBuffer();
            if (bImage != null) {
                Graphics2D graphics = (Graphics2D) bImage.getGraphics();
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
                graphics.setComposite(AlphaComposite.SrcOver);
            }

            setImageRaster(null);
        }

        protected BufferedImage getImageBuffer() {
            return imageBuffer;
        }

        protected void setImageBuffer(BufferedImage bImage) {
            if (imageBuffer != null && imageBuffer != bImage) {
                imageBuffer.flush();
            }

            imageBuffer = bImage;
        }

        /**
         * @return the imageRaster
         */
        protected OMScalingRaster getImageRaster() {
            return imageRaster;
        }

        /**
         * @param imageRaster the imageRaster to set
         */
        protected void setImageRaster(OMScalingRaster imageRaster) {
            this.imageRaster = imageRaster;
        }

    }
}