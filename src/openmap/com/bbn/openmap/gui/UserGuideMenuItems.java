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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/UserGuideMenuItems.java,v $
// $RCSfile: UserGuideMenuItems.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import com.bbn.openmap.*;

/**
 * This class provides MenuItems that get added under HelpMenu.  This
 * object needs an instance of InformationDelegator, which can be set
 * programatically.  If this objects environment is capable of
 * providing BeanContextServices, it will look for
 * InformationDelegator as a service.  
 */
public class UserGuideMenuItems 
  implements HelpMenuItems, ActionListener, BeanContextServicesListener, BeanContextChild
{  
    private InformationDelegator informationDelegator;
    private Vector menuItems = new Vector();
  
    private BeanContextServicesSupport beanContextServicesSupport = new BeanContextServicesSupport();

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
    public void setInformationDelegator(InformationDelegator in_informationDelegator) {
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
   
       public void setMenuItems(Iterator iterator) {
       //menuItems = in_menuItems;
       }
    */
  
    /**
     * Called when our menu item is clicked by user.
    */
    public void actionPerformed(ActionEvent ae) {
	// check if we have the object that generated this event. if
	// yes do it else complain
	if (getMenuItems().contains(ae.getSource() ) ){
	    if (informationDelegator != null) {
		informationDelegator.displayURL(Environment.get(Environment.HelpURL, "http://javamap.bbn.com/projects/openmap/openmap_maindes.html"));
	    }
	}
    }

    //-----------------------------------------------------------
    // BeanContextChild interface methods
    //-----------------------------------------------------------

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
	return beanContextServicesSupport.getBeanContext();
    }
    
    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
	if (!(in_bc instanceof BeanContextServices)) {
	    System.err.println("UserGuideMenuItem | setBeanContext -- Expecting BeanContextServices Object..might not work correctly in ContextEnvironment");
	    return;
	}
	BeanContextServices bcs  = (BeanContextServices)in_bc;
	if (bcs == null) {
	    beanContextServicesSupport.setBeanContext(bcs);
	    return;
	}
	beanContextServicesSupport.setBeanContext(bcs);
	bcs.addBeanContextServicesListener(this);

	if (bcs.hasService(com.bbn.openmap.InformationDelegator.class) ) {
	    InformationDelegator info=null;
	    try {
		info = (InformationDelegator)bcs.getService(this, this, com.bbn.openmap.InformationDelegator.class, null, this);
	    } catch(TooManyListenersException tmle) {
		System.out.println("UserGuideMenuItems.setBeanContext: caught TooManyListenersException");
	    }
	    if (info!= null) {
		setInformationDelegator(info);
	    }
	}
    }
    
    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName,
					  PropertyChangeListener in_pcl) {
	beanContextServicesSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void removePropertyChangeListener(String propertyName, 
					     PropertyChangeListener in_pcl) {
	beanContextServicesSupport.removePropertyChangeListener(propertyName, in_pcl);
    }
    
    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName, 
					  VetoableChangeListener in_vcl) {
	beanContextServicesSupport.addVetoableChangeListener(propertyName, in_vcl);
    }
  
    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, 
					     VetoableChangeListener in_vcl) {
	beanContextServicesSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }
  
    /**
     * Called by BeanContextServices Object, if this object is a part
     * of it and registered to listen for services that might be
     * available from the context.
     *
     * It looks for InformationDelegator Service.  
     */
    public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) {
	if (bcsae.getServiceClass() ==  com.bbn.openmap.InformationDelegator.class) {
	    BeanContextServices bcs = bcsae.getSourceAsBeanContextServices();
	    InformationDelegator info=null;
	    try {
		info = (InformationDelegator)bcs.getService(this,this,com.bbn.openmap.InformationDelegator.class, null, this);
	    } catch(TooManyListenersException tmle) {
		System.out.println("Caught exception Too many listenes in UserGuideMenuItems|serviceAvailable");
	    }
	    if (info != null) {
		setInformationDelegator(info);
	    }
	}
    }
  
    /**
     * Called by BeanContextService object if an service is not available any more.  
     */
    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
	if (bcsre.getServiceClass() ==  com.bbn.openmap.InformationDelegator.class) {
	    if (bcsre.isCurrentServiceInvalidNow()){
		setInformationDelegator(null);
	    }
	}
    }

    /** Method for BeanContextMembership interface. */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
    }

    /** Method for BeanContextMembership interface. */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
    }
}
