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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/app/OpenMap.java,v $
// $RCSfile: OpenMap.java,v $
// $Revision: 1.5 $
// $Date: 2003/04/08 16:28:37 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.app;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * The OpenMap application framework.  This class creates a
 * PropertyHandler that searches the classpath, config directory and
 * user's home directory for an openmap.properties file, and creates
 * the application based on the contents of the properties files.  It
 * also creates an MapPanel and an OpenMapFrame to be used for the
 * application and adds them to the MapHandler contained in the
 * MapPanel.  All other components are added to that MapHandler as
 * well, and they use the MapHandler to locate, connect and
 * communicate with each other.
 */
public class OpenMap {
  
    protected MapPanel mapPanel;

    /**
     * Create a new OpenMap framework object - creates a MapPanel,
     * OpenMapFrame, and brings up the layer palettes that are being
     * told to be open at startup.  The MapPanel will create a
     * PropertiesHandler that will search for an openmap.properties
     * file.
     */
    public OpenMap() {
	this(null);
    }

    /**
     * Create a new OpenMap framework object - creates a MapPanel,
     * OpenMapFrame, and brings up the layer palettes that are being
     * told to be open at startup.  The properties in the
     * PropertyHandler will be used to configure the application.
     * PropertyHandler may be null.
     */
    public OpenMap(PropertyHandler propertyHandler) {
	mapPanel = new BasicMapPanel(propertyHandler);
	mapPanel.getMapHandler().add(new OpenMapFrame());
	mapPanel.getMapBean().showLayerPalettes();
    }

    /**
     * Get the MapHandler used for the OpenMap object.
     */      
    public MapHandler getMapHandler() {
	return mapPanel.getMapHandler();
    }

    /**
     * Get the MapPanel, the container for the OpenMap components.
     */
    public MapPanel getMapPanel() {
	return mapPanel;
    }

    /**
     * Create and return an OpenMap object that uses a standard
     * PropertyHandler to configure itself.  The OpenMap object has a
     * MapHandler that you can use to gain access to all the
     * components.
     * @return OpenMap
     * @see #getMapHandler
     */
    public static OpenMap create() {
	return new OpenMap(null);
    }

    /**
     * Create and return an OpenMap object that uses a standard
     * PropertyHandler to configure itself.  The OpenMap object has a
     * MapHandler that you can use to gain access to all the
     * components.
     * @return OpenMap
     * @see #getMapHandler
     */
    public static OpenMap create(String propertiesFile) {
	Debug.init();

	PropertyHandler propertyHandler = null;

	if (propertiesFile != null) {
	    try {
		URL propURL = LayerUtils.getResourceOrFileOrURL(null, propertiesFile);
		propertyHandler = new PropertyHandler(propURL);
	    } catch (MalformedURLException murle) {
		Debug.error(murle.getMessage());
		murle.printStackTrace();
		propertyHandler = null;
	    } catch (IOException ioe) {
		Debug.error(ioe.getMessage());
		ioe.printStackTrace();
		propertyHandler = null;
	    }
	}

	return new OpenMap(propertyHandler);
    }

    /**
     * The main OpenMap application.
     */
    static public void main(String args[]) {

	ArgParser ap = new ArgParser("OpenMap");
	String propArgs = null;
	ap.add("properties","A resource, file path or URL to properties file\n Ex: http://myhost.com/xyz.props or file:/myhome/abc.pro\n See Java Documentation for java.net.URL class for more details",1);

	ap.parse(args);
	
	String[] arg = ap.getArgValues("properties");
	if (arg != null) {
	    propArgs = arg[0];
	}

	OpenMap.create(propArgs);
    }
}
