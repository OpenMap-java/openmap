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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/DefaultHelpMenu.java,v $
// $RCSfile: DefaultHelpMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import com.bbn.openmap.*;

/**
 * HelpMenu is instance of JMenu. When added to beancontext it looks
 * for objects that implements HelpMenuItem interface. When objects
 * implementing HelpMenuItems interface are found, it simply retrieves the
 * menu items and adds them to itself.  Note: It is the responsibility
 * of the MenuItems themselves to respond to clicks on them.  
 */
public class DefaultHelpMenu extends JMenu 
  implements HelpMenu, BeanContextMembershipListener, BeanContextChild {

    private InformationDelegator informationDelegator = null;
    private JMenuItem helpOMMenuItem = new JMenuItem("OpenMap");
    private String defaultText = "Help";
    private int defaultMnemonic = 'H';

    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    public DefaultHelpMenu() {
	setText(defaultText);
	setMnemonic(defaultMnemonic);    
    }
  
    public DefaultHelpMenu(String in_text) {
	super(in_text);
    }
    
    /** 
     * Look for objects that implement HelpMenuItemsI interface and
     * add them to itself.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
	Iterator bcmeit = bcme.iterator();
	Object someObj;
	while (bcmeit.hasNext()) {
	    someObj = bcmeit.next();
	    if (someObj instanceof HelpMenuItems){
		Iterator hmiit = ((HelpMenuItems)someObj).iterator();
		JMenuItem mi = null;
		while (hmiit.hasNext()) {
		    add((JMenuItem)hmiit.next());
		}
	    }
	}
    }
  
    /**
     * If an object implementing helpMenuItemsI is found, remove it.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	Iterator bcmeit = bcme.iterator();
	Object someObj;
	while (bcmeit.hasNext()) {
	    someObj = bcmeit.next();
	    if (someObj instanceof HelpMenuItems) {
		Iterator hmiit = ((HelpMenuItems)someObj).iterator();
		JMenuItem mi = null;
		while (hmiit.hasNext()) {
		    remove((JMenuItem)hmiit.next());
		}
	    }
	}
    }
  
    //-----------------------------------------------------------
    // BeanContextChild interface methods
    //-----------------------------------------------------------

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
	return beanContextChildSupport.getBeanContext();
    }
    
    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
	if (in_bc != null) {
	    in_bc.addBeanContextMembershipListener(this);
	    beanContextChildSupport.setBeanContext(in_bc);	   
	}
    }
    
    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName,
					  PropertyChangeListener in_pcl) {
	beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void removePropertyChangeListener(String propertyName, 
					     PropertyChangeListener in_pcl) {
	beanContextChildSupport.removePropertyChangeListener(propertyName, in_pcl);
    }
    
    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName, 
					  VetoableChangeListener in_vcl) {
	beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }
  
    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, 
					     VetoableChangeListener in_vcl) {
	beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }
}
