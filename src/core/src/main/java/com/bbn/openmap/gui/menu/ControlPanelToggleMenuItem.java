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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/ControlPanelToggleMenuItem.java,v $
// $RCSfile: ControlPanelToggleMenuItem.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.gui.OMControlPanel;

/**
 * Menu item that holds onto the OMControlPanel, and hides/displays it
 * when selected.
 */
public class ControlPanelToggleMenuItem extends JMenuItem implements
        ActionListener, LightMapHandlerChild {

    protected OMControlPanel controlPanel = null;
    protected final static String hideLabel = "Hide Control Panel";
    protected final static String displayLabel = "Display Control Panel";

    public ControlPanelToggleMenuItem() {
        // assume that the control panel isn't there.
        // Won't be visible if it isn't.
        super(hideLabel);
        init(null);
    }

    public ControlPanelToggleMenuItem(OMControlPanel cp) {
        super(cp.isVisible() ? hideLabel : displayLabel);
        init(cp);
    }

    public void init(OMControlPanel cp) {
        setControlPanel(cp);
        addActionListener(this);
    }

    public void setControlPanel(OMControlPanel cp) {
        controlPanel = cp;
        this.setVisible(controlPanel != null);
    }

    public OMControlPanel getControlPanel() {
        return controlPanel;
    }

    public boolean forControlPanel(OMControlPanel cp) {
        return (cp == controlPanel);
    }

    public void actionPerformed(ActionEvent ae) {
        if (controlPanel != null) {
            boolean selected = controlPanel.isVisible();
            controlPanel.setVisible(!selected);
            setText(selected ? displayLabel : hideLabel);
        }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof OMControlPanel) {
            setControlPanel((OMControlPanel) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof OMControlPanel
                && getControlPanel() == (OMControlPanel) someObj) {
            setControlPanel(null);
        }
    }
}