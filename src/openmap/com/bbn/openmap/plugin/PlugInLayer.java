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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/PlugInLayer.java,v $
// $RCSfile: PlugInLayer.java,v $
// $Revision: 1.2 $
// $Date: 2003/02/20 02:43:50 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin;


/*  Java Core  */
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.Properties;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.*;

/**
 * The PlugInLayer is a kind of layer that has a direct interface
 * with the MapBean.  The Layer contains a handle to a PlugIn object,
 * which is, in effect, a module that knows how to respond to
 * geographical requests for information, and can create graphics to
 * be drawn.
 * <p>
 * The PlugInLayer has a standard interface to the PlugIn module
 * object, and knows to call certain PlugIn methods to respond to
 * Layer methods.  It also knows about the OMGraphicsList that is
 * part of the PlugIn, and when graphical objects are to be rendered,
 * it tells the plugin's OMGraphicsList to render the object using a
 * Graphics that the Layer provides.
 */
public class PlugInLayer extends OMGraphicHandlerLayer {

    /**
     * If the PlugInLayer creates the PlugIn, it will append
     * ".plugin" to the properties prefix it will send to
     * PlugIn.setProperties(). So, the PlugIn properties should look
     * like layerPrefix.plugin.pluginPropertyName=value.<P>
     *
     * NOTE: This is different than when a PlugIn is created 
     * as a component by the ComponentFactory called by the 
     * PropertyHandler.  If the PropertyHandler calls the 
     * ComponentFactory, then the properties should look like 
     * pluginComponentPrefix.pluginProperty=value.
     */
    public final static String PlugInProperty = "plugin";

    /** The handle to the PlugIn object. */
    protected transient PlugIn plugin = null;

    /** 
     * The MapMouseListener for the layer/plugin combo that knows how
     * to respond to mouse events.
     */
    protected MapMouseListener mml;

    /**
     * This string is the deciding factor in how independent the
     * PlugIn gets to be with respect to PropertyConsumer methods.
     */
    protected String plugInClass = null;

