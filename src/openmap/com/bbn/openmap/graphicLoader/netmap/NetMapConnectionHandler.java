// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapConnectionHandler.java,v $
// $RCSfile: NetMapConnectionHandler.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import com.bbn.openmap.plugin.graphicLoader.GraphicLoaderConnector;

/**
 * The NetMapConnectionHandler is an extension to the
 * GraphicLoaderConnector that looks for a NetMapConnector object in
 * the MapHandler. If it finds one, it creates a NetMapGraphicLoader
 * for it, connects the two, and then starts acting like a
 * GraphicLoaderConnector to merge the NetMapGraphicLoader with a
 * GraphicLoaderPlugIn/PlugInLayer. It uses the same properties as the
 * GraphicLoaderConnector, and the properties refer to how to handle
 * the PlugInLayer that will eventually get created if a
 * NetMapConnector is found.
 */
public class NetMapConnectionHandler extends GraphicLoaderConnector {

    public NetMapConnectionHandler() {}

    /**
     * Find GraphicLoaders and LayerHandler in the MapHandler.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof NetMapConnector) {
            checkGraphicLoader(new NetMapGraphicLoader((NetMapConnector) obj));
        } else {
            super.findAndInit(obj);
        }
    }
}