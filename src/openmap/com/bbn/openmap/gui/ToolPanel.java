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
// $Revision: 1.4 $
// $Date: 2003/09/08 22:25:44 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
import java.util.Vector;
import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/** 
 * Represents the toolbar containing tools to apply to the map.
 * Tools can be added in sequential order, and retrieved using the
 * tool's keyword.  NOTE: Every time a string is passed into a method
 * of this class, the interned version of it is used as a key. <P>
 *
 * When the ToolPanel is part of the BeanContext, it looks for Tools
 * that have also been added to the BeanContext.  If there is more
 * than one ToolPanel in a BeanContext at a time, both will show the
 * same Tool faces.  The 'components' property can be used to control
 * which tools can be added to a specific instance of a ToolPanel.
 * That property should contain a space separated list of prefixes
 * used for Tools, which in turn should be set in the Tools as their
 * keys.
 *
 * @see Tool
 * @author john gash 
 */
public class ToolPanel extends JToolBar 
    implements BeanContextChild, BeanContextMembershipListener, MapPanelChild, PropertyConsumer, ComponentListener {

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
     * The property prefix used for this ToolPanel.
     */
    protected String propertyPrefix = null;

    /**
     * A list of components to use for filtering tools found in the
     * MapHandler to add to this ToolPanel.
     */
    public final static String ComponentsProperty = "components";

    /**
     * A filter list of components to look for and add.
     */
    protected List componentList = null;

    /**
     * Constructor
     */
    public ToolPanel() {
	super();
	setLayout(new FlowLayout(FlowLayout.LEFT));
	setFloatable(false);
	setVisible(false);
    }
    
    /**
     * Add an item to the tool bar.
     *
     * @param key The key associated with the item.
     * @param item The Tool to add.
     */
    public void add(String key, Tool item) {
	add(key, item, -1);
    }
    
    /**
     * Add an item to the tool bar.
     *
     * @param key The key associated with the item.
     * @param item The Tool to add.
     * @param index The position index for placement of the tool.  -1
     * puts it at the end, and if the position is greater than the
     * size, it is placed at the end.  This class does not remember
     * where items were asked to be placed, so later additions may
     * mess up intended order.
     */
    public void add(String key, Tool item, int index) {
	Container face = item.getFace();
	if (face != null) {
	    face.addComponentListener(this);
	    items.put(key.intern(), item); 
	    
	    if (autoSpace) {
		index *= 2;
	    }

	    if (index < getComponentCount()) {
		add(face, index);
	    } else {
		add(face);
	    }

	    if (autoSpace) {
		addSpace();
	    }
	}
	setVisibility();
    }

    /** 
     * Add an item to the tool bar.  Assumes that the key will be
     * picked out of the Tool.
     *
     * @param item The Tool to add.
     */
    public void add(Tool item) {
	add(item, -1);
    }

    /** 
     * Add an item to the tool bar.  Assumes that the key will be
     * picked out of the Tool.
     *
     * @param item The Tool to add.
     * @param index The position to add the Tool.  -1 will add it to
     * the end.
     */
    public void add(Tool item, int index) {
	try {
	    add(item.getKey().intern(), item, index);
	} catch (NullPointerException npe) {
	    if (item != null) {
		Debug.error("ToolPanel.add(): no name for " + item.getClass().getName());
	    } else {
		Debug.error("ToolPanel.add(): no name for null tool.");
	    }
	}
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
	    tool.getFace().removeComponentListener(this);
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

    /**
     * BorderLayout.NORTH by default for this class.
     */
    protected String preferredLocation = java.awt.BorderLayout.NORTH;

    /**
     * MapPanelChild method.
     */
    public void setPreferredLocation(String value) {
	preferredLocation = value;
    }

    /** MapPanelChild method. */
    public String getPreferredLocation() {
	return preferredLocation;
    }

    /** Find out whether spaces are being placed between tools. */
    public boolean isAutoSpace() {
	return autoSpace;
    }

    /**
     * Set the list of strings used by the ToolPanel to figure out
     * which Tools should be added (in the findAndInit()) method and
     * where they should go.
     */
    public void setComponentList(List list) {
	componentList = list;
    }

    /**
     * Get the list of strings used by the ToolPanel to figure out
     * which Tools should be added (in the findAndInit()) method and
     * where they should go.
     */
    public List getComponentList() {
	return componentList;
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
	    List list = getComponentList();
	    // If no list filtering set, just add.
	    if (list == null) {
		// do the initializing that need to be done here
		Debug.message("basic","ToolPanel: found a tool Object");
		add((Tool)someObj);
	    } else {
		// Otherwise, check the component list for object, add
		// it to panel if it's found.
		String key = ((Tool)someObj).getKey();
		Iterator it = list.iterator();
		int index = 0;
		while (it.hasNext()) {
		    String listKey = (String)it.next();
		    if (listKey.equalsIgnoreCase(key)) {
			if (Debug.debugging("basic")) {
			    Debug.output("ToolPanel: found a tool Object " + 
					 key + " for placement at " + index);
			}

			if (index < getComponentCount()) {
			    add(((Tool)someObj).getFace(), index);
			} else {
			    add((Tool)someObj);
			}
		    }
		    index++;
		}
	    }
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

    public void setPropertyPrefix(String prefix) {
	propertyPrefix = prefix;
    }

    public String getPropertyPrefix() {
	return propertyPrefix;
    }

    public void setProperties(Properties props) {
	setProperties(null, props);
    }

    public void setProperties(String prefix, Properties props) {
	setPropertyPrefix(prefix);

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	String componentsString = 
	    props.getProperty(prefix + ComponentsProperty);

	if (componentsString != null) {
	    setComponentList(PropUtils.parseSpacedMarkers(componentsString));
	}
    }

    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	List compList = getComponentList();

	if (compList != null) {
	    StringBuffer list = new StringBuffer();
	    Iterator it = compList.iterator();
	    while (it.hasNext()) {
		list.append((String)it.next() + " ");
	    }

	    props.put(PropUtils.getScopedPropertyPrefix(this) + ComponentsProperty, list.toString());
	}

	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	props.put(ComponentsProperty, "List of Names of Tools to Add");
	    
	return props;
    }

    /**
     * If any of the components are visible, set the ToolPanel to be
     * visible.  If all of them are invisible, make the ToolPanel
     * invisible.
     */
    protected void setVisibility() {
	setVisible(areComponentsVisible());
    }

    public boolean areComponentsVisible() {
	Enumeration enum = items.elements();
	while (enum.hasMoreElements()) {
	    if (((Component)enum.nextElement()).isVisible()) {
		return true;
	    }
	}
	return false;
    }

    public void componentHidden(ComponentEvent ce) {
	setVisibility();
    }

    public void componentMoved(ComponentEvent ce) {

    }

    public void componentResized(ComponentEvent ce) {

    }

    public void componentShown(ComponentEvent ce) {
	setVisibility();
    }

}

