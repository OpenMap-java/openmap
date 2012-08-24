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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/Layer.java,v $
// $RCSfile: Layer.java,v $
// $Revision: 1.34 $
// $Date: 2008/09/28 19:06:07 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.InfoDisplayListener;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.LayerStatusListener;
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.gui.ScrollPaneWindowSupport;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.Inspector;

/**
 * Layer objects are components which can be added to the MapBean to make a map.
 * <p>
 * 
 * Layers implement the ProjectionListener interface to listen for
 * ProjectionEvents. When the projection changes, they may need to refetch,
 * regenerate their graphics, and then repaint themselves into the new view.
 * <p>
 * 
 * When the Layer is added to the MapBean, it will start receiving
 * ProjectionEvents via the ProjectionListener.projectionChanged() method it has
 * to implement. There is a setProjection(ProjectionEvent) methods that should
 * be called from there if you want to save the projection for later use
 * (handling MouseEvents, etc). If you call getProjection() before calling
 * setProjection(), getProjection() will return null, and your OMGraphics will
 * complain and probably freak out at some point.
 * 
 * <pre>
 * // // SAMPLE handling of the ProjectionListener interface.
 * 
 * public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe) {
 *     Projection proj = setProjection(pe);
 *     if (proj != null) {
 *         // Use the projection to gather OMGraphics in the layer,
 *         // and prepare the layer so that in the paint() method,
 *         // the OMGraphics get rendered.
 * 
 *         // Call any methods that kick off work to build graphics
 *         // here...
 * 
 *         // You get the paint() methods called by calling
 *         // repaint():
 *         repaint();
 *     }
 * 
 *     fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
 * }
 * </pre>
 * 
 * @see com.bbn.openmap.event.ProjectionListener
 * @see com.bbn.openmap.event.ProjectionEvent
 * @see com.bbn.openmap.PropertyConsumer
 */
