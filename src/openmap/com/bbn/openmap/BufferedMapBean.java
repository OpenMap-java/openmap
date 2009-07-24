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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;

import com.bbn.openmap.util.Debug;

/**
 * The BufferedMapBean extends the MapBean by adding (you guessed it) buffering.
 * <p>
 * Specifically, the layers are stored in a java.awt.Image so that the frequent
 * painting done by Swing on lightweight components will not cause the layers to
 * do unnecessary work rerendering themselves each time.
 * <P>
 * Changing the default clipping area may cause some Layers to not be drawn
 * completely, depending on what the clipping area is set to and when the layer
 * is trying to get itself painted. When manually adjusting clipping area, make
 * sure that when restricted clipping is over that a full repaint occurs if
 * there is a chance that another layer may be trying to paint itself.
 */
public class BufferedMapBean extends MapBean {

    protected boolean bufferDirty = true;
    protected Image drawingBuffer = null;

    public BufferedMapBean() {
        super();
    }

    public BufferedMapBean(boolean useThreadedNotification) {
        super(useThreadedNotification);
    }

    /**
     * Set the layers of the MapBean.
     * 
     * @param evt LayerEvent
     */
    public void setLayers(com.bbn.openmap.event.LayerEvent evt) {

        bufferDirty = true;
        super.setLayers(evt);
    }

    /**
     * Invoked when component has been resized. Layer buffer is nullified. and
     * super.componentResized(e) is called.
     * 
     * @param e ComponentEvent
     */
    public void componentResized(ComponentEvent e) {
        // reset drawingBuffer
        boolean bad = false;
        try {
            if (drawingBuffer != null) {
                drawingBuffer.flush();
            }
            drawingBuffer = createImage(getWidth(), getHeight());
        } catch (java.lang.NegativeArraySizeException nae) {
            bad = true;
        } catch (java.lang.IllegalArgumentException iae) {
            bad = true;
        }

        if (bad) {
            Debug.message("mapbean",
                    "BufferedMapBean: component resizing is not valid for buffer.");
            drawingBuffer = createImage(1, 1);
        }

        super.componentResized(e);
    }

    /**
     * Paint the child components of this component.
     * <p>
     * WE STRONGLY RECOMMEND THAT YOU DO NOT OVERRIDE THIS METHOD The map layers
     * are buffered in an Image which is drawn to the screen. The buffer is
     * refreshed after repaint() is called on a layer.
     * <p>
     * In our view, paint() is called on the MapBean excessively, such as when
     * tool tips are displayed and removed on the LayerPanel, or on when menu
     * items are highlighted. This method should greatly reduce the number of
     * times Layers are rendered.
     * 
     * @param g Graphics
     */
    public void paintChildren(Graphics g) {
        paintChildren(g, null);
    }

    /**
     * Same as paintChildren, but allows you to set a clipping area to paint. Be
     * careful with this, because if the clipping area is set while some layer
     * decides to paint itself, that layer may not have all it's objects
     * painted. Same warnings apply.
     */
    public void paintChildren(Graphics g, Rectangle clip) {

        // if a layer has requested a render, then we render all of
        // them into
        // a drawing buffer
        if (bufferDirty) {
            bufferDirty = false;

            int w = getWidth();
            int h = getHeight();

            if (drawingBuffer == null) {
                drawingBuffer = createVolatileImage(w, h);
            }

            // draw the old image
            Graphics gr = getMapBeanRepaintPolicy().modifyGraphicsForPainting(drawingBuffer.getGraphics());

            if (clip == null) {
                gr.setClip(0, 0, w, h);
            } else {
                gr.setClip(clip);
            }
            // gr.drawImage(drawingBuffer,0,0,null);
            if (Debug.debugging("mapbean")) {
                Debug.output("BufferedMapBean.rendering layers to buffer.");
            }

            paintChildrenWithBorder(gr, false);

            // reset the clip to full map
            // gr.setClip(0, 0, w, h);
            gr.dispose();
        } else if (Debug.debugging("mapbean")) {
            Debug.output("BufferedMapBean.rendering buffer.");
        }

        Image daImage = drawingBuffer;

        // Should be be clipping the graphics here? I'm not sure.
        // Think so.
        if (clip != null) {
            g.setClip(clip);
        }

        if (rotHelper != null) {
            daImage = rotHelper.paintChildren(g, clip);
        }

        // draw the buffer to the screen, daImage will be drawingBuffer without rotation
        g.drawImage(daImage, 0, 0, null);

        // Take care of the PaintListeners for no rotation
        if (rotHelper == null && painters != null) {
            painters.paint(g);
        }
        
        // border gets overwritten accidentally, so redraw it now
        paintBorder(g);
    }

    /**
     * Interface-like method to query if the MapBean is buffered, so you can
     * control behavior better. Allows the removal of specific instance-like
     * queries for, say, BufferedMapBean, when all you really want to know is
     * if you have the data is buffered, and if so, should be buffer be cleared.
     * For the BufferedMapBean, always true.
     */
    public boolean isBuffered() {
        return true;
    }

    /**
     * Marks the image buffer as dirty if value is false. On the next
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

    public void dispose() {
        if (drawingBuffer != null) {
            drawingBuffer.flush();
        }
        drawingBuffer = null;
        super.dispose();
    }
}