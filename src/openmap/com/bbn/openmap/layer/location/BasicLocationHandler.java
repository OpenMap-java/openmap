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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/BasicLocationHandler.java,v $
// $RCSfile: BasicLocationHandler.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;

import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A basic location handler, that just returns simple testing locations.
 */
public class BasicLocationHandler
        implements LocationHandler, ActionListener {
    /** The parent layer. */
    protected LocationLayer layer;
    /** PropertyConsumer property prefix. */
    protected String propertyPrefix = null;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public BasicLocationHandler() {
    }

    /** Set the layer this handler is serving. */
    public void setLayer(LocationLayer l) {
        layer = l;
    }

    /** Get the layer the handler is serving. */
    public LocationLayer getLayer() {
        return layer;
    }

    public void reloadData() {
    }

    public boolean isShowNames() {
        return true;
    }

    public void setShowNames(boolean set) {
    }

    public boolean isShowLocations() {
        return true;
    }

    public void setShowLocations(boolean set) {
    }

    public boolean isForceGlobal() {
        return true;
    }

    public void setForceGlobal(boolean set) {
    }

    /**
     * Called by the LocationLayer when the layer is removed from the map. The
     * LocationHandler should release expensive resources if this is called.
     */
    public void removed(java.awt.Container cont) {
    }

    protected Color[] colors = null;

    public OMGraphicList get(float nwLat, float nwLon, float seLat, float seLon, OMGraphicList graphicList) {

        if (colors == null) {
            colors = new Color[8];
            colors[0] = Color.red;
            colors[1] = Color.green;
            colors[2] = Color.yellow;
            colors[3] = Color.blue;
            colors[4] = Color.black;
            colors[5] = Color.white;
            colors[6] = Color.orange;
            colors[7] = Color.pink;
        }

        for (int i = 0; i < 10; i++) {
            Location location = new BasicLocation(42f, -72f, "testing" + i, null);
            location.setLocationHandler(this);
            location.getLabel().setLinePaint(colors[i % 8]);
            // location.getLabel().setShowBounds(true);
            location.setShowName(true);
            location.setShowLocation(true);
            graphicList.add(location);
        }

        return graphicList;
    }

    public List<Component> getItemsForPopupMenu(Location loc) {
        return null;
    }

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.  Here for override reasons.
     * 
     * @return null for this class.
     */
    public java.awt.Component getGUI() {
        return null;
    }

    // ----------------------------------------------------------------------
    // ActionListener interface implementation
    // ----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets actions.
     */
    public void actionPerformed(ActionEvent e) {
    }

    // ----------------------------------------------------------------------
    // PropertyConsumer interface implementation
    // ----------------------------------------------------------------------

    /**
     * Sets the properties for the handler. This particular method assumes that
     * the marker name is not needed, because all of the contents of this
     * Properties object are to be used for this object, and scoping the
     * properties with a prefix is unnecessary.
     * 
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    /**
     * Set up the properties of the handler. Part of the PropertyConsumer
     * interface.
     * 
     * Supported properties include:
     * <UL>
     * <LI>locationColor - number of seconds between attempts to retrieve
     * Features data
     * <LI>featuresSvcURL - URL to invoke to retrieve the XML Features document.
     * </UL>
     */
    public void setProperties(String prefix, Properties properties) {
        setPropertyPrefix(prefix);
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration. This method takes care of
     * the basic LocationHandler parameters, so any LocationHandlers that extend
     * the AbstractLocationHandler should call this method, too, before adding
     * any specific properties.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into. If props equals null, then a new Properties object should be
     *        created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). This method takes care of the basic
     * LocationHandler parameters, so any LocationHandlers that extend the
     * AbstractLocationHandler should call this method, too, before adding any
     * specific properties.
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        return list;
    }

    /**
     * Set the property key prefix that should be used by the PropertyConsumer.
     * The prefix, along with a '.', should be prepended to the property keys
     * known by the PropertyConsumer.
     * 
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to the property
     * keys for Properties lookups.
     * 
     * @return thre property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

}