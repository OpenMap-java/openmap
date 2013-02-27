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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/AbstractOpenMapMenu.java,v $
// $RCSfile: AbstractOpenMapMenu.java,v $
// $Revision: 1.10 $
// $Date: 2006/02/13 16:58:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Abstract Menu Object that takes care of common bean context-related
 * functionality. Components on this Menu that implement the
 * LightMapHandlerChild interface, that also are created via the constructor, or
 * created when the properties are evaluated, will be given the MapHandler
 * through the findAndInit(Object) method. These components will also receive
 * findAndInit() and findAndUndo() calls when other components are added to the
 * MapHandler (BeanContext). This makes the LightMapHandlerChild menu components
 * invisible to MapHandler components, but able to connect to them.
 * <P>
 * 
 * If you have a component that needs to be added to the menu that should in the
 * MapHandler, you should *NOT* make it a LightMapHandlerChild, but implement
 * the same methods as the MapHandlerChild and add it to the MapHandler before
 * adding it to this menu. If the component is a LightMapHandlerChild, added to
 * this menu, and also added to the MapHandler, it will receive double
 * membership notifications which may be confusing.
 * <P>
 * 
 * AbstractOpenMapMenus can be configure via properties. You can set, with
 * 'menu' as a property prefix:
 * <P>
 * 
 * <pre>
 * 
 * 
 * 
 *   # Title used in the Menu Bar.
 *   menu.prettyName=Menu Title
 *   # The letter to use as Mnemonic
 *   menu.mnemonic=M
 *   # A marker name list of items to use in the menu. 'sep' inserts a
 *   # separator, property classname definition not needed.
 *   menu.items=first second sep third
 *   first.class=Classname of first menu item
 *   # You can add properties for Menu Item, if it is a PropertyConsumer.
 *   #first.property1=prop1
 *   second.class=classname
 *   third.class=classname
 * 
 * 
 * 
 * </pre>
 * 
 * If no properties are set, the name, mnemonic and any items added in the
 * constructor will be in the menu. Any items in the properties will be added to
 * items created and added in the constructor - this really applies to any
 * subclasses to this class.
 */
