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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapHandlerChild.java,v $
// $RCSfile: MapHandlerChild.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;

/**
 * MapHandlerChild shows you all the methods you need to add to an object for it
 * be a good member of the MapHandler. The MapHandler is actually a BeanContext
 * object, which is simply a container for a bunch of objects that may be
 * interested in other objects. If you are using this object as a model to
 * figure out what methods to add to other objects, there are a couple of things
 * to notice. First, java.awt.Components already have a PropertyChangeSupport
 * object in it, so you don't need to implement the methods that deal with
 * property changes. For javax.swing.JComponents, they have
 * VetoablePropertySupport build in, but that object doesn't handle certain
 * methods needed by the BeanContextChild, most notably the
 * (add/remove)VetoableChangeListener() methods with a specific property as an
 * argument.
 * <P>
 * 
 * When you design a MapHandlerChild, you should make it comfortable running
 * without references to objects it depends on. It should wait patiently for the
 * other objects to be added to the MapHandler, and then do the work itself to
 * hook up. It should also listen for those objects to be removed from the
 * MapHandler, disengage gracefully, and wait patiently until it finds something
 * else to hook up to.
 * <P>
 * 
 * An object does not have to be a MapHandlerChild to be added to the
 * MapHandler, but it does need to be one to be able to use it. If you override
 * and use the findAndInit(Iterator) method to look for objects, you'll find it
 * is called on two different conditions. It's called when this MapHandlerChild
 * is added to the MapHandler, and it then receives a list of all the objects
 * currently contained in the MapHandler. It is also called when other objects
 * are added to the MapHandler. The list then contains objects that have just
 * been added. The findAndInit(Object) method has been added to allow subclassed
 * objects to call super.findAndInit(Object) to let the super classes handles
 * the objects they care about. You don't call the findAndInit(Object) method.
 * You override it and implement the method so that you can look for the objects
 * you need.
 * <P>
 * 
 * When objects are removed from the BeanContext, the childrenRemoved() method
 * is called with a list of objects being removed. Likewise, the
 * findAndUndo(Object) method has been added for the benefit of subclasses.
 * <P>
 * 
 * MapHandlerChild objects expect to be added to only one BeanContext. The
 * BeanContextChildSupport object detects when it has a different BeanContext
 * added to it, and it will fire property change notifications to get itself
 * removed from the first BeanContext.
 */
public class MapHandlerChild
        implements BeanContextChild, BeanContextMembershipListener, LightMapHandlerChild {

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

    /**
     * This is the method that your object can use to find other objects within
     * the MapHandler (BeanContext). This method gets called when the object
     * gets added to the MapHandler, or when another object gets added to the
     * MapHandler after the object is a member. It's probably better to not
     * override this method, just override the findAndUndo(Object) method
     * instead.
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
     * The findAndInit method has been made non-abstract, because it now calls
     * this method for every object that is in the iterator it receives. This
     * lets subclasses call a method on super classes so they can handle their
     * needs as well.
     */
    public void findAndInit(Object obj) {
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

    /**
     * The childrenRemoved has been changed to go through its iterator to call
     * this method with every object. This lets subclasses call this method on
     * their super class, so it can handle what it needs to with objects it may
     * be interested in.
     */
    public void findAndUndo(Object obj) {
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
    public void setBeanContext(BeanContext in_bc)
            throws PropertyVetoException {

        if (in_bc != null) {
            if (!isolated || beanContextChildSupport.getBeanContext() == null) {
                in_bc.addBeanContextMembershipListener(this);
                beanContextChildSupport.setBeanContext(in_bc);
                findAndInit(in_bc.iterator());
            }
        } else {
            beanContextChildSupport.setBeanContext(in_bc);
        }
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to add a listener to this object's property. You don't need this function
     * for objects that extend java.awt.Component.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
        beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /**
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to remove a listener to this object's property. You don't need this
     * function for objects that extend java.awt.Component.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
        beanContextChildSupport.removePropertyChangeListener(propertyName, in_pcl);
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
     * Method for BeanContextChild interface. Uses the BeanContextChildSupport
     * to fire a property change. You don't need this function for objects that
     * extend java.awt.Component.
     */
    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        beanContextChildSupport.firePropertyChange(name, oldValue, newValue);
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
        beanContextChildSupport.fireVetoableChange(name, oldValue, newValue);
    }

    public boolean isIsolated() {
        return isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }
}