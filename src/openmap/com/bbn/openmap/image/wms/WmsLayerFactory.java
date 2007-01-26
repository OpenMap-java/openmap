/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WmsLayerFactory.java,v 1.1 2007/01/26 15:04:22 dietrick Exp $
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
 * Class responsible for creation IWmsLayer for gi
 * 
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/WmsLayerFactory.java,v
 *          1.1 2006/03/21 10:27:54 tomrak Exp $
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

    IWmsLayer createWmsLayer(Layer layer) {
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