abstract public class AbstractOpenMapMenu extends JMenu implements BeanContextChild,
        BeanContextMembershipListener, PropertyConsumer, MenuBarMenu, LightMapHandlerChild {

    /**
     * All AbstractOpenMapMenus have access to an I18n object, which is provided
     * by the Environment.
     */
    protected I18n i18n = Environment.getI18n();

    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * Token uniquely identifying this component in the application properties.
     */
    protected String propertyPrefix = null;
    /**
     * A hashtable to keep track of item prefixes and their components, for
     * restructuring properties later. Only created if menu uses properties to
     * create items.
     */
    protected Hashtable items = null;

    public final static String ItemsProperty = "items";
    public final static String SeparatorProperty = "sep";
    public final static String PrettyNameProperty = Layer.PrettyNameProperty;
    public final static String MnemonicProperty = "mnemonic";

    protected String itemsPropertyContents = null;
    protected Hashtable itemsProperties = null;

    public AbstractOpenMapMenu() {
        super();
    }

    public AbstractOpenMapMenu(String title) {
        super(title);
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        beanContextChildSupport.setBeanContext(in_bc);
        if (in_bc == null) {
            return;
        }
        in_bc.addBeanContextMembershipListener(this);

        // let MenuItems find it if they want to look for
        // it there, or if they want to add themselves. Not sure what
        // the ConcurrentModificationException ramifications will be,
        // though.
        findAndInit((Object) in_bc);
        findAndInit(in_bc.iterator());
    }

    /** Method for BeanContextMembership interface. */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /** Method for BeanContextMembership interface. */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        findAndUndo(bcme.iterator());
    }

    /**
     * Subclasses should no longer implement this method. Use the
     * findAndUndo(Object) instead, so subclasses and superclasses can be given
     * the opportunity to use the object, too.
     */
    public void findAndUndo(Iterator it) {
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    /**
     * Method called when an object is removed from the MapHandler. Called to
     * let menu objects disconnect from it.
     */
    public void findAndUndo(Object someObj) {
        Component menuItems[] = getMenuComponents();

        for (int i = 0; i < menuItems.length; i++) {
            Component item = menuItems[i];
            if (item instanceof LightMapHandlerChild) {
                ((LightMapHandlerChild) item).findAndUndo(someObj);
            }
        }
    }

    /**
     * Subclasses should no longer implement this method. Use the
     * findAndInit(Object) instead, so subclasses and superclasses can be given
     * the opportunity to use the object, too.
     */
    public void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * Method called when an object is added from the MapHandler. Called to let
     * menu objects connect to it.
     */
    public void findAndInit(Object someObj) {
        Component menuItems[] = getMenuComponents();

        for (int i = 0; i < menuItems.length; i++) {
            Component item = menuItems[i];
            if (item instanceof LightMapHandlerChild) {
                ((LightMapHandlerChild) item).findAndInit(someObj);
            }
        }
    }

    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }

    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
        beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
        beanContextChildSupport.removePropertyChangeListener(propertyName, in_pcl);
    }

    /**
     * Return the MapHandler, if it's been set. May be null if the Menu hasn't
     * been added to the MapHandler.
     */
    public MapHandler getMapHandler() {
        return (MapHandler) beanContextChildSupport.getBeanContext();
    }

    /**
     * Sets the properties for the OMComponent.
     * 
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(java.util.Properties props) {
        setProperties(getPropertyPrefix(), props);
    }

    /**
     * Sets the properties for the OMComponent.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, java.util.Properties props) {
        setPropertyPrefix(prefix);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String prettyName = props.getProperty(realPrefix + PrettyNameProperty);
        if (prettyName != null) {
            setText(prettyName);
        }

        // We need to update this, as requested by the Javadocs, to use VK_XXX
        // ints.
        // String mnemonicString = props.getProperty(realPrefix +
        // MnemonicProperty);
        // if (mnemonicString != null) {
        // setMnemonic((int) mnemonicString.charAt(0));
        // }

        itemsPropertyContents = props.getProperty(realPrefix + ItemsProperty);
        Vector menuItems = PropUtils.parseSpacedMarkers(itemsPropertyContents);

        if (!menuItems.isEmpty()) {

            int nMenuItems = menuItems.size();

            if (Debug.debugging("menu")) {
                Debug.output("Menu " + getText() + " created with " + nMenuItems + " item"
                        + (nMenuItems == 1 ? "" : "s") + " in properties");
            }

            for (int i = 0; i < nMenuItems; i++) {
                String itemPrefix = (String) menuItems.elementAt(i);
                if (itemPrefix.equals(SeparatorProperty)) {
                    add(new JSeparator());
                    continue;
                }

                String classProperty = itemPrefix + ".class";
                String className = props.getProperty(classProperty);
                if (className == null) {
                    Debug.error("Menu " + getText()
                            + ".setProperties(): Failed to locate property \"" + classProperty
                            + "\"\n  Skipping menu item \"" + itemPrefix + "\"");
                    continue;
                }

                if (itemsProperties == null) {
                    itemsProperties = new Properties();
                }

                itemsProperties.put(classProperty, className);

                Object obj = ComponentFactory.create(className, itemPrefix, props);
                if (obj instanceof Component) {
                    add((Component) obj);
                } else if (obj instanceof JMenuItem) {
                    add((JMenuItem) obj);
                }
            }
        } else {
            if (Debug.debugging("menu")) {
                Debug.output("Menu " + getText() + " created without items in properties");
            }
        }

    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the OMComponent. If the component has a propertyPrefix
     * set, the property keys should have that prefix plus a separating '.'
     * prepended to each property key it uses for configuration.
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

        props.put(prefix + PrettyNameProperty, getText());

        // Mnemonics are handled differently now, needs to be updated to use
        // VK_XXX
        // props.put(prefix + MnemonicProperty, "" + ((char) getMnemonic()));

        if (itemsPropertyContents != null) {
            props.put(prefix + ItemsProperty, itemsPropertyContents);
        }

        if (itemsProperties != null) {
            props.putAll(itemsProperties);
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
     * @return the property prefix for the menu
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
}