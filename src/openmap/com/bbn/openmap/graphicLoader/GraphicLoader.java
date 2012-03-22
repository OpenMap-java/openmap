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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/GraphicLoader.java,v $
// $RCSfile: GraphicLoader.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.Component;

import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.proj.Projection;

/**
 * The interface that describes an object that supplies updates to
 * OMGraphics presented by the GraphicLoaderPlugIn.
 */
public interface GraphicLoader {
    /**
     * The method that calls the GUI that controls the GraphicLoader.
     */
    public Component getGUI();

    /**
     * Let the GraphicLoader know what the projection is.
     */
    public void setProjection(Projection p);

    /**
     * Set the OMGraphicHandler that will receive OMGraphic updates
     * from the GraphicLoader.
     */
    public void setReceiver(OMGraphicHandler r);

    /**
     * Get the OMGraphicHandler that will receive OMGraphic updates
     * from the GraphicLoader.
     */
    public OMGraphicHandler getReceiver();

    /**
     * Get a pretty name for GUI representation that lets folks know
     * what the GraphicLoader does.
     */
    public String getName();
}