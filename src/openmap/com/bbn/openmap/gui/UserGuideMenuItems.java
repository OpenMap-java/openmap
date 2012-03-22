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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/UserGuideMenuItems.java,v $
// $RCSfile: UserGuideMenuItems.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;

/**
 * This class provides MenuItems that get added under HelpMenu. This
 * object needs an instance of InformationDelegator, which can be set
 * programmatically. If this objects environment is capable of
 * providing BeanContextServices, it will look for
 * InformationDelegator as a service.
 */
public class UserGuideMenuItems implements HelpMenuItems, ActionListener,
        BeanContextMembershipListener, BeanContextChild {
    private InformationDelegator informationDelegator;
    private Vector menuItems = new Vector();

    public UserGuideMenuItems() {
        JMenuItem mi = new JMenuItem("OpenMap");
        mi.addActionListener(this);
        getMenuItems().add(mi);
    }

    /**
     * Initializes the object with given InformationDelegator.
     * 
     * @param in_informationDelegator
     */
    public UserGuideMenuItems(InformationDelegator in_informationDelegator) {
        setInformationDelegator(in_informationDelegator);
    }

    /**
     * @param in_informationDelegator
     */
    public void setInformationDelegator(
                                        InformationDelegator in_informationDelegator) {
        informationDelegator = in_informationDelegator;
    }

    /**
     * Return current value of InformationDelegator.
     */
    protected InformationDelegator getInformationDelegator() {
        return informationDelegator;
    }

    /**
     * Returns a vector of MenuItems that are part of this object.
     */
    protected Vector getMenuItems() {
        return menuItems;
    }

    /**
     * Returns an Iterator to the MenuItems it holds.
     */
    public Iterator iterator() {
        return getMenuItems().iterator();
    }

    /**
     * Called when our menu item is clicked by user.
     */
    public void actionPerformed(ActionEvent ae) {
        // check if we have the object that generated this event. if
        // yes do it else complain
        if (getMenuItems().contains(ae.getSource())) {
            if (informationDelegator != null) {
                informationDelegator.displayURL(Environment.get(Environment.HelpURL,
                        "http://javamap.bbn.com/projects/openmap/openmap_maindes.html"));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //// MapHandlerChild methods to make the tool work with
    //// the MapHandler to find any SelectionProviders.
    ///////////////////////////////////////////////////////////////////////////

    public void findAndInit(Object obj) {
        if (obj instanceof InformationDelegator) {
            setInformationDelegator((InformationDelegator) obj);
        }
    }

    public void findAndUndo(Object obj) {
        if (obj instanceof InformationDelegator) {
            setInformationDelegator(null);
        }
    }

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * This is the method that your object can use to find other
     * objects within the MapHandler (BeanContext). This method gets
     * called when the object gets added to the MapHandler, or when
     * another object gets added to the MapHandler after the object is
     * a member.
     * 
     * @param it Iterator to use to go through a list of objects. Find
     *        the ones you need, and hook yourself up.
     */
    public void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * BeanContextMembershipListener method. Called when a new object
     * is added to the BeanContext of this object.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembershipListener method. Called when a new object
     * is removed from the BeanContext of this object. For the Layer,
     * this method doesn't do anything. If your layer does something
     * with the childrenAdded method, or findAndInit, you should take
     * steps in this method to unhook the layer from the object used
     * in those methods.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator it = bcme.iterator();
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
     * BeanContextMembership listener, set the BeanContext in this
     * objects BeanContextSupport, and receives the initial list of
     * objects currently contained in the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {

        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /**
     * Method for BeanContextChild interface. Uses the
     * BeanContextChildSupport to add a listener to this object's
     * property. This listener wants to have the right to veto a
     * property change.
     */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * Method for BeanContextChild interface. Uses the
     * BeanContextChildSupport to remove a listener to this object's
     * property. The listener has the power to veto property changes.
     */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName,
                in_vcl);
    }

    /**
     * Report a vetoable property update to any registered listeners.
     * If anyone vetos the change, then fire a new event reverting
     * everyone to the old value and then rethrow the
     * PropertyVetoException.
     * <P>
     * 
     * No event is fired if old and new are equal and non-null.
     * <P>
     * 
     * @param name The programmatic name of the property that is about
     *        to change
     * 
     * @param oldValue The old value of the property
     * @param newValue - The new value of the property
     * 
     * @throws PropertyVetoException if the recipient wishes the
     *         property change to be rolled back.
     */
    public void fireVetoableChange(String name, Object oldValue, Object newValue)
            throws PropertyVetoException {
        beanContextChildSupport.fireVetoableChange(name, oldValue, newValue);
    }

    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener in_pcl) {
        beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener in_pcl) {
        beanContextChildSupport.removePropertyChangeListener(propertyName,
                in_pcl);
    }
}