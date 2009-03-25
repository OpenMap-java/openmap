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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MapPanel.java,v $
// $RCSfile: MapPanel.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;

/**
 * The MapPanel is a interface describing a component that contains a MapBean,
 * MapHandler, menu widgets and all the other components connected to make an
 * OpenMap map widget. A MapPanel is a self-contained OpenMap Swing component.
 * It is expected that the MapPanel will extend from java.awt.Container. If it
 * doesn't, it might not be automatically added to the OpenMapFrame or
 * OpenMapApplet if it is found in the MapHandler.
 */
public interface MapPanel {

    /**
     * Get the MapBean used for the MapPanel.
     */
    public MapBean getMapBean();

    /**
     * Get the MapHandler used for the MapPanel. You should be able to use the
     * MapHandler to get to any component used in the MapPanel.
     */
    public MapHandler getMapHandler();

    /**
     * Get a JMenuBar containing menus to control the map.
     */
    public JMenuBar getMapMenuBar();

    /**
     * Get a JMenu containing sub-menus to control the map.
     */
    public JMenu getMapMenu();

    /**
     * Tell the panel to release components.
     */
    public void dispose();
}