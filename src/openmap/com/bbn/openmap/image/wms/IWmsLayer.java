/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v 1.1 2007/01/26 15:04:23 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

/**
 * @version $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayer.java,v 1.1 2007/01/26 15:04:23 dietrick Exp $
 * @author pitek
 */
public interface IWmsLayer {

    /**
     * @return
     */
    public String getTitle();

    /**
     * @return
     */
    public String getName();

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
