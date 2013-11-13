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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/BasicMapPanel.java,v $
// $RCSfile: BasicMapPanel.java,v $
// $Revision: 1.17 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.menu.MenuList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The BasicMapPanel is a MapPanel and OMComponentPanel that is the heart of the
 * OpenMap application framework. It can be used in a application or applet. The
 * Panel has a BorderLayout, and creates a MapBean for its center area. It
 * creates a MapHandler to use to hold all of its OpenMap components, and uses
 * the PropertyHandler given to it in its constructor to create and configure
 * all of the application components. The best way to add components to the
 * MapPanel is to get the MapHandler from it and add components to that. The
 * BasicMapPanel also adds itself to its MapHandler, so when the PropertyHandler
 * adds MapPanelChildren components to the MapHandler, the BasicMapPanel is able
 * to find them via the findAndInit method. By default, the BasicMapPanel looks
 * for MapPanelChildren and asks them for where they would prefer to be located
 * (BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST,
 * BorderLayout.WEST). If you extend this component, though, other components
 * could be found via that same findAndInit method.
 * <p>
 * If a property prefix is set on this MapPanel, that property prefix can be
 * used to designate MapPanelChild objects for this MapPanel. The setName
 * variable should be set to true, and the children's parent name should match
 * whatever property prefix is given to the panel.
 * 
 */
