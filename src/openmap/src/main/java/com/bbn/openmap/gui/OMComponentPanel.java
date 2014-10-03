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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OMComponentPanel.java,v $
// $RCSfile: OMComponentPanel.java,v $
// $Revision: 1.10 $
// $Date: 2006/03/06 15:41:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.PropertyConsumer;

/**
 * The OMComponentPanel is a convenient super class intended to provide an easy
 * way to extend JPanel while also implementing the common functions of an
 * OMComponent (PropertyConsumer, BeanContextMembershipListener and
 * BeanContextChild). The PropertyListener methods in the BeanContextChild
 * aren't needed, because the java.awt.Component provides them.
 */
public abstract class OMComponentPanel extends JPanel implements
        PropertyConsumer, BeanContextChild, BeanContextMembershipListener,
        LightMapHandlerChild {

    /**
     * All OMComponentPanels have access to an I18n object, which is provided by
     * the Environment.
     */
    protected I18n i18n = Environment.getI18n();

    protected int orientation = SwingConstants.HORIZONTAL;

    /**
     * A boolean that prevents the BeanContextChild from looking at events from
     * BeanContext other than the one it was originally added to. Set to false
     * by default.
     */
    protected boolean isolated = false;

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

    protected OMComponentPanel() {
        super();
    }

    protected WindowSupport windowSupport;

    public void setWindowSupport(WindowSupport ws) {
        windowSupport = ws;
    }

    public WindowSupport getWindowSupport() {
        return windowSupport;
    }

    // /////////////////////////////////////////////////////////////////////////
    // // PropertyConsumer methods
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Token uniquely identifying this components in the application properties.
     */
    protected String propertyPrefix = null;

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

        // String realPrefix =
        // PropUtils.getScopedPropertyPrefix(prefix);
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

        // String prefix =
        // PropUtils.getScopedPropertyPrefix(propertyPrefix);

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
     * @return the property prefix for the panel
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    // /////////////////////////////////////////////////////////////////////////
    // // MapHandlerChild methods to make the tool work with
    // // the MapHandler to find any SelectionProviders.
    // /////////////////////////////////////////////////////////////////////////

    public void findAndInit(Object obj) {}

    public void findAndUndo(Object obj) {}

    /**
     * This is the method that your object can use to find other objects within
     * the MapHandler (BeanContext). This method gets called when the object
     * gets added to the MapHandler, or when another object gets added to the
     * MapHandler after the object is a member.
     * 
     * @param it Iterator to use to go through a list of objects. Find the ones
     *        you need, and hook yourself up.
     */
    public void findAndInit(Iterator<?> it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is added
     * to the BeanContext of this object.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        if (!isolated || bcme.getBeanContext().equals(getBeanContext())) {
            findAndInit(bcme.iterator());
        }
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

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /**
     * Method for BeanContextChild interface. Adds this object as a
     * BeanContextMembership listener, set the BeanContext in this objects
     * BeanContextSupport, and receives the initial list of objects currently
     * contained in the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {

        if (in_bc != null) {
            if (!isolated || beanContextChildSupport.getBeanContext() == null) {
                in_bc.addBeanContextMembershipListener(this);
                beanContextChildSupport.setBeanContext(in_bc);
                findAndInit(in_bc.iterator());
            }
        }
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to add a listener to this object's property. This listener wants to have
     * the right to veto a property change.
     */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to remove a listener to this object's property. The listener has the
     * power to veto property changes.
     */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName,
                in_vcl);
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

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }
}