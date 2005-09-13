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
// $Revision: 1.7 $
// $Date: 2005/09/13 14:33:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

/**
 * The BufferedImageRenderPolicy is a RenderPolicy that creates and
 * uses an image buffer based on the painting times for the layer. If
 * the time to paint exceeds the bufferTiggerDelay, an image buffer
 * for the layer is used for paints as long as the projection doesn't
 * change. A new buffer is used for a projection change because we
 * need the image buffer to be transparent for parts of the map that
 * are not used by the layer.
 */
public class BufferedImageRenderPolicy extends RenderingHintsRenderPolicy {

    public final static long bufferTriggerDelay = 150;

    protected BufferedImage buffer = null;

    protected boolean useImageBuffer = false;
    
    protected boolean DEBUG = false;
    
    /**
     * Set the layer at some point before use.
     */
    public BufferedImageRenderPolicy() {
        super();
        DEBUG = Debug.debugging("policy");
    }

    /**
     * Don't pass in a null layer.
     */
    public BufferedImageRenderPolicy(OMGraphicHandlerLayer layer) {
        super(layer);
        DEBUG = Debug.debugging("policy");
    }

    public OMGraphicList prepare() {
        if (layer != null) {
            setBuffer(null);
            OMGraphicList list = layer.prepare();
            if (isUseImageBuffer()) {
                setBuffer(createAndPaintImageBuffer(list));
            }
            return list;
        } else {
            Debug.error("BufferedImageRenderPolicy.prepare():  NULL layer, can't do anything.");
        }
        return null;
    }

    public void paint(Graphics g) {
        if (layer == null) {
            Debug.error("BufferedImageRenderPolicy.paint():  NULL layer, skipping...");
            return;
        }

        OMGraphicList list = layer.getList();
        Projection proj = layer.getProjection();

        if (list != null && layer.isProjectionOK(proj)) {

            if (isUseImageBuffer() && getBuffer() == null) {
                setBuffer(createAndPaintImageBuffer(list));
            }

            BufferedImage bufferedImage = getBuffer();

            if (bufferedImage != null) {

                if (proj != null) {
                    // Gets reset by JComponent
                    g.setClip(0, 0, proj.getWidth(), proj.getHeight());
                }

                ((Graphics2D) g).drawRenderedImage((BufferedImage) bufferedImage,
                        new AffineTransform());
                if (DEBUG) {
                    Debug.output("RenderingPolicy:" + layer.getName()
                            + ": rendering buffer");
                }

                if (!isUseImageBuffer()) {
                    setBuffer(null);
                }
            } else {
                super.setRenderingHints(g);
                long startPaint = System.currentTimeMillis();
                list.render(g);
                long endPaint = System.currentTimeMillis();

                if (endPaint - startPaint > bufferTriggerDelay) {
                    setUseImageBuffer(true);
                }

                if (DEBUG) {
                    Debug.output("RenderingPolicy:" + layer.getName()
                            + ": rendering list, buffer(" + isUseImageBuffer()
                            + ")");
                }
            }
        } else if (DEBUG) {
            Debug.output(layer.getName()
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
            long startPaint = System.currentTimeMillis();
            list.render(g2d);
            long endPaint = System.currentTimeMillis();
            if (Debug.debugging("policy")) {
                Debug.output("RenderingPolicy:" + layer.getName()
                        + ": rendering list into buffer");
            }
            if (endPaint - startPaint < bufferTriggerDelay) {
                // OK, paint didn't take that long, don't use buffer
                // on the next time around. Since we've gone through
                // the effort of creating an image that's painted, use
                // it.
                setUseImageBuffer(false);
            }
        }
        return bufferedImage;
    }

    public synchronized void setUseImageBuffer(boolean value) {
        useImageBuffer = value;
    }

    public synchronized boolean isUseImageBuffer() {
        return useImageBuffer;
    }
}