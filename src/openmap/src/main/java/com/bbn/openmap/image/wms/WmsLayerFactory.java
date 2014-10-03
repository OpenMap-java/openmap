/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WmsLayerFactory.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Class responsible for creation IWmsLayer for Layers.
 * 
 * @author tomrak
 */
public class WmsLayerFactory {

    private final Map wmsLayersMap = new HashMap();

    public WmsLayerFactory(Properties props) throws WMSException {
        String wmsLayers = props.getProperty("wms.layers");
        try {
            for (Iterator it = PropUtils.parseSpacedMarkers(wmsLayers)
                    .iterator(); it.hasNext();) {
                Object key = it.next();
                // TODO: looks like layerClass is supposed to be the IWmsLayer. really messy
                Class layerClass = Class.forName(props.getProperty((String) key
                        + ".layerClass"));
                Class clazz = Class.forName(props.getProperty((String) key
                        + ".class"));
                wmsLayersMap.put(layerClass, clazz);
            }
        } catch (ClassNotFoundException ex) {
            throw new WMSException("Problem with wmsLayers configuration");
        }
    }

    /**
     * "Convert" the Layer to a IWmsLayer.
     * 
     * 1. pri: Will return the given Layer if it is a IWmsLayer.
     * 
     * 2. pri: Use the .layerClass property for a IWmsLayer that takes Layer in
     * the constructor to wrap the Layer in a IWmsLayer. TODO: This is a bit messy and
     * should be refactored.
     * 
     * 3. pri: Wrap it in DefaultLayerAdapter.
     * 
     * @param layer source layer
     * @return IWmsLayer created from layer
     */
    IWmsLayer createWmsLayer(Layer layer) {
        if (layer instanceof IWmsLayer) {
            return (IWmsLayer) layer;
        }
        Class layerClass = layer.getClass();
        Class wmsLayerClass = (Class) wmsLayersMap.get(layerClass);
        if (wmsLayerClass == null) {
            return new DefaultLayerAdapter(layer);
        }
        IWmsLayer wmsLayer = null;
        try {
            Constructor constructor = wmsLayerClass.getConstructor(new Class[] { Layer.class });
            wmsLayer = (IWmsLayer) constructor.newInstance(new Object[] { layer });
        } catch (Exception ex) {
            Debug.message("ms", "Problem calling constructor for class "
                    + wmsLayerClass.getName() + ":" + ex.getMessage());
        }
        if (wmsLayer == null) {
            wmsLayer = new DefaultLayerAdapter(layer);
        }
        return wmsLayer;
    }

}
