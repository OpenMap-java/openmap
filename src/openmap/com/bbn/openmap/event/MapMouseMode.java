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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseMode.java,v $
// $RCSfile: MapMouseMode.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;

/**
 * Interface for handling mouse behavior while the mouse is operating
 * over the MapBean.  A "MouseMode" object exists to interpret the
 * meaning of mouse events.  For instance, you could have a mode where
 * mouse events (click, drag-select) are interpreted as navigation
 * commands, (recenter, zoom-and-recenter).  There may be other modes
 * depending on how your application wants to interpret MouseEvents.
 * @see AbstractMouseMode
 * @see NavMouseMode
 * @see SelectMouseMode
 * @see NullMouseMode
 */
public interface MapMouseMode extends MouseListener, MouseMotionListener {

    /**
     * Returns the id (MapMouseMode name).
     * This name should be unique for each MapMouseMode.
     * @return String ID
     */
    public String getID();

    /**
     * Gets the mouse cursor recommended for use when this mouse mode
     * is active.
     * @return Cursor the mouse cursor recommended for use when this
     * mouse mode is active.
     */
    public Cursor getModeCursor();

    /**
     * Gets the Icon to represent the Mouse Mode in a GUI.
     */
    public Icon getGUIIcon();

    /**
     * Add a MapMouseListener to the MouseMode.
     * @param l the MapMouseListener to add.
     */
    public void addMapMouseListener(MapMouseListener l);

    /**
     * Remove a MapMouseListener from the MouseMode.
     * @param l the MapMouseListener to remove.
     */
    public void removeMapMouseListener(MapMouseListener l);

    /**
     * Remove all MapMouseListeners from the mode.
     */
    public void removeAllMapMouseListeners();

    /**
     * Let the MapMouseMode know if it is active or not.  Called by
     * the MouseDelegator.
     *
     * @param active true if the MapMouseMode has been made the active
     * one, false if it has been set inactive.  
     */
    public void setActive(boolean active);

    /**
     * Lets the MouseDelegator know if the MapMouseMode should be
     * visible in the GUI, in order to create certain mouse modes that
     * may be controlled by other tools.
     */
    public boolean isVisible();
}
