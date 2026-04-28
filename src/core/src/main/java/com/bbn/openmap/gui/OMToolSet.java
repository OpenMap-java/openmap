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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OMToolSet.java,v $
// $RCSfile: OMToolSet.java,v $
// $Revision: 1.10 $
// $Date: 2005/08/09 19:14:52 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The OMToolSet bundles other control beans, and is a Tool used in
 * the OpenMap application.
 * <P>
 * 
 * It contains a NavigatePanel, which is the directional rosette used
 * to pan the MapBean, the ZoomPanel that has a zoom in and zoom out
 * button, and a scale text window. To use the OMToolSet, create an
 * instance of it, and then call setupListeners() with the MapBean.
 * All the event handling is automatically set up.
 * <P>
 * 
 * If the OMToolSet is added to a BeanContext, it should be found by a
 * ToolPanel. It looks for a MapBean add as listeners to the various
 * widgets. If it doesn't get a MapBean, then the projection widgets
 * will appear to be non-functioning.
 */
public class OMToolSet extends OMComponentPanel implements Serializable, Tool {

    public final static String defaultKey = "omtoolset";

    protected String key = defaultKey;

    /** Navigation rosette */
    protected transient NavigatePanel navPanel = null;
    /** Zoom buttons */
    protected transient ZoomPanel zoomPanel = null;
    /** ScaleTextPanel for scale text. */
    protected transient ScaleTextPanel scaleField = null;

    public final static String AddZoomProperty = "addZoom";
    public final static String AddPanProperty = "addPan";
    public final static String AddScaleProperty = "addScale";

    protected boolean addZoom = true;
    protected boolean addPan = true;
    protected boolean addScale = true;

    /**
     * Create the OMToolSet.
     */
    public OMToolSet() {
        super();
        Debug.message("omtoolset", "OMToolSet()");

        setLayout(new FlowLayout(FlowLayout.LEFT));

        navPanel = new NavigatePanel();

        zoomPanel = new ZoomPanel();

        scaleField = new ScaleTextPanel();

        add(navPanel);
        add(zoomPanel);
        add(scaleField);

        createFace();
    }

    /**
     * Tool interface method. The retrieval tool's interface. This is
     * added to the tool bar.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        return this;
    }

    /**
     * Sets the visibility of the default components, based on their
     * boolean settings.
     */
    protected void createFace() {
        zoomPanel.setVisible(addZoom);
        navPanel.setVisible(addPan);
        scaleField.setVisible(addScale);
    }

    /**
     * Tool interface method. The retrieval key for this tool.
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return key;
    }

    /**
     * Tool interface method. Set the retrieval key for this tool.
     * 
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        key = aKey;
    }

    /**
     * Get the ZoomPanel
     * 
     * @return the ZoomPanel
     */
    public ZoomPanel getZoomPanel() {
        return zoomPanel;
    }

    /**
     * Get the NavigatePanel.
     * 
     * @return the NaviationPanel (directional rosette)
     */
    public NavigatePanel getNavigatePanel() {
        return navPanel;
    }

    /**
     * Get the scale field widget.
     * 
     * @return ScaleTextPanel that is rigged to set the scale for the
     *         map.
     */
    public ScaleTextPanel getScaleField() {
        return scaleField;
    }

    /**
     * Convenience function to set up listeners of the components. If
     * you are hooking the MapBean up to the OMToolSet, this is what
     * you need to call.
     * 
     * @param aMapBean a map object.
     */
    public void setupListeners(MapBean aMapBean) {
        if (aMapBean != null) {
            findAndInit(aMapBean);
        }
    }

    /**
     * This function removes the mapBean object from its set of
     * Listeners. An inverse of setupListeners() method.
     * 
     * @param aMapBean a map object.
     */
    public void removeFromAllListeners(MapBean aMapBean) {
        if (aMapBean != null) {
            findAndUndo(aMapBean);
        }
    }

    /**
     * Add a button to the panel. Will attempt to create a URL from
     * the name of the image file.
     * 
     * @param name image filename
     * @param info tool tip
     * @param al ActionListener
     */
    public void addButton(String name, String info, ActionListener al) {
        try {
            URL url = PropUtils.getResourceOrFileOrURL(null, name);
            if (url != null) {
                addButton(url, info, al);
            }
        } catch (MalformedURLException murle) {
            Debug.error("OMToolSet.addButton: can't create button for " + info);
        }
    }

    /**
     * Add a button to the panel.
     * 
     * @param url URL for image
     * @param info tool tip
     * @param al ActionListener
     */
    public void addButton(URL url, String info, ActionListener al) {
        JButton b = new JButton(new ImageIcon(url, info));
        b.setToolTipText(info);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.addActionListener(al);
        b.setBorderPainted(false);
        add(b);
    }

    /**
     * MapHandlerChild method.
     */
    public void findAndInit(Object someObj) {
        navPanel.findAndInit(someObj);
        zoomPanel.findAndInit(someObj);
        scaleField.findAndInit(someObj);
    }

    /**
     * MapHandlerChild method.
     */
    public void findAndUndo(Object someObj) {
        navPanel.findAndUndo(someObj);
        zoomPanel.findAndUndo(someObj);
        scaleField.findAndUndo(someObj);
    }

    /**
     * Method to set the properties in the PropertyConsumer. The
     * prefix is a string that should be prepended to each property
     * key (in addition to a separating '.') in order for the
     * PropertyConsumer to uniquely identify properties meant for it,
     * in the midst of of Properties meant for several objects.
     * 
     * @param prefix a String used by the PropertyConsumer to prepend
     *        to each property value it wants to look up -
     *        setList.getProperty(prefix.propertyKey). If the prefix
     *        had already been set, then the prefix passed in should
     *        replace that previous value.
     * @param setList a Properties object that the PropertyConsumer
     *        can use to retrieve expected properties it can use for
     *        configuration.
     */
    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);

        // Important for ToolPanel that controls what it is listening
        // for, instead of grabbing any Tool. The prefix will be used
        // as a discriminator.
        if (prefix != null) {
            setKey(prefix);
        }

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        addZoom = PropUtils.booleanFromProperties(setList, prefix
                + AddZoomProperty, addZoom);
        addPan = PropUtils.booleanFromProperties(setList, prefix
                + AddPanProperty, addPan);
        addScale = PropUtils.booleanFromProperties(setList, prefix
                + AddScaleProperty, addScale);

        createFace();
    }

    /**
     * Method to fill in a Properties object, reflecting the current
     * values of the PropertyConsumer. If the PropertyConsumer has a
     * prefix set, the property keys should have that prefix plus a
     * separating '.' prepended to each property key it uses for
     * configuration.
     * 
     * @param getList a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        getList.put(prefix + AddZoomProperty, new Boolean(addZoom).toString());
        getList.put(prefix + AddPanProperty, new Boolean(addPan).toString());
        getList.put(prefix + AddScaleProperty, new Boolean(addScale).toString());
        return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer. The key
     * for each property should be the raw property name (without a
     * prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        list.put(AddZoomProperty, "Flag to add the Zoom buttons");
        list.put(AddZoomProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(AddPanProperty, "Flag to add the Pan buttons");
        list.put(AddPanProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(AddScaleProperty, "Flag to add the scale field");
        list.put(AddScaleProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return list;
    }
}