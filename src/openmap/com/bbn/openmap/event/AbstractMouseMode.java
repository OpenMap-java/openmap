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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/AbstractMouseMode.java,v $
// $RCSfile: AbstractMouseMode.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.proj.Projection;

/**
 * Base class of the MouseModes.  It takes care of the administrative
 * aspects of being a mouse mode, but does not respond to MouseEvents.
 * <p>
 * This class delegates much of the work of managing its listeners to
 * a MapMouseSupport object.
 * @see MapMouseSupport
 */
public class AbstractMouseMode extends OMComponent
    implements MapMouseMode, Serializable {

    /**
     * The identifier for the mode, which is also the name that will
     * be used in a used interface describing the mode to a user.
     */
    protected String ID = null;

    /**
     * The object used to handle the listeners and to pass out the
     * event to the layers interesed in it.
     */
    protected MapMouseSupport mouseSupport;

    /**
     * The cursor that appears on the map when this Mouse Mode is
     * active.
     */
    protected Cursor cursor = Cursor.getDefaultCursor();

    /**
     * The Icon that can be used in a GUI.  Can be null.  The class
     * will look for a resource gif file that has the same ID string -
     * Navigation.gif for the NavMouseMode, for instance.
     */
    protected Icon guiIcon = null;

    public DecimalFormat df = new DecimalFormat("0.###");

    protected boolean visible = true;

    /**
     * Construct an AbstractMouseMode.
     * Default constructor, allocates the mouse support object.
     */
    public AbstractMouseMode() {
	this("Unnamed Mode", true);
    }

    /**
     * Construct an AbstractMouseMode.
     * @param name the ID of the mode.
     * @param shouldConsumeEvents if true, events are propagated to
     * the first MapMouseListener that successfully processes the
     * event, if false, events are propagated to all MapMouseListeners
     */
    public AbstractMouseMode(String name, boolean shouldConsumeEvents) {
	mouseSupport = new MapMouseSupport(shouldConsumeEvents);
	ID = name;

	java.net.URL url = getClass().getResource(name + ".gif");
	if (url != null) {
	    guiIcon = new ImageIcon(url);
	}
    }

    /**
     * Returns the id (mode name).
     * @return String ID
     */
    public String getID(){
	return ID;
    }

    /**
     * Set the id (mode name).
     * @param id string that identifies the delegate.
     */
    public void setID(String id) {
	ID = id;
    }

    /**
     * Gets the mouse cursor recommended for use when this mouse mode
     * is active.
     * @return Cursor the mouse cursor recommended for use when this
     * mouse mode is active.
     */
    public Cursor getModeCursor() {
	return cursor;
    }

    /**
     * Sets the cursor that is recommended for use on the map
     * when this mouse mode is active.
     * @param curs the cursor that is recommended for use on the map
     * when this mouse mode is active. 
     */
    public void setModeCursor(Cursor curs) {
	cursor = curs;
    }

    /**
     * Gets the Icon to represent the Mouse Mode in a GUI.  May be null.
     */
    public Icon getGUIIcon() {
	return guiIcon;
    }

    /**
     * Set the icon that should be used for this Mouse Mode in a GUI.
     */
    public void setGUIIcon(Icon icon) {
	guiIcon = icon;
    }
    
    /**
     * Sets how the delegate passes out events.  If the value passed
     * in is true, the delegate will only pass the event to the first
     * listener that can respond to the event.  If false, the delegate
     * will pass the event on to all its listeners.
     * @param value true for limited distribution.
     */
    public void setConsumeEvents(boolean value) {
	mouseSupport.setConsumeEvents(value);
    }

    /**
     * Returns how the delegate (and it's mouse support) is set up to
     * distribute events.
     * @return true if only one listner gets to act on an event.
     */
    public boolean isConsumeEvents() {
	return mouseSupport.isConsumeEvents();
    }

    /**
     * Add a MapMouseListener to the MouseMode.  The listener will
     * then get events from the delegator if the delegator is
     * active. 
     * @param l the MapMouseListener to add.
     */
    public void addMapMouseListener (MapMouseListener l) {
	mouseSupport.addMapMouseListener(l);
    }

    /**
     * Remove a MapMouseListener from the MouseMode.
     * @param l the MapMouseListener to remove.
     */
    public void removeMapMouseListener (MapMouseListener l) {
	mouseSupport.removeMapMouseListener(l);
    }

    /**
     * Remove all MapMouseListeners from the mode.
     */
    public void removeAllMapMouseListeners () {
	mouseSupport.removeAllMapMouseListeners();
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Invoked when a mouse button has been pressed on a component.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Invoked when a mouse button has been released on a component.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * Invoked when the mouse enters a component.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseDragged(MouseEvent e) {}

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     * This does nothing.  Extend this class to add functionality.
     * @param e MouseEvent
     */
    public void mouseMoved(MouseEvent e) {}

    /**
     * Part of the MapMouseMode interface.  Called when the MouseMode
     * is made active or inactive.
     *
     * @param active true if the mode has been made active, false if
     * it has been made inactive.
     */
    public void setActive(boolean active) {}

    /**
     * Set a MouseSupport explicitly.
     * @param support The new MapMouseSupport instance
     */
    public void setMouseSupport(MapMouseSupport support) {
	mouseSupport = support;
    }

    /**
     * Get the MouseSupport.
     * @return the MapMouseSupport used by the MouseMode.
     */
    public MapMouseSupport getMouseSupport() {
	return mouseSupport;
    }

    /**
     * Method to let the MouseDelegator know if the MapMouseMode
     * should be visible, as opposed to a MapMouseMode that is being
     * provided and controlled by another tool.  True by default.
     */
    public boolean isVisible() {
	return visible;
    }

    /**
     * Method to set if the MapMouseMode should be visible, as opposed
     * to a MapMouseMode that is being provided and controlled by
     * another tool.
     */
    public void setVisible(boolean value) {
	visible = value;
    }
}
