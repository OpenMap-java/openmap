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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/BufferedMapBean.java,v $
// $RCSfile: BufferedMapBean.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.proj.Projection;

/**
 * The BufferedMapBean extends the MapBean by adding (you guessed it) buffering.
 * <p>
 * Specifically, the layers are stored in a java.awt.Image so that the frequent
 * painting done by Swing on lightweight components will not cause the layers to
 * do unnecessary work re-rendering themselves each time.
 * <P>
 * Changing the default clipping area may cause some Layers to not be drawn
 * completely, depending on what the clipping area is set to and when the layer
 * is trying to get itself painted. When manually adjusting clipping area, make
 * sure that when restricted clipping is over that a full repaint occurs if
 * there is a chance that another layer may be trying to paint itself.
 */
public class BufferedMapBean extends MapBean {

    private static Logger logger = Logger.getLogger(BufferedMapBean.class.getName());
    protected boolean bufferDirty = true;
    protected BufferedImage drawingBuffer = null;

    protected PanHelper panningTransform = null;

    public BufferedMapBean() {
        super();
    }

    public BufferedMapBean(boolean useThreadedNotification) {
        super(useThreadedNotification);
    }

    /**
     * Invoked when component has been resized. Layer buffer is nullified. and
     * super.componentResized(e) is called.
     * 
     * @param e ComponentEvent
     */
    public void componentResized(ComponentEvent e) {
        setBufferDirty(true);

        super.componentResized(e);
    }

    /**
     * Provide a drawing buffer for the layers based on the projection
     * parameters. If the currentImageBuffer is the right size, the pixels will
     * be cleared.
     * 
     * @param currentImageBuffer the buffer to reuse and return, if the size is
     *        appropriate. Flushed if another BufferedImage is returned.
     * @param proj the current projection of the map
     * @return BufferedImage to be used for image buffer.
     */
    protected BufferedImage resetDrawingBuffer(BufferedImage currentImageBuffer, Projection proj) {
        try {

            int w = proj.getWidth();
            int h = proj.getHeight();

            if (currentImageBuffer != null) {
                int cibWidth = currentImageBuffer.getWidth();
                int cibHeight = currentImageBuffer.getHeight();

                if (cibWidth == w && cibHeight == h) {
                    Graphics2D graphics = (Graphics2D) currentImageBuffer.getGraphics();
                    graphics.setComposite(AlphaComposite.Clear);
                    graphics.fillRect(0, 0, w, h);
                    graphics.setComposite(AlphaComposite.SrcOver);
                    return currentImageBuffer;
                } else {
                    currentImageBuffer.flush();
                }
            }

            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        } catch (java.lang.NegativeArraySizeException nae) {
        } catch (java.lang.IllegalArgumentException iae) {
        }

        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Same as paintChildren, but allows you to set a clipping area to paint. Be
     * careful with this, because if the clipping area is set while some layer
     * decides to paint itself, that layer may not have all it's objects
     * painted. Same warnings apply.
     */
    public void paintChildren(Graphics g, Rectangle clip) {

        // if a layer has requested a render, then we render all of
        // them into a drawing buffer
        BufferedImage localDrawingBuffer = drawingBuffer;

        if (panningTransform == null && bufferDirty) {
            bufferDirty = false;

            localDrawingBuffer = resetDrawingBuffer(localDrawingBuffer, getProjection());
            // In case it's been resized
            drawingBuffer = localDrawingBuffer;

            // draw the old image
            Graphics gr = getMapBeanRepaintPolicy().modifyGraphicsForPainting(localDrawingBuffer.getGraphics());

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("BufferedMapBean rendering layers to buffer.");
            }

            super.paintChildren(gr, null);
            gr.dispose();
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine("BufferedMapBean rendering buffer.");
        }

        if (panningTransform != null) {

            panningTransform.render((Graphics2D) g);
            return;

        } else if (localDrawingBuffer != null) {

            RotationHelper rotHelper = getRotHelper();

            if (rotHelper != null) {
                rotHelper.paintChildren(g, clip);
                rotHelper.paintPainters(g);
            } else {
                drawProjectionBackground(g);
                // draw the buffer to the screen, daImage will be drawingBuffer
                // without rotation
                g.drawImage(localDrawingBuffer, 0, 0, null);

                painters.paint(g);
            }
        }
    }

    /**
     * Interface-like method to query if the MapBean is buffered, so you can
     * control behavior better. Allows the removal of specific instance-like
     * queries for, say, BufferedMapBean, when all you really want to know is if
     * you have the data is buffered, and if so, should be buffer be cleared.
     * For the BufferedMapBean, always true.
     */
    public boolean isBuffered() {
        return true;
    }

    /**
     * Marks the image buffer as dirty if value is true. On the next
     * <code>paintChildren()</code>, we will call <code>paint()</code> on all
     * Layer components.
     * 
     * @param value boolean
     */
    public void setBufferDirty(boolean value) {
        bufferDirty = value;
    }

    /**
     * Checks whether the image buffer should be repainted.
     * 
     * @return boolean whether the layer buffer is dirty
     */
    public boolean isBufferDirty() {
        return bufferDirty;
    }

    /**
     * Clear out resources for the current drawing buffer.
     */
    protected void disposeDrawingBuffer() {
        Image localDrawingBuffer = drawingBuffer;
        drawingBuffer = null;
        if (localDrawingBuffer != null) {
            localDrawingBuffer.flush();
        }
    }

    public void dispose() {
        disposeDrawingBuffer();
        super.dispose();
    }

    public AffineTransform getPanningTransform() {
        return panningTransform;
    }

    /**
     * Set a panning transform on the buffer for rendering in a different place,
     * quickly. Sets the buffer to be dirty, so when the panning transform is
     * removed, it will be recreated.
     * 
     * @param transform
     */
    public void setPanningTransform(AffineTransform transform) {
        if (transform != null) {
            if (panningTransform == null) {
                panningTransform = new PanHelper(transform);
                setBufferDirty(true);
            } else {
                panningTransform.update(transform);
            }
        } else {
            if (panningTransform != null) {
                panningTransform.dispose();
            }
            panningTransform = null;
        }
    }

    protected class PanHelper extends AffineTransform {
        protected Image buffer;

        protected PanHelper(AffineTransform aft) {
            super(aft);
            this.buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            paintChildren(this.buffer.getGraphics(), null);
        }

        protected void update(AffineTransform aft) {
            super.setTransform(aft);
        }

        protected void render(Graphics2D g) {
            drawProjectionBackground(g);
            ((Graphics2D) g).setTransform(this);
            if (buffer != null) {
                g.drawImage(buffer, 0, 0, null);
            }

            RotationHelper rotationHelper = getRotHelper();
            if (rotationHelper == null) {
                painters.paint(g);
            }
        }

        protected void dispose() {
            if (buffer != null) {
                buffer.flush();
                buffer = null;
            }
        }

    }
}