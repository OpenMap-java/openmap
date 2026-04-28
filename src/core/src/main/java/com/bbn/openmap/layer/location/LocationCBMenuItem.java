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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationCBMenuItem.java,v $
// $RCSfile: LocationCBMenuItem.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

/**
 * This is an checkbox item that sits on the popup menu. It knows how to get
 * information it needs from the menu, and recenters the map, or brings up more
 * information about the location.
 */
public class LocationCBMenuItem
        extends JCheckBoxMenuItem
        implements ActionListener {

    protected Location location;

    public LocationCBMenuItem(String text, Location loc) {
        super(text);
        setLoc(loc);
        this.addActionListener(this);
        setSelected(loc.isShowName());
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();

        if (e.getSource().equals(this)) {
            if (command.equals(LocationHandler.showname)) {
                JCheckBoxMenuItem btn = (JCheckBoxMenuItem) e.getSource();
                try {
                    location.setShowName(btn.getState());
                    location.getLocationHandler().getLayer().repaint();
                } catch (NullPointerException npe) {

                }
            }
        }
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
}