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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MouseModeButtonPanel.java,v $
// $RCSfile: MouseModeButtonPanel.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseMode;

/**
 * The MouseModeButtonPanel is an alternative to the MouseModePanel. Instead of
 * providing an option menu listing all the MouseModes, a series of buttons for
 * all the MouseModes is displayed.
 * 
 * The MouseModeButtonPanel asks the MapMouseModes for their GUI icons. If they
 * don't have one, their ID Strin will be used.
 */
public class MouseModeButtonPanel extends MouseModePanel {

    private JToolBar toolBar = null;
    /** for initialization, false */
    protected boolean floatable = false;
    /**
     * to turn a button on when the active mouse mode changes behind the scenes.
     */
    protected HashMap buttonSet = new HashMap();

    protected JToggleButton enabledButton = null;

    public final static String defaultKey = "mousemodebuttonpanel";

    public MouseModeButtonPanel() {
        super();
        setKey(defaultKey);
    }

    /**
     * Method overrides MouseModePane.setPanel. Construct the toolbar buttons
     * from the mouse modes that are handled by the delegator.
     */
    protected void setPanel(MouseDelegator md) {
        if (toolBar != null) {
            remove(toolBar); // remove it
        }

        reset();
        if (md == null) {
            return;
        }

        toolBar = new JToolBar(); // Create a new one
        toolBar.setFloatable(floatable);
        String activeMode = md.getActiveMouseModeID();

        ButtonGroup bg = new ButtonGroup();

        MapMouseMode[] modes = md.getMouseModes();
        for (int i = 0; i < modes.length; i++) {
            String modeStr = modes[i].getID();
            boolean on = (modeStr == null && activeMode == null)
                    || (modeStr != null && modeStr.equals(activeMode));

            JToggleButton btn;
            Icon icon = modes[i].getGUIIcon();

            if (icon != null) {
                btn = new JToggleButton(icon, on);
            } else {
                btn = new JToggleButton(modeStr, on);
            }

            if (on) {
                enabledButton = btn;
            }

            buttonSet.put(modeStr, btn);

            bg.add(btn);
            btn.setToolTipText(modes[i].getPrettyName());
            toolBar.add(btn);
            btn.setActionCommand(modeStr);
            btn.addActionListener(this);

            btn.setVisible(modes[i].isVisible());
        }

        this.add(toolBar);
        this.revalidate();
    } // End of setPanel()

    /**
     * actionPerformed - Handle the mouse clicks on the button(s)
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

        if (mouseDelegator == null) {
            return;
        } else {
            mouseDelegator.setActiveMouseModeWithID(e.getActionCommand());
        }
    }

    /**
     * Set whether the MouseModeButtonPanel can be detached.
     */
    public void setFloatable(boolean floatable) {
        this.floatable = floatable;
        toolBar.setFloatable(floatable);
    }

    /**
     * Get whether the MouseModeButtonPanel can be detached.
     */
    public boolean getFloatable() {
        return toolBar.isFloatable();
    }

    // //////////////////////////////////////////////////////////////////////////
    /**
     * propertyChange - Listen for changes to the active mouse mode and for any
     * changes to the list of available mouse modes.
     */
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
            String mmID = ((MapMouseMode) evt.getNewValue()).getID();

            JToggleButton btn = (JToggleButton) buttonSet.get(mmID);
            if (btn != null) {
                btn.setSelected(true);
                enabledButton = btn;
            }

        } else if (evt.getPropertyName() == MouseDelegator.MouseModesProperty) {
            toolBar.removeAll();
            setPanel(mouseDelegator);
        }
    } // End of propertyChange()

    public void reset() {
        if (toolBar != null) {
            remove(toolBar);
            toolBar = null;
        }
        enabledButton = null;
        buttonSet.clear();
    }
}