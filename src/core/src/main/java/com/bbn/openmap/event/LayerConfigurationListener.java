/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.event;

import java.util.List;

import com.bbn.openmap.Layer;

/**
 * A LayerConfigurationListener is a component that receives LayerEvents from
 * the LayerHandler before changes are implemented, so conditions can be
 * implemented to the layer list. These conditions may include controlling which
 * layers are enabled at the same time, or controlling the projection used when
 * certain layers are activated.
 * 
 * You can add this component to the components property in the
 * openmap.properties file, or simply add it to the MapHandler. The LayerHandler
 * will find it and add it as a listener, and then start making calls to the
 * checkLayerConfiguration method.
 * 
 * The LayerConfiguratListener.checkLayerConfiguration(List layers) is called
 * before new changes are applied to the layer cake in the application. If
 * changes are desired, they should be returned from this method. If no changes
 * are required, then null should be returned. You should only return something
 * from this method if the layer order is modified, or layers should be added or
 * removed from the LayerHandler. If you add or remove layers here, they will
 * not be added to the MapHandler.
 * 
 * @author dietrick
 */
public interface LayerConfigurationListener {
    /**
     * The LayerConfiguratListener.checkLayerConfiguration(List layers) is
     * called before new changes are applied to the layer cake in the
     * application. If changes are desired, they should be returned from this
     * method. If no changes are required, then null should be returned.
     * 
     * @param layers a List of layers
     * @return the layers as they should be modified. If you just want to change
     *         which layers will be visible on the map, you can toggle the
     *         visibility of the layer directory (setVisible(boolean)), and you
     *         don't have to return anything. Anything you return will be set as
     *         the available Layer list in the LayerHandler. Use with care.
     */
    List<Layer> checkLayerConfiguration(List<Layer> layers);
}
