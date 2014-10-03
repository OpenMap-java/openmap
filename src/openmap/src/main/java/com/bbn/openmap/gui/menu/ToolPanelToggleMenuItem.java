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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/ToolPanelToggleMenuItem.java,v $
// $RCSfile: ToolPanelToggleMenuItem.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.gui.ToolPanel;

/**
 * Menu item that holds onto the tool panel, and hides/displays it when
 * selected. Since the ToolPanel will make itself invisible if all of its
 * components are invisible, this menu item will disable itself when the
 * ToolPanel has set itself to be invisible, and vice-versa.
 */
public class ToolPanelToggleMenuItem extends JMenuItem implements
        ActionListener, ComponentListener, PropertyChangeListener {

    protected ToolPanel toolPanel = null;

    public final static String HideLabelProperty = "hide";
    public final static String DisplayLabelProperty = "display";

    protected final static String DefaultHideLabel = "Hide";
    protected final static String DefaultDisplayLabel = "Display";
    protected final static String DefaultToolPanelName = "Tool Panel";

    protected String hideLabel = DefaultHideLabel;
    protected String displayLabel = DefaultDisplayLabel;

    public ToolPanelToggleMenuItem() {
        // assume that the tool panel isn't there.
        // Won't be visible if it isn't.
        super(DefaultHideLabel);
        init(null);
    }

    public ToolPanelToggleMenuItem(ToolPanel tp) {
        super(tp.isVisible() ? DefaultHideLabel : DefaultDisplayLabel);
        init(tp);
    }

    public void init(ToolPanel tp) {
        setI18NLabels(tp);
        setToolPanel(tp);
        addActionListener(this);
    }

    public void setToolPanel(ToolPanel tp) {
        if (toolPanel != null) {
            toolPanel.removeComponentListener(this);
            toolPanel.removePropertyChangeListener(this);
        }

        toolPanel = tp;
        this.setVisible(toolPanel != null);

        if (toolPanel != null) {
            toolPanel.addComponentListener(this);
            toolPanel.addPropertyChangeListener(this);
            stateCheck();
        }
    }

    public ToolPanel getToolPanel() {
        return toolPanel;
    }

    public boolean forToolPanel(ToolPanel tp) {
        return (tp == toolPanel);
    }

    public void actionPerformed(ActionEvent ae) {
        if (toolPanel != null) {
            boolean selected = toolPanel.isVisible();
            toolPanel.setVisible(!selected);
            setText(selected ? displayLabel : hideLabel);
        }
    }

    public void dispose() {
        setToolPanel(null);
    }

    /**
     * Check the state of the ToolPanel and set enabled state and text
     * accordingly.
     */
    public void stateCheck() {
        if (toolPanel != null) {
            setEnabled(toolPanel.areComponentsVisible());
            setText(toolPanel.isVisible() ? hideLabel : displayLabel);
        }
    }

    public void componentHidden(ComponentEvent ce) {
        stateCheck();
    }

    public void componentMoved(ComponentEvent ce) {}

    public void componentResized(ComponentEvent ce) {}

    public void componentShown(ComponentEvent ce) {
        stateCheck();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName.equals(ToolPanel.MembershipProperty)) {
            stateCheck();
        }

    }

    protected void setI18NLabels(ToolPanel tp) {

        I18n i18n = Environment.getI18n();
        String name = DefaultToolPanelName;
        if (tp != null) {
            name = tp.getName();
        }

        String interString = i18n.get(ToolPanelToggleMenuItem.class,
                HideLabelProperty,
                I18n.TOOLTIP,
                hideLabel);

        hideLabel = interString + " " + name;

        interString = i18n.get(ToolPanelToggleMenuItem.class,
                DisplayLabelProperty,
                I18n.TOOLTIP,
                displayLabel);

        displayLabel = interString + " " + name;
    }
}