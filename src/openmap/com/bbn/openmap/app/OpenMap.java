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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.app;

import java.awt.BorderLayout;
import java.awt.Color;

import java.io.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.ArgParser;

/**
 * This application demonstrates how various OpenMap components can be
 * added to an application.  If the application is run with a
 * -Ddebug.blmb flag, a BufferedLayerMapBean will be used
 * instead of a BufferedMapBean.
 */
public class OpenMap {
  
    protected MapHandler beanHandler;

    public OpenMap(PropertyHandler propertyHandler) { 
      	MapBean mapBean;

	if (Debug.debugging("blmb")) {
	    mapBean = new BufferedLayerMapBean();
	} else {
	    mapBean = new BufferedMapBean();
	}

	mapBean.setBorder(new BevelBorder(BevelBorder.LOWERED));
	int envWidth = Environment.getInteger(Environment.Width, MapBean.DEFAULT_WIDTH);
	int envHeight = Environment.getInteger(Environment.Height, MapBean.DEFAULT_HEIGHT);
	// Initialize the map projection, scale, center with user prefs or
	// defaults
	Projection proj = ProjectionFactory.makeProjection(
	    ProjectionFactory.getProjType(Environment.get(Environment.Projection, Mercator.MercatorName)),
	    Environment.getFloat(Environment.Latitude, 0f),
	    Environment.getFloat(Environment.Longitude, 0f),
	    Environment.getFloat(Environment.Scale, Float.POSITIVE_INFINITY),
	    envWidth, envHeight);

	mapBean.setProjection(proj);
	mapBean.setPreferredSize(new java.awt.Dimension(envWidth, envHeight));

	beanHandler = getMapHandler();
	
	try {
	    beanHandler.add(propertyHandler);
	    beanHandler.add(mapBean);
	    propertyHandler.createComponents(beanHandler);
	} catch (MultipleSoloMapComponentException msmce) {
	    Debug.error("OpenMapNG: tried to add multiple components of the same type when only one is allowed! - " + msmce);
	}
	mapBean.showLayerPalettes();
    }

    /**
     * Get the MapHandler used for the OpenMap object.
     */      
    public MapHandler getMapHandler() {
	if (beanHandler == null) {
	    beanHandler = new MapHandler();
	}
	return beanHandler;
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
	PropertyHandler propertyHandler;

	if (propertiesFile == null) {
	    propertyHandler = new PropertyHandler();
	} else {

	    try {
		java.net.URL propURL = 
		    com.bbn.openmap.layer.util.LayerUtils.getResourceOrFileOrURL(null, propertiesFile);
		propertyHandler = new PropertyHandler(propURL);
	    } catch (java.net.MalformedURLException murle) {
		Debug.error(murle.getMessage());
		murle.printStackTrace();
		propertyHandler = new PropertyHandler();
	    } catch(IOException ioe) {
		Debug.error(ioe.getMessage());
		ioe.printStackTrace();
		propertyHandler = new PropertyHandler();
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

	Debug.init();

	OpenMap.create(propArgs);
    }
}