    /**
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public PlugInLayer() {
	setName("PlugInLayer");
    }

    /**
     * Set the properties for the PlugIn Layer.
     */ 
    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);

	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
	
	plugInClass = props.getProperty(realPrefix + PlugInProperty);

	if (plugInClass != null) {
	    String plugInPrefix = PlugInProperty;
	    plugInPrefix = realPrefix + PlugInProperty;
	    setPlugIn((PlugIn)ComponentFactory.create(plugInClass, plugInPrefix, props));
	} else {
	    // If plugInClass is not defined, then we want the
	    // PlugInLayer to be invisible - the PlugIn should be
	    // the only thing in the properties, and ther other
	    // components should be OK with that.
	    PlugIn pi = getPlugIn();
	    if (pi != null) {
		pi.setProperties(prefix, props);
	    }
	}
    }

    public Properties getProperties(Properties props) {

	PlugIn pi = getPlugIn();
	String prefix;
	if (pi != null) {
	    if (plugInClass != null) {
		prefix = PropUtils.getScopedPropertyPrefix(this);
		props = super.getProperties(props);
		props.put(prefix + PlugInProperty, pi.getClass().getName());
	    } else {
		// If plugInClass is not defined, then we want the
		// PlugInLayer to be invisible - the PlugIn should be
		// the only thing in the properties, and ther other
		// components should be OK with that.
		prefix = PropUtils.getScopedPropertyPrefix(pi);
		props.put(prefix + "class", pi.getClass().getName());
		props.put(prefix + PrettyNameProperty, getName());
	    }

	    pi.getProperties(props);
	}

	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	PlugIn pi = getPlugIn();

	if (plugInClass != null || pi == null) {
	    // If plugInClass is not defined, then we want the
	    // PlugInLayer to be invisible - the PlugIn should be
	    // the only thing in the properties, and ther other
	    // components should be OK with that.
	    
	    props = super.getProperties(props);
	    props.put(PlugInProperty, "Class name of PlugIn");
	    props.put(PlugInProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
	} else {
	    props.put("class", "Class name of PlugIn");
	    props.put("class" + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
	    props.put(PrettyNameProperty, getName());
	    props.put(PrettyNameProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
	}

	if (pi != null) {
	    pi.getPropertyInfo(props);
	}

	return props;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {
	super.setPropertyPrefix(prefix);

	PlugIn pi = getPlugIn();

	if (pi != null) {
	    if (plugInClass != null) {
		pi.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(prefix) + PlugInProperty);
	    } else {
		plugin.setPropertyPrefix(prefix);
	    }
	}
    }

    /**
     * Sets the current graphics list to the given list.
     *
     * @param aList a list of OMGraphics
     * @param deprecated call setList() instead.
     */
    public synchronized void setGraphicList(OMGraphicList aList) {
	setList(aList);
    }

    /**
     * Retrieves the current graphics list.
     * @param deprecated call getList() instead.
     */
    public synchronized OMGraphicList getGraphicList() {
	return getList();
    }

    /** 
     *  Returns the plugin module of the layer.
     */
    public PlugIn getPlugIn() {
        return plugin;
    }

    /**
     *  Sets the plugin module of the layer.  This method also calls
     *  setLayer on the plugin, and gets the MapMouseListener from the
     *  plugin, too.
     */
    public void setPlugIn(PlugIn aPlugIn) {
	plugin = aPlugIn;
	if (aPlugIn != null) {
	    plugin.setComponent(this);
	    mml = plugin.getMapMouseListener();
	} else if (Debug.debugging("plugin")) {
	    Debug.output("PlugInLayer: null PlugIn set!");
	}
    }

    /**
     * Returns the MapMouseListener object that handles the mouse
     * events.
     * @return the MapMouseListener for the layer, or null if none
     */
    public synchronized MapMouseListener getMapMouseListener() {
	return mml;
    }
 
    /**
     * Set the MapMouseListener for the layer.
     * @param mml the object that will handle the mouse events for the
     * layer.
     */
    public synchronized void setMapMouseListener(MapMouseListener mml) {
	this.mml = mml;
    }

    /**
     * Overriding what happens to the internal OMGraphicList when the
     * projection changes.  For this layer, we want to reset the
     * internal OMGraphicList when the projection changes.
     */
    protected void resetListForProjectionChange() {
	setList(null);
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the plugin.  This is
     * called by the PulgInWorker, or can be called from a different
     * thread than the AWT thread.  If you're not sure, call
     * doPrepare() instead, and a separate thread will be launched to
     * call this.
     *
     * @return new OMGraphicList filled by plugin.
     */
    public OMGraphicList prepare() {
	Debug.message("plugin", getName()+"|PlugInLayer.prepare()");

	if (isCancelled()) {
	    Debug.message("plugin", getName()+"|PlugInLayer.prepare(): aborted.");
	    return null;
	}

	if (plugin == null) {
	    System.out.println(getName()+"|PlugInLayer.prepare(): No plugin in layer.");
	    return null;
	}

	Debug.message("basic", getName()+"|PlugInLayer.prepare(): doing it");

	// Setting the OMGraphicsList for this layer.  Remember, the
	// OMGraphicList is made up of OMGraphics, which are generated
	// (projected) when the graphics are added to the list.  So,
	// after this call, the list is ready for painting.

	// call getRectangle();
	Projection proj = getProjection();
	if (Debug.debugging("plugin") && proj != null) {
	    System.out.println(
		      getName()+"|PlugInLayer.prepare(): " +
		      "calling getRectangle " +
		      " with projection: " + proj +
		      " ul = " + proj.getUpperLeft() + " lr = " + 
		      proj.getLowerRight()); 
	}

	OMGraphicList omGraphicList = null;

	if (plugin != null) {
	    omGraphicList = plugin.getRectangle(proj);
	}

	/////////////////////
	// safe quit
	int size = 0;
	if (omGraphicList != null) {
	    size = omGraphicList.size();	
	    if (Debug.debugging("basic")) {
		Debug.output(getName() + "|PlugInLayer.prepare(): finished with "+
			     size + " graphics");
	    }
	} else {
	    if (Debug.debugging("basic")) {
		Debug.output(getName() + "|PlugInLayer.prepare(): finished with null graphics list");
	    }
	    omGraphicList = new OMGraphicList();
	}

	// NOTE - We've assumed that the graphics are projected!

	return omGraphicList;
    }

    /**
     * Checks the PlugIn to see if it has a GUI.  Returns null if the
     * PlugIn doesn't exist.
     */
    public java.awt.Component getGUI() {
	if (plugin != null) {
	    return plugin.getGUI();
	} else {
	    return null;
	}
    }

    /**
     * Layer method, enhanced to check if the PlugIn is interested in
     * being added to the BeanContext.
     */
    public boolean getAddToBeanContext() {
	if (plugin != null && 
	    (plugin instanceof BeanContextChild ||
	     plugin instanceof BeanContextMembershipListener)) {
	    Debug.message("plugin", getName() + ".addToBeanContext is true");
	    return true;
	} else {
	    return super.getAddToBeanContext();
	}
    }

    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) 
	throws PropertyVetoException {
	super.setBeanContext(in_bc);

	if (plugin != null && plugin instanceof BeanContextChild) {
	    in_bc.add(plugin);
	}
    }
}
