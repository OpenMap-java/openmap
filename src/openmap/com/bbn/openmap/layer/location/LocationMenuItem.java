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
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;

/**
 * This is an item that sits on the popup menu. It knows how to get
 * information it needs from the menu, and recenters the map, or
 * brings up more information about the locaiton.
 */
public class LocationMenuItem extends JMenuItem implements ActionListener {
    /** the location popup menu for the item */
    protected LocationPopupMenu clp;
    /** the layer the item is for */
    protected LocationLayer layer;

    /**
     * Construct an empty item
     */
    public LocationMenuItem() {}

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
     * Construct a menuitem with a label and mnemonic
     * 
     * @param text the text for the item
     * @param mnemonic the mnemonic key for the item
     */
    public LocationMenuItem(String text, int mnemonic) {
        super(text, mnemonic);
        this.addActionListener(this);
    }

    public LocationMenuItem(String text, LocationPopupMenu aCLP,
            LocationLayer aLayer) {
        this(text);
        setLocationPopupMenu(aCLP);
        setLayer(aLayer);
    }

    public void setLocationPopupMenu(LocationPopupMenu aCLP) {
        clp = aCLP;
    }

    public LocationPopupMenu getLocationPopupMenu() {
        return clp;
    }

    public void setLayer(LocationLayer aLayer) {
        layer = aLayer;
    }

    public LocationLayer getLayer() {
        return layer;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        //          Debug.output("Action: " + e);
        String command = e.getActionCommand();

        if (layer != null && e.getSource().equals(this)) {
            if (command == LocationLayer.recenter) {
                MouseEvent evt = clp.getEvent();
                Point2D llp = layer.getProjection().inverse(evt.getX(),
                        evt.getY());
                clp.getMap().setCenter(llp.getY(), llp.getX());
            } else if (command.equals(LocationHandler.showdetails)) {
                clp.getLoc().showDetails(layer);
            }
        }
    }
}