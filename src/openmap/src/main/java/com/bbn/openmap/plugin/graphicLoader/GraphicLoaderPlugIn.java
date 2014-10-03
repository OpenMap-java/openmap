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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/graphicLoader/GraphicLoaderPlugIn.java,v $
// $RCSfile: GraphicLoaderPlugIn.java,v $
// $Revision: 1.9 $
// $Date: 2005/08/09 20:35:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.graphicLoader;

import java.awt.Component;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.util.Properties;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.graphicLoader.GraphicLoader;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.plugin.OMGraphicHandlerPlugIn;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A GraphicLoaderPlugIn is a PlugIn that receives its OMGraphics from
 * some other source, at any time. It just listens to its
 * GraphicLoader for updates to the OMGraphicList, and then updates
 * the map as necessary.
 * <P>
 * The GraphicLoaderPlugIn passes projection changes onto the
 * GraphicLoader, and if the GraphicLoader is a MapMouseListener, the
 * GraphicLoaderPlugIn will defer all MouseEvents to it.
 * <p>
 * 
 * To add a GraphicLoader to the OpenMap application, you can do it
 * several ways:
 * <UL>
 * <LI>You can create a specific GraphicLoaderPlugIn that creates its
 * own GraphicLoader, and initializes it accordingly. You would add
 * the GraphicLoaderPlugIn to the openmap.layers property in the
 * openmap.properties file.
 * <LI>You can create a GraphicLoaderPlugIn by adding an entry to the
 * openmap.layer property in the openmap.properties file, and define
 * what kind of GraphicLoader to create in the properties for the
 * GraphicLoaderPlugIn.
 * <P>
 * 
 * <pre>
 * 
 *  graphicLoaderPlugIn.class=com.bbn.openmap.plugin.graphicLoader.GraphicLoaderPlugIn
 *  graphicLoaderPlugIn.prettyName=Name of Layer
 *  graphicLoaderPlugIn.graphicLoader=GraphicLoader Classname
 *  graphicLoaderPlugIn.addGraphicLoaderToMapHandler=true/false (false by default)
 *  
 * </pre>
 * 
 * <LI>You can add a
 * com.bbn.openmap.plugin.graphicLoader.GraphicLoaderConnector to the
 * openmap.components property, and then add the GraphicLoader to the
 * openmap.components property as well. The GraphicLoaderConnector
 * will find the GraphicLoader, and create a
 * GraphicLoaderPlugIn/PlugInLayer for the GraphicLoader and add it to
 * the LayerHandler on top of the map.
 * </UL>
 *  
 */
public class GraphicLoaderPlugIn extends OMGraphicHandlerPlugIn {

    protected GraphicLoader loader = null;
    public final static String GraphicLoaderProperty = "graphicLoader";
    public final static String AddGraphicLoaderToMapHandlerProperty = "addGraphicLoaderToMapHandler";

    protected boolean addGraphicLoaderToMapHandler = false;
    protected boolean needToAddGraphicLoaderToMapHandler = false;

    private boolean inGetRectangle = false;
    protected Object lock = new Object();

    public GraphicLoaderPlugIn() {
        super();
    }

    /**
     * @param comp the PlugInLayer to work with.
     */
    public GraphicLoaderPlugIn(Component comp) {
        super(comp);
    }

    /**
     * The getRectangle call is the main call into the PlugIn module.
     * The module is expected to fill the graphics list with objects
     * that are within the screen parameters passed.
     * 
     * @param p projection of the screen, holding scale, center
     *        coords, height, width.
     */
    public OMGraphicList getRectangle(Projection p) {
        // Used to control the doPrepare() call in setList().
        synchronized (lock) {
            inGetRectangle = true;
        }

        if (loader != null) {
            loader.setProjection(p);
        }

        OMGraphicList list = (OMGraphicList) super.getList();
        list.generate(p);

        if (Debug.debugging("graphicloader")) {
            Debug.output("GraphicLoaderPlugIn returning list of " + list.size()
                    + " objects.");
        }

        // Used to control the doPrepare() call in setList().
        synchronized (lock) {
            inGetRectangle = false;
        }

        return list;
    }

