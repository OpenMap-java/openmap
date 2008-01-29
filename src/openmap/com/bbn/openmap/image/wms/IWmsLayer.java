/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

/**
 * @version $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
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
    public String getKeywordList();

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
    public boolean isCascaded();

    /**
     * @return
     */
    public boolean isOpaque();

    /**
     * @return
     */
    public boolean isNoSubsets();

    /**
     * @return
     */
    public int getFixedWidth();

    /**
     * @return
     */
    public int getFixedHeight();

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
    
}
