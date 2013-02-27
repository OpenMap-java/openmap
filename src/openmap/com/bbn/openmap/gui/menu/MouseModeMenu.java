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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/MouseModeMenu.java,v $
// $RCSfile: MouseModeMenu.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChildSupport;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.gui.AbstractOpenMapMenu;
import com.bbn.openmap.util.Debug;

/**
 * It provides GUI based on Mouse modes available from MouseDelegator
 * object
 */
public class MouseModeMenu extends AbstractOpenMapMenu implements
        PropertyChangeListener, ActionListener {

    public static final String defaultText = "Mouse Mode";
    public final static transient String mouseModeCmd = "setMouseMode";

    protected transient MouseDelegator mouseDelegator = null;
    // mouse mode widgets
    protected transient JRadioButtonMenuItem[] mouseModeButtons = new JRadioButtonMenuItem[0];
    protected transient ButtonGroup group2 = null;
    
    public MouseModeMenu() {
        super();
        setText(i18n.get(this, "mouseModeMenu", defaultText));
        addActionListener(this);
    }

    /**
     * Sets up the MouseModes submenu.
     * 
     * @param md MouseDelegator
     */
    public void setMouseDelegator(MouseDelegator md) {
        mouseDelegator = md;
        if (mouseDelegator != null) {
            mouseDelegator.addPropertyChangeListener(this);
            MapMouseMode[] modes = mouseDelegator.getMouseModes();
            String activeMode = mouseDelegator.getActiveMouseModeID();
            Debug.message("mousemodemenuitem",
                    "MouseModeMenuItem.setMouseDelegator MouseDelegator has "
                            + modes.length + " modes");
            setUpItems(modes, activeMode);
        }
    }

    public void unsetMouseDelegator(MouseDelegator md) {
        if (md != null) {
            mouseDelegator.removePropertyChangeListener(this);

            if (mouseModeButtons != null) {
                for (int mms = 0; mms < mouseModeButtons.length; mms++) {
                    mouseModeButtons[mms].removeActionListener(this);
                    group2.remove(mouseModeButtons[mms]);
                }
            }
        }
        group2 = null;
        mouseModeButtons = null;
        mouseDelegator = null;
    }

    protected void setUpItems(MapMouseMode[] modes, String activeMode) {
        if (group2 == null) {
            group2 = new ButtonGroup();
        }
        mouseModeButtons = new JRadioButtonMenuItem[modes.length];

        for (int mms = 0; mms < modes.length; mms++) {
            Debug.message("mousemodemenuitem",
                    "MouseModeMenuItem.setUpItems adding " + modes[mms].getID());
            mouseModeButtons[mms] = (JRadioButtonMenuItem) this.add(new JRadioButtonMenuItem(modes[mms].getPrettyName()));

            mouseModeButtons[mms].setActionCommand(mouseModeCmd);
            mouseModeButtons[mms].setName(modes[mms].getID());
            mouseModeButtons[mms].addActionListener(this);
            mouseModeButtons[mms].setVisible(modes[mms].isVisible());
            group2.add(mouseModeButtons[mms]);

            if ((activeMode != null) && activeMode.equals(modes[mms].getID())) {
                mouseModeButtons[mms].setSelected(true);
            }
        }
    }

    /**
     * ActionListener interface.
     * 
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(mouseModeCmd)) {
            JRadioButtonMenuItem rb = (JRadioButtonMenuItem) (e.getSource());
            mouseDelegator.setActiveMouseModeWithID(rb.getName());
        }
    }

    /**
     * This method gets called when a bound property is changed.
     * <p>
     * 
     * @param evt A PropertyChangeEvent object describing the event
     *        source and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Debug.message("mousemodemenuitem", "MouseModeMenuItem.propertyChange()");

        if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
            // Mark the radio button representing the new mode as
            // active
            String mmID = ((MapMouseMode) evt.getNewValue()).getID();
            for (int i = 0; i < mouseModeButtons.length; i++) {
                //System.out.println(mmID + " "
                // +mouseModeButtons[i].getName());
                if (mouseModeButtons[i].getName().equals(mmID)) {
                    mouseModeButtons[i].setSelected(true);
                    //System.out.println("MouseModeMenu: New Active
                    // Mode " + mmID);
                    break;
                }
            }
        }

        else if (evt.getPropertyName() == MouseDelegator.MouseModesProperty) {
            // Redo the whole submenu
            for (int i = 0; i < mouseModeButtons.length; i++) {
                remove(mouseModeButtons[i]);
            }
            MapMouseMode[] modes = mouseDelegator.getMouseModes();
            String activeMode = mouseDelegator.getActiveMouseModeID();
            setUpItems(modes, activeMode);
        }
    }

    /**
     * Called when a component that is needed, and not available with
     * an appropriate iterator from the BeanContext. This lets this
     * object hook up with what it needs.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MouseDelegator) {
            // do the initializing that need to be done here
            Debug.message("mousemodemenuitem",
                    "MouseModeMenuItem found a MouseDelegator.");
            setMouseDelegator((MouseDelegator) someObj);
        }
    }

    /**
     * AbstractOpenMapMenu method. Called when an objects have been
     * removed from the parent BeanContext.
     */
    public void findAndUnInit(Object someObj) {
        if (someObj instanceof MouseDelegator) {
            // do the initializing that need to be done here
            Debug.message("mousemodemenuitem",
                    "MouseModeMenuItem removing MouseDelegator.");
            setMouseDelegator(null);
        }
    }

}