    /**
     * OMGraphicHandler method. This will cause doPrepare() to be
     * called on the PlugInLayer, which will result in a
     * getRectangle() being called the GraphicLoaderPlugIn, which will
     * in turn cause setProjection() to be called on the
     * GraphicLoader. Watch out, in the GraphicLoader, for setting the
     * list as a result of a projection change - you can get into a
     * loop. The AbstractGraphicLoader checks to see if the projection
     * has changed.
     */
    public synchronized void setList(OMGraphicList graphics) {
        super.setList(graphics);

        synchronized (lock) {
            if (!inGetRectangle) {
                // Should be OK, launching a separate thread to
                // come back into getRectangle. We only want to call
                // doPrepare() if setList is being called in a thread
                // from something else controlling the GraphicLoader,
                // like a timer or something.
                doPrepare();
            }
        }
    }

    /** OMGraphicHandler method. */
    public synchronized boolean doAction(OMGraphic graphic, OMAction action) {
        boolean ret = super.doAction(graphic, action);
        doPrepare();
        return ret;
    }

    /**
     * Set the GraphicLoader for the PlugIn. If the GraphicLoader is a
     * MapMouseListener, it will be used as such for this PlugIn.
     */
    public void setGraphicLoader(GraphicLoader gl) {
        loader = gl;

        gl.setReceiver(this);

        if (gl instanceof MapMouseListener) {
            setMapMouseListener((MapMouseListener) gl);
        } else {
            setMapMouseListener(this);
        }

        if (needToAddGraphicLoaderToMapHandler && getBeanContext() != null
                && loader != null) {

            getBeanContext().add(loader);
            needToAddGraphicLoaderToMapHandler = false;
        }
    }

    /**
     * Get the GraphicLoader loader.
     */
    public GraphicLoader getGraphicLoader() {
        return loader;
    }

    /**
     * Set whether to add the GraphicLoader, which is assumed to yet
     * be added to the GraphicLoaderPlugIn, to the MapHandler.
     */
    public void setAddGraphicLoaderToMapHandler(boolean val) {
        addGraphicLoaderToMapHandler = val;
        needToAddGraphicLoaderToMapHandler = val;
    }

    public boolean getAddGraphicLoaderToMapHandler() {
        return addGraphicLoaderToMapHandler;
    }

    /**
     * Method for BeanContextChild interface. Adds this object as a
     * BeanContextMembership listener, set the BeanContext in this
     * objects BeanContextSupport, and receives the initial list of
     * objects currently contained in the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        super.setBeanContext(in_bc);

        if (in_bc != null && needToAddGraphicLoaderToMapHandler
                && getGraphicLoader() != null) {
            in_bc.add(getGraphicLoader());
            needToAddGraphicLoaderToMapHandler = false;
        }
    }

    /**
     * PropertyConsumer interface method.
     * 
     * @see com.bbn.openmap.PropertyConsumer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String glString = props.getProperty(realPrefix + GraphicLoaderProperty);
        addGraphicLoaderToMapHandler = PropUtils.booleanFromProperties(props,
                realPrefix + AddGraphicLoaderToMapHandlerProperty,
                addGraphicLoaderToMapHandler);

        if (glString != null) {
            GraphicLoader gl = (GraphicLoader) ComponentFactory.create(glString,
                    prefix,
                    props);
            if (gl != null) {
                needToAddGraphicLoaderToMapHandler = addGraphicLoaderToMapHandler;
                setGraphicLoader(gl);
            }
        }
    }

    /**
     * PropertyConsumer interface method.
     * 
     * @see com.bbn.openmap.PropertyConsumer
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        GraphicLoader gl = getGraphicLoader();

        if (gl != null) {
            String prefix = PropUtils.getScopedPropertyPrefix(this);
            props.setProperty(prefix + GraphicLoaderProperty, gl.getClass()
                    .getName());
            if (gl instanceof PropertyConsumer) {
                ((PropertyConsumer) gl).getProperties(props);
            }
        }
        return props;
    }

    /**
     * PropertyConsumer interface method.
     * 
     * @see com.bbn.openmap.PropertyConsumer
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        props.setProperty(GraphicLoaderProperty, "Classname of GraphicLoader");

        GraphicLoader gl = getGraphicLoader();
        if (gl instanceof PropertyConsumer) {
            ((PropertyConsumer) gl).getPropertyInfo(props);
        }

        return props;
    }

    /**
     * Standard PlugIn method to provide palette. Differed to the
     * GraphicLoader.
     */
    public Component getGUI() {
        if (loader != null) {
            return loader.getGUI();
        } else {
            return null;
        }
    }
}