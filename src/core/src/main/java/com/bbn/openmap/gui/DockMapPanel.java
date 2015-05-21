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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/DockMapPanel.java,v $
// $RCSfile: DockMapPanel.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JComponent;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.dock.BasicDockPanel;
import com.bbn.openmap.gui.dock.DockConstraint;
import com.bbn.openmap.gui.dock.DockPanel;

/**
 * The DockMapPanel is a MapPanel that uses a DockPanel for its
 * layout.
 * <P>
 * Like BasicMapPanel, the Panel creates a MapBean for its center
 * area. It creates a MapHandler to use to hold all of its OpenMap
 * components, and uses the PropertyHandler given to it in its
 * constructor to create and configure all of the application
 * components. The best way to add components to the MapPanel is to
 * get the MapHandler from it and add the component to that. The
 * DockMapPanel also adds itself to its MapHandler
 */
public class DockMapPanel extends BasicMapPanel implements DockPanel {

    protected BasicDockPanel dockPanel;

    /**
     * Create a MapPanel that creates its own PropertyHandler, which
     * will then search the classpath, config directory and user home
     * directory for the openmap.properties file to configure
     * components for the MapPanel.
     */
    public DockMapPanel() {
        this(false);
    }

    /**
     * Create a MapPanel with the option of delaying the search for
     * properties until the <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the
     *        artful programmer will call <code>create()</code>
     */
    public DockMapPanel(boolean delayCreation) {
        this(null, delayCreation);
    }

    /**
     * Create a MapPanel that configures itself with the properties
     * contained in the PropertyHandler provided. If the
     * PropertyHandler is null, a new one will be created.
     */
    public DockMapPanel(PropertyHandler propertyHandler) {
        this(propertyHandler, false);
    }

    /**
     * Create a MapPanel that configures itself with properties
     * contained in the PropertyHandler provided, and with the option
     * of delaying the search for properties until the
     * <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the
     *        artful programmer will call <code>create()</code>
     */
    public DockMapPanel(PropertyHandler propertyHandler, boolean delayCreation) {
        super(propertyHandler, delayCreation);
    }

    //From BasicMapPanel:
    /////////////////////

    protected final LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    protected final void addMapBeanToPanel(MapBean map) {
        setBackgroundComponent(map);
    }

    protected void createComponents() {
        dockPanel = new BasicDockPanel();
        super.add(dockPanel, BorderLayout.CENTER);
        super.createComponents();
    }

    /**
     * Add a child to the MapPanel.
     */
    protected void addMapPanelChild(MapPanelChild mpc) {
        //For now, just dock it somewhere... really we need to
        //determine constraints for it some how (maybe by asking it
        // for them?
        //mps.getPreferredLocation()
        //Debug.output("Adding MapPanelChild: " + mpc);
        dockPanel.add((JComponent) mpc);
        dockPanel.dockSomewhere((JComponent) mpc);
    }

    //From DockablePanel:
    /////////////////////

    public JComponent getBackgroundComponent() {
        return dockPanel.getBackgroundComponent();
    }

    public void setBackgroundComponent(JComponent back) {
        dockPanel.setBackgroundComponent(back);
    }

    public void setConstraint(JComponent child, DockConstraint c) {
        dockPanel.setConstraint(child, c);
    }

    public DockConstraint getConstraint(JComponent child) {
        return dockPanel.getConstraint(child);
    }

    public void removeConstraint(JComponent child) {
        dockPanel.removeConstraint(child);
    }

    public void setPreferredHeight(JComponent child, int i) {
        dockPanel.setPreferredHeight(child, i);
    }

    public void setPreferredWidth(JComponent child, int i) {
        dockPanel.setPreferredWidth(child, i);
    }

    public void setCanOcclude(JComponent child, boolean b) {
        dockPanel.setCanOcclude(child, b);
    }

    public void setCanTransparent(JComponent child, boolean b) {
        dockPanel.setCanTransparent(child, b);
    }

    public void setCanResize(JComponent child, boolean b) {
        dockPanel.setCanResize(child, b);
    }

    public void setCanTab(JComponent child, boolean b) {
        dockPanel.setCanTab(child, b);
    }

    public void setTabName(JComponent child, String tabName) {
        dockPanel.setTabName(child, tabName);
    }

    public void setCanExternalFrame(JComponent child, boolean b) {
        dockPanel.setCanExternalFrame(child, b);
    }

    public void setCanInternalFrame(JComponent child, boolean b) {
        dockPanel.setCanInternalFrame(child, b);
    }

    public void setCanClose(JComponent child, boolean b) {
        dockPanel.setCanClose(child, b);
    }

    public void setCanDockNorth(JComponent child, boolean b) {
        dockPanel.setCanDockNorth(child, b);
    }

    public void setCanDockSouth(JComponent child, boolean b) {
        dockPanel.setCanDockSouth(child, b);
    }

    public void setCanDockEast(JComponent child, boolean b) {
        dockPanel.setCanDockEast(child, b);
    }

    public void setCanDockWest(JComponent child, boolean b) {
        dockPanel.setCanDockWest(child, b);
    }

    public void dockNorth(JComponent child) {
        dockPanel.dockNorth(child);
    }

    public void dockNorth(JComponent child, int idx) {
        dockPanel.dockNorth(child, idx);
    }

    public void dockSouth(JComponent child) {
        dockPanel.dockSouth(child);
    }

    public void dockSouth(JComponent child, int idx) {
        dockPanel.dockSouth(child, idx);
    }

    public void dockEast(JComponent child) {
        dockPanel.dockEast(child);
    }

    public void dockEast(JComponent child, int idx) {
        dockPanel.dockEast(child, idx);
    }

    public void dockWest(JComponent child) {
        dockPanel.dockWest(child);
    }

    public void dockWest(JComponent child, int idx) {
        dockPanel.dockWest(child, idx);
    }

    public void dockSomewhere(JComponent child) {
        dockPanel.dockSomewhere(child);
    }

    public void dock(JComponent outter, JComponent inner) {
        dockPanel.dock(outter, inner);
    }

    public void dock(JComponent outter, JComponent inner, int idx) {
        dockPanel.dock(outter, inner, idx);
    }

    public void internalFrame(JComponent child) {
        dockPanel.internalFrame(child);
    }

    public void externalFrame(JComponent child) {
        dockPanel.externalFrame(child);
    }

    //Overwrite from Component:
    ///////////////////////////

    /**
     * We need to handle adding the component specially.
     */
    public Component add(Component comp) {
        return dockPanel.add(comp);
    }

    /**
     * We need to handle adding the component specially.
     */
    public void add(Component comp, Object constraints) {
        dockPanel.add(comp, constraints);
    }

    /**
     * We need to handle removing the component specially.
     */
    public void remove(Component comp) {
        dockPanel.remove(comp);
    }

    /**
     * We need to handle removing all components specially.
     */
    public void removeAll() {
        dockPanel.removeAll();
    }
}