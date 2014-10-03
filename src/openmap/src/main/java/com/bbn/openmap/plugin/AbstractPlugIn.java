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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/AbstractPlugIn.java,v $
// $RCSfile: AbstractPlugIn.java,v $
// $Revision: 1.13 $
// $Date: 2006/01/13 21:05:22 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.plugin;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Properties;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * This class is an abstract implementation of the PlugIn. It takes care of
 * setting up the layer, setting properties, etc.
 * 
 * @see com.bbn.openmap.plugin.PlugInLayer
 * @see com.bbn.openmap.plugin.PlugIn
 */
public abstract class AbstractPlugIn implements PlugIn, PropertyConsumer,
        MapMouseListener {

    /**
     * Property 'removable' to designate this layer as removable from the
     * application, or able to be deleted. True by default.
     */
    public static final String RemovableProperty = "removable";

    /**
     * Flag to designate the layer as removable or not.
     */
    protected boolean removable = true;

    /** The parent component, usually the PlugInLayer. */
    protected Component component = null;
    /** The prefix for the plugin's properties. */
    protected String prefix = null;
    /**
     * The pretty name for a plugin, if it was set in the properties.
     */
    protected String name = this.getClass().getName();

    /**
     * The object handling mouse events for the plugin. By default, the Plugin
     * is it, but it doesn't have to be.
     */
    protected MapMouseListener mml = this;

    /**
     * Flag to denote whether the plugin should be added to the bean context
     * (MapHandler). True by default.
     */
    protected boolean addToBeanContext = true;

    /**
     * Internationalization
     */
    public I18n i18n = Environment.getI18n();

    public AbstractPlugIn() {}

    public AbstractPlugIn(Component comp) {
        setComponent(comp);
    }

    /**
     * Set the name of the plugin. If the parent component is a layer, set its
     * pretty name as well.
     */
    public void setName(String name) {
        this.name = name;
        Component comp = getComponent();
        if (comp != null) {
            comp.setName(name);
        }
    }

    /**
     * Get the pretty name of the plugin, which is really the pretty name of the
     * parent component if it's set.
     */
    public String getName() {
        Component comp = getComponent();
        if (comp != null) {
            name = comp.getName();
        }
        return name;
    }

    /**
     * Set the component that this PlugIn uses as a grip to the map.
     */
    public void setComponent(Component comp) {
        this.component = comp;
    }

    /**
     * Get the component that this plugin uses as a grip to the map.
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Call repaint on the parent component.
     */
    public void repaint() {
        component.repaint();
    }

    /**
     * Checks to see if the parent component is a PlugInLayer, and calls
     * doPrepare() on it if it is.
     */
    public void doPrepare() {
        if (component instanceof PlugInLayer) {
            ((PlugInLayer) component).doPrepare();
        }
    }

    /**
     * Set the MapMouseListener for this PlugIn. The MapMouseListener is
     * responsible for handling the MouseEvents that are occurring over the layer
     * using the PlugIn, as well as being able to let others know which
     * MouseModes are of interest to receive MouseEvents from.
     * 
     * @param mml MapMouseListener.
     */
    public void setMapMouseListener(MapMouseListener mml) {
        this.mml = mml;
    }

    /**
     * Returns the MapMouseListener that the plugin thinks should be used.
     */
    public MapMouseListener getMapMouseListener() {
        return mml;
    }

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to fill a graphics list with objects that are within the
     * screen parameters passed. It's assumed that the PlugIn will call
     * generate(projection) on the OMGraphics returned! If you don't call
     * generate on the OMGraphics, they will not be displayed on the map.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width. May be null if the parent component hasn't been given a
     *        projection.
     */
    public abstract OMGraphicList getRectangle(Projection p);

    /**
     */
    public Component getGUI() {
        return null;
    }

    public void setAddToBeanContext(boolean value) {
        addToBeanContext = value;
    }

    public boolean getAddToBeanContext() {
        return addToBeanContext;
    }

    /**
     * Mark the plugin (and layer) as removable, or one that can be deleted from
     * the application. What that means is up to the LayerHandler or other
     * application components.
     */
    public void setRemovable(boolean set) {
        this.removable = set;
        Component comp = getComponent();
        if ((comp != null) && (comp instanceof Layer)) {
            ((Layer) comp).setRemovable(set);
        }
    }

    /**
     * Check to see if the plugin (and layer) is marked as one that can be
     * removed from an application.
     * 
     * @return true if plugin should be allowed to be deleted.
     */
    public boolean isRemovable() {
        Component comp = getComponent();
        if ((comp != null) && (comp instanceof Layer)) {
            this.removable = ((Layer) comp).isRemovable();
        }
        return removable;
    }

    // //// PropertyConsumer Interface Methods

    /**
     * Method to set the properties in the PropertyConsumer. It is assumed that
     * the properties do not have a prefix associated with them, or that the
     * prefix has already been set.
     * 
     * @param setList a properties object that the PropertyConsumer can use to
     *        retrieve expected properties it can use for configuration.
     */
    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    /**
     * Method to set the properties in the PropertyConsumer. The prefix is a
     * string that should be prepended to each property key (in addition to a
     * separating '.') in order for the PropertyConsumer to uniquely identify
     * properties meant for it, in the midst of of Properties meant for several
     * objects.
     * 
     * @param prefix a String used by the PropertyConsumer to prepend to each
     *        property value it wants to look up -
     *        setList.getProperty(prefix.propertyKey). If the prefix had already
     *        been set, then the prefix passed in should replace that previous
     *        value.
     * @param setList a Properties object that the PropertyConsumer can use to
     *        retrieve expected properties it can use for configuration.
     */
    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        name = setList.getProperty(realPrefix + Layer.PrettyNameProperty);
        setAddToBeanContext(PropUtils.booleanFromProperties(setList, realPrefix
                + Layer.AddToBeanContextProperty, addToBeanContext));
        setRemovable(PropUtils.booleanFromProperties(setList, realPrefix
                + RemovableProperty, removable));
    }

    /**
     * Method to fill in a Properties object, reflecting the current values of
     * the PropertyConsumer. If the PropertyConsumer has a prefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param getList a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new Properties
     *        object should be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        String realPrefix = PropUtils.getScopedPropertyPrefix(this);
        getList.put(realPrefix + Layer.AddToBeanContextProperty,
                new Boolean(addToBeanContext).toString());
        getList.put(prefix + RemovableProperty,
                new Boolean(removable).toString());
        return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.).
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

        String internString = i18n.get(Layer.class,
                Layer.AddToBeanContextProperty,
                I18n.TOOLTIP,
                "Flag to give access to all of the other application components.");
        list.put(Layer.AddToBeanContextProperty, internString);
        internString = i18n.get(Layer.class,
                Layer.AddToBeanContextProperty,
                "Add to MapHandler");
        list.put(Layer.AddToBeanContextProperty + LabelEditorProperty, internString);
        list.put(Layer.AddToBeanContextProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        internString = i18n.get(AbstractPlugIn.class,
                RemovableProperty,
                I18n.TOOLTIP,
                "Flag to allow layer to be deleted.");
        list.put(RemovableProperty, internString);
        internString = i18n.get(Layer.class, RemovableProperty, "Removable");
        list.put(RemovableProperty + LabelEditorProperty, internString);
        list.put(RemovableProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

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
        this.prefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to the property
     * keys for Properties lookups.
     * 
     * @return the property prefix for the plugin.
     */
    public String getPropertyPrefix() {
        return prefix;
    }

    // /////// MapMouseListener interface methods

    /**
     * Return a list of the modes that are interesting to the MapMouseListener.
     * The source MouseEvents will only get sent to the MapMouseListener if the
     * mode is set to one that the listener is interested in. Layers interested
     * in receiving events should register for receiving events in "select"
     * mode: <code>
     * <pre>
     * return new String[] { SelectMouseMode.modeID };
     * </pre>
     * <code>
     * @return String[] of modeID's
     * @see com.bbn.openmap.event.NavMouseMode#modeID
     * @see com.bbn.openmap.event.SelectMouseMode#modeID
     * @see com.bbn.openmap.event.NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    // Mouse Listener events
    // //////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component. The listener will
     * receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener processes the
     * event. If the listener successfully processes <code>mouseClicked()</code>,
     * then it will receive the next <code>mouseClicked()</code> notifications
     * that have a click count greater than one.
     * <p>
     * NOTE: We have noticed that this method can sometimes be erroneously
     * invoked. It seems to occur when a light-weight AWT component (like an
     * internal window or menu) closes (removes itself from the window
     * hierarchy). A specific OpenMap example is when you make a menu selection
     * when the MenuItem you select is above the MapBean canvas. After making
     * the selection, the mouseClicked() gets invoked on the MouseDelegator,
     * which passes it to the appropriate listeners depending on the MouseMode.
     * The best way to avoid this problem is to not implement anything crucial
     * in this method. Use a combination of <code>mousePressed()</code> and
     * <code>mouseReleased()</code> instead.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    // Mouse Motion Listener events
    // /////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * The listener will receive these events if it successfully processes
     * mousePressed(), or if no other listener processes the event.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons down).
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed. This event
     * is intended to tell the listener that there was a mouse movement, but
     * that the event was consumed by another layer. This will allow a mouse
     * listener to clean up actions that might have happened because of another
     * motion event response.
     */
    public void mouseMoved() {}

    /**
     * Method that gets called when the PlugInLayer has been removed from the
     * map, so the PlugIn can free up resources.
     */
    public void removed() {}
    
    /**
     * Notification to the PlugIn that it has been removed from the application,
     * so it can disconnect from all other objects that may be holding a
     * reference to it.
     */
    public void dispose() {}

}