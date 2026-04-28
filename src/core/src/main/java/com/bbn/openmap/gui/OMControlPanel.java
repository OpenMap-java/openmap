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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OMControlPanel.java,v $
// $RCSfile: OMControlPanel.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.PropUtils;

/**
 * The OMControlPanel is an all-in-one panel that holds an overview
 * map, pan and zoom buttons, projection stack buttons, scale text
 * field and a LayersPanel. All of the sub-components share the same
 * property prefix as the OMControlPanel, all have access to
 * components in the MapHandler. The sub-components are not given to
 * the MapHandler themselves, however.
 */
public class OMControlPanel extends OMComponentPanel implements MapPanelChild {

    LinkedList children = new LinkedList();

    public OMControlPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel navBox = new JPanel();
        navBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        navBox.setLayout(new BorderLayout());

        OverviewMapHandler overviewMap = new OverviewMapHandler();
        overviewMap.setUseAsTool(false);
        overviewMap.setPreferredSize(new Dimension(100, 100));
        overviewMap.setBorder(BorderFactory.createRaisedBevelBorder());
        overviewMap.setPropertyPrefix("OverviewMapHandler");
        children.add(overviewMap);

        NavigatePanel navPanel = new NavigatePanel();
        navPanel.setPropertyPrefix("NavigatePanel");
        ZoomPanel zoomPanel = new ZoomPanel();
        zoomPanel.setPropertyPrefix("ZoomPanel");
        ProjectionStackTool projStack = new ProjectionStackTool();
        projStack.setPropertyPrefix("ProjectionStackTool");
        ScaleTextPanel scalePanel = new ScaleTextPanel();
        scalePanel.setPropertyPrefix("ScaleTextPanel");
        scalePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel navBoxRN = new JPanel();
        children.add(navPanel);
        navBoxRN.add(navPanel);
        navBoxRN.add(Box.createHorizontalGlue());
        children.add(zoomPanel);
        navBoxRN.add(zoomPanel);

        JPanel navBoxRS = new JPanel();
        navBoxRS.setLayout(new BorderLayout());
        children.add(projStack);
        children.add(scalePanel);
        navBoxRS.add(projStack, BorderLayout.NORTH);
        navBoxRS.add(scalePanel, BorderLayout.SOUTH);

        JPanel navBoxR = new JPanel();
        navBoxR.setLayout(new BorderLayout());
        navBoxR.add(navBoxRN, BorderLayout.NORTH);
        navBoxR.add(navBoxRS, BorderLayout.SOUTH);

        navBox.add(overviewMap, BorderLayout.CENTER);
        navBox.add(navBoxR, BorderLayout.EAST);

        add(navBox);

        LayersPanel layersPanel = new LayersPanel();
        layersPanel.setPropertyPrefix("LayersPanel");
        layersPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        children.add(layersPanel);
        add(layersPanel);
        validate();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String pl = props.getProperty(prefix + PreferredLocationProperty);
        if (pl != null) {
            setPreferredLocation(pl);
        }

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof PropertyConsumer) {
                // Each property prefix will be set with the marker
                // name for the OMControlPanel plus the class name
                // already set as property prefix in the constructor.
                String newPrefix = prefix
                        + ((PropertyConsumer) obj).getPropertyPrefix();
                ((PropertyConsumer) obj).setProperties(newPrefix, props);
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        props.put(PropUtils.getScopedPropertyPrefix(this)
                + PreferredLocationProperty, getPreferredLocation());

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof PropertyConsumer) {
                ((PropertyConsumer) obj).getProperties(props);
            }
        }
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        props.put(PreferredLocationProperty,
                "The preferred BorderLayout direction to place this component.");

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof PropertyConsumer) {
                ((PropertyConsumer) obj).getPropertyInfo(props);
            }
        }
        return props;
    }

    public void findAndInit(Object someObj) {
        Iterator it = children.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof LightMapHandlerChild) {
                ((LightMapHandlerChild) obj).findAndInit(someObj);
            }
        }
    }

    public void findAndUndo(Object someObj) {
        Iterator it = children.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof LightMapHandlerChild) {
                ((LightMapHandlerChild) obj).findAndUndo(someObj);
            }
        }
    }

    /**
     * BorderLayout.WEST by default for this class.
     */
    protected String preferredLocation = java.awt.BorderLayout.WEST;

    /**
     * MapPanelChild method.
     */
    public void setPreferredLocation(String value) {
        preferredLocation = value;
    }

    /**
     * MapPanelChild method.
     */
    public String getPreferredLocation() {
        return preferredLocation;
    }

    public String getParentName() {
        // TODO Auto-generated method stub
        return null;
    }
}