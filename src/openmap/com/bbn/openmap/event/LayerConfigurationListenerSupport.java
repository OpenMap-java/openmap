/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.event;

import java.util.List;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;

/**
 * ListenerSupport for the LayerHandler to use for managing LayerConfigurationListeners.
 * 
 * @author dietrick
 */
public class LayerConfigurationListenerSupport extends ListenerSupport<LayerConfigurationListener> {

    /**
     * @param sourceBean The bean to be given as the source for any events
     */
    public LayerConfigurationListenerSupport(Object sourceBean) {
        super(sourceBean);
    }

    /**
     * Check the list of layers, make changes as required.
     * 
     * @param layerList the list of layers to check.
     */
    public synchronized List<Layer> checkLayerConfiguration(List<Layer> layerList) {
        if (size() == 0)
            return null;

        List<Layer> newList = null;

        for (LayerConfigurationListener listener : this) {
            newList = listener.checkLayerConfiguration(layerList);

            // We'll see how this works out. Changes made to the list are passed
            // to the next listener. Otherwise we pass the original list. If, at
            // the end, newList is null, we don't bother telling the
            // LayerHandler about the changes.
            if (newList != null) {
                layerList = newList;
            }
        }

        return newList;
    }

}
