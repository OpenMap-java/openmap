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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MapPanel.java,v $
// $RCSfile: MapPanel.java,v $
// $Revision: 1.2 $
// $Date: 2003/04/05 05:39:01 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Properties;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;

/**
 * The MapPanel is an OMComponentPanel that is the heart of the
 * OpenMap application framework.  It can be used in a application or
 * applet.  The Panel has a BorderLayout, and creates a MapBean for
 * its center area.  It creates a MapHandler to use to hold all of its
 * OpenMap components, and uses the PropertyHandler given to it in its
 * constructor to create and configure all of the application
 * components.  The best way to add components to the MapPanel is to
 * get the MapHandler from it and add the component to that.  The
 * MapPanel also adds itself to its MapHandler, so when the
 * PropertyHandler adds components to the MapHandler, the MapPanel is
 * able to find them via the findAndInit method.  By default, the
 * MapPanel looks for MapPanelChildren and asks them for where they
 * would prefer to be located (BorderLayout.NORTH, BorderLayout.SOUTH,
 * BorderLayout.EAST, BorderLayout.WEST).
 */
public class MapPanel extends OMComponentPanel {

    protected MapHandler mapHandler;
    protected MapBean mapBean;
    protected PropertyHandler propertyHandler;
    
    /**
     * Create a MapPanel that creates its own PropertyHandler, which
     * will then search the classpath, config directory and user home
     * directory for the openmap.properties file to configure
     * components for the MapPanel.
     */
    public MapPanel() {
	this(null);
    }

    /**
     * Create a MapPanel that configures itself with the properties
     * contained in the PropertyHandler provided. If the
     * PropertyHandler is null, a new one will be created.
     */
    public MapPanel(PropertyHandler propertyHandler) {
	if (propertyHandler == null) {
	    propertyHandler = new PropertyHandler();
	}

	setLayout(createLayoutManager());
	createComponents(propertyHandler);
    }

    /**
     * The constructor calls this method that sets the LayoutManager
     * for this MapPanel.  It returns a BorderLayout by default, but
     * this method can be overridden to change how the MapPanel places
     * components.  If you change what this method returns, you should
     * also change how components are added in the findAndInit()
     * method.
     */
    protected LayoutManager createLayoutManager() {
	return new BorderLayout();
    }

    /**
     * The constructor calls this method that creates the MapHandler
     * and MapBean, and then tells the PropertyHandler to create the
     * components described in its properties.  This method calls
     * getMapHandler() and getMapBean().
     */
    protected void createComponents(PropertyHandler propertyHandler) {
	MapHandler mapHandler = getMapHandler();
	MapBean mapBean = getMapBean();

	add(mapBean, BorderLayout.CENTER);

	try {
	    mapHandler.add(this);
	    mapHandler.add(propertyHandler);
	    mapHandler.add(mapBean);
	    propertyHandler.createComponents(mapHandler);
	} catch (MultipleSoloMapComponentException msmce) {
	    Debug.error("MapPanel: tried to add multiple components of the same type when only one is allowed! - " + msmce);
	}
    }

    /**
     * Get the MapBean used for the MapPanel.  If the MapBean is null,
     * calls createMapBean() which will create a BufferedLayerMapBean.
     * If you want something different, override this method.
     */      
    public MapBean getMapBean() {
	if (mapBean == null) {
	    mapBean = MapPanel.createMapBean();
	}
	return mapBean;
    }

    /**
     * Get the MapHandler used for the MapPanel.  Creates a standard
     * MapHandler if it hasn't been created yet.
     */      
    public MapHandler getMapHandler() {
	if (mapHandler == null) {
	    mapHandler = new MapHandler();
	}
	return mapHandler;
    }

    /**
     * A static method that creates a MapBean with it's projection set
     * to the values set in the Environment.  Also creates a
     * BevelBorder.LOWERED border for the MapBean.
     */
    public static MapBean createMapBean() {
	int envWidth = Environment.getInteger(Environment.Width, 
					      MapBean.DEFAULT_WIDTH);
	int envHeight = Environment.getInteger(Environment.Height,
					       MapBean.DEFAULT_HEIGHT);
	// Initialize the map projection, scale, center
	// with user prefs or defaults
	Projection proj = ProjectionFactory.makeProjection(
	    ProjectionFactory.getProjType(Environment.get(Environment.Projection, Mercator.MercatorName)),
	    Environment.getFloat(Environment.Latitude, 0f),
	    Environment.getFloat(Environment.Longitude, 0f),
	    Environment.getFloat(Environment.Scale, Float.POSITIVE_INFINITY),
	    envWidth, envHeight);

	if (Debug.debugging("mappanel")) {
	    Debug.output("MapPanel: creating MapBean with initial projection " + proj);
	}
	
	return createMapBean(proj, new BevelBorder(BevelBorder.LOWERED));
    }

    /**
     * A static method that creates a MapBean and sets its projection
     * and border to the values given.
     */
    public static MapBean createMapBean(Projection proj, Border border) {
	MapBean mapBeano = new BufferedLayerMapBean();
	mapBeano.setBorder(border);
	mapBeano.setProjection(proj);
	mapBeano.setPreferredSize(new Dimension(proj.getWidth(), proj.getHeight()));
	return mapBeano;
    }

    /**
     * The MapPanel looks for MapPanelChild components, finds out from
     * them where they prefer to be placed, and adds them.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof MapPanelChild && someObj instanceof Component) {
	    if (Debug.debugging("basic")) {
		Debug.output("MapPanel: adding " + 
			     someObj.getClass().getName());
	    }
	    MapPanelChild mpc = (MapPanelChild) someObj;
	    add((Component)mpc, mpc.getPreferredLocation());
	    invalidate();
	}
    }

    /**
     * The MapPanel looks for MapPanelChild components and removes
     * them from iteself.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof MapPanelChild && someObj instanceof Component) {
	    if (Debug.debugging("basic")) {
		Debug.output("MapPanel: removing " + 
			     someObj.getClass().getName());
	    }
	    remove((Component)someObj);
	    invalidate();
	}
    }
}
