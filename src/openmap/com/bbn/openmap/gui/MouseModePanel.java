// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.Serializable;
import java.util.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.border.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
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
    public void setMouseDelegator(MouseDelegator md){

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
	    JMenuItem rb = new JMenuItem(modes[i].getID());
	    rb.setActionCommand(newMouseModeCmd);
	    rb.setName(modes[i].getID());
	    rb.setBorderPainted(false);
	    if (Debug.debugging("mousemode")) {
		Debug.output("MouseModePanel.setPanel(): Adding " + 
			     rb.getName() + " button to menu.");
	    }
	    rb.addActionListener(this);
	    if (activeMode.equals(modes[i].getID())) {
		if (Debug.debugging("mousemode")) {
		    Debug.output("MouseModePanel.setPanel: Setting " + 
				 activeMode + " to active");
		}
  		rb.setSelected(true);
		titleButton.setText(activeMode);
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
	    String mmID = ((MapMouseMode)evt.getNewValue()).getID();
	    if (Debug.debugging("mousemode")){
		Debug.output("propertyChange: action mode property " + mmID);
	    }
	    titleButton.setText(mmID);

	} else if (evt.getPropertyName() == MouseDelegator.MouseModesProperty) {
	    Debug.message("mousemode", "propertyChange: mouse modes property");
	    rbs.removeAll();
	    setPanel(mouseDelegator);
	}
    }

    /**
     * Called when a component that is needed, and not available with
     * an appropriate interator from the BeanContext.  This lets this
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

    /*
    public static void main(String args[]) {
	final JFrame frame = new JFrame("Mouse Modes");
	final MouseDelegator md = new MouseDelegator();
	final MouseModePanel mmp = new MouseModePanel(md);
	frame.setSize(400, 110);
	frame.setVisible(true);
	JPanel panel = new JPanel();

	JButton but1 = new JButton("add");
	but1.addActionListener( new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		SelectMouseMode gmed = 
		    new SelectMouseMode(true);
		md.addMouseMode(gmed);
		System.out.println("SelectMouseMode ID " +gmed.getID());
	    }
	});

	JButton but2 = new JButton("remove");
	but2.addActionListener( new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		md.removeMouseMode("Select");
		mmp.repaint();
	    }
	});

	JButton but3 = new JButton("init");
	but3.addActionListener( new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		md.setDefaultMouseModes();
		mmp.repaint();
	    }
	});

	panel.add(but3);
	panel.add(but1);
	panel.add(but2);
	panel.add(mmp);
	    
	frame.getContentPane().add(panel);
	
    }
    */
}
