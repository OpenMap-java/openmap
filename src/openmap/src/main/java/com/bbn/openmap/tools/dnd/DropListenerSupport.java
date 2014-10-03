//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/dnd/DropListenerSupport.java,v $
//$RCSfile: DropListenerSupport.java,v $
//$Revision: 1.3 $
//$Date: 2005/08/09 20:45:09 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.dnd;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.SoloMapComponent;
import com.bbn.openmap.util.Debug;

/**
 * The DropListenerSupport manages the DefaultDnDCatchers that handle
 * Drag and Drop events on the map. There should only be one
 * DropListenerSupport within a MapHandler.
 * 
 * DropListenerSupport keeps a list of all DefaultDnDCatcher objects
 * from a MapHandler. It adds itself to the MapBean as MouseListener
 * and MouseMotionListener. On MousePressed, MouseDragged, and
 * MouseReleased events it loops through the DnDCatchers and invokes a
 * consume() method in each of them.
 * 
 * @see DefaultDnDCatcher
 */
public class DropListenerSupport implements PropertyChangeListener,
        java.io.Serializable, BeanContextChild, BeanContextMembershipListener,
        SoloMapComponent, MouseListener, MouseMotionListener {

    /**
     * Holds a list of DefaultDndCatchers
     */
    protected transient Vector dndCatchers = new Vector(2);

    /**
     * The MapBean.
     */
    protected transient MapBean map;

    /**
     * PropertyChangeSupport for handling listeners.
     */
    protected PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * Construct a DropListenerSupport without an associated MapBean.
     * You will need to set the MapBean via <code>setMap()</code>.
     * 
     * @see #setMap
     */
    public DropListenerSupport() {
        this(null);
    }

    /**
     * Construct a DropListenerSupport with an associated MapBean.
     * 
     * @param map MapBean
     */
    public DropListenerSupport(MapBean map) {
        setMap(map);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.addPropertyChangeListener(listener);
    }

    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener in_pcl) {
        pcSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * BeanContextMembershipListener method. Called when new objects
     * are added to the parent BeanContext.
     * 
     * @param bcme event that contains an iterator that can be used to
     *        go through the new objects.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembershipListener method. Called when objects have
     * been removed from the parent BeanContext. The
     * DropListenerSupport looks for the MapBean it is managing
     * MouseEvents for, and any DefaultDndCatchers that may be
     * removed.
     * 
     * @param bcme event that contains an iterator that can be used to
     *        go through the removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator it = bcme.iterator();
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    /**
     * Called when an object should be evaluated by the
     * DropListenerSupport to see if it is needed.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            Debug.message("droplistenersupport",
                    "DropListenerSupport found a map.");
            setMap((MapBean) someObj);
        }

        if (someObj instanceof DefaultDnDCatcher) {
            getBeanContext().addBeanContextMembershipListener((DefaultDnDCatcher) someObj);
            Debug.message("DropListener",
                    "DropListener found a DefaultDnDCatcher.");
            dndCatchers.addElement(someObj);
        }
    }

    /**
     * Eventually gets called when the DropListenerSupport is added to
     * the BeanContext, and when other objects are added to the
     * BeanContext anytime after that. The DropListenerSupport looks
     * for a MapBean to manage MouseEvents for, and DefaultDndCatchers
     * to use to manage those events. If a MapBean is added to the
     * BeanContext while another already is in use, the second MapBean
     * will take the place of the first.
     * 
     * @param it iterator to use to go through the new objects in the
     *        BeanContext.
     */
    public void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * Called by childrenRemoved.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean) {
            if (getMap() == (MapBean) someObj) {
                Debug.message("droplistenersupport",
                        "DropListenerSupport: removing the map.");
                setMap(null);
            }
        }
    }

    public void firePropertyChange(String property, Object oldObj, Object newObj) {

        pcSupport.firePropertyChange(property, oldObj, newObj);
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
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /**
     * Get the associated MapBean.
     * 
     * @return MapBean
     */
    public MapBean getMap() {
        return map;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged. Mouse drag events will continue to be delivered to the
     * component where the first originated until the mouse button is
     * released (regardless of whether the mouse position is within
     * the bounds of the component).
     */
    public void mouseDragged(java.awt.event.MouseEvent e) {
        for (int i = 0; i < dndCatchers.size(); i++) {
            if (((DefaultDnDCatcher) dndCatchers.get(i)).consume(e))
                break;
        }
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(java.awt.event.MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(java.awt.event.MouseEvent e) {}

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     */
    public void mouseMoved(java.awt.event.MouseEvent e) {}

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(java.awt.event.MouseEvent e) {
        for (int i = 0; i < dndCatchers.size(); i++) {
            if (((DefaultDnDCatcher) dndCatchers.get(i)).consume(e))
                break;
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(java.awt.event.MouseEvent e) {
        for (int i = 0; i < dndCatchers.size(); i++) {
            if (((DefaultDnDCatcher) dndCatchers.get(i)).consume(e))
                break;
        }
    }

    /**
     * PropertyChangeListenter Interface method.
     * 
     * @param evt PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {}

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.removePropertyChangeListener(listener);
    }

    /** Method for BeanContextChild interface. */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener in_pcl) {
        pcSupport.removePropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName,
                in_vcl);
    }

    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /**
     * Set the associated MapBean.
     * 
     * @param mapbean MapBean
     */
    public void setMap(MapBean mapbean) {
        if (map != null) {
            map.removePropertyChangeListener(this);
            map.removeMouseListener(this);
            map.removeMouseMotionListener(this);
        }

        map = mapbean;
        if (map != null) {
            map.addPropertyChangeListener(this);
            map.addMouseListener(this);
            map.addMouseMotionListener(this);
        }
    }
}