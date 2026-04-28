/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v 1.3 2008/09/19 14:20:14 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.util.Properties;

/**
 * @version $Header:
 *          /cvs/openmap/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v
 *          1.2.2.1 2008/08/20 11:28:43 halset Exp $
 * @author pitek
 */
public interface IWmsLayer {

    /**
     * @return a string for the layer title
     */
    public String getTitle();

    /**
     * @return a String representing the short layer name. It is like
     *         {@link com.bbn.openmap.Layer#getPropertyPrefix()} and are used in WMS Urls.
     */
    public String getWmsName();

    /**
     * @return a string description of the layer
     */
    public String getAbstract();

    /**
     * @return true if layer is queryable
     */
    public boolean isQueryable();

    /**
     * Do a GetFeatureInfo query.
     * 
     * @param x
     * @param y
     * @return LayerFeatureInfoResponse from query 
     */
    public LayerFeatureInfoResponse query(int x, int y);

    /**
     * @return IWmsLayerStyle array of available styles.
     */
    public IWmsLayerStyle[] getStyles();

    /**
     * Set the style in the layer
     * 
     * @param name style name
     */
    public void setStyle(String name);

    /**
     * @param name style name for query
     * @return true if style supported
     */
    public boolean isStyleSupported(String name);

    /**
     */
    public void setDefaultStyle();

    /**
     * OpenMap will use this method to set request parameters to the layer. The
     * layer can then pick up extra non-standard parameters if needed.
     * <p>
     * For nested layers, this method may be called several times.
     * <p>
     * See wms-1.1.1 chapter 6.5.11 Vendor-Specific Parameters
     * 
     * @param requestParameters
     */
    public void setRequestParameters(Properties requestParameters);

    /**
     * Return a {@link Legend} with legend information or null if legend can not
     * be created.
     * 
     * @return Legend for layer.
     */
    public Legend getLegend();
    
}
