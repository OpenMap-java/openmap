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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationPopupMenu.java,v $
// $RCSfile: LocationPopupMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.location;


/*  Java Core  */
import java.awt.event.*;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;

import javax.swing.JPopupMenu;


/** 
 * This is the menu that pops up when the mouse button is pressed,
 * giving a set of options to the user.  The LocationMenu keeps
 * track of the location of the last mouse event, the last
 * Location (if there was one), and the MapBean. 
 */
public class LocationPopupMenu extends JPopupMenu { 
    /** The location of the event. */
    protected MouseEvent event;
    /** Used to recenter the map. */
    protected MapBean map;
    /** Used as a reference for the details gathering. */
    protected Location loc;
    
    /**
     * Construct an empty object
     */
    public LocationPopupMenu(){
    }
    
    /**
     * set the location of the menu
     * @param location the location to place the menu at
     */
    public void setLoc(Location location){
	loc = location;
    }

    /**
     * set the map the menu is associated with
     * @param aMap the map the menu is for
     */
    public void setMap(MapBean aMap){
	map = aMap;
    }
    
    /**
     * set the event the menu is for
     * @param anEvt the event for the menu
     */
    public void setEvent(MouseEvent anEvt){
	event = anEvt;
    }

    /**
     * returns the location of the menu
     * @return the location
     */
    public Location getLoc(){
	return loc;
    }

    /**
     * returns the event for the menu
     * @return the event
     */
    public MouseEvent getEvent(){
	return event;
    }
    
    /**
     * returns the map the menu is for
     * @return the menu
     */
    public MapBean getMap(){
	return map;
    }
}

