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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationCBMenuItem.java,v $
// $RCSfile: LocationCBMenuItem.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.location;

import java.awt.event.*;

import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;

/**  
 * This is an checkbox item that sits on the popup menu.  It knows
 * how to get information it needs from the menu, and recenters the
 * map, or brings up more information about the location. 
 */
public class LocationCBMenuItem extends JCheckBoxMenuItem 
    implements ActionListener {

    protected LocationPopupMenu clp;
    protected LocationLayer layer;

    public LocationCBMenuItem() {
    }
    
    public LocationCBMenuItem(String text) {
	super(text);
	this.addActionListener(this);
    }
    
    public LocationCBMenuItem(String text, LocationPopupMenu aCLP, 
				 LocationLayer aLayer) {
	this(text);
	setLocationPopupMenu(aCLP);
	setLayer(aLayer);
    }

    public void setLocationPopupMenu(LocationPopupMenu aCLP){
	clp = aCLP;
    }

    public LocationPopupMenu getLocationPopupMenu(){
	return clp;
    }

    public void setLayer(LocationLayer aLayer){
	layer = aLayer;
    }

    public LocationLayer getLayer(){
	return layer;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
// 	    Debug.output("Action: " + e);
	String command = e.getActionCommand();

	if (layer != null && e.getSource().equals(this)){
	    if (command.equals(LocationHandler.showname)){
		JCheckBoxMenuItem btn = (JCheckBoxMenuItem)e.getSource();
		clp.getLoc().setShowName(btn.getState());
		layer.repaint();
	    }
	}
    }
}