public class BasicMapPanel
        extends OMComponentPanel
        implements MapPanel {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.MapPanel");

    public final static String SET_NAME_PROPERTY = "setName";

    protected MapHandler mapHandler;
    protected MapBean mapBean;
    protected PropertyHandler propertyHandler;
    protected MenuList menuList;
    protected boolean setName = false;

    /**
     * Creates an empty MapPanel that creates its own empty PropertyHandler. The
     * MapPanel will contain a MapBean, a MapHandler, and a PropertyHandler with
     * no properties. The constructor to use to create a blank map framework to
     * add components to.
     */
    public BasicMapPanel() {
        this(new PropertyHandler(new Properties()), false);
    }

    /**
     * Create a MapPanel with the option of delaying the search for properties
     * until the <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the artful
     *        programmer will call <code>create()</code>
     */
    public BasicMapPanel(boolean delayCreation) {
        this(null, delayCreation);
    }

    /**
     * Create a MapPanel that configures itself with the properties contained in
     * the PropertyHandler provided. If the PropertyHandler is null, a new one
     * will be created.
     */
    public BasicMapPanel(PropertyHandler propertyHandler) {
        this(propertyHandler, false);
    }

    /**
     * Create a MapPanel that configures itself with properties contained in the
     * PropertyHandler provided, and with the option of delaying the search for
     * properties until the <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the artful
     *        programmer will call <code>create()</code>
     */
    public BasicMapPanel(PropertyHandler propertyHandler, boolean delayCreation) {
        MapHandler mh = getMapHandler();
        mh.add(this);

        setPropertyHandler(propertyHandler);
        if (!delayCreation) {
            create();
        }
    }

    /**
     * Sets the properties in the PropertyHandler managed by this BasicMapPanel.
     * This method is intended to be called when the PropertyHandler is set on
     * the panel, in order for configuration parameters to be set on this panel
     * before configuration.
     * 
     * @param prefix property prefix for scoping properties for this panel
     * @param props the properties to search for properties in.
     */
    public void setProperties(String prefix, Properties props) {
        String scopedPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        setSetName(PropUtils.booleanFromProperties(props, scopedPrefix + SET_NAME_PROPERTY, isSetName()));
        if (isSetName()) {
            super.setProperties(prefix, props);
        }
    }

    /**
     * The method that triggers setLayout() and createComponents() to be called.
     * If you've told the BasicMapPanel to delay creation, you should call this
     * method to trigger the PropertyHandler to create components based on the
     * contents of its properties.
     */
    public void create() {
        setLayout(createLayoutManager());
        createComponents();
    }

    /**
     * The constructor calls this method that sets the LayoutManager for this
     * MapPanel. It returns a BorderLayout by default, but this method can be
     * overridden to change how the MapPanel places components. If you change
     * what this method returns, you should also change how components are added
     * in the findAndInit() method.
     */
    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    /**
     * Position the map bean in this panel according to the layout manger.
     * Defaults to BorderLayout.CENTER.
     */
    protected void addMapBeanToPanel(MapBean map) {
        add(map, BorderLayout.CENTER);
    }

    /**
     * The constructor calls this method that creates the MapHandler and
     * MapBean, and then tells the PropertyHandler to create the components
     * described in its properties. This method calls getMapHandler() and
     * getMapBean(). If the PropertyHandler is not null, it will be called to
     * created components based on its properties, and those components will be
     * added to the MapHandler in this MapPanel.
     */
    protected void createComponents() {
        // Make this call first to load the properties into
        // Environment, before the MapBean gets created.
        PropertyHandler ph = getPropertyHandler();
        // Make sure the MapBean is created and added to the
        // MapHandler.
        MapBean mb = getMapBean();
        MapHandler mh = getMapHandler();
        ph.createComponents(getMapHandler());

        // At this point, check the MapHandler to see if a
        // ProjectionFactory has been added. If it hasn't, create one
        // with the default ProjectionLoaders. We might want to
        // remove this at some point, but not having it here will
        // catch some people by surprise when 4.6.1 comes out.
        Object obj = mh.get(com.bbn.openmap.proj.ProjectionFactory.class);
        if (obj == null) {
            Debug.message("basic",
                          "BasicMapPanel adding ProjectionFactory and projections to MapHandler since there are none to be found.");
            mh.add(ProjectionFactory.loadDefaultProjections());
        }

        // Environment will only get loaded after the property file is
        // read.
        mb.setProjection(mb.getProjectionFactory().getDefaultProjectionFromEnvironment(Environment.getInstance()));
        mb.setBckgrnd(Environment.getCustomBackgroundColor());
    }

    /**
     * MapPanel method. Get the MapBean used for the MapPanel. If the MapBean is
     * null, calls createMapBean() which will create a BufferedLayerMapBean and
     * add it to the MapHandler via a setMapBean call. If you want something
     * different, override this method.
     */
    public MapBean getMapBean() {
        if (mapBean == null) {
            setMapBean(BasicMapPanel.createMapBean());
        }
        return mapBean;
    }

    /**
     * Set the map bean used in this map panel, replace the map bean in the
     * MapHandler if there isn't already one, or if the policy allows
     * replacement. The MapHandler will be created if it doesn't exist via a
     * getMapHandler() method call.
     * 
     * @throws MultipleSoloMapComponentException if there is already a map bean
     *         in the map handler and the policy is to reject duplicates (since
     *         the MapBean is a SoloMapComponent).
     */
    public void setMapBean(MapBean bean) {
        if (bean == null && mapBean != null) {
            // remove the current MapBean from the application...
            getMapHandler().remove(mapBean);
        }

        mapBean = bean;

        if (mapBean != null) {
            getMapHandler().add(mapBean);
            addMapBeanToPanel(mapBean);
        }
    }

    /**
     * Get the PropertyHandler containing properties used to configure the
     * panel, creating it if it doesn't exist.
     */
    public PropertyHandler getPropertyHandler() {
        if (propertyHandler == null) {
            setPropertyHandler(new PropertyHandler());
        }
        return propertyHandler;
    }

    /**
     * Set the PropertyHandler containing the properties used to configure this
     * panel. Adds the PropertyHandler to the MapHandler. If the MapHandler
     * isn't set at this point, it will be created via a getMapHandler() call.
     */
    public void setPropertyHandler(PropertyHandler handler) {
        propertyHandler = handler;
        if (handler != null) {
            getMapHandler().add(handler);

            setProperties(handler.getPropertyPrefix(), handler.getProperties());
        }
    }

    /**
     * MapPanel method. Get the MapHandler used for the MapPanel. Creates a
     * standard MapHandler if it hasn't been created yet.
     */
    public MapHandler getMapHandler() {
        if (mapHandler == null) {
            mapHandler = new MapHandler();
        }
        return mapHandler;
    }

    /**
     * MapPanel method. Get a JMenuBar containing menus created from properties.
     */
    public JMenuBar getMapMenuBar() {
        if (menuList != null) {
            return menuList.getMenuBar();
        } else {
            return null;
        }
    }

    /**
     * MapPanel method. Get a JMenu containing sub-menus created from
     * properties.
     */
    public JMenu getMapMenu() {
        if (menuList != null) {
            return menuList.getMenu();
        } else {
            return null;
        }
    }

    // Map Component Methods:
    // //////////////////////

    /**
     * Adds a component to the map bean context. This makes the
     * <code>mapComponent</code> available to the map layers and other
     * components.
     * 
     * @param mapComponent a component to be added to the map bean context
     * @throws MultipleSoloMapComponentException if mapComponent is a
     *         SoloMapComponent and another instance already exists and the
     *         policy is a reject policy.
     */
    public void addMapComponent(Object mapComponent) {
        if (mapComponent != null) {
            getMapHandler().add(mapComponent);
        }
    }

    /**
     * Remove a component from the map bean context.
     * 
     * @param mapComponent a component to be removed to the map bean context
     * @return true if the mapComponent was removed.
     */
    public boolean removeMapComponent(Object mapComponent) {
        if (mapComponent != null) {
            return getMapHandler().remove(mapComponent);
        }
        return true;
    }

    /**
     * Given a Class, find the object in the MapHandler. If the class is not a
     * SoloMapComponent and there are more than one of them in the MapHandler,
     * you will get the first one found.
     */
    public Object getMapComponentByType(Class<?> c) {
        return getMapHandler().get(c);
    }

    /**
     * Get all of the mapComponents that are of the given class type.
     */
    public Collection<?> getMapComponentsByType(Class<?> c) {
        return getMapHandler().getAll(c);
    }

    /**
     * Find the object with the given prefix by looking it up in the prefix
     * librarian in the MapHandler.
     */
    public Object getMapComponent(String prefix) {
        return getPropertyHandler().get(prefix);
    }

    /**
     * The BasicMapPanel looks for MapPanelChild components, finds out from them
     * where they prefer to be placed, and adds them.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapPanelChild && someObj instanceof Component) {

            String parentName = ((MapPanelChild) someObj).getParentName();
            boolean hasNamedParent = parentName != null && parentName.trim().length() != 0; 
           
            String myName = getPropertyPrefix();
            boolean hasName = myName != null && myName.trim().length() != 0;
            
            @SuppressWarnings("null")
            boolean makeMyChild =
                    (hasName && hasNamedParent && myName.equalsIgnoreCase(parentName))
                            || (!hasName && !hasNamedParent);

            if (makeMyChild) {

                if (Debug.debugging("basic")) {
                    Debug.output("MapPanel: adding " + someObj.getClass().getName());
                }
                MapPanelChild mpc = (MapPanelChild) someObj;
                addMapPanelChild(mpc);
                invalidate();
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("MapPanel with name: " + myName + " not adding child (" + someObj.getClass().getName()
                            + ") looking for: " + parentName);
                }
            }
        }

        if (someObj instanceof MenuList) {
            menuList = (MenuList) someObj;
        }
    }

    /**
     * Add a child to the MapPanel.
     */
    protected void addMapPanelChild(MapPanelChild mpc) {
        add((Component) mpc, mpc.getPreferredLocation());
    }

    /**
     * The MapPanel looks for MapPanelChild components and removes them from
     * iteself.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapPanelChild && someObj instanceof Component) {
            if (Debug.debugging("basic")) {
                Debug.output("MapPanel: removing " + someObj.getClass().getName());
            }
            remove((Component) someObj);
            invalidate();
        }

        if (someObj instanceof MenuList && menuList == someObj) {
            menuList = null;
        }

        if (this.equals(someObj)) {
            dispose();
        }
    }

    /**
     * Sets the MapBean variable to null and removes all children.
     */
    public void dispose() {
        setMapBean(null);
        setLayout(null);
        removeAll();
    }

    // MapBean Methods:
    // ////////////////

    /**
     * A static method that creates a MapBean with it's projection set to the
     * values set in the Environment. Also creates a BevelBorder.LOWERED border
     * for the MapBean.
     */
    public static MapBean createMapBean() {
        int envWidth = Environment.getInteger(Environment.Width, MapBean.DEFAULT_WIDTH);
        int envHeight = Environment.getInteger(Environment.Height, MapBean.DEFAULT_HEIGHT);

        if (envWidth <= 0 || envHeight <= 0) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            if (envWidth <= 0) {
                envWidth = (int) d.getWidth();
            }
            if (envHeight <= 0) {
                envHeight = (int) d.getHeight();
            }
        }

        Projection proj = new ProjectionFactory().getDefaultProjectionFromEnvironment(Environment.getInstance());

        if (Debug.debugging("mappanel")) {
            Debug.output("MapPanel: creating MapBean with initial projection " + proj);
        }

        return createMapBean(proj, new BevelBorder(BevelBorder.LOWERED));
    }

    /**
     * A static method that creates a MapBean and sets its projection and border
     * to the values given.
     */
    public static MapBean createMapBean(Projection proj, Border border) {
        MapBean mapBeano = new BufferedLayerMapBean();
        mapBeano.setBorder(border);
        mapBeano.setProjection(proj);
        mapBeano.setPreferredSize(new Dimension(proj.getWidth(), proj.getHeight()));
        return mapBeano;
    }

    // Property Functions:
    // ///////////////////

    /**
     * Get the current properties.
     */
    public Properties getProperties() {
        return getPropertyHandler().getProperties();
    }

    /**
     * Remove an existing property if it exists.
     * 
     * @return true if a property was actually removed.
     */
    public boolean removeProperty(String property) {
        return getPropertyHandler().removeProperty(property);
    }

    /**
     * Add (or overwrite) a property to the current properties
     */
    public void addProperty(String property, String value) {
        getPropertyHandler().addProperty(property, value);
    }

    /**
     * Add in the properties from the given URL. Any existing properties will be
     * overwritten except for openmap.components, openmap.layers and
     * openmap.startUpLayers which will be appended.
     */
    public void addProperties(URL urlToProperties) {
        getPropertyHandler().addProperties(urlToProperties);
    }

    /**
     * Add in the properties from the given source, which can be a resource,
     * file or URL. Any existing properties will be overwritten except for
     * openmap.components, openmap.layers and openmap.startUpLayers which will
     * be appended.
     * 
     * @throws MalformedURLException if propFile doesn't resolve properly.
     */
    public void addProperties(String propFile)
            throws java.net.MalformedURLException {
        getPropertyHandler().addProperties(propFile);
    }

    /**
     * remove a marker from a space delimited set of properties.
     */
    public void removeMarker(String property, String marker) {
        getPropertyHandler().removeMarker(property, marker);
    }

    /**
     * Add in the properties from the given Properties object. Any existing
     * properties will be overwritten except for openmap.components,
     * openmap.layers and openmap.startUpLayers which will be appended.
     */
    public void addProperties(Properties p) {
        getPropertyHandler().addProperties(p);
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, Properties src) {
        getPropertyHandler().appendProperty(property, src);
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, String value) {
        getPropertyHandler().appendProperty(property, value);
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, Properties src) {
        getPropertyHandler().prependProperty(property, src);
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, String value) {
        getPropertyHandler().prependProperty(property, value);
    }

    /**
     * @return the setName setting, whether the property prefix will be set on
     *         the MapPanel when setProperties is called.
     */
    public boolean isSetName() {
        return setName;
    }

    /**
     * @param setName whether the property prefix provided in setProperties will
     *        be set on the MapPanel. If it is, then when MapPanelChild objects
     *        are found in the MapHandler, they will only be added if the names
     *        match.
     */
    public void setSetName(boolean setName) {
        this.setName = setName;
    }
}