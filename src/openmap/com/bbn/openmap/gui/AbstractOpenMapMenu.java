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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/AbstractOpenMapMenu.java,v $
// $RCSfile: AbstractOpenMapMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.awt.*;
import java.util.Iterator;
import com.bbn.openmap.*;


/**
 * Abstract Menu Object that takes care of common bean context-related
 * functionality.  When this menu object is added to bean context, it
 * also adds its Components (generally Menu and MenuItems) to the
 * context and calls findAndInit() which inheriting classes should
 * implement if they have interest in certain objects that might be
 * available from context.
 */
abstract public class AbstractOpenMapMenu extends JMenu
    implements BeanContextChild, BeanContextMembershipListener { 

    public I18n I18N = new I18n("GUI");

    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

    public AbstractOpenMapMenu() {
	super();        
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
	findAndInit(in_bc.iterator());
	// Iterate through its menu items and see if any of them is an
	// instance of BeanContextChild. if yes, then add it to
	// beancontext so that they initialize and hook themselves
	// with other objects
	Component menuItems[] = getMenuComponents();
	// Create fake event to get the iterator of objects currently
	// in the BeanContext to all components interested.
	for (int i=0; i< menuItems.length;i++) {
	    if (menuItems[i] instanceof BeanContextChild) {
  		((BeanContextChild)menuItems[i]).setBeanContext(in_bc);
	    }	
	}
    }
 
    /** Method for BeanContextMembership interface. */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
	findAndInit(bcme.iterator());
  	// Iterate through its menu items and see if any of them is an
  	// instance of BeanContextChild. if yes, then add it to
  	// beancontext so that they initialize and
  	// hook themselves with other objects

//  	Component menuItems[] = getMenuComponents();
//  	for (int i=0; i< menuItems.length;i++) {
//  	    if (menuItems[i] instanceof BeanContextMembershipListener) {
//  		((BeanContextMembershipListener)menuItems[i]).childrenAdded(bcme);
//  	    }	
//  	}

    }
  
    /** Method for BeanContextMembership interface. */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	findAndUnInit(bcme.iterator());
  	// Iterate through its menu items and see if any of them is an
  	// instance of BeanContextChild. if yes, then add it to
  	// beancontext so that they initialize and hook themselves
  	// with other objects

//  	Component menuItems[] = getMenuComponents();
//  	for (int i=0; i< menuItems.length;i++) {
//  	    if (menuItems[i] instanceof BeanContextMembershipListener) {
//  		((BeanContextMembershipListener)menuItems[i]).childrenRemoved(bcme);
//  	    }	
//  	}
    }
  
    /**
     * Clases should implement this method
     */
    abstract public void findAndUnInit(Iterator it);
  
    /**
     * Clases should implement this method
     */
    abstract public void findAndInit(Iterator it);
  
    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName,
					  VetoableChangeListener in_vcl) {
	beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }
  
    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, 
					     VetoableChangeListener in_vcl){
	beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }
    /**
     */
    public MapHandler getMapHandler() {
	return (MapHandler)beanContextChildSupport.getBeanContext();
    }
}
