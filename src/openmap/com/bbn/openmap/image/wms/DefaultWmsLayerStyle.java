/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/DefaultWmsLayerStyle.java,v 1.1 2007/01/26 15:04:22 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;


/**
 * @version $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/DefaultWmsLayerStyle.java,v 1.1 2007/01/26 15:04:22 dietrick Exp $
 * @author pitek
 */
public class DefaultWmsLayerStyle implements IWmsLayerStyle {

    private final String name;
    private final String title;
    private final String styleAbstract;

    /**
     * Creates a new instance of DefaultWmsLayerStyle
     *
     * @param name
     * @param title
     * @param styleAbstract
     */
    public DefaultWmsLayerStyle(String name, String title, String styleAbstract) {
        this.name = name;
        this.title = title;
        this.styleAbstract = styleAbstract;
    }

    /**
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getAbstract() {
        return styleAbstract;
    }
}
