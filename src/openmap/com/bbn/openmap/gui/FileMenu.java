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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/FileMenu.java,v $
// $RCSfile: FileMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.beans.*;
import java.beans.beancontext.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import com.bbn.openmap.*;
import com.bbn.openmap.image.*;
import com.bbn.openmap.util.Debug;

/**
 * FileMenu creates AboutMenuItem, SavePropertiesMenuItem,
 * SaveImageMenuItem, ExitMenuItem. It only adds AboutMenuItem if
 * runing as an Applet, all otherwise. These menu items are added by
 * default.  
 */
public class FileMenu extends AbstractOpenMapMenu
    implements MenuBarMenu {
    
    private String defaultText = "File";
    private int defaultMnemonic= 'F';
    
    //protected Vector menuItems = new Vector();
    SavePropertiesMenuItem spMenu = null;
    LoadPropertiesMenuItem lpMenu = null;
    Vector saimis = new Vector();

    /**
     * Create and add menuitems(About, SaveProperties, SaveAsImage and
     * Exit) 
     */
    public FileMenu() {
	super();
	setText(defaultText);
	setMnemonic(defaultMnemonic);
	createAndAdd();
    }
    
    /** Create and add default menu items */
    public void createAndAdd() {
	add(new AboutMenuItem());
	
	if(Environment.isApplet()) {
	    return;
	}
	
	add(new JSeparator());
	add(createSavePropertiesMenuItem());
	add(createLoadPropertiesMenuItem());
	add(new JSeparator());
	JMenu saveMenu = new JMenu("Save Map As");
	saveMenu.add(createSaveAsJpegMenuItem());
	saveMenu.add(createSaveAsGifMenuItem());
	addSVGMenuItem(saveMenu);
	add(saveMenu);
// 	add(createSaveAsVirtualJpegMenuItem());
	add(new JSeparator());
	add(createExitMenu());
    } 

    /**
     * Creates an exit MenuItem which when clicked closes the
     * application by calling System.exit()
     *
     * @return JMenuItem An Exit Menu Item.
     */
    public JMenuItem createExitMenu() {    
	if (Environment.isApplication()) {      
	    JMenuItem exitMenuItem = new JMenuItem("Quit");      
	    exitMenuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			// HACK - need to call shutdown() on mapbean
			// actually we should broadcast a shutdown
			// event so thato ther gui components can
			// clean up, and maybe only one can call exit.
			System.exit(0);
		    }
		});
	    return exitMenuItem;
	}
	return null;
    }
  
    /**
     * Creates a menu item that takes care of saving current state of
     * OpenMap as properties file.
     *
     * @return JMenuItem 
     */
    public JMenuItem createSavePropertiesMenuItem() {
	spMenu  = new SavePropertiesMenuItem();	
	if (getBeanContext() != null) {
	    spMenu.setMapHandler(getMapHandler());
	}
	return spMenu;
    }
    
    /**
     * Creates a menu item that takes care of loading a properties
     * file.
     *
     * @return JMenuItem 
     */
    public JMenuItem createLoadPropertiesMenuItem() {
	lpMenu  = new LoadPropertiesMenuItem();	
	if (getBeanContext() != null) {
	    lpMenu.setMapHandler(getMapHandler());
	}
	return lpMenu;
    }
    
    public JMenuItem createSaveAsImageMenuItem(String guiName, AbstractImageFormatter formatter) {
	SaveAsImageMenuItem menuItem = 
	    new SaveAsImageMenuItem(guiName, formatter);
	if (getBeanContext() != null) {
	    menuItem.setMapHandler(getMapHandler());
	}
	saimis.add(menuItem);
	return menuItem;
    }

    /**
     * Goes to the image formatter menu items and sets the MapHandler
     * in them so they will know how to create and image of the map.
     */
    protected void setMapHandlerInImageFormatters(MapHandler mapHandler) {
	Iterator it = saimis.iterator();
	while (it.hasNext()) {
	    SaveAsImageMenuItem saimi = (SaveAsImageMenuItem)it.next();
	    saimi.setMapHandler(mapHandler);
	}
    }

    /**
     * Creates a menuitem that knows how to save MapBean as an JPEG
     * image.
     *
     * @return JMenuItem 
     */
    // For now quality is hard coded as 0.8 which seems to be quite
    // reasonable.  It uses com.bbn.openmap.image.SunJPEGFormatter
    public JMenuItem createSaveAsJpegMenuItem() {
	SunJPEGFormatter formatter = new SunJPEGFormatter();
	formatter.setImageQuality(.8f);
	return createSaveAsImageMenuItem("JPEG", formatter);
    }
    
 
     /**
      * Creates a menuitem that knows how to save MapBean as a virtual JPEG
      * image, where the image size is independent of the view presented
      * to the user.
      *
      * @return JMenuItem 
      */
     public JMenuItem createSaveAsVirtualJpegMenuItem() {
         SunJPEGFormatter formatter = new SunJPEGFormatter();
	 formatter.setImageQuality(1.0f);
         SaveAsVirtualImageMenuItem virtualJpegMenuItem = new SaveAsVirtualImageMenuItem("Custom JPEG...",formatter);

         if (getBeanContext() != null) {
             virtualJpegMenuItem.setMapHandler(getMapHandler());
         }
	 saimis.add(virtualJpegMenuItem);
         return virtualJpegMenuItem;
     }

    /**
     * Creates a menuitem that knows how to save MapBean as an GIF
     * image.
     *
     * @return JMenuItem 
     */
    // It uses com.bbn.openmap.image.AcmeGifFormatter
    public JMenuItem createSaveAsGifMenuItem() {
	return createSaveAsImageMenuItem("GIF", new AcmeGifFormatter());
    }

    /**
     * Method checks to see if the SVGFormatter can be created, and if
     * it can, adds it to the FileMenu->Save As menu.  The
     * SVGFormatter needs the right Batik jars in the classpath to
     * compile.
     */
    public void addSVGMenuItem(JMenu menu) {
	try {
	    Object obj = com.bbn.openmap.util.ComponentFactory.create("com.bbn.openmap.image.SVGFormatter", null);

	    if (obj != null) {
		// This is a test to see if the batik package is
		// available.  If it isn't, this statement should
		// throw an exception, and the SVG option will not be
		// added to the SaveAs Menu item.
		Object batikTest =  Class.forName("org.apache.batik.swing.JSVGCanvas").newInstance();
		menu.add(createSaveAsImageMenuItem("SVG", (AbstractImageFormatter)obj));
		return;
	    }
	} catch (ClassNotFoundException cnfe) {
	} catch (InstantiationException ie) {
	} catch (IllegalAccessException iae) {
	} catch (NoClassDefFoundError ncdfe) {
	}

	if (Debug.debugging("basic")) {
	    Debug.output("SVG not added to the Save As options, because Batik was not found in classpath.");
	}
    }

    /** 
     * This method does nothing, but is required as a part of
     * MenuInterface 
     */
    public void findAndUnInit(Iterator it) {}
  
    /**
     * This method does nothing, but is required as a part of
     * MenuInterface 
     */
    public void findAndInit(Iterator it) {}
    
    /** 
     * When this method is called, it sets the given BeanContext on
     * menu items that need to find objects to get their work done.
     * Note: Menuitems are not added to beancontext 
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
	super.setBeanContext(in_bc);
	if(!Environment.isApplication()) { //running as an Applet
	    return;
	}
	MapHandler mapHandler = getMapHandler();
	if (spMenu != null) spMenu.setMapHandler(mapHandler);
	if (lpMenu != null) lpMenu.setMapHandler(mapHandler);
	setMapHandlerInImageFormatters(mapHandler);
    }
}
