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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationMenuItem.java,v $
// $RCSfile: LocationMenuItem.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * This is an item that sits on the popup menu. It knows how to get information
 * it needs from the menu, and recenters the map, or brings up more information
 * about the location.
 */
public class LocationMenuItem
        extends JMenuItem
        implements ActionListener {

    protected Location location;

    /**
     * Construct an empty item
     */
    public LocationMenuItem() {
    }

    /**
     * Construct a menuitem with a label
     * 
     * @param text the text for the item
     */
    public LocationMenuItem(String text) {
        super(text);
        this.addActionListener(this);
    }

    /**
     * Construct a menu item with a label and mnemonic
     * 
     * @param text the text for the item
     * @param mnemonic the mnemonic key for the item
     */
    public LocationMenuItem(String text, int mnemonic) {
        super(text, mnemonic);
        this.addActionListener(this);
    }

    public LocationMenuItem(String text, Location loc) {
        this(text);
        setLoc(loc);
    }

    /**
     * @return the location
     */
    public Location getLoc() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLoc(Location location) {
        this.location = location;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        // Debug.output("Action: " + e);
        String command = e.getActionCommand();

        if (command.equals(LocationHandler.showdetails)) {
            try {
                getLoc().showDetails();
            } catch (NullPointerException npe) {
            }
        }
    }
}