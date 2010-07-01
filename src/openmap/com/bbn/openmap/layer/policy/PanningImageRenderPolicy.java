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
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;

/**
 * The PanningImageRenderPolicy renders all OMGraphics into a buffering image,
 * and responds to projection changes by immediately rendering a translated
 * version of the image at a new projection location before calling prepare() on
 * the layer to get the OMGraphics for the new projection and untranslated
 * image. When the new image is ready, it will be painted. Using this policy on
 * background layers eliminates the flashing effect seen when panning or zooming
 * a map when a faster-responding layer is on top.
 * <P>
 * You can set this object on an OMGraphicHandlerLayer through the API, or set it in the properties:
 * <pre>
 * layer.renderPolicy=rp
 * layer.rp.class=com.bbn.openmap.layer.policy.PanningImageRenderPolicy
 * </pre> 
 */
public class PanningImageRenderPolicy extends RenderingHintsRenderPolicy {

    protected BufferedImage buffer = null;

    protected Point2D offset = new Point2D.Double();
    protected Point2D oldUL;
    protected float oldScale;
    protected Class<? extends Proj> oldProjType = null;

    /**
     * Set the layer at some point before use.
     */
    public PanningImageRenderPolicy() {
        super();
    }

    /**
     * Don't pass in a null layer.
     */
    public PanningImageRenderPolicy(OMGraphicHandlerLayer layer) {
        super(layer);
    }

    public OMGraphicList prepare() {
        if (layer != null) {
            // setBuffer(null);
            Projection proj = layer.getProjection();
            if (layer.isProjectionOK(proj)) {
                // set the offsets depending on how much the image moves
                Point2D ul = proj.getUpperLeft();
                if (oldUL != null && !oldUL.equals(ul)
                        && oldScale == proj.getScale()
                        && proj.getClass().equals(oldProjType)) {
                    Point2D currentPoint = proj.forward(ul);
                    Point2D oldPoint = proj.forward(oldUL);

                    offset.setLocation(oldPoint.getX() - currentPoint.getX(),
                            oldPoint.getY() - currentPoint.getY());

                    layer.repaint();
                }

                oldUL = ul;
                oldProjType = ((Proj) proj).getClass();
                oldScale = proj.getScale();

                OMGraphicList list = layer.prepare();
                setBuffer(createAndPaintImageBuffer(list));

                return list;
            } else {
                logger.warning("NULL projection, can't do anything.");
            }
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
        Graphics2D g2 = (Graphics2D) g;

        if (layer.isProjectionOK(proj)) {

            if (getBuffer() == null) {
                // Not sure how we get here, but it's here just in case so that
                // the list might get painted if it exists and the buffered
                // image wasn't created.
                logger.fine("creating image buffer in paint");
                if (list != null) {
                    setBuffer(createAndPaintImageBuffer(list));
                }
            }

            BufferedImage bufferedImage = getBuffer();

            setCompositeOnGraphics(g2);

            if (bufferedImage != null) {

                if (proj != null) {
                    // Gets reset by JComponent
                    g.setClip(0, 0, proj.getWidth(), proj.getHeight());
                }

                AffineTransform af = new AffineTransform();
                af.translate(offset.getX(), offset.getY());
                g2.drawRenderedImage((BufferedImage) bufferedImage, af);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("RenderingPolicy:" + layer.getName()
                            + ": rendering buffer");
                }

            } else if (list != null) {
                super.setRenderingHints(g);
                list.render(g);

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(layer.getName() + ": rendering list directly");
                }
            }
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName()
                    + ".paint(): "
                    + (list == null ? "NULL list, skipping..."
                            : " skipping due to projection."));
        }

    }

    /** Get the BufferedImage for the layer. */
    protected BufferedImage getBuffer() {
        return buffer;
    }

    /** Set the BufferedImage for the layer. */
    protected void setBuffer(BufferedImage bi) {
        offset.setLocation(0, 0);
        buffer = bi;
    }

    protected BufferedImage createAndPaintImageBuffer(OMGraphicList list) {
        BufferedImage bufferedImage = null;
        if (list != null && layer != null) {
            int w = layer.getProjection().getWidth();
            int h = layer.getProjection().getHeight();
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
            super.setRenderingHints(g2d);
            list.render(g2d);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(layer.getName() + ": rendering list into buffer");
            }
        }
        return bufferedImage;
    }
}