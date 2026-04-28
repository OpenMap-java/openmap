/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/IWmsLayerStyle.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap
 *          /wms/IWmsLayerStyle.java,v 1.1 2006/03/21 10:27:54 tomrak Exp $
 * @author pitek
 */
public interface IWmsLayerStyle {

    /**
     * @return title
     */
    public String getTitle();

    /**
     * @return name
     */
    public String getName();

    /**
     * @return abstract
     */
    public String getAbstract();
    // public IWmsLegend getLegend();
}
