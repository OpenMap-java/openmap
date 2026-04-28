/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.event;

import java.util.List;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.layer.rpf.RpfLayer;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;

/**
 * A LayerToggleConfigurationListener is a LayerConfigurationListener
 * implementation that demonstrates how to create a component that might make
 * adjustments to a List of layers being changed in a LayerHandler, before the
 * changes hit the map. Right now, the only this component does is change the
 * current projection to CADRG if an RPF layer is made active.
 * 
 * You can add this component to the components property in the
 * openmap.properties file, or simply add it to the MapHandler. The LayerHandler
 * will find it and add it as a listener, and then start making calls to the
 * checkLayerConfiguration method.
 * 
 * If you want to adjust the visibility of layers, so that layers turn off or on
 * based on other layers being activated, you can just set the visibility of the
 * layer in this method without returning anything from the
 * checkLayerConfiguration method. You only have to return a new List if you
 * want to change the order, add or remove layers from the application.
 * 
 * @author dietrick
 */
public class BasicLayerConfigurationListener extends OMComponent implements
        LayerConfigurationListener {

    public BasicLayerConfigurationListener() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.
     * PropertyChangeEvent)
     */
    public List<Layer> checkLayerConfiguration(List<Layer> layers) {
        // Let's say, whenever an RpfLayer is made active, I want to change the
        // projection on the map to the CADRG projection.
        for (Layer layer : layers) {
            if (layer instanceof RpfLayer && layer.isVisible()) {
                MapBean mapBean = ((MapHandler) getBeanContext()).get(com.bbn.openmap.MapBean.class);
                if (mapBean != null) {
                    Projection proj = mapBean.getProjection();
                    if (!(proj instanceof CADRG)) {
                        ProjectionFactory pFactory = mapBean.getProjectionFactory();
                        if (pFactory != null) {
                            Projection newProj = pFactory.makeProjection("com.bbn.openmap.proj.CADRG", proj);
                            if (newProj != null) {
                                mapBean.setProjection(newProj);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

}
