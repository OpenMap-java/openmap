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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/PlugInLayer.java,v $
// $RCSfile: PlugInLayer.java,v $
// $Revision: 1.18 $
// $Date: 2006/02/13 16:58:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin;

/*  Java Core  */
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Properties;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The PlugInLayer is a kind of layer that has a direct interface with the
 * MapBean. The Layer contains a handle to a PlugIn object, which is, in effect,
 * a module that knows how to respond to geographical requests for information,
 * and can create graphics to be drawn.
 * <p>
 * The PlugInLayer has a standard interface to the PlugIn module object, and
 * knows to call certain PlugIn methods to respond to Layer methods. It also
 * knows about the OMGraphicsList that is part of the PlugIn, and when graphical
 * objects are to be rendered, it tells the plugin's OMGraphicsList to render
 * the object using a Graphics that the Layer provides.
 * 
 * <pre>
 * 
 * #Properties for basic PlugInLayer:
 * pluginlayer.class=com.bbn.openmap.plugin.PlugInLayer
 * pluginlayer.prettyName=PRETTY NAME
 * pluginlayer.plugin=classname of plugin
 * #.... followed by plugin properties with the &quot;pluginlayer&quot; prefix...
 * 
 * </pre>
 */
public class PlugInLayer extends OMGraphicHandlerLayer {

    /**
     * If the PlugInLayer creates the PlugIn, it will append ".plugin" to the
     * properties prefix it will send to PlugIn.setProperties(). So, the PlugIn
     * properties should look like layerPrefix.plugin.pluginPropertyName=value.
     * <P>
     * 
     * NOTE: This is different than when a PlugIn is created as a component by
     * the ComponentFactory called by the PropertyHandler. If the
     * PropertyHandler calls the ComponentFactory, then the properties should
     * look like pluginComponentPrefix.pluginProperty=value.
     */
    public final static String PlugInProperty = "plugin";

    /** The handle to the PlugIn object. */
    protected transient PlugIn plugin = null;

    /**
     * The MapMouseListener for the layer/plugin combo that knows how to respond
     * to mouse events.
     */
    protected MapMouseListener mml;

