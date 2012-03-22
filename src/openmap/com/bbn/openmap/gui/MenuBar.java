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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MenuBar.java,v $
// $RCSfile: MenuBar.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:49:51 $
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
import java.io.Serializable;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.bbn.openmap.util.Debug;

/**
 * This object looks for objects implementing MenuI interface and adds
 * them to itself. if an object implements HelpMenuI, it is then added
 * as the last element.
 */
public class MenuBar extends JMenuBar implements Serializable,
        BeanContextMembershipListener, BeanContextChild {
    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * Default Constructor is required to create instances at runtime
     */
    public MenuBar() {}

    /**
     * Called when the MenuBar is a part of a BeanContext, and it is
     * added to the BeanContext, or while other objects are added to
     * the BeanContext after that.
     * 
     * @param it Iterator
     */
    protected void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    public void findAndInit(Object someObj) {
        int menuCount = getMenuCount();

        // Check for HelpMenu first.
        if (someObj instanceof HelpMenu) {
            //setHelpMenu((JMenu)someObj);
            // We will use it when its implementation is available.
            // get the last menu and see if it is helpmenu
            if (menuCount > 0 && (getMenu(menuCount - 1) instanceof HelpMenu)) {
                System.err.println("HelpMenu already exists in MenuBar..overriding it");
            }
            // make the help menu as the last menu
            if (Debug.debugging("menubar")) {
                Debug.output("MenuBar: Adding help menu at " + getMenuCount());
            }
            add((JMenu) someObj, getMenuCount());

        } else if (someObj instanceof MenuBarMenu) {

            if (Debug.debugging("menubar")) {
                Debug.output("MenuBar: Adding Menu " + ((JMenu) someObj)
                        + "to index " + menuCount);
            }

            JMenu lastMenu = getLastMenu();
            if (lastMenu instanceof HelpMenu) {
                remove(lastMenu);
                add((JMenu) someObj, menuCount - 1);
                add(lastMenu, menuCount);
                if (Debug.debugging("menubar")) {
                    Debug.output("MenuBar: last menu is HelpMenu\n moving helpMenu to "
                            + menuCount);
                }
            } else {
                add((JMenu) someObj, menuCount);
            }
        }
    }

    /**
     * Get the last menu item on the menu bar. If there are no menu
     * items, it returns null.
     */
    public JMenu getLastMenu() {
        int menuCount = getMenuCount();
        if (menuCount > 0) {
            return getMenu(menuCount - 1);
        } else {
            return null;
        }
    }

    /** Method for BeanContextChild interface */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        beanContextChildSupport.setBeanContext(in_bc);
        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            findAndInit(in_bc.iterator());
        }
    }

    /** Method for BeanContextChild interface */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener in_pcl) {
        beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener in_pcl) {
        beanContextChildSupport.removePropertyChangeListener(propertyName,
                in_pcl);
    }

    /** Method for BeanContextChild interface */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /** Method for BeanContextChild interface */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName,
                in_vcl);
    }

    /** Method for BeanContextChild interface */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /** Method for BeanContextMembership interface */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        Iterator it = bcme.iterator();
        findAndInit(it);
    }

    /** Method for BeanContextMembership interface */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator it = bcme.iterator();
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    public void findAndUndo(Object someObj) {
        // Check for HelpMenu first.
        if (someObj instanceof HelpMenu) {
            setHelpMenu(null);
        } else if (someObj instanceof MenuBarMenu) {
            remove((Component) someObj);
        }
    }
}