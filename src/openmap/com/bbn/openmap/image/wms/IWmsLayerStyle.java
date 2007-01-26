/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayerStyle.java,v 1.1 2007/01/26 15:04:23 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/IWmsLayerStyle.java,v
 *          1.1 2006/03/21 10:27:54 tomrak Exp $
 * @author pitek
 */
public interface IWmsLayerStyle {

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
    // public IWmsLegend getLegend();
}
