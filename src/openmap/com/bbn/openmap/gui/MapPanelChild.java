// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1 $
// $Date: 2003/04/04 14:34:26 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.BorderLayout;

/**
 * A simple interface to let an object know it wants to be added to
 * the MapPanel, and where it would like to be placed.  The MapPanel
 * looks for these components in the MapHandler.
 */
public interface MapPanelChild {

    public static final String PreferredLocationProperty = "preferredLocation";

    /**
     * Should be provided with BorderLayout.NORTH, BorderLayout.SOUTH,
     * BorderLayout.EAST or BorderLayout.WEST.  BorderLayout.Center is
     * generally reserved for the MapBean.
     */
    public void setPreferredLocation(String string);

    /**
     * Should return BorderLayout.NORTH, BorderLayout.SOUTH,
     * BorderLayout.EAST or BorderLayout.WEST.  BorderLayout.Center is
     * generally reserved for the MapBean.
     */
    public String getPreferredLocation();
}
