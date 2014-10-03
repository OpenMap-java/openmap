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
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.PropUtils;

/**
 * The <tt>AbstractLocationHandler</tt> class facilitates the implementation of
 * a <code>LocationHandler</code> by implementing a number of methods. By
 * extending this class, a developer need only implement get(), setProperties(),
 * and reloadData().
 * 
 * <pre>
 * 
 *     locationhandler.location.lineColor=FF0000
 *     locationhandler.name.lineColor=008C54
 *     locationhandler.showNames=false
 *     locationhandler.showLocations=true
 *     locationhandler.override=true
 *     
 *     # optional, can be used if you override createLocation and need access to varying rendering attributes.
 *     # ra1, ra2 and ra3 would be used as keys in renderAttributes map.  All GraphicAttributes properties are available, not
 *     # just lineColor.
 *     
 *     locationhandler.renderAttributesList=ra1 ra2 ra3
 *     locationhandler.ra1.lineColor=0xFFFF0000
 *     locationhandler.ra2.lineColor=0xFF00FF00
 *     locationhandler.ra3.lineColor=0xFF00FFFF
 * 
 * </pre>
 * 
 * @see com.bbn.openmap.layer.location.LocationHandler
 * @version $Revision: 1.10 $ $Date: 2006/02/13 16:54:18 $
 * @author Michael E. Los D530/23448
 */
public abstract class AbstractLocationHandler extends OMComponent implements LocationHandler {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.location.LocationHandler");

    /** The parent layer. */
    protected LocationLayer zLayer;

    /** The default setting for the labels at startup. */
    private boolean showNames = false;
    /** The rendering attributes for the names. */
    /** The default setting for the locations at startup. */
    private boolean showLocations = true;

    /**
     * Property for properties to populate the renderAttributes HashMap. The
     * property should contain a space separated list of keys that will be then
     * used as a prefix for the properties and key for the Map.
     */
    public final static String RenderAttributesListProperty = "renderAttributesList";

    /**
     * Map that holds rendering attribute objects, under a key. The "name" and
     * "location" DrawingAttributes are stored here by default.
     */
    Map<String, GraphicAttributes> renderAttributes = new HashMap<String, GraphicAttributes>();

    /**
     * Force global settings to override local Location settings for
     * showLocation and showName.
     */
    private boolean forceGlobal = true;

    private String prettyName = "";

    /**
     * All LocationHandlers have access to an I18n object, which is provided by
     * the Environment.
     */
    protected transient I18n i18n = Environment.getI18n();

    protected AbstractLocationHandler() {
        GraphicAttributes nAttributes = new GraphicAttributes();
        nAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        renderAttributes.put(NamePropertyPrefix, nAttributes);

        GraphicAttributes lAttributes = new GraphicAttributes();
        lAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        renderAttributes.put(LocationPropertyPrefix, lAttributes);
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
        renderAttributes.get(NamePropertyPrefix).setLinePaint(nColor);
    }

    /**
     * Get the color used for the name label.
     */
    public Color getNameColor() {
        return (Color) renderAttributes.get(NamePropertyPrefix).getLinePaint();
    }

    /**
     * Set the color used for the location graphic.
     */
    public void setLocationColor(Color lColor) {
        renderAttributes.get(LocationPropertyPrefix).setLinePaint(lColor);
    }

    /**
     * Get the color used for the location graphic.
     */
    public Color getLocationColor() {
        return (Color) renderAttributes.get(LocationPropertyPrefix).getLinePaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.layer.location.LocationHandler#getItemsForPopupMenu(com
     * .bbn.openmap.layer.location.Location)
     */
    public List<Component> getItemsForPopupMenu(Location loc) {
        return null;
    }

    /**
     * Called by the LocationLayer when the layer is removed from the map. The
     * LocationHandler should release expensive resources if this is called.
     */
    public void removed(java.awt.Container cont) {
    }

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

        showLocations = PropUtils.booleanFromProperties(properties, prefix + ShowLocationsProperty, showLocations);
        showNames = PropUtils.booleanFromProperties(properties, prefix + ShowNamesProperty, showNames);

        renderAttributes.get(NamePropertyPrefix).setProperties(prefix + NamePropertyPrefix, properties);
        renderAttributes.get(LocationPropertyPrefix).setProperties(prefix + LocationPropertyPrefix, properties);

        forceGlobal = PropUtils.booleanFromProperties(properties, prefix + ForceGlobalProperty, forceGlobal);
        setPrettyName(properties.getProperty(prefix + Layer.PrettyNameProperty, getPrettyName()));

        Vector<String> renAttKeys = PropUtils.parseSpacedMarkers(properties.getProperty(prefix
                + RenderAttributesListProperty));
        if (renAttKeys != null) {
            for (String renAttKey : renAttKeys) {
                String key = prefix + renAttKey;
                renderAttributes.put(key, new GraphicAttributes(key, properties));
            }
        }
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
        props.put(prefix + ShowLocationsProperty, new Boolean(showLocations).toString());
        props.put(prefix + ForceGlobalProperty, new Boolean(forceGlobal).toString());
        props.put(prefix + Layer.PrettyNameProperty, getPrettyName());

        StringBuilder renAttList = new StringBuilder();
        for (String key : renderAttributes.keySet()) {
            // Only create a list if more than name/location in the list
            if (!key.equals(NamePropertyPrefix) && !key.equals(LocationPropertyPrefix)) {
                renAttList.append(key).append(" ");
            }
            // But still put name/location attributes in properties.
            GraphicAttributes ga = renderAttributes.get(key);
            ga.getProperties(props);
        }
        if (renAttList.length() > 0) {
            props.put(prefix + RenderAttributesListProperty, renAttList.toString());
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

        PropUtils.setI18NPropertyInfo(i18n, list, AbstractLocationHandler.class, ShowNamesProperty, "Show names", "Display all the location name labels.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, AbstractLocationHandler.class, ShowLocationsProperty, "Show locations", "Display all the location markers.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, AbstractLocationHandler.class, ForceGlobalProperty, "Layer Override", "Layer settings override map object settings.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, AbstractLocationHandler.class, Layer.PrettyNameProperty, "Pretty Name", "Name for Data Set.", null);

        renderAttributes.get(NamePropertyPrefix).getPropertyInfo(list);
        renderAttributes.get(LocationPropertyPrefix).getPropertyInfo(list);

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

    public GraphicAttributes getLocationDrawingAttributes() {
        return renderAttributes.get(LocationPropertyPrefix);
    }

    public void setLocationDrawingAttributes(GraphicAttributes lda) {
        renderAttributes.put(LocationPropertyPrefix, lda);
    }

    public GraphicAttributes getNameDrawingAttributes() {
        return renderAttributes.get(NamePropertyPrefix);
    }

    public void setNameDrawingAttributes(GraphicAttributes nda) {
        renderAttributes.put(NamePropertyPrefix, nda);
    }

    /**
     * Set the name used in the GUI to represent this data set.
     * 
     * @param prettyName A GUI pretty name.
     */
    public void setPrettyName(String prettyName) {
        this.prettyName = prettyName;
    }

    /**
     * Get the GUI pretty name for the data set retrieved by this
     * LocationHandler.
     * 
     * @return pretty name for location handler, for GUI use.
     */
    public String getPrettyName() {
        return prettyName;
    }

}