    /**
     * This string is the deciding factor in how independent the PlugIn gets to
     * be with respect to PropertyConsumer methods.
     */
    protected String plugInClass = null;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public PlugInLayer() {
        setName("PlugInLayer");
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /**
     * Layer method that gets called when the Layer gets removed from the map.
     */
    public void removed(java.awt.Container container) {
        PlugIn pi = getPlugIn();
        if (pi != null) {
            pi.removed();
        }
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
            setPlugIn((PlugIn) ComponentFactory.create(plugInClass,
                    plugInPrefix,
                    props));
        } else {
            // If plugInClass is not defined, then we want the
            // PlugInLayer to be invisible - the PlugIn should be
            // the only thing in the properties, and the other
            // components should be OK with that.
            PlugIn pi = getPlugIn();
            if (pi != null) {
                pi.setProperties(prefix, props);
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        PlugIn pi = getPlugIn();
        String prefix;
        if (pi != null) {
            if (plugInClass != null) {
                prefix = PropUtils.getScopedPropertyPrefix(this);
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
        props = super.getPropertyInfo(props);

        if (plugInClass != null || pi == null) {
            // If plugInClass is not defined, then we want the
            // PlugInLayer to be invisible - the PlugIn should be
            // the only thing in the properties, and the other
            // components should be OK with that.

            props.put(PlugInProperty, "Class name of PlugIn");
            props.put(PlugInProperty + ScopedEditorProperty,
                    "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
        } else {
            props.put("class", "Class name of PlugIn");
            props.put("class" + ScopedEditorProperty,
                    "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
            props.put(PrettyNameProperty, getName());
            props.put(PrettyNameProperty + ScopedEditorProperty,
                    "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
        }

        if (pi != null) {
            pi.getPropertyInfo(props);
        }

        return props;
    }

    /**
     * Set the property key prefix that should be used by the PropertyConsumer.
     * The prefix, along with a '.', should be prepended to the property keys
     * known by the PropertyConsumer.
     * 
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {
        super.setPropertyPrefix(prefix);

        PlugIn pi = getPlugIn();

        if (pi != null) {
            if (plugInClass != null) {
                pi.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(prefix)
                        + PlugInProperty);
            } else {
                plugin.setPropertyPrefix(prefix);
            }
        }
    }

    public void dispose() {
        if (plugin != null) {
            plugin.setComponent(null);
        }
        removePlugInFromBeanContext(plugin);
        plugin = null;
    }

    /**
     * Sets the current graphics list to the given list.
     * 
     * @param aList a list of OMGraphics
     * @deprecated call setList() instead.
     */
    public synchronized void setGraphicList(OMGraphicList aList) {
        setList(aList);
    }

    /**
     * Retrieves the current graphics list.
     * 
     * @deprecated call getList() instead.
     */
    public synchronized OMGraphicList getGraphicList() {
        return getList();
    }

    /**
     * Returns the plugin module of the layer.
     */
    public PlugIn getPlugIn() {
        return plugin;
    }

    /**
     * Sets the plugin module of the layer. This method also calls setLayer on
     * the plugin, and gets the MapMouseListener from the plugin, too.
     */
    public void setPlugIn(PlugIn aPlugIn) {
        // Need to remove from BeanContext if it was added previously.
        if (plugin != null) {
            removePlugInFromBeanContext(plugin);
        }

        plugin = aPlugIn;
        if (aPlugIn != null) {
            plugin.setComponent(this);
            setMapMouseListener(plugin.getMapMouseListener());
            // This might be called as a result of setProperties() and
            // then this call won't do anything because the
            // BeanContext hasn't been set yet. We need to call it
            // now in case the plugin is set in the layer later.
            addPlugInToBeanContext(plugin);
        } else if (Debug.debugging("plugin")) {
            Debug.output("PlugInLayer: null PlugIn set!");
        }
    }

    /**
     * Returns the MapMouseListener object that handles the mouse events.
     * 
     * @return the MapMouseListener for the layer, or null if none
     */
    public synchronized MapMouseListener getMapMouseListener() {
        return mml;
    }

    /**
     * Set the MapMouseListener for the layer.
     * 
     * @param mmlIn the object that will handle the mouse events for the layer.
     */
    public synchronized void setMapMouseListener(MapMouseListener mmlIn) {
        mml = mmlIn;
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the plugin. This is called by the PulgInWorker, or
     * can be called from a different thread than the AWT thread. If you're not
     * sure, call doPrepare() instead, and a separate thread will be launched to
     * call this.
     * 
     * @return new OMGraphicList filled by plugin.
     */
    public synchronized OMGraphicList prepare() {
        Debug.message("plugin", getName() + "|PlugInLayer.prepare()");

        if (isCancelled()) {
            Debug.message("plugin", getName()
                    + "|PlugInLayer.prepare(): aborted.");
            return null;
        }

        if (plugin == null) {
            System.out.println(getName()
                    + "|PlugInLayer.prepare(): No plugin in layer.");
            return null;
        }

        Debug.message("basic", getName() + "|PlugInLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        Projection proj = getProjection();
        if (Debug.debugging("plugin") && proj != null) {
            System.out.println(getName() + "|PlugInLayer.prepare(): "
                    + "calling getRectangle " + " with projection: " + proj
                    + " ul = " + proj.getUpperLeft() + " lr = "
                    + proj.getLowerRight());
        }

        OMGraphicList omGraphicList = null;

        if (plugin != null && proj != null) {
            omGraphicList = plugin.getRectangle(proj);
        }

        // ///////////////////
        // safe quit
        int size = 0;
        if (omGraphicList != null) {
            size = omGraphicList.size();
            if (Debug.debugging("basic")) {
                Debug.output(getName()
                        + "|PlugInLayer.prepare(): finished with " + size
                        + " graphics");
            }
        } else {
            if (Debug.debugging("basic")) {
                Debug.output(getName()
                        + "|PlugInLayer.prepare(): finished with null graphics list");
            }
            omGraphicList = new OMGraphicList();
        }

        // NOTE - We've assumed that the graphics are projected!

        return omGraphicList;
    }

    /**
     * Checks the PlugIn to see if it has a GUI. Returns null if the PlugIn
     * doesn't exist.
     */
    public java.awt.Component getGUI() {
        if (plugin != null) {
            return plugin.getGUI();
        } else {
            return null;
        }
    }

    /**
     * Layer method, enhanced to check if the PlugIn is interested in being
     * added to the BeanContext.
     */
    public boolean getAddToBeanContext() {
        boolean ret = false;
        if (plugin != null
                &&

                (plugin instanceof BeanContextChild || plugin instanceof BeanContextMembershipListener)) {

            if (plugin instanceof AbstractPlugIn) {
                ret = ((AbstractPlugIn) plugin).getAddToBeanContext();
            } else {
                ret = true;
            }

        } else {
            ret = super.getAddToBeanContext();
        }

        if (Debug.debugging("plugin")) {
            Debug.output(getName() + ".addToBeanContext is " + ret);
        }

        return ret;
    }

    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        super.setBeanContext(in_bc);

        // Needs to be done here, because if the plugin was created
        // from the properties, it will have already been set but the
        // BeanContext wasn't yet available.
        addPlugInToBeanContext(getPlugIn());
    }

    /**
     * Gets the current BeanContext from itself, if it's been set and the
     * provided PlugIn wants/can be added to the BeanContext, it will be added..
     */
    public void addPlugInToBeanContext(PlugIn pi) {
        BeanContext bc = getBeanContext();

        if (bc != null
                && pi != null
                &&

                (pi instanceof BeanContextChild || (pi instanceof AbstractPlugIn && ((AbstractPlugIn) pi).getAddToBeanContext()))) {

            bc.add(pi);
        }
    }

    /**
     * Gets the current BeanContext from itself, if it's been set and the
     * provided PlugIn wants/can be added to the BeanContext, it assumes it was
     * and removes it from the BeanContext.
     */
    public void removePlugInFromBeanContext(PlugIn pi) {
        BeanContext bc = getBeanContext();

        if (bc != null
                && pi != null
                &&

                (pi instanceof BeanContextChild || (pi instanceof AbstractPlugIn && ((AbstractPlugIn) pi).getAddToBeanContext()))) {

            // Of course, we don't need all these conditions met to
            // order the removal, but they are the ones in place that would
            // cause it to be added, so we don't waste the effort
            // unless the same conditions are met.
            bc.remove(pi);
        }
    }

}