public abstract class Layer
        extends JComponent
        implements ProjectionListener, ProjectionPainter, BeanContextChild, BeanContextMembershipListener, PropertyConsumer,
        ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * Pre-caches the swing package. Computed based on the package of
     * <code>JComponent</code>.
     */
    protected static final String SWING_PACKAGE = getPackage(JComponent.class);

    /**
     * The String to use for a key lookup in a Properties object to find the
     * name to use in a GUI relating to this layer.
     */
    public static final String PrettyNameProperty = "prettyName";

    /**
     * The property to set to add the layer to the BeanContext
     * "addToBeanContext". This probably needs be set by the layer itself,
     * because it knows whether it needs other components or not. However, this
     * property is defined in case an option can be given to the user. If a
     * Layer doesn't want this option given, it should reset the
     * addToBeanContext variable after setProperties() is called. The
     * Layer.setProperties() methods maintain the current state of the variable
     * if undefined, which is true by default.
     */
    public static final String AddToBeanContextProperty = "addToBeanContext";

    /**
     * Property 'background' to designate this layer as a background layer,
     * which will cause extra buffering to occur if the application can handle
     * it. False by default.
     */
    public static final String AddAsBackgroundProperty = "background";

    /**
     * Property 'removable' to designate this layer as removable from the
     * application, or able to be deleted. True by default.
     */
    public static final String RemovableProperty = "removable";

    /**
     * The property for designating the minimum projection scale value that the
     * layer will respond to. This Layer class doesn't limit how subclasses will
     * react to projections with scale values smaller than the specified value.
     */
    public static final String MinScaleProperty = "minScale";
    /**
     * The property for designating the maximum projection scale value that the
     * layer will respond to. This Layer class doesn't limit how subclasses will
     * react to projections with scale values greater than the specified value.
     */
    public static final String MaxScaleProperty = "maxScale";

    /**
     * The property to show the palette when the layer is created - or, more
     * accurately, when the properties are set.
     */
    public static final String AutoPaletteProperty = "autoPalette";
    /** Layer-defined action event command to display the palette. */
    public static final String DisplayPaletteCmd = "displayPaletteCmd";
    /** Layer-defined action event command to hide the palette. */
    public static final String HidePaletteCmd = "hidePaletteCmd";
    /**
     * Layer-defined action event command to display the properties using an
     * Inspector.
     */
    public static final String DisplayPropertiesCmd = "displayPropertiesCmd";
    /**
     * Layer-defined action event command to force a redraw on the layer. The
     * Layer class does not respond to this command, it's provided as a
     * convenience.
     */
    public static final String RedrawCmd = "redrawCmd";
    /**
     * Generic property that may be set for a layer to let it know at runtime
     * that a path prefix needs to be added to a relative data path. This
     * property should be set in the attributes of a layer after being read from
     * properties.
     */
    public static final String DataPathPrefixProperty = "dataPathPrefix";

    /**
     * The property used to name a class providing translation services
     * (optional, depends on input data).
     */
    public final static String TransformProperty = "transform";

    /**
     * The listeners to the Layer that respond to requests for information
     * displays, like messages, requests for URL displays, etc.
     */
    private final ListenerSupport<InfoDisplayListener> IDListeners = new ListenerSupport<InfoDisplayListener>(this);

    /**
     * List of LayerStatusListeners.
     */
    private final ListenerSupport<LayerStatusListener> lsListeners = new ListenerSupport<LayerStatusListener>(this);

    /**
     * Token uniquely identifying this layer in the application properties.
     */
    protected String propertyPrefix = null;

    /**
     * Used by the LayerHandler to check if the layer should be added to the
     * MapHandler BeanContext. See the comments under the
     * AddToBeanContextProperty. True by default.
     */
    protected boolean addToBeanContext = true;

    /**
     * Flag used by the layer to indicate that it should be treated as a
     * background layer, indicating that any cache mechanism available can
     * enable extra buffering. This may prevent mouse events from being received
     * by the layer.
     */
    protected boolean addAsBackground = false;

    /**
     * Flag to designate the layer as removable or not.
     */
    protected boolean removable = true;

    /**
     * A flag to have the layer display it's palette when the properties are
     * set. If you are creating a layer manually, just call showPalette()
     * instead.
     */
    protected boolean autoPalette = false;

    /**
     * A minimum projection scale value that the layer will respond to. Using
     * this value for reacting to the projection depends on the Layer
     * implementation, the Layer class doesn't limit subclasses from doing their
     * own thing in response to the scale setting on a projection.
     */
    protected float minScale = Float.MIN_VALUE;

    /**
     * A maximum projection scale value that the layer will respond to. Using
     * this value for reacting to the projection depends on the Layer
     * implementation, the Layer class doesn't limit subclasses from doing their
     * own thing in response to the scale setting on a projection.
     */
    protected float maxScale = Float.MAX_VALUE;

    /**
     * This is a convenience copy of the latest projection received from the
     * MapBean, when the Layer is added to the map. If you need it, use the
     * accessor!.
     */
    private Projection projection = null;

    /**
     * Support class that now handles palette windows.
     */
    protected transient WindowSupport windowSupport;

    /**
     * A helper component listener that is paying attention to the visibility of
     * the palette.
     */
    protected transient ComponentListener paletteListener;

    /**
     * A pointer to the JDialog or JInternalFrame. May be used by the layer's
     * ComponentListeners to figure out if a component event is for the layer or
     * for the palette.
     */
    protected transient Container palette;

    /**
     * The BeanContext allows Layers to find other components, and other
     * components to find the layer, if the layer is added to it.
     */
    protected transient BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * All layers have access to an I18n object, which is provided by the
     * Environment.
     */
    protected transient I18n i18n = Environment.getI18n();

    /**
     * Icon associated with layer.
     */
    private Icon icon = null;

    /**
     * Hashtable containing attribute information for the layer. For serialized
     * layers, we're not really going out of our way to make sure that keys and
     * values are serializable.
     */
    protected Hashtable attributes;

    /**
     * A translator that can be set to convert pre-projected coordinates from
     * the file into decimal degree lat/lon, and for saving data to a file in
     * pre-projected coordinates.
     */
    protected GeoCoordTransformation coordTransform;

    /**
     * Returns the package of the given class as a string.
     * 
     * @param c a class
     */
    protected static String getPackage(Class c) {
        String className = c.getName();
        int lastDot = className.lastIndexOf('.');
        return className.substring(0, lastDot);
    }

    /**
     * Override to only allow swing package listeners. If Listeners get added to
     * the Layers, the mouse events don't make it to the map. Ever.
     * <p>
     * Swing popup menus, like <code>JPopupMenu</code> grab the JComponent by
     * adding themselves as <code>MouseListener</code> s. So this method allows
     * instances of classes in the xxx.swing package to be added as
     * <code>MouseListener</code>s, and no one else.
     * 
     * @param l a mouse listener.
     */
    public final void addMouseListener(MouseListener l) {
        String pkg = getPackage(l.getClass());
        if (java.beans.Beans.isDesignTime() || pkg.equals(SWING_PACKAGE) || pkg.startsWith(SWING_PACKAGE)
                || pkg.startsWith("com.sun.java.accessibility.util")) {

            // Used to do nothing for the equals and startsWith
            // comparison, but that breaks the menus from being
            // recinded when something else is clicked on. Thanks to
            // Tom Peel for pointing this out, 11/29/00.
            super.addMouseListener(l);
        } else {
            throw new IllegalArgumentException("This operation is disallowed because the package \"" + getPackage(l.getClass())
                    + "\" is not in the swing package (\"" + SWING_PACKAGE + "\").");
        }
    }

    /**
     * Sets the properties for the <code>Layer</code>. This particular method
     * assumes that the marker name is not needed, because all of the contents
     * of this Properties object are to be used for this layer, and scoping the
     * properties with a prefix is unnecessary.
     * 
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(Properties props) {
        setProperties(getPropertyPrefix(), props);
    }

    /**
     * Sets the properties for the <code>Layer</code>. Part of the
     * PropertyConsumer interface. Layers which override this method should do
     * something like:
     * 
     * <code><pre>
     * public void setProperties(String prefix, Properties props) {
     *     super.setProperties(prefix, props);
     *     // do local stuff
     * }
     * </pre></code>
     * 
     * If the addToBeanContext property is not defined, it maintains the same
     * state.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        setLayerProperties(prefix, props);
    }

    /**
     * Called by setProperties. Allows subclasses to avoid super.setProperties()
     * while still having basic Layer settings taken care of.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    protected void setLayerProperties(String prefix, Properties props) {
        String prettyName = PrettyNameProperty;
        setPropertyPrefix(prefix);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        prettyName = realPrefix + PrettyNameProperty;

        String defaultName = getName();
        if (defaultName == null) {
            defaultName = "Anonymous";
        }

        setName(props.getProperty(prettyName, defaultName));

        setAddToBeanContext(PropUtils.booleanFromProperties(props, realPrefix + AddToBeanContextProperty, addToBeanContext));

        setAddAsBackground(PropUtils.booleanFromProperties(props, realPrefix + AddAsBackgroundProperty, addAsBackground));

        setRemovable(PropUtils.booleanFromProperties(props, realPrefix + RemovableProperty, removable));

        autoPalette = PropUtils.booleanFromProperties(props, realPrefix + AutoPaletteProperty, autoPalette);

        setMinScale(PropUtils.floatFromProperties(props, realPrefix + MinScaleProperty, getMinScale()));
        setMaxScale(PropUtils.floatFromProperties(props, realPrefix + MaxScaleProperty, getMaxScale()));

        String dataPathPrefix = props.getProperty(realPrefix + DataPathPrefixProperty, "");
        if (dataPathPrefix.length() > 0) {
            putAttribute(DataPathPrefixProperty, dataPathPrefix);
        }

        String transClassName = props.getProperty(realPrefix + TransformProperty);
        if (transClassName != null) {
            try {
                coordTransform =
                        (GeoCoordTransformation) ComponentFactory.create(transClassName, realPrefix + TransformProperty, props);
            } catch (ClassCastException cce) {

            }
        }
    }

    public void setName(String name) {
        super.setName(name);

        BeanContext bc = getBeanContext();
        if (bc instanceof MapHandler) {
            LayerHandler lh = (LayerHandler) ((MapHandler) bc).get("com.bbn.openmap.LayerHandler");

            if (lh != null) {
                lh.setLayers();
            }
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
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

        String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);
        props.put(prefix + "class", this.getClass().getName());

        String prettyName = getName();
        if (prettyName != null) {
            props.put(prefix + PrettyNameProperty, prettyName);
        }

        props.put(prefix + AutoPaletteProperty, new Boolean(autoPalette).toString());
        props.put(prefix + AddAsBackgroundProperty, new Boolean(addAsBackground).toString());
        props.put(prefix + RemovableProperty, new Boolean(removable).toString());
        props.put(prefix + AddToBeanContextProperty, new Boolean(addToBeanContext).toString());

        if (getMinScale() != Float.MIN_VALUE) {
            props.put(prefix + MinScaleProperty, Float.toString(getMinScale()));
        }
        if (getMaxScale() != Float.MAX_VALUE) {
            props.put(prefix + MaxScaleProperty, Float.toString(getMaxScale()));
        }

        String dataPathPrefix = (String) getAttribute(DataPathPrefixProperty);
        if (dataPathPrefix != null) {
            props.put(prefix + DataPathPrefixProperty, dataPathPrefix);
        }

        if (coordTransform instanceof PropertyConsumer) {
            ((PropertyConsumer) coordTransform).getProperties(props);
        }

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). For Layer, this method should at least
     * return the 'prettyName' property.
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

        list.put("class", "Class Name used for Layer.");
        list.put("class.editor", "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");

        String internString = i18n.get(Layer.class, PrettyNameProperty, I18n.TOOLTIP, "Presentable name for Layer");
        list.put(PrettyNameProperty, internString);
        internString = i18n.get(Layer.class, PrettyNameProperty, "Layer Name");
        list.put(PrettyNameProperty + LabelEditorProperty, internString);
        list.put(PrettyNameProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");

        internString =
                i18n.get(Layer.class, AutoPaletteProperty, I18n.TOOLTIP,
                         "Flag to automatically display palette when properties are set");
        list.put(AutoPaletteProperty, internString);
        internString = i18n.get(Layer.class, AutoPaletteProperty, "Open Palette At Start");
        list.put(AutoPaletteProperty + LabelEditorProperty, internString);
        list.put(AutoPaletteProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        internString = i18n.get(Layer.class, AddAsBackgroundProperty, I18n.TOOLTIP, "Flag to use the layer as a background layer");
        list.put(AddAsBackgroundProperty, internString);
        internString = i18n.get(Layer.class, AddAsBackgroundProperty, "Background");
        list.put(AddAsBackgroundProperty + LabelEditorProperty, internString);
        list.put(AddAsBackgroundProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        internString = i18n.get(Layer.class, RemovableProperty, I18n.TOOLTIP, "Flag to allow layer to be deleted.");
        list.put(RemovableProperty, internString);
        internString = i18n.get(Layer.class, RemovableProperty, "Removable");
        list.put(RemovableProperty + LabelEditorProperty, internString);
        list.put(RemovableProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        internString =
                i18n.get(Layer.class, AddToBeanContextProperty, I18n.TOOLTIP,
                         "Flag to give the layer access to all of the other application components.");
        list.put(AddToBeanContextProperty, internString);
        internString = i18n.get(Layer.class, AddToBeanContextProperty, "Add to MapHandler");
        list.put(AddToBeanContextProperty + LabelEditorProperty, internString);
        list.put(AddToBeanContextProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        internString =
                i18n.get(Layer.class, MinScaleProperty, I18n.TOOLTIP,
                         "Minimum projection scale value that the layer will respond to.");
        list.put(MinScaleProperty, internString);
        internString = i18n.get(Layer.class, MinScaleProperty, "Minimum Scale Value");
        list.put(MinScaleProperty + LabelEditorProperty, internString);

        internString =
                i18n.get(Layer.class, MaxScaleProperty, I18n.TOOLTIP,
                         "Maximum projection scale value that the layer will respond to.");
        list.put(MaxScaleProperty, internString);
        internString = i18n.get(Layer.class, MaxScaleProperty, "Maximum Scale Value");
        list.put(MaxScaleProperty + LabelEditorProperty, internString);

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
     * @return the property prefix for the layer
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * Set the projection the layer should use for calculations. You probably
     * don't need this if you are wondering if you do. Call
     * setProjection(projEvent) instead.
     */
    public void setProjection(Projection proj) {
        projection = proj;
    }

    /**
     * This method lets you take the ProjectionEvent received from the MapBean,
     * and lets you know if you should do something with it. MUST to be called
     * in the projectionChanged() method of your layer, if you want to refer to
     * the projection later. If this methods returns null, you probably just
     * want to call repaint() if your layer.paint() method is ready to paint
     * what it should.
     * 
     * @param projEvent the ProjectionEvent from the ProjectionListener method.
     * @return The new Projection if it is different from the one we already
     *         have, null if is the same as the current one.
     */
    public Projection setProjection(ProjectionEvent projEvent) {
        Projection newProjection = projEvent.getProjection();

        if (!newProjection.equals(getProjection())) {
            Projection clone = newProjection.makeClone();
            setProjection(clone);
            return clone;
        } else {
            return null;
        }
    }

    /**
     * Get the latest projection.
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Returns the MapMouseListener object that handles the mouse events. This
     * method is IGNORED in this class: it returns null. Derived Layers should
     * return the appropriate object if they desire to receive MouseEvents. The
     * easiest thing for a Layer to do in order to receive MouseEvents is to
     * implement the MapMouseListener interface and return itself. A code
     * snippet: <code><pre>
     * public MapMouseListener getMapMouseListener() {
     *     return this;
     * }
     * 
     * public String[] getMouseModeServiceList() {
     *     return new String[] { SelectMouseMode.modeID };
     * }
     * </pre></code>
     * 
     * @return null for the default, method can be overridden to return
     *         something interested in mouse events.
     */
    public MapMouseListener getMapMouseListener() {
        return null;
    }

    /**
     * A GUI query method for other components to use to display whether gui
     * controls are available for this layer. If your layer provides gui
     * controls and the LayersPanel is being used in your application, you
     * should override this method in your layer to return true. This will allow
     * the other gui components to know that your layer has controls, while
     * still not actually building those controls until they are needed by the
     * user.
     * 
     * @return true if there is a GUI for controlling the layer settings, false
     *         if not or if the getGUI() method hasn't been overridden.
     */
    public boolean hasGUI() {
        boolean hasGUI = false;

        try {
            Method guiMethod = getClass().getMethod("getGUI", (Class[]) null);
            hasGUI = (guiMethod.getDeclaringClass() != Layer.class);
        } catch (Exception ex) {
            hasGUI = getGUI() != null;
        }

        return hasGUI;
    }

    /**
     * Gets the gui controls associated with the layer. This default
     * implementation returns null indicating that the layer has no gui
     * controls.
     * 
     * @return java.awt.Component or null, null by default.
     */
    public Component getGUI() {
        return null;
    }

    // /////////////////////////////////////////////////
    // InfoDisplay Handling Setup and Firing

    /**
     * Adds a listener for <code>InfoDisplayEvent</code>s.
     * 
     * @param aInfoDisplayListener the listener to add
     */
    public void addInfoDisplayListener(InfoDisplayListener aInfoDisplayListener) {
        // synchronized (IDListeners) {    //2012.06.15 TAW
            IDListeners.add(aInfoDisplayListener);
        //}
    }

    /**
     * Removes an InfoDisplayListener from this Layer.
     * 
     * @param aInfoDisplayListener the listener to remove
     */
    public void removeInfoDisplayListener(InfoDisplayListener aInfoDisplayListener) {
        // synchronized (IDListeners) {  //2012.06.15 TAW
            IDListeners.remove(aInfoDisplayListener);
       // }
    }

    /**
     * Sends a request to the InfoDisplayListener to show the information in the
     * InfoDisplay event on an single line display facility.
     * 
     * @param evt the InfoDisplay event carrying the string.
     */
    public void fireRequestInfoLine(InfoDisplayEvent evt) {
        //synchronized (IDListeners) { // //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                listener.requestInfoLine(evt);
            }
        //}
    }

    /**
     * Sends a request to the InfoDisplay listener to display the information on
     * an single line display facility. The InfoDisplayEvent is created inside
     * this function.
     * 
     * @param infoLine the string to put in the InfoDisplayEvent.
     */
    public void fireRequestInfoLine(String infoLine) {
        fireRequestInfoLine(new InfoDisplayEvent(this, infoLine));
    }

    /**
     * Sends a request to the InfoDisplay listener to display the information on
     * an single line display facility at preferred location. The
     * InfoDisplayEvent is created inside this function.
     * 
     * @param infoLine the string to put in the InfoDisplayEvent.
     * @param loc the index of a preferred location, starting at 0.
     */
    public void fireRequestInfoLine(String infoLine, int loc) {
        fireRequestInfoLine(new InfoDisplayEvent(this, infoLine, loc));
    }

    /**
     * Sends a request to the InfoDisplay listener to display the information in
     * the InfoDisplay event in a Browser.
     * 
     * @param evt the InfoDisplayEvent holding the contents to put in the
     *        Browser.
     */
    public void fireRequestBrowserContent(InfoDisplayEvent evt) {
        // synchronized (IDListeners) {   //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                listener.requestBrowserContent(evt);
            }
        // }
    }

    /**
     * Sends a request to the InfoDisplayListener to display the information in
     * a Browser. The InfoDisplayEvent is created here holding the
     * browserContent
     * 
     * @param browserContent the contents to put in the Browser.
     */
    public void fireRequestBrowserContent(String browserContent) {
        fireRequestBrowserContent(new InfoDisplayEvent(this, browserContent));
    }

    /**
     * Sends a request to the InfoDisplayListener to display a URL given in the
     * InfoDisplay event in a Browser.
     * 
     * @param evt the InfoDisplayEvent holding the url location to give to the
     *        Browser.
     */
    public void fireRequestURL(InfoDisplayEvent evt) {
        // synchronized (IDListeners) {      //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                listener.requestURL(evt);
            }
        // }
    }

    /**
     * Sends a request to the InfoDisplayListener to display a URL in a browser.
     * The InfoDisplayEvent is created here, and the URL location is put inside
     * it.
     * 
     * @param url the url location to give to the Browser.
     */
    public void fireRequestURL(String url) {
        fireRequestURL(new InfoDisplayEvent(this, url));
    }

    /**
     * Sends a request to the InfoDisplayListener to show a specific cursor over
     * its component area.
     * 
     * @param cursor the cursor to use.
     */
    public void fireRequestCursor(java.awt.Cursor cursor) {
        // synchronized (IDListeners) {  //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                listener.requestCursor(cursor);
            }
        // }
    }

    /**
     * Sends a request to the InfoDisplayListener to put the information in the
     * InfoDisplay event in a dialog window.
     * 
     * @param evt the InfoDisplayEvent holding the message to put into the
     *        dialog window.
     */
    public void fireRequestMessage(InfoDisplayEvent evt) {
        // synchronized (IDListeners) {  //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                listener.requestMessage(evt);
            }
        // }
    }

    /**
     * Sends a request to the InfoDisplayListener to display the information in
     * a dialog window. The InfoDisplayEvent is created here, and the URL
     * location is put inside it.
     * 
     * @param message the message to put in the dialog window.
     */
    public void fireRequestMessage(String message) {
        fireRequestMessage(new InfoDisplayEvent(this, message));
    }

    /**
     * Request to show the tool tips on the map.
     * 
     * @param tip string to display.
     */
    public void fireRequestToolTip(String tip) {
        fireRequestToolTip(new InfoDisplayEvent(this, tip));
    }

    /**
     * Request to hide the tool tips on the map.
     */
    public void fireHideToolTip() {
        fireRequestToolTip((InfoDisplayEvent) null);
    }

    /**
     * Fire off a Tool Tip request to the InfoDisplayListeners. If the
     * InfoDisplayEvent is null, then a requestHideToolTip will be fired.
     */
    public void fireRequestToolTip(InfoDisplayEvent event) {
        // synchronized (IDListeners) {   //2012.06.15 TAW
            for (InfoDisplayListener listener : IDListeners) {
                if (event != null) {
                    listener.requestShowToolTip(event);
                } else {
                    listener.requestHideToolTip();
                }
            }
        //}
    }

    // /////////////////////////////////////////////////
    // LayerStatus Handling Setup and Firing

    /**
     * Adds a listener for <code>LayerStatusEvent</code>s.
     * 
     * @param aLayerStatusListener LayerStatusListener
     */
    public void addLayerStatusListener(LayerStatusListener aLayerStatusListener) {
        //synchronized (lsListeners) {  //2012.06.15 TAW
            lsListeners.add(aLayerStatusListener);
        //}
    }

    /**
     * Removes a LayerStatusListene from this Layer.
     * 
     * @param aLayerStatusListener the listener to remove
     */
    public void removeLayerStatusListener(LayerStatusListener aLayerStatusListener) {
        // synchronized (lsListeners) {  //2012.06.15 TAW
            lsListeners.remove(aLayerStatusListener);
        //}
    }

    /**
     * Sends a status update to the LayerStatusListener.
     * 
     * @param evt LayerStatusEvent
     */
    public void fireStatusUpdate(LayerStatusEvent evt) {
        // synchronized (lsListeners) {  //2012.06.15 TAW
            // AWTAvailable conditional removed, not used, not useful.
            for (LayerStatusListener listener : lsListeners) {
                listener.updateLayerStatus(evt);
            }
        // }
    }

    /**
     * Sends a status update to the LayerStatusListener.
     * 
     * @param status the new status
     */
    public void fireStatusUpdate(int status) {
        fireStatusUpdate(new LayerStatusEvent(this, status));
    }

    /**
     * Repaint the layer. If you are using BufferedMapBean for your application,
     * WE STRONGLY RECOMMEND THAT YOU DO NOT OVERRIDE THIS METHOD. This method
     * marks the layer buffer so that it will be refreshed. If you override this
     * method, and don't call super.repaint(), the layers will not be repainted.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
        Component p = getParent();
        if (p instanceof MapBean) {
            ((MapBean) p).setBufferDirty(true);
            if (Debug.debugging("basic")) {
                Debug.output(getName() + "|Layer: repaint(tm=" + tm + ", x=" + x + ", y=" + y + ", width=" + width + ", height="
                        + height + ")");
            }

            // How dangerous is this? Let the MapBean manage the
            // repaint call? Seems to work OK, and lets the buffered
            // MapBeans work better when they are embedded in other
            // components. It's this call here that makes the
            // BufferedLayer work right.

            // This repaint request has been changed to call a
            // specific
            // method on the MapBean, which includes the layer making
            // the request. This is a hook for a policy object in the
            // MapBean to make a decision on whether to honor the
            // request, or to handle it in a different way if the
            // environment dictates that should happen.

            // ((MapBean)p).repaint(); to ->
            ((MapBean) p).repaint(this);
        } else if (p != null) {
            p.repaint(tm, x, y, width, height);
        } else {
            super.repaint(tm, x, y, width, height);
        }
    }

    /**
     * This method is here to provide a default action for Layers as they act as
     * a ProjectionPainter. Normally, ProjectionPainters are expected to receive
     * the projection, gather/create OMGraphics that apply to the projection,
     * and render them into the Graphics provided. This is supposed to be done
     * in the same thread that calls this function, so the caller knows that
     * when this method returns, everything that the ProjectionPainter needed to
     * do is complete.
     * <P>
     * If the layer doesn't override this method, then the paint(Graphics)
     * method will be called.
     * 
     * @param proj Projection of the map.
     * @param g java.awt.Graphics to draw into.
     */
    public void renderDataForProjection(Projection proj, Graphics g) {
        if (isProjectionOK(proj)) {
            paint(g);
        }
    }

    /**
     * Method that responds whether the Layer should render on the map, given a
     * particular projection. The method currently just tests the projection's
     * scale against the min and max values set on the layer.
     * 
     * @param proj The projection to test against.
     * @return true if OK.
     */
    public boolean isProjectionOK(Projection proj) {
        return (proj != null && proj.getScale() >= minScale && proj.getScale() <= maxScale);
    }

    /**
     * This method is called when the layer is added to the MapBean
     * 
     * @param cont Container
     */
    public void added(Container cont) {
    }

    /**
     * This method is called after the layer is removed from the MapBean and
     * when the projection changes. We recommend that Layers override this
     * method and nullify memory-intensive variables.
     * 
     * @param cont Container
     */
    public void removed(Container cont) {
    }

    /**
     * Method called when layer detects that it has been removed from
     * MapHandler, assumes it's being thrown away. Use this method to let go of
     * everything and to make any calls necessary to remove from listener lists
     * that might not get picked up via MapHandler calls.
     */
    public void dispose() {
        clearListeners();
        if (attributes != null) {
            attributes.clear();
        }
        removed(null);
    }

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    protected ListenerSupport<ComponentListener> localHackList;

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works. Set to false to test.
     */
    protected boolean doHack = true;

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    public void setVisible(boolean show) {
        super.setVisible(show);
        if (doHack && !show) {
            notifyHideHack();
        }
    }

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    public void addComponentListener(ComponentListener cl) {
        super.addComponentListener(cl);
        if (localHackList == null) {
            localHackList = new ListenerSupport<ComponentListener>(this);
        }
        localHackList.add(cl);
    }

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    public void removeComponentListener(ComponentListener cl) {
        super.removeComponentListener(cl);
        if (localHackList != null) {
            localHackList.remove(cl);
        }
    }

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    public void notifyHideHack() {
        if (localHackList == null) {
            return;
        }

        ComponentEvent ce = new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN);

        for (ComponentListener listener : localHackList) {
            listener.componentHidden(ce);
        }
    }

    /**
     * Set whether the Layer should be added to the BeanContext.
     */
    public void setAddToBeanContext(boolean set) {
        addToBeanContext = set;
    }

    /**
     * Set whether the Layer should be added to the BeanContext.
     */
    public boolean getAddToBeanContext() {
        return addToBeanContext;
    }

    /**
     * Mark the layer as one that should be considered a background layer. What
     * that means is up to the MapBean or application.
     */
    public void setAddAsBackground(boolean set) {
        addAsBackground = set;
    }

    /**
     * Check to see if the layer is marked as one that should be considered a
     * background layer. What that means is up to the MapBean or application.
     * 
     * @return true if layer is a background layer, meaning that it should be
     *         dropped to the bottom of the map and incorporated into a
     *         background layer image buffer if one is available.
     */
    public boolean getAddAsBackground() {
        return addAsBackground;
    }

    /**
     * Mark the layer as removable, or one that can be deleted from the
     * application. What that means is up to the LayerHandler or other
     * application components.
     */
    public void setRemovable(boolean set) {
        removable = set;
    }

    /**
     * Check to see if the layer is marked as one that can be removed from an
     * application.
     * 
     * @return true if layer should be allowed to be deleted.
     */
    public boolean isRemovable() {
        return removable;
    }

    /**
     * Check to see if the removable layer can be removed now.
     * 
     * @return true if layer should be allowed to be deleted.
     */
    public boolean removeConfirmed() {
        return true;
    }

    /**
     * This is the method that your layer can use to find other objects within
     * the MapHandler (BeanContext). This method gets called when the Layer gets
     * added to the MapHandler, or when another object gets added to the
     * MapHandler after the Layer is a member. If the LayerHandler creates the
     * Layer from properties, the LayerHandler will add the Layer to the
     * BeanContext if Layer.addToBeanContext is true. It is false by default.
     * 
     * For Layers, this method doesn't do anything by default. If you need your
     * layer to get ahold of another object, then you can use the Iterator to go
     * through the objects to look for the one you need.
     */
    public void findAndInit(Iterator<?> it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * This method is called by the findAndInit(Iterator) method, once for every
     * object inside the iterator. It's here to allow subclasses a way to
     * receive objects and still let the super classes have a shot at the
     * object. So, you can override this method can call super.findAndInit(obj),
     * or override the findAndInit(Iterator) method and call
     * super.findAndInit(obj). Whatever.
     */
    public void findAndInit(Object obj) {
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is added
     * to the BeanContext of this object.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is removed
     * from the BeanContext of this object. For the Layer, this method doesn't
     * do anything. If your layer does something with the childrenAdded method,
     * or findAndInit, you should take steps in this method to unhook the layer
     * from the object used in those methods.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator<?> it = bcme.iterator();
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    /**
     * This is the method that does the opposite as the findAndInit(Object).
     * Lets you call super classes with objects that need to be removed. At this
     * level, if the layer detects that it is being removed from the MapHandler,
     * it calls dispose on itself.
     */
    public void findAndUndo(Object obj) {
        if (obj == this) {
            this.dispose();
        }
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /**
     * Method for BeanContextChild interface. Gets an iterator from the
     * BeanContext to call findAndInit() over.
     */
    public void setBeanContext(BeanContext in_bc)
            throws PropertyVetoException {

        if (in_bc != null) {
            connectToBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /**
     * Layer method to just connect to the BeanContext, without grabbing the
     * iterator as in setBeanContext(). Good for protected sub-layers where you
     * want to optimize the calling of the findAndInit() method over them.
     */
    public void connectToBeanContext(BeanContext in_bc)
            throws PropertyVetoException {

        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
        }
    }

    /**
     * Layer method to just disconnect from the BeanContext, without grabbing
     * the iterator as in setBeanContext(). Good for protected sub-layers where
     * you want to optimize the calling of the findAndUndo() method over them.
     */
    public void disconnectFromBeanContext()
            throws PropertyVetoException {

        BeanContext bc = getBeanContext();
        if (bc != null) {
            bc.removeBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(null);
        }
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to add a listener to this object's property. This listener wants to have
     * the right to veto a property change.
     */
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to remove a listener to this object's property. The listener has the
     * power to veto property changes.
     */
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * Report a vetoable property update to any registered listeners. If anyone
     * vetos the change, then fire a new event reverting everyone to the old
     * value and then rethrow the PropertyVetoException.
     * <P>
     * 
     * No event is fired if old and new are equal and non-null.
     * <P>
     * 
     * @param name The programmatic name of the property that is about to change
     * 
     * @param oldValue The old value of the property
     * @param newValue - The new value of the property
     * 
     * @throws PropertyVetoException if the recipient wishes the property change
     *         to be rolled back.
     */
    public void fireVetoableChange(String name, Object oldValue, Object newValue)
            throws PropertyVetoException {
        super.fireVetoableChange(name, oldValue, newValue);
        beanContextChildSupport.fireVetoableChange(name, oldValue, newValue);
    }

    public void clearListeners() {
        if (localHackList != null) {
            localHackList.clear();
        }
        // synchronized (IDListeners) {  //2012.06.15 TAW
            IDListeners.clear();
        //}
        //synchronized (lsListeners) {
            lsListeners.clear();
        //}
        BeanContext bc = getBeanContext();
        if (bc != null) {
            bc.removeBeanContextMembershipListener(this);
        }
    }

    protected void finalize() {
        if (Debug.debugging("gc")) {
            Debug.output("Layer |" + getName() + " |: getting GC'd");
        }
    }

    /**
     * Fire a component event to the Layer component listeners, with the palette
     * as the component, letting them know if it's visible or not.
     */
    public void firePaletteEvent(ComponentEvent event) {
        if (localHackList == null) {
            return;
        }

        palette = (Container) event.getSource();
        int eventType = event.getID();
        for (ComponentListener listener : localHackList) {
            if (eventType == ComponentEvent.COMPONENT_HIDDEN) {
                listener.componentHidden(event);
            } else if (eventType == ComponentEvent.COMPONENT_SHOWN) {
                listener.componentShown(event);
            }
        }

        if (eventType == ComponentEvent.COMPONENT_HIDDEN) {
            palette = null;
        }
    }

    /**
     * Return the JDialog, or JInternalFrame, that serves as the palette for the
     * layer. May be null.
     */
    public Container getPalette() {
        return palette;
    }

    /**
     * Called when something about the layer has changed that would require the
     * palette to be reconfigured. Will cause getGUI() to be called again. You
     * should take steps before calling this method to make sure that the
     * getGUI() method is ready to recreate the palette components from scratch
     * if needed.
     */
    protected void resetPalette() {
        java.awt.Container pal = getPalette();
        boolean putUp = false;
        if (pal != null && pal.isVisible()) {
            putUp = true;
            setPaletteVisible(false);
        }

        if (putUp) {
            setPaletteVisible(true);
        }
    }

    /**
     * Make the palette visible or not, destroy if invisible.
     */
    public void setPaletteVisible(boolean visible) {
        if (visible) {
            showPalette();
        } else {
            hidePalette();
        }
    }

    /**
     * Set the WindowSupport object handling the palette.
     */
    public void setWindowSupport(WindowSupport ws) {
        windowSupport = ws;
    }

    /**
     * Get the WindowSupport object handling the palette.
     */
    public WindowSupport getWindowSupport() {
        return windowSupport;
    }

    /**
     * Callback method to override how window support is created.
     * 
     * @return WindowSupport object for layer palette.
     */
    protected WindowSupport createWindowSupport() {
        return new ScrollPaneWindowSupport(getGUI(), getName());
    }

    /**
     * Make the palette visible. Will automatically determine if we're running
     * in an applet environment and will use a JInternalFrame over a JFrame if
     * necessary.
     */
    public void showPalette() {

        WindowSupport ws = getWindowSupport();
        if (ws == null) {
            ws = createWindowSupport();
            paletteListener = new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    firePaletteEvent(e);
                }

                public void componentHidden(ComponentEvent e) {
                    firePaletteEvent(e);
                }
            };
            setWindowSupport(ws);
        } else {
            ws.setTitle(getName());
            ws.setContent(getGUI());
        }

        if (ws != null) {
            MapHandler mh = (MapHandler) getBeanContext();
            Frame frame = null;
            if (mh != null) {
                frame = (Frame) mh.get(java.awt.Frame.class);

                if (frame == null) {
                    MapBean mapBean = (MapBean) mh.get("com.bbn.openmap.MapBean");
                    if (mapBean == null) {
                        Debug.message("layer", "Layer.showPalette: Warning...mapBean = null");
                    } else {
                        try {
                            java.awt.Component parent = mapBean.getParent();
                            while (parent.getParent() != null && !(parent instanceof java.awt.Frame)) {
                                parent = parent.getParent();
                            }

                            if (parent instanceof java.awt.Frame) {
                                frame = (java.awt.Frame) parent;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } // ignore any problems here
                    }
                }
            }

            if (paletteListener != null) {
                ws.addComponentListener(paletteListener);
            }
            ws.displayInWindow(frame);
        }
    }

    /**
     * Hide the layer's palette.
     */
    public void hidePalette() {
        WindowSupport ws = getWindowSupport();
        if (ws != null) {
            ws.killWindow();
        }
    }

    /**
     * The default actionPerformed method for Layer. Make sure you call
     * super.actionPerformed if you care about receiving palette show/hide
     * commands. This method is also set up to receive the DisplayPropertiesCmd,
     * and will bring up the Inspector for the layer.
     */
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command == DisplayPaletteCmd) {
            if (Debug.debugging("layer")) {
                Debug.output(getName() + " displaying palette");
            }
            showPalette();
        } else if (command == HidePaletteCmd) {
            if (Debug.debugging("layer")) {
                Debug.output(getName() + " hiding palette");
            }
            hidePalette();
        } else if (command == DisplayPropertiesCmd) {
            Inspector inspector = new Inspector();
            inspector.inspectPropertyConsumer(this);
        }
    }

    /**
     * Handle Serialization a little bit better, replacing the I18n and
     * BeanContextChildSupport.
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        i18n = Environment.getI18n();
        beanContextChildSupport = new BeanContextChildSupport(this);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public boolean isAutoPalette() {
        return autoPalette;
    }

    public void setAutoPalette(boolean autoPalette) {
        this.autoPalette = autoPalette;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public float getMinScale() {
        return minScale;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public void putAttribute(Object key, Object value) {
        if (attributes == null) {
            attributes = new Hashtable();
        }
        attributes.put(key, value);
    }

    public Object getAttribute(Object key) {
        Object ret = null;
        if (key != null && attributes != null) {
            ret = attributes.get(key);
        }
        return ret;
    }

    public GeoCoordTransformation getCoordTransform() {
        return coordTransform;
    }

    public void setCoordTransform(GeoCoordTransformation coordTranslator) {
        this.coordTransform = coordTranslator;
    }

    /**
     * The dataPathPrefix lets you append a path to a relative path at runtime.
     * This method checks the layer attribute for such a prefix path, and
     * prepends it to the given string.
     */
    protected String prependDataPathPrefix(String fileName) {
        String dataPathPrefix = (String) getAttribute(Layer.DataPathPrefixProperty);
        if (dataPathPrefix != null && dataPathPrefix.length() > 0) {
            fileName = dataPathPrefix + "/" + fileName;
        }
        return fileName;
    }

}