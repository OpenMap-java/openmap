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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ToolPanel.java,v $
// $RCSfile: ToolPanel.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import com.bbn.openmap.util.Debug;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;

/** 
 * Represents the toolbar containing tools to apply to the map.
 * Tools can be added in sequential order, and retrieved using the
 * tool's keyword.  NOTE: Every time a string is passed into a method
 * of this class, the interned version of it is used as a key. <P>
 *
 * When the ToolPanel is part of the BeanContext, it looks for Tools
 * that have also been added to the BeanContext.  If there is more
 * than one ToolPanel in a BeanContext at a time, both will show the
 * same Tool faces.
 *
 * @author john gash 
 */
public class ToolPanel extends JToolBar 
    implements BeanContextChild, BeanContextMembershipListener {

    /** The set of tools contained on the toolbar. */
    protected Hashtable items = new Hashtable();
    /**
     * A flag to note whether the ToolPanel inserts spaces between
     * tools. 
     */
    protected boolean autoSpace = false;
    
    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

    /**
     * Constructor
     */
    public ToolPanel() {
	super();
	setLayout(new FlowLayout(FlowLayout.LEFT));
    }
    
    /**
     * Add an item to the tool bar.
     *
     * @param key The key associated with the item.
     * @param item The jamsTool to add.
     */
    public void add(String key, Tool item) {
	Container face = item.getFace();
	if (face != null) {
	    items.put(key.intern(), item); 
	    if (autoSpace && items.size() > 0) {
		addSpace();
	    }
	    add(face);
	}
    }

    /** 
     * Add an item to the tool bar.  Assumes that the key will be
     * picked out of the Tool.
     *
     * @param item The Tool to add.
     */
    public void add(Tool item) {
	add(item.getKey().intern(), item);
    }

    /**
     * Get an item from the tool bar.
     *
     * @param key The key associated with the item.
     * @return The tool associated with the key, null if not found.
     */
    public Tool get(String key) {
	return (Tool)items.get(key.intern()); 
    } 
    
    /** Remove a tool with the right key */
    public void remove(String key) {
	Tool tool = (Tool) items.remove(key.intern());
	if (tool != null) {
	    remove(tool.getFace());
	}
    }

    /** Add a space between tools. */
    protected void addSpace() { 
	add(new JLabel("   "));
    }

    /** Set whether spaces are placed between tools. */
    public void setAutoSpace(boolean set) {
	autoSpace = set;
    }

    /** Find out whether spaces are being placed between tools. */
    public boolean isAutoSpace() {
	return autoSpace;
    }

    /**
     * Called when the ToolPanel is added to the BeanContext, and when
     * new objects are added to the BeanContext after that.  The
     * ToolPanel looks for Tools that are part of the BeanContext.
     *
     * @param it iterator to use to go through the new objects.
     */
    public void findAndInit(Iterator it) {
	while (it.hasNext()) {
	    findAndInit(it.next());
	} 
    }

    public void findAndInit(Object someObj) {
	if (someObj instanceof Tool) {
	    // do the initializing that need to be done here
	    Debug.message("basic","ToolPanel: found a tool Object");
	    add((Tool)someObj);
	}	  
    }

    /** 
     * BeanContextMembershipListener method.  Called when objects have
     * been added to the parent BeanContext.
     *
     * @param bcme the event containing the iterator with new objects.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
	findAndInit(bcme.iterator());      
    }
    
    /** 
     * BeanContextMembershipListener method.  Called when objects have
     * been removed from the parent BeanContext.  If the ToolPanel
     * finds a Tool in the list, it removes it from the ToolPanel.
     *
     * @param bcme the event containing the iterator with removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	Iterator it = bcme.iterator();
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof Tool) {
		// do the initializing that need to be done here
		Debug.message("toolpanel","ToolPanel removing tool Object");
		remove(((Tool)someObj).getKey());
	    }
	}
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
	return beanContextChildSupport.getBeanContext();
    }
  
    /** 
     * Method for BeanContextChild interface.  Called when the
     * ToolPanel is added to the BeanContext.
     *
     * @param in_bc the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
	if (in_bc != null) {
	    in_bc.addBeanContextMembershipListener(this);
	    beanContextChildSupport.setBeanContext(in_bc);
	    findAndInit(in_bc.iterator());
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

