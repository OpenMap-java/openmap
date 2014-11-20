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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/StandardPCPolicy.java,v $
// $RCSfile: StandardPCPolicy.java,v $
// $Revision: 1.9 $
// $Date: 2006/05/19 16:44:57 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.policy;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * ProjectionChangePolicy that uses a Layer SwingWorker to kick off a thread to
 * call layer.prepare() and doesn't delete the OMGraphicList between projection
 * changes. The standard behavior for simple layers that maintain a constant
 * list of OMGraphics.
 */
public class StandardPCPolicy extends AbstractProjectionChangePolicy {

    /**
     * The number of OMGraphics that have to be on the layer's OMGraphicList
     * before a thread is spawned. The default is 50;
     */
    protected int graphicCutoff = 50;

    /**
     * A flag to tell the policy to always spawn a thread.
     */
    protected boolean alwaysSpawnThread = false;

    /**
     * You must set a layer at some point before using this class.
     */
    public StandardPCPolicy() {
    }

    /**
     * Don't pass in a null layer.
     */
    public StandardPCPolicy(OMGraphicHandlerLayer layer) {
        this(layer, true);
    }

    /**
     * Don't pass in a null layer.
     * 
     * @param layer layer to work for
     * @param alwaysSpawnThreadForPrepare should be true if the layer's prepare
     *        method takes a while. Normally, the policy looks at the number of
     *        OMGraphics on the list to determine if a thread should be spawned.
     *        True by default.
     */
    public StandardPCPolicy(OMGraphicHandlerLayer layer, boolean alwaysSpawnThreadForPrepare) {
        setLayer(layer);
        setAlwaysSpawnThread(alwaysSpawnThreadForPrepare);
    }

    /**
     * Tell the policy whether to spawn a thread when projectionChanged() is
     * called with a new projection.
     * 
     * @param val setting for always spawning thread for every projection
     *        change.
     */
    public void setAlwaysSpawnThread(boolean val) {
        alwaysSpawnThread = val;
    }

    public boolean getAlwaysSpawnThread() {
        return alwaysSpawnThread;
    }

    /**
     * When the projection changes, the StandardPCPolicy sets the current
     * projection on the layer, and calls prepare() on the layer. repaint() will
     * be automatically called. This method does not generate the OMGraphics in
     * the list. The layer is still expected to do that in prepare(), as well as
     * return that list from prepare.
     * <P>
     * If a Layer is using this PC policy, then it's kind of assumed that the
     * layer is setting the list and generating the same list over and over
     * again with the new projection. So, when we look at the min and max scales
     * set on the layer, we don't want to clear out that list if the projection
     * scale is outside of the acceptable range of good projection scales.
     * Instead, we want to skip the prepare() method call as to not waste CPU
     * cycles generating things we won't see, and let the RenderPolicy check to
     * see if the list should be painted.
     * 
     * @param pe The ProjectionEvent received from the MapBean.
     */
    public void projectionChanged(ProjectionEvent pe) {
        Logger logger = getLogger();
        if (layer != null) {
            Projection proj = layer.setProjection(pe);
            // proj will be null if the projection hasn't changed, a
            // signal that work does not need to be done.
            if (proj != null) {
                // Some criteria can decide whether
                // starting another thread is worth it...
                if (shouldSpawnThreadForPrepare()) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(getLayer().getName()
                                + ": StandardPCPolicy projectionChanged with NEW projection, spawning thread to handle it.");
                    }

                    if (layer.isProjectionOK(proj)) {
                        layer.doPrepare();
                    }
                    return;
                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(getLayer().getName()
                                + ": StandardPCPolicy projectionChanged with NEW projection, handling it within current thread.");
                    }
                    layer.fireStatusUpdate(LayerStatusEvent.START_WORKING);
                    if (layer.isProjectionOK(proj)) {
                        layer.setList(layer.prepare());
                    }

                    layer.repaint();
                }
            } else {
                layer.repaint();
            }
            layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
        } else {
            Debug.error("StandardPCPolicy.projectionChanged(): NULL layer, can't do anything.");
        }
    }

    /**
     * We're assuming that the list is somewhat constant for the layer. If the
     * number of OMGraphics on the layer's list is more than the cutoff number,
     * a thread should be launched to generate them with the new projection. You
     * can extend this method so that different criteria may be considered.
     * 
     * @return true of thread should always be spawned.
     */
    protected boolean shouldSpawnThreadForPrepare() {
        if (layer != null && !alwaysSpawnThread) {
            OMGraphicList list = layer.getList();
            if (list != null) {
                return layer.getList().size() > graphicCutoff;
            }
        }
        // If we have to create a list, might as well assume that
        // is should be done in a new thread.
        return true;
    }

    /**
     * @param number Set the number of OMGraphics that have to be on the list
     *        before a thread gets spawned to call generate() on them.
     */
    public void setGraphicCutoff(int number) {
        graphicCutoff = number;
    }

    public int getGraphicCutoff() {
        return graphicCutoff;
    }

    // <editor-fold defaultstate="collapsed" desc="Logger Code">
    /**
     * Holder for this class's Logger. This allows for lazy initialization of
     * the logger.
     */
    private static final class LoggerHolder {

        /**
         * The logger for this class
         */
        private static final Logger LOGGER = Logger.getLogger(StandardPCPolicy.class.getName());

        /**
         * Prevent instantiation
         */
        private LoggerHolder() {
            throw new AssertionError("This should never be instantiated");
        }
    }

    /**
     * Get the logger for this class.
     * 
     * @return logger for this class
     */
    private static Logger getLogger() {
        return LoggerHolder.LOGGER;
    }
    // </editor-fold>

}