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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/NavigateMenu.java,v $
// $RCSfile: NavigateMenu.java,v $
// $Revision: 1.2 $
// $Date: 2003/03/06 02:36:21 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.event.*;

/**
 * Provides MenuItems that lets users control the projection.  This
 * includes providing a means to call up the Coordinate Window to let
 * users enter coordinates to center the map, a projection choice
 * menu, and zooming choices.
 */
public class NavigateMenu extends AbstractOpenMapMenu 
  implements ActionListener, MenuBarMenu {

    public static final String defaultText = "Navigate";
    public static final int defaultMnemonic = 'N';

    public final static transient String coordCmd = "coordinates";
  
    protected transient CoordInternalFrame coordDialog=null;
    protected transient CoordDialog coordDialog2 = null;

    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);
    protected ZoomSupport zoomSupport = new ZoomSupport(this);
    public final static transient String zoomIn2Cmd = "zoomIn2Cmd";
    public final static transient String zoomIn4Cmd = "zoomIn4Cmd";
    public final static transient String zoomOut2Cmd = "zoomOut2Cmd";
    public final static transient String zoomOut4Cmd = "zoomOut4Cmd";

    /**
     * This constructor automatically configures the Menu to have
     * choices to bring up the Coordinates Window, the projection
     * choice menu, and the zoom menus. 
     */
    public NavigateMenu() {
	super();
	setText(I18N.get("menu.navigate",defaultText));
	setMnemonic(defaultMnemonic);
	add(createCoordinatesMenuItem());
    
	JMenuItem mi;
	JMenu submenu = (JMenu)add(new JMenu(
	    I18N.get("menu.navigate.proj.zoomin", "Zoom In")));
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.2X", "2X")));
	mi.setActionCommand(zoomIn2Cmd);
	mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.4X", "4X")));
	mi.setActionCommand(zoomIn4Cmd);
	mi.addActionListener(this);


        submenu = (JMenu) add(new JMenu(
	    I18N.get("menu.navigate.proj.zoomout", "Zoom Out")));
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.2X", "2X")));
	mi.setActionCommand(zoomOut2Cmd);
	mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.4X", "4X")));
	mi.setActionCommand(zoomOut4Cmd);
	mi.addActionListener(this);

	add(new ProjectionMenu());
    }    

    /**
     * A MenuItem which upon clicked, creates CoordDialog object.
     */
    public JMenuItem createCoordinatesMenuItem() {
	JMenuItem coordMenuItem = new JMenuItem(I18N.get(
	    "menu.navigate.coords", "Coordinates..."));
	coordMenuItem.addActionListener(this);
	coordMenuItem.setActionCommand(coordCmd);
    
	if (Environment.getBoolean(Environment.UseInternalFrames)) {
	    coordDialog = new CoordInternalFrame();
	} else {
	    coordDialog2 = new CoordDialog();
	}
	return coordMenuItem;
    }  
  
    /**
     * ActionListener interface, lets the Menu act on the actions of
     * the MenuItems.
     */
    public void actionPerformed(ActionEvent ae) {
    	String command = ae.getActionCommand();

	Debug.message("NavigateMenu", "MenuPanel.actionPerformed(): " + command);

	if (command.equals(zoomIn2Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 0.5f);
	} else if (command.equals(zoomIn4Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 0.25f);
	} else if (command.equals(zoomOut2Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 2.0f);
	} else if (command.equals(zoomOut4Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 4.0f);
	} else if (command.equals(coordCmd)) {
	    doCoordCommand();
	} 	
    }
  
    /**
     * Called when Coordinates MenuItem is clicked.
     */
    protected void doCoordCommand() {
	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    if (coordDialog.isIcon()) {
		try {
		    coordDialog.setIcon(false);
		} catch (PropertyVetoException pv) {
		    System.err.println("setIcon(false) vetoed!" + pv);
		}
	    } else {
		Component obj = getParent();
		while (!(obj instanceof JLayeredPane)) {
		    obj = obj.getParent();
		}
		((JLayeredPane)obj).add(coordDialog);
	    }
	} else {
	    coordDialog2.setVisible(true);
	}
    }
  
  
    /** 
     * Convenience function for setting up listeners.
     */
    public void setupListeners(MapBean map) {     
	addZoomListener(map);   
	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    coordDialog.addCenterListener(map);
	} else {
	    coordDialog2.addCenterListener(map);
	}
    }
  
    /** 
     * Convenience function for undoing set up listeners.
     */
    public void undoListeners(MapBean map) {   
	removeZoomListener(map);  
	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    coordDialog.removeCenterListener(map);
	} else {
	    coordDialog2.removeCenterListener(map);
	}
    }
  
    /*----------------------------------------------------------------------
     * Zoom Support - for broadcasting zoom events
     *----------------------------------------------------------------------
     */
  
    /**
     *
     */
    public synchronized void addZoomListener(ZoomListener l) {
	zoomSupport.addZoomListener(l);
    }

  
    /**
     *
     */
    public synchronized void removeZoomListener(ZoomListener l) {
	zoomSupport.removeZoomListener(l);
    }

    /**
     *
     */
    public void fireZoom(int zoomType, float amount) {
	zoomSupport.fireZoom(zoomType, amount);
    }
   
    public void findAndInit(Object someObj) {
	if(someObj instanceof MapBean) {
	    setupListeners((MapBean)someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	if(someObj instanceof MapBean) {
	    undoListeners((MapBean)someObj);
	}
    }

}
