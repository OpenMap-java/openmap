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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OpenMapFrame.java,v $
// $RCSfile: OpenMapFrame.java,v $
// $Revision: 1.6 $
// $Date: 2003/12/23 20:47:46 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.bbn.openmap.Environment;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;

/**
 * The OpenMapFrame is the application window frame that holds the
 * MapPanel, and eventually the MapBean.  It listens to the
 * MapHandler for the addition of Beans to the MapHandler BeanContext,
 * and then positions the widgets it can deal with within itself.  The
 * frame does not present itself until an MapPanel is found.
 *
 * <p>The OpenMapFrame is intended to be used in an application
 * environment.  The applet checks and code to handle the applet
 * environment was moved to the OpenMapApplet class.
 */
public class OpenMapFrame extends JFrame 
    implements BeanContextMembershipListener, BeanContextChild, PropertyConsumer {
    
    /** Starting X coordinate of window */
    public static final String xProperty = Environment.OpenMapPrefix + ".x";
    
    /** Starting Y coordinate of window */
    public static final String yProperty = Environment.OpenMapPrefix + ".y";

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.  
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);
    
    /**
     * Create the frame with "OpenMap <version>" in the title.
     */
    public OpenMapFrame() {
	this(Environment.get(Environment.Title));
    }

    /**
     * Create a OpenMap frame with a title, with a WindowListner that
     * says what to do when the OpenMapFrame is closed.
     * 
     * @param title The Frame title.
     */
    public OpenMapFrame(String title) {
	super(title);
    }

    /**
     * For applications, checks where the Environment says the window
     * should be placed, and then uses the packed height and width to
     * make adjustments.
     */
    protected void setPosition() {
	// get starting width and height
	pack();
	int w = getWidth();
	int h = getHeight();

	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Debug.message("basic","Screen dimensions are " + d);

	if (w > d.width) w = d.width - d.width/10;
	if (h > d.height) h = d.height - d.height/10;

	int x = Environment.getInteger(xProperty, -1);
	int y = Environment.getInteger(yProperty, -1);
	if (x < 0)
	    x = d.width/2 - w/2;
	if (y < 0)
	    y = d.height/2 -h/2;
	
	if (Debug.debugging("basic")) {
	    Debug.output("Setting Frame X and Y from properties to " + x + " " + y);
	}

	// compose the frame, but don't show it here
	// contentPane.setBounds(x, y, w, h);
	setBounds(x,y,w,h);
    }

    /**
     * Called when the OpenMapFrame is added to a BeanContext, and
     * when other objects are added to the BeanContext.  The
     * OpenMapFrame looks for objects that it knows how to place upon
     * itself (MapPanel, JMenuBar).  The OpenMapFrame does not check
     * to see if the objects looked for are already added to itself.
     * It assumes that if some object type is getting added to it, the
     * caller must know what they are doing - just like a regular
     * JFrame.
     *
     * @param it Iterator to use to go through the BeanContext objects.  
     */
    public void findAndInit(Iterator it) {
	Object someObj;
	while (it.hasNext()) {
	    findAndInit(it.next());
	}
    }

    /**
     * Called when an object is added to the MapHandler.
     */
    public void findAndInit(Object someObj) {

	if (someObj instanceof MapPanel && someObj instanceof Container) {
	    Debug.message("basic", "OpenMapFrame: Found a MapPanel");
	    getContentPane().add((Container)someObj);

	    JMenuBar jmb = ((MapPanel)someObj).getMapMenuBar();
	    if (jmb != null) {
		Debug.message("basic", "OpenMapFrame: Got MenuBar from MapPanel");
		getRootPane().setJMenuBar(jmb);
	    }

	    setPosition();
	    invalidate();
	    show();
	}

	// We shouldn't find this if we've already defined one 
	// in the MapPanel, but we have this for backward
	// compatibility.
	if (someObj instanceof JMenuBar) {
	    Debug.message("basic", "OpenMapFrame: Found a MenuBar");
	    getRootPane().setJMenuBar((JMenuBar)someObj);
	    invalidate();
	}
    }
    
    /**
     * BeanContextMembership interface method.  Called when objects
     * are added to the BeanContext.
     *
     * @param bcme contains an Iterator that lets you go through the
     * new objects.  
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
	findAndInit(bcme.iterator());      
    }

    /**
     * BeanContextMembership interface method.  Called by BeanContext
     * when children are being removed.  Unhooks itself from the
     * objects that are being removed if they are contained within the
     * Frame.
     *
     * @param bcme event that contains an Iterator to use to go
     * through the removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	Object someObj;
	Iterator it = bcme.iterator();
	while (it.hasNext()) {
	    findAndUndo(it.next());
	}
    }

    /**
     * Called when an object is removed from the MapHandler.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof MapPanel && someObj instanceof Container) {
	    Debug.message("basic", "OpenMapFrame: MapBean is being removed from frame");
	    getContentPane().remove((Container)someObj);

	    if (getJMenuBar() == ((MapPanel)someObj).getMapMenuBar()) {
		Debug.message("basic", "OpenMapFrame: MenuPanel's MenuBar is being removed");
		setJMenuBar(null);
	    }
	}
	    
	if (someObj instanceof JMenuBar) {
	    if (getJMenuBar() == (JMenuBar) someObj) {
		Debug.message("basic", "OpenMapFrame: MenuPanel is being removed");
		setJMenuBar(null);
	    }
	}
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext()	{
	return beanContextChildSupport.getBeanContext();
    }
    
    /** Method for BeanContextChild interface. 
     * 
     * @param in_bc The context to which this object is being added
     */
    public void setBeanContext(BeanContext in_bc) 
	throws PropertyVetoException {
	if (in_bc != null) {
	    in_bc.addBeanContextMembershipListener(this);
	    beanContextChildSupport.setBeanContext(in_bc);
	    findAndInit(in_bc.iterator());
	}
    }
    
    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName,
					  VetoableChangeListener in_vcl) {
	beanContextChildSupport.addVetoableChangeListener(propertyName,
							  in_vcl);
    }
  
    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, 
					     VetoableChangeListener in_vcl) {
	beanContextChildSupport.removeVetoableChangeListener(propertyName,
							     in_vcl);
    }
    
    // Implementation of PropertyConsumer Interface
    /**
     * Method to set the properties in the PropertyConsumer.  It is
     * assumed that the properties do not have a prefix associated
     * with them, or that the prefix has already been set.
     *
     * @param setList a properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.
     */
    public void setProperties(Properties setList) {}

    /**
     * Method to set the properties in the PropertyConsumer.  The
     * prefix is a string that should be prepended to each property
     * key (in addition to a separating '.') in order for the
     * PropertyConsumer to uniquely identify properies meant for it, in
     * the midst of of Properties meant for several objects.
     *
     * @param prefix a String used by the PropertyConsumer to prepend
     * to each property value it wants to look up -
     * setList.getProperty(prefix.propertyKey).  If the prefix had
     * already been set, then the prefix passed in should replace that
     * previous value.
     * @param setList a Properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.  
     */
    public void setProperties(String prefix, Properties setList) {}

    /**
     * Method to fill in a Properties object, reflecting the current
     * values of the PropertyConsumer.  If the PropertyConsumer has a
     * prefix set, the property keys should have that prefix plus a
     * separating '.' prepended to each propery key it uses for
     * configuration. 
     *
     * @param getList a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
	if (getList == null) {
	    getList = new Properties();
	}

	getList.setProperty(xProperty,""+getBounds().x);
	getList.setProperty(yProperty,""+getBounds().y);
	getList.setProperty(Environment.Width, Integer.toString(getWidth()));
	getList.setProperty(Environment.Height, Integer.toString(getHeight()));

	return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}
	list.setProperty("x","Starting X coordinate of window");
	list.setProperty("y","Starting Y coordinate of window");
	return list;
    }

    /**
     * Doesn't do anything.  The OpenMapFrame looks for properties set
     * with the "openmap" property prefix.  This method is part of the
     * PropertyConsumer interface.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {}

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.  Returns "openmap".
     *
     * @return the property prefix for the frame
     */
    public String getPropertyPrefix() {
	return Environment.OpenMapPrefix;
    }
}
