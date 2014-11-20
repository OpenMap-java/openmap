/*
 * AbstractProjectionChangePolicy.java        Apr 29, 2014 11:25:15 PM
 *
 * Copyright (c)  2014-2014 CSC, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * CSC, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with CSC.
 *
 */

package com.bbn.openmap.layer.policy;

import java.util.logging.Logger;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * Implements the common functionality of all projection change policies.
 * 
 * @author dietrick
 */
public abstract class AbstractProjectionChangePolicy implements ProjectionChangePolicy {

    protected OMGraphicHandlerLayer layer;

    protected AbstractProjectionChangePolicy() {
    }

    public AbstractProjectionChangePolicy(OMGraphicHandlerLayer omghl) {
        layer = omghl;
    }

    public void setLayer(OMGraphicHandlerLayer omghl) {
        this.layer = omghl;
    }

    public OMGraphicHandlerLayer getLayer() {
        return this.layer;
    }

    /**
     * This is a subtle call, that dictates what should happen when the
     * LayerWorker has completed working in it's thread. The LayerWorker.get()
     * method returns whatever was returned in the OMGraphicHandler.prepare()
     * method, an OMGraphicList. In most cases, this object should be set as the
     * Layer's list at this time. Some Layers, working asynchronously with their
     * data sources, might want nothing to happen and should use a policy that
     * overrides this method so that nothing does.
     * <P>
     * 
     * Modified as of 5.1.2/5.1 to control when layer.repaint() is called. If
     * the previous OMGraphicList is null, and the current OMGraphicList is also
     * null, then repaint is not called. This is to cut back on a flashing
     * effect when layers that aren't doing anything call for repaints before
     * those that are call for painting.
     * 
     * @param aList the current OMGraphicList returned from the prepare() method
     *        via the SwingWorker thread.
     */
    public void workerComplete(OMGraphicList aList) {
        if (layer != null) {
            boolean repaintIt = layer.getList() != null;
            layer.setList(aList);

            // Don't call repaint if the layer list was null, and still is.
            if (repaintIt || aList != null) {
                layer.repaint();
            } else {
                getLogger().fine("Not painting cause of nothin'");
            }
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
        private static final Logger LOGGER = Logger.getLogger(AbstractProjectionChangePolicy.class.getName());

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
