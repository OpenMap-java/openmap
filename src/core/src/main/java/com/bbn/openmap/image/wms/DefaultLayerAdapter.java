/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/DefaultLayerAdapter.java,v 1.3 2008/09/19 14:20:14 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.util.Properties;

import com.bbn.openmap.Layer;

/**
 * Used to wrap a {@link Layer}s that does not implement {@link IWmsLayer}
 * 
 * @version $Header:
 *          /cvs/openmap/openmap/src/openmap/com/bbn/openmap/image/wms/DefaultLayerAdapter.java,v
 *          1.2.2.1 2008/08/20 11:28:43 halset Exp $
 * @author pitek
 */
class DefaultLayerAdapter implements IWmsLayer {

    private static final IWmsLayerStyle[] defaultStyle = new IWmsLayerStyle[1];

    static {
        defaultStyle[0] = new DefaultWmsLayerStyle("default", "Default style", null);
    }
    
    protected final Layer layer;

    /**
     * Creates a new instance of OpenMapLayerAdapter
     * 
     * @param layer
     */
    public DefaultLayerAdapter(Layer layer) {
        this.layer = layer;
    }

    /**
     * @return
     */
    public boolean isQueryable() {
        return false;
    }

    public LayerFeatureInfoResponse query(int x, int y) {
        return new LayerFeatureInfoResponse() {

            public void output(String contentType, StringBuffer out) {
            }

        };
    }

    /**
     * @return
     */
    public String getTitle() {
        return layer.getName();
    }

    /**
     * @return
     */
    public IWmsLayerStyle[] getStyles() {
        return defaultStyle;
    }

    /**
     * @return
     */
    public String getWmsName() {
        return layer.getPropertyPrefix();
    }

    /**
     * @return
     */
    public String getAbstract() {
        return layer.getName();
    }

    /**
     * @param name
     * @throws IllegalArgumentException
     */
    public void setStyle(String name) {
        if (!isStyleSupported(name)) {
            throw new IllegalArgumentException("Unsupported style " + name);
        }
    }

    /**
     * @param name
     * @return
     */
    public boolean isStyleSupported(String name) {
        IWmsLayerStyle[] styles = getStyles();
        for (int i = 0; i < styles.length; i++) {
            IWmsLayerStyle style = styles[i];
            if (style.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public void setDefaultStyle() {
        // DO NOTHING
    }

    public void setRequestParameters(Properties requestParameters) {
        // DO NOTHING
    }

    public Legend getLegend() {
        return null;
    }

}
