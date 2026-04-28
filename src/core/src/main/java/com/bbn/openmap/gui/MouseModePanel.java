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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MouseModePanel.java,v $
// $RCSfile: MouseModePanel.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.util.Debug;


/**
 *  MouseModePanel displays the mouse modes available to the map, as a
 *  popup option menu.
 */
public class MouseModePanel extends OMToolComponent 
    implements Serializable, ActionListener, PropertyChangeListener {

    public final static transient String mouseModeCmd = "setMouseMode";
    public final static transient String newMouseModeCmd = "newMouseMode";
    public final static transient String defaultKey = "mousemodepanel";

    protected transient JButton titleButton;
    protected transient JPopupMenu rbs = null;
    protected transient MouseDelegator mouseDelegator;
    protected transient TitledBorder border=null;
    protected transient Dimension dim;

    /**
     *  For use with the MapHandler (BeanContext) object.  The
     *  MouseDelegator will be found if it's added to the MapHandler,
     *  and will get set automatically.
     */
    public MouseModePanel() {
        super();
        setKey(defaultKey);
    } 

    /**
     *  @param md the map's MouseDelegator object.
     */
    public MouseModePanel(MouseDelegator md) {
        this();
        setMouseDelegator(md);
    } 

    /**
     * Set the MouseDelegator used to hold the different MouseModes
     * available to the map.  
     */
    public void setMouseDelegator(MouseDelegator md) {

        if (mouseDelegator != null) {
            mouseDelegator.removePropertyChangeListener(this);
            setPanel(null);
        }

        mouseDelegator=md;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        if (mouseDelegator == null) {
            return;
        }

        mouseDelegator.addPropertyChangeListener(this);
        setPanel(mouseDelegator);
    }

    /**
     * Get the MouseDelegator used to control mouse gestures over the
     * map.  
     */
    public MouseDelegator getMouseDelegator() {
        return mouseDelegator;
    }

    /**
     * Given a MouseDelegator, set up the pop-up menu to reflect the
     * MouseMode choices.  
     */
    protected void setPanel(MouseDelegator md) {
        if (titleButton != null) {
            remove(titleButton);
        }
        titleButton = new JButton();
        titleButton.addActionListener(this);

        String activeMode = md.getActiveMouseModeID();
        MapMouseMode[] modes = md.getMouseModes();

        rbs = new JPopupMenu("Mouse Modes");
        dim = titleButton.getMinimumSize();
        for (int i=0; i<modes.length; i++) {
            JMenuItem rb = new JMenuItem(modes[i].getPrettyName());
            rb.addActionListener(new MouseModeButtonListener(modes[i].getID(), this));
            rb.setBorderPainted(false);
            if (Debug.debugging("mousemode")) {
                Debug.output("MouseModePanel.setPanel(): Adding " + 
                             modes[i].getID() + " mode to menu with label: " +
                             rb.getName());
            }

            if (activeMode.equals(modes[i].getID())) {
                if (Debug.debugging("mousemode")) {
                    Debug.output("MouseModePanel.setPanel: Setting " + 
                                 activeMode + " to active");
                }
                rb.setSelected(true);
                titleButton.setText(modes[i].getPrettyName());
            }
            rbs.add(rb);
            rb.setVisible(modes[i].isVisible());
        }
        this.setMinimumSize(dim);

        if (modes.length > 0) {
            border = new TitledBorder(new EtchedBorder(), "Mouse Mode");
            setBorder(border);
        }

        titleButton.setActionCommand(mouseModeCmd);
        titleButton.setBorderPainted(false);
        add(titleButton);
        // HACK - the button keeps changing size depending on which
        // choice is made.  I'd like to set the size based on the
        // size of the largest mouse mode name, but I can't figure out
        // when that's available, before we actually have to present
        // the button.
        this.setPreferredSize(new Dimension(140, 45));
        this.revalidate();
    }

        

    public void actionPerformed(java.awt.event.ActionEvent e) {

        if (mouseDelegator == null) {
            return;
        }

        String command = e.getActionCommand();
        if (command.equals(mouseModeCmd)) {
            Point loc = titleButton.getLocation(new Point(0,0));
            rbs.show(titleButton, loc.x, loc.y);
        } else if (command.equals(newMouseModeCmd)) {
            JMenuItem rb = (JMenuItem)(e.getSource());
            if (Debug.debugging("mousemode")){
                Debug.output("MouseModePanel.actionPerformed: " + rb.getName());
            }
            mouseDelegator.setActiveMouseModeWithID(rb.getName());
        }
    }

    /**
     *  Listen for changes to the active mouse mode and for any changes
     *  to the list of available mouse modes
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
            String mmID = ((MapMouseMode)evt.getNewValue()).getPrettyName();
            if (Debug.debugging("mousemode")){
                Debug.output("propertyChange: action mode property " + mmID);
            }
            titleButton.setText(mmID);
        } else if (evt.getPropertyName() == MouseDelegator.MouseModesProperty) {
            // This won't work if prettyNames for the mouse modes are
            // different than the ID.  That's why the
            // MouseModeButtonListener has been added to the class,
            // and is used instead.
            Debug.message("mousemode", "propertyChange: mouse modes property");
            rbs.removeAll();
            setPanel(mouseDelegator);
        }
    }

    /**
     * Called when a component that is needed, and not available with
     * an appropriate iterator from the BeanContext.  This lets this
     * object hook up with what it needs.  
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MouseDelegator) {
            // do the initializing that need to be done here
            Debug.message("mousemodepanel","MouseModePanel found a MouseDelegator.");
            setMouseDelegator((MouseDelegator)someObj);
        }         
    }

    /** 
     * BeanContextMembershipListener method.  Called when an object
     * has been removed from the parent BeanContext. 
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MouseDelegator) {
            // do the initializing that need to be done here
            Debug.message("mousemodepanel","MouseModePanel removing MouseDelegator.");
            if (someObj == getMouseDelegator()) {
                setMouseDelegator(null);
            }
        }
    }

    public static class MouseModeButtonListener implements ActionListener {
        String mmid = null;
        MouseModePanel mouseModePanel = null;

        public MouseModeButtonListener(String id, MouseModePanel mmp) {
            mmid = id;
            mouseModePanel = mmp;
        }

        public void actionPerformed(ActionEvent ae) {
            if (mouseModePanel != null) {
                MouseDelegator md = mouseModePanel.getMouseDelegator();
                if (md != null) {
                    md.setActiveMouseModeWithID(mmid);
                }
            }
        }
    }

}
