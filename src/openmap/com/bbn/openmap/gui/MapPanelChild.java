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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MapPanelChild.java,v $
// $RCSfile: MapPanelChild.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

/**
 * A simple interface to let an object know it wants to be added to
 * the MapPanel, and where it would like to be placed. The MapPanel
 * can look for these components in the MapHandler.
 */
public interface MapPanelChild {

    public static final String PreferredLocationProperty = "preferredLocation";

    /**
     * Should be provided with BorderLayout.NORTH, BorderLayout.SOUTH,
     * BorderLayout.EAST or BorderLayout.WEST. BorderLayout.Center is
     * generally reserved for the MapBean.
     */
    public void setPreferredLocation(String string);

    /**
     * Should return BorderLayout.NORTH, BorderLayout.SOUTH,
     * BorderLayout.EAST or BorderLayout.WEST. BorderLayout.Center is
     * generally reserved for the MapBean.
     */
    public String getPreferredLocation();
}