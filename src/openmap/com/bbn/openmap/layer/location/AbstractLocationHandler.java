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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/AbstractLocationHandler.java,v $
// $Revision: 1.5 $ $Date: 2003/12/23 22:55:24 $ $Author: wjeuerle $
// **********************************************************************


package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Color;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JLabel;

import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The <tt>AbstractLocationHandler</tt> class facilitates the
 * implementation of a <code>LocationHandler</code> by implementing a
 * number of methods.  By extending this class, a developer need only
 * implement get(), setProperties(), and reloadData().
 * <pre>
 * locationhandler.locationColor=FF0000
 * locationhandler.nameColor=008C54
 * locationhandler.showNames=false
 * locationhandler.showLocations=true
 * locationhandler.override=true
 * </pre>
 * @see        com.bbn.openmap.layer.location.LocationHandler
 * @version    $Revision: 1.5 $ $Date: 2003/12/23 22:55:24 $
 * @author     Michael E. Los D530/23448
 */
public abstract class AbstractLocationHandler implements LocationHandler {
    
    /** The parent layer. */
    protected LocationLayer zLayer;
    
    // - - - - - - - - - - - - - -
    // Name-related Variables
    // - - - - - - - - - - - - - -
    /** The default setting for the labels at startup. */
    private boolean showNames = false;
    /** The color for the names. */
    protected Color nameColor;
    
    // - - - - - - - - - - - - - -
    // Location-related Variables
    // - - - - - - - - - - - - - -
    /** The default setting for the locations at startup. */
    private boolean showLocations = true;
    /** The color for the locations. */
    protected Color locationColor;

    /**
     * Force global settings to override local Location settings for
     * showLocation and showName.
     */
    private boolean forceGlobal = true;
    
    /**
     * Token uniquely identifying this LocationHandler in the application
     * properties.
     */
    protected String propertyPrefix = null;
    
    protected AbstractLocationHandler() {
	try {
	    nameColor = ColorFactory.parseColor(defaultNameColorString, true);
	    locationColor = ColorFactory.parseColor(defaultLocationColorString, true);
	} catch (NumberFormatException nfe) {
	    nameColor = Color.black;
	    locationColor = Color.black;
	}
    }


    /**
     * The location layer passes a LocationPopupMenu to the handler
     * when on of its locations has been clicked on.  This is an
     * opportunity for the handler to add options to the menu that can
     * bring up further information about the location, or to change
     * the appearance of the location.  This implementation makes no
     * changes to the popup menu.
     *
     * @param lpm LocationPopupMenu to add buttons to.
     */
    public void fillLocationPopUpMenu(LocationPopupMenu lpm) {}
    
    /**
     * Set the layer the handler is responding to.  This is needed in
     * case the handler has updates that it wants to show, and needs
     * to trigger a repaint.  It can also be used to communicate with
     * the information delegator.
     *
     * @param l a LocationLayer
     */
    public void setLayer(LocationLayer l) {
	zLayer = l;
    }
    
    /** Get the layer the handler is serving. */
    public LocationLayer getLayer() {
	return zLayer;
    }
    
    /**
     * See if the handler is displaying labels at a global level.
     */
    public boolean isShowNames() {
	return showNames;
    }
    
    /**
     * Set the handler to show/hide labels at a global level.
     */
    public void setShowNames(boolean set) {
	showNames = set;
    }
    
    /**
     * See if the handler is displaying location graphics at a global
     * level.
     */
    public boolean isShowLocations() {
	return showLocations;
    }
    
    /**
     * Set the handler to show/hide location graphics at a global
     * level.
     */
    public void setShowLocations(boolean set) {
	showLocations = set;
    }

    /**
     * Find out whether global settings should override local ones.
     */
    public boolean isForceGlobal() {
	return forceGlobal;
    }

    /**
     * Set whether global settings should override local ones.
     */
    public void setForceGlobal(boolean set) {
	forceGlobal = set;
    }

    /**
     * Set the color used for the name label.
     */
    public void setNameColor(Color nColor) {
	nameColor = nColor;
    }

    /**
     * Get the color used for the name label.
     */
    public Color getNameColor() {
	return nameColor;
    }

    /**
     * Set the color used for the location graphic.
     */
    public void setLocationColor(Color lColor) {
	locationColor = lColor;
    }

    /**
     * Get the color used for the location graphic.
     */
    public Color getLocationColor() {
	return locationColor;
    }
    
    /**
     * A set of controls to manipulate and control the display of data
     * from the handler. This implementation returns a JPanel with a
     * "No Palette" message.
     *
     * @return a JPanel with text, No Pallette
     */
    public java.awt.Component getGUI() {
	// LocationLayer.java chokes if we return null
	JPanel jp = new JPanel();
	jp.add(new JLabel("No Palette"));
	return jp;
    }

    /**
     * Called by the LocationLayer when the layer is removed from the
     * map.  The LocationHandler should release expensive resources if
     * this is called.  
     */
    public void removed(java.awt.Container cont) {
    }

    /**
     * Sets the properties for the handler. This particular method assumes
     * that the marker name is not needed, because all of the contents
     * of this Properties object are to be used for this object, and
     * scoping the properties with a prefix is unnecessary.
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(Properties props) {
	setProperties(null, props);
    }

    /** 
     * Set up the properties of the handler.  Part of the
     * PropertyConsumer interface.
     *
     * Supported properties include:
     * <UL>
     * <LI>locationColor - number of seconds between attempts to retrieve
     Features data
     * <LI>featuresSvcURL - URL to invoke to retrieve the XML Features
     document
     * </UL> */
    public void setProperties(String prefix, Properties properties) {
	propertyPrefix = prefix;

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	showLocations = PropUtils.booleanFromProperties(properties, prefix + ShowLocationsProperty, showLocations);
	
	locationColor = PropUtils.parseColorFromProperties(properties, prefix + LocationColorProperty, defaultLocationColorString);
	
	showNames = PropUtils.booleanFromProperties(properties, prefix + ShowNamesProperty, showNames);	
	nameColor = PropUtils.parseColorFromProperties(properties, prefix + NameColorProperty,	defaultNameColorString);
	forceGlobal = PropUtils.booleanFromProperties(properties, prefix + ForceGlobalProperty, forceGlobal);

    }
    
    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer.  If the layer has a
     * propertyPrefix set, the property keys should have that prefix
     * plus a separating '.' prepended to each propery key it uses for
     * configuration. This method takes care of the basic
     * LocationHandler parameters, so any LocationHandlers that extend
     * the AbstractLocationHandler should call this method, too,
     * before adding any specific properties.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.  If props equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	String prefix = PropUtils.getScopedPropertyPrefix(this);

	props.put(prefix + ShowNamesProperty, new Boolean(showNames).toString());
	props.put(prefix + NameColorProperty,
		  Integer.toHexString(nameColor.getRGB()));
	props.put(prefix + ShowLocationsProperty, new Boolean(showLocations).toString());
	props.put(prefix + LocationColorProperty,
		  Integer.toHexString(locationColor.getRGB()));
	props.put(prefix + ForceGlobalProperty, new Boolean(forceGlobal).toString());

	return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).  This method takes care of the basic LocationHandler
     * parameters, so any LocationHandlers that extend the
     * AbstractLocationHandler should call this method, too, before
     * adding any specific properties.
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}
	
	list.put(ShowNamesProperty, "Display all the location name labels");
	list.put(ShowNamesProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
	list.put(NameColorProperty, "Color of name label");
	list.put(NameColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(ShowLocationsProperty, "Display all the location markers");
	list.put(ShowLocationsProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
	list.put(LocationColorProperty, "Color of location marker");
	list.put(LocationColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(ForceGlobalProperty, "Layer settings override map object settings");
	list.put(ForceGlobalProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

	return list;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {
	propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @return the property prefix
     */
    public String getPropertyPrefix() {
	return propertyPrefix;
    }

}
