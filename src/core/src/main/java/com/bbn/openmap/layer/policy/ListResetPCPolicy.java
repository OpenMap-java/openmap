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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/ListResetPCPolicy.java,v $
// $RCSfile: ListResetPCPolicy.java,v $
// $Revision: 1.8 $
// $Date: 2005/09/13 14:33:11 $
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

/**
 * ProjectionChangePolicy that uses a Layer SwingWorker to kick off a thread to
 * call layer.prepare() and deletes the current OMGraphicList between projection
 * changes. The standard behavior for layers that gather new OMGraphics for new
 * projections.
 */
public class ListResetPCPolicy extends AbstractProjectionChangePolicy {

    /**
     * You MUST set a layer at some point.
     */
    public ListResetPCPolicy() {
    }

    /**
     * Don't pass in a null layer.
     * 
     * @param layer OMGraphicHandlerLayer
     */
    public ListResetPCPolicy(OMGraphicHandlerLayer layer) {
        setLayer(layer);
    }

    /**
     * The method that is called when the projection changes. For the
     * ListResetPCPolicy, the current OMGraphicList for the layer is cleared.
     * 
     * @param pe the ProjectionEvent received from the MapBean when the
     *        projection changes.
     */
    public void projectionChanged(ProjectionEvent pe) {

        Logger logger = getLogger();
        if (layer != null) {
            Projection proj = layer.setProjection(pe);
            // proj will be null if the projection hasn't changed, a
            // signal that work does not need to be done.
            if (proj != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(getLayer().getName()
                            + ": projectionChanged with NEW projection, resetting list.");
                }

                /*
                 * Check and see of the layer list is not null. If it isn't,
                 * just replace it with an empty list. We want a null list to
                 * mean the layer isn't contributing to the map. Later, in
                 * workerComplete, if a null list is to be replaced by an empty
                 * list, repaint will not be called.
                 */
                if (layer.getList() != null) {
                    layer.setList(new OMGraphicList());
                }

                // Check to see if the projection is worth reacting to.
                if (layer.isProjectionOK(proj)) {
                    layer.doPrepare();
                }
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(getLayer().getName()
                            + ": projectionChanged with OLD projection, repainting.");
                }
                if (!layer.isWorking()) {
                    // This repaint may look redundant, but it handles
                    // the situation where a layer is removed from a
                    // map and readded when the projection doesn't
                    // change. Since it already had the projection,
                    // and remove() hasn't been called yet, the proj
                    // == null. When the new layer is added, it
                    // receives a projectionChanged call, and even
                    // though it's all set, it still needs to call
                    // repaint to have itself show up on the map.
                    layer.repaint();
                    layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
                }
            }
        } else {
            logger.warning("NULL layer, can't do anything.");
        }
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
        private static final Logger LOGGER = Logger.getLogger(ListResetPCPolicy.class.getName());

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