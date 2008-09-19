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
     * @return
     */
    public String getTitle();

    /**
     * @return a String representing the short layer name. It is like
     *         {@link Layer#getPropertyPrefix()} and are used in WMS Urls.
     */
    public String getWmsName();

    /**
     * @return
     */
    public String getAbstract();

    /**
     * @return
     */
    public boolean isQueryable();

    /**
     * Do a GetFeatureInfo query.
     * 
     * @param x
     * @param y
     * @return
     */
    public LayerFeatureInfoResponse query(int x, int y);

    /**
     * @return
     */
    public IWmsLayerStyle[] getStyles();

    /**
     * Ustawia styl w warstwie.
     * 
     * @param name
     */
    public void setStyle(String name);

    /**
     * @param name
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

}
