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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.beans.beancontext.*;
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;

/**
 * The OpenMapFrame is the application window frame that holds the
 * MapBean.  It listeners for the addition of Beans to the MapHandler
 * BeanContext, and then positions the widgets it can deal with within
 * itself.  <P>Right now, the frame does not present itself until a
 * MapBean is found.
 */
public class OpenMapFrame extends JFrame 
    implements BeanContextMembershipListener, BeanContextChild, PropertyConsumer {
    
    /** The pane to which we add OpenMap components in application.*/
    Container contentPane=null;
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
     * Create a OpenMap frame with a title.
     * 
     * @param title The Frame title.
     */
    public OpenMapFrame(String title) {
	super(title);
					
	// show the window
	if (Environment.isApplication()) {
	    contentPane = getContentPane();
	    contentPane.setLayout(new BorderLayout());
	    addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    // need a shutdown event to notify other gui beans and
		    // then exit.
		    System.exit(0);
		}
	    });
	} else {
	    contentPane = ((JApplet)(Environment.getApplet())).getContentPane();
	}
    }

    /**
     * Called when the OpenMapFrame is added to a BeanContext, and
     * when other objects are added to the BeanContext.  The
     * OpenMapFrame looks for objects that it knows how to place upon
     * itself (MapBean, ToolPanel, JMenuBar, InformationDelegator).
     * The OpenMapFrame does not check to see if the objects looked
     * for are already added to itself.  It assumes that if some
     * object type is getting added to it, the caller must know what
     * they are doing - just like a regular JFrame.
     *
     * @param it Iterator to use to go through the BeanContext objects.  
     */
    protected void findAndInit(Iterator it) {
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof MapBean) {
		// do the initializing that need to be done here
		MapBean mapBean = (MapBean)someObj;
		if (Debug.debugging("basic")) {
		    Debug.output("OpenMapFrame: found a MapBean, size " + mapBean.getSize() +
				 ", preferred size " + mapBean.getPreferredSize() +
				 ", " + mapBean.getProjection());
		}
		contentPane.add((MapBean)someObj, BorderLayout.CENTER);
		
		if (!Environment.isApplet()) {
		    setPosition();
		    show();
		    if (Debug.debugging("basic")) {
			Debug.output("OpenMapFrame: After layout, MapBean size " + mapBean.getSize());
		    }
		}
	    }
	    
	    if (someObj instanceof ToolPanel) {
		// do the initializing that need to be done here
		Debug.message("basic", "OpenMapFrame: found a ToolPanel.");
		ToolPanel toolPanel = (ToolPanel)someObj;
		toolPanel.setFloatable(false);
		contentPane.add(toolPanel, BorderLayout.NORTH);
	    }

	    if (someObj instanceof JMenuBar) {
		Debug.message("basic", "OK...Frame Found a MenuBar Object");
		if(!Environment.isApplet()){
		    getRootPane().setJMenuBar((JMenuBar)someObj);
		} else {
		    ((JApplet)Environment.getApplet()).getRootPane().setJMenuBar((JMenuBar)someObj);
		}

	    }

	    if (someObj instanceof InformationDelegator) {
		Debug.message("basic", "OpenMapFrame: found an InfoDelegator.");
		InformationDelegator info = (InformationDelegator)someObj;
		info.setFloatable(false);
		contentPane.add(info, BorderLayout.SOUTH);
	    }
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
	    someObj = it.next();
	    if (someObj instanceof MapBean) {		
		Debug.message("basic", "OpenMapFrame: MapBean is being removed from frame");
		// if it's not on the content pane, no foul...
		getContentPane().remove((MapBean)someObj);
	    }
	    
	    if (someObj instanceof ToolPanel) {		
		Debug.message("basic", "OpenMapFrame: ToolPanel is being removed from frame");
		// if it's not on the content pane, no foul...
		getContentPane().remove((ToolPanel)someObj);
	    }

	    if (someObj instanceof JMenuBar) {
		if (getJMenuBar() == (JMenuBar) someObj) {
		    Debug.message("basic", "OpenMapFrame: MenuPanel is being removed");
		    setJMenuBar(null);
		}
	    }

	    if (someObj instanceof InformationDelegator) {
		Debug.message("basic", "OpenMapFrame: InfoDelegator being removed.");
		// if it's not on the content pane, no foul...
		getContentPane().remove((InformationDelegator)someObj);
	    }
	}
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext()	{
	return beanContextChildSupport.getBeanContext();
    }
    
    /** Method for BeanContextChild interface. 
     * 
     * @param BeanContext in_bc The context to which this object is being added
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
    public void setProperties(Properties setList) {
    }

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
     * @param getList a Properties object to load the PropertyConsumer
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
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {}

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @param String prefix String.  
     */
    public String getPropertyPrefix() {
	return Environment.OpenMapPrefix;
    }
}
