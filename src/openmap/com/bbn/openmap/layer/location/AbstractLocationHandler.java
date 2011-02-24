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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/AbstractLocationHandler.java,v $
// $Revision: 1.10 $ $Date: 2006/02/13 16:54:18 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Color;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.PropUtils;

/**
 * The <tt>AbstractLocationHandler</tt> class facilitates the implementation
 * of a <code>LocationHandler</code> by implementing a number of methods. By
 * extending this class, a developer need only implement get(), setProperties(),
 * and reloadData().
 * 
 * <pre>
 *    
 *     locationhandler.locationColor=FF0000
 *     locationhandler.nameColor=008C54
 *     locationhandler.showNames=false
 *     locationhandler.showLocations=true
 *     locationhandler.override=true
 *     
 * </pre>
 * 
 * @see com.bbn.openmap.layer.location.LocationHandler
 * @version $Revision: 1.10 $ $Date: 2006/02/13 16:54:18 $
 * @author Michael E. Los D530/23448
 */
public abstract class AbstractLocationHandler implements LocationHandler {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.location.LocationHandler");
    
    /** The parent layer. */
    protected LocationLayer zLayer;

    // - - - - - - - - - - - - - -
    // Name-related Variables
    // - - - - - - - - - - - - - -
    /** The default setting for the labels at startup. */
    private boolean showNames = false;
    /** The rendering attributes for the names. */
    protected DrawingAttributes nameDrawingAttributes;

    // - - - - - - - - - - - - - -
    // Location-related Variables
    // - - - - - - - - - - - - - -
    /** The default setting for the locations at startup. */
    private boolean showLocations = true;
    /** The rendering attributes for the locations. */
    protected DrawingAttributes locationDrawingAttributes;

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

    /**
     * All LocationHandlers have access to an I18n object, which is provided by
     * the Environment.
     */
    protected transient I18n i18n = Environment.getI18n();

    protected AbstractLocationHandler() {
        nameDrawingAttributes = new DrawingAttributes();
        locationDrawingAttributes = new DrawingAttributes();

        // try {
        // nameDrawingAttributes.setLinePaint(ColorFactory.parseColor(defaultNameColorString,
        // true));
        // locationDrawingAttributes.setLinePaint(ColorFactory.parseColor(defaultLocationColorString,
        // true));
        // } catch (NumberFormatException nfe) {
        // }
    }

    /**
     * Set the layer the handler is responding to. This is needed in case the
     * handler has updates that it wants to show, and needs to trigger a
     * repaint. It can also be used to communicate with the information
     * delegator.
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
     * See if the handler is displaying location graphics at a global level.
     */
    public boolean isShowLocations() {
        return showLocations;
    }

    /**
     * Set the handler to show/hide location graphics at a global level.
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
        nameDrawingAttributes.setLinePaint(nColor);
    }

    /**
     * Get the color used for the name label.
     */
    public Color getNameColor() {
        return (Color) nameDrawingAttributes.getLinePaint();
    }

    /**
     * Set the color used for the location graphic.
     */
    public void setLocationColor(Color lColor) {
        locationDrawingAttributes.setLinePaint(lColor);
    }

    /**
     * Get the color used for the location graphic.
     */
    public Color getLocationColor() {
        return (Color) locationDrawingAttributes.getLinePaint();
    }

    /**
     * A set of controls to manipulate and control the display of data from the
     * handler. This implementation returns a JPanel with a "No Palette"
     * message.
     * 
     * @return a JPanel with text, No Palette
     */
    public java.awt.Component getGUI() {
        // LocationLayer.java chokes if we return null
        JPanel jp = new JPanel();
        jp.add(new JLabel("No Palette"));
        return jp;
    }

    /**
     * Called by the LocationLayer when the layer is removed from the map. The
     * LocationHandler should release expensive resources if this is called.
     */
    public void removed(java.awt.Container cont) {}

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
     * <LI>featuresSvcURL - URL to invoke to retrieve the XML Features document
     * </UL>
     */
    public void setProperties(String prefix, Properties properties) {
        propertyPrefix = prefix;

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        showLocations = PropUtils.booleanFromProperties(properties, prefix
                + ShowLocationsProperty, showLocations);
        showNames = PropUtils.booleanFromProperties(properties, prefix
                + ShowNamesProperty, showNames);

        nameDrawingAttributes.setProperties(prefix + NamePropertyPrefix,
                properties);
        locationDrawingAttributes.setProperties(prefix + LocationPropertyPrefix,
                properties);

        // For backward compatibility
        setLocationColor((Color) PropUtils.parseColorFromProperties(properties,
                prefix + LocationColorProperty,
                getLocationColor()));
        setNameColor((Color) PropUtils.parseColorFromProperties(properties,
                prefix + NameColorProperty,
                getNameColor()));
        //

        forceGlobal = PropUtils.booleanFromProperties(properties, prefix
                + ForceGlobalProperty, forceGlobal);
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

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + ShowNamesProperty, new Boolean(showNames).toString());
        props.put(prefix + ShowLocationsProperty,
                new Boolean(showLocations).toString());
        props.put(prefix + ForceGlobalProperty,
                new Boolean(forceGlobal).toString());

        // Just to make sure.
        nameDrawingAttributes.setPropertyPrefix(prefix + NamePropertyPrefix);
        locationDrawingAttributes.setPropertyPrefix(prefix + LocationPropertyPrefix);
        
        nameDrawingAttributes.getProperties(props);
        locationDrawingAttributes.getProperties(props);

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

        PropUtils.setI18NPropertyInfo(i18n,
                list,
                AbstractLocationHandler.class,
                ShowNamesProperty,
                "Show names",
                "Display all the location name labels.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        
        PropUtils.setI18NPropertyInfo(i18n,
                list,
                AbstractLocationHandler.class,
                ShowLocationsProperty,
                "Show locations",
                "Display all the location markers.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                list,
                AbstractLocationHandler.class,
                ForceGlobalProperty,
                "Layer Override",
                "Layer settings override map object settings.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        
        nameDrawingAttributes.getPropertyInfo(list);
        locationDrawingAttributes.getPropertyInfo(list);

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
     * @return the property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public DrawingAttributes getLocationDrawingAttributes() {
        return locationDrawingAttributes;
    }

    public void setLocationDrawingAttributes(DrawingAttributes lda) {
        this.locationDrawingAttributes = lda;
    }

    public DrawingAttributes getNameDrawingAttributes() {
        return nameDrawingAttributes;
    }

    public void setNameDrawingAttributes(DrawingAttributes nda) {
        this.nameDrawingAttributes = nda;
    